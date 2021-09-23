package com.github.xuan.task.service;

import com.github.xuan.task.config.TaskConfig;
import com.github.xuan.task.dao.domain.TaskDO;
import com.github.xuan.task.dao.mapper.TaskMapper;
import com.github.xuan.task.dto.TaskMeta;
import com.github.xuan.task.enums.TaskStatus;
import com.github.xuan.task.param.TaskType;
import com.github.xuan.task.handler.LongRunTaskHandler;
import com.github.xuan.task.handler.TaskHandler;
import com.github.xuan.task.param.TaskCreateParam;
import com.github.xuan.task.param.TaskQuery;
import com.github.xuan.task.result.TaskResult;
import com.github.xuan.task.util.EnvUtil;
import com.github.xuan.task.util.JsonUtil;
import com.github.xuan.task.util.Validates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 整个task系统的核心类
 */
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    private TaskConfig taskConfig;

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private HandlerContext handlerContext;

    @Resource
    private ExecutorService executor;

    /**
     * 长任务Map
     */
    private final ConcurrentMap<Long, TaskMeta> tasksForReport = Maps.newConcurrentMap();

    @Override
    public boolean submitTask(TaskCreateParam taskCreateParam) {
        Validates.checkNotNull(taskCreateParam, "taskCreateParam is null");
        Validates.checkNotNull(taskCreateParam.getTypeHandler(), "typeHandler is null");
        TaskDO taskDO = TaskDO.builder()
                .submitTime(LocalDateTime.now())
                .type(handlerContext.ofHandlerType(taskCreateParam.getTypeHandler().getSimpleName()))
                .taskKey(taskCreateParam.getTaskKey())
                .context(taskCreateParam.getContext())
                .expectExecuteTime(taskCreateParam.getExpectExecuteTime())
                .delay(taskCreateParam.getDelay())
                .multiplier(taskCreateParam.getMultiplier())
                .maxAttempts(taskCreateParam.getMaxAttempts())
                .attempts(taskCreateParam.getAttempts())
                .prior(taskCreateParam.getPrior())
                .host(EnvUtil.getLocalIp())
                .build();
        boolean result = taskMapper.create(taskDO) == 1;
        if (!result) {
            log.error("创建task失败，违反唯一索引。data={}", JsonUtil.toJsonStr(taskDO));
        }
        return result;
    }

    /**
     * 【扫描普通任务】需要使用方合理评估！
     * fixedDelay的方案：上次任务结束时间 + fixedDelay = 下次任务开启时间
     * 建议：机器数量越多，fixedDelay给的值越大，这样可以降低扫库频次
     * eg1：假设共10个容器，fixedDelay=10000——平均扫库频次=10/10=1，极端情况下，某个task任务要等10秒
     * eg2：假设共6个容器，fixedDelay=3000——平均扫库频次=6/3=2，极端情况下，某个task任务要等3秒
     */
    @Override
    @Scheduled(fixedDelayString = "${task.github.fixedDelay.normal:3000}")
    public void execute() {
        if (handlerContext.supportedTaskTypes().isEmpty()) {
            return;
        }
        List<TaskDO> tasks = taskMapper.batchGetTask(taskConfig.getNormalBatchSize(), handlerContext.supportedTaskTypes());
        grabAndAsyncExecTask(tasks);
    }

    /**
     * 【扫描超时任务】
     * 默认60秒一次
     */
    @Override
    @Scheduled(fixedDelayString = "${task.github.fixedDelay.timeout:60000}")
    public void executeTimeout() {
        if (handlerContext.supportedTaskTypes().isEmpty()) {
            return;
        }
        List<TaskDO> tasks = taskMapper.batchGetTimeoutTask(taskConfig.getTimeoutBatchSize(), handlerContext.supportedTaskTypes());
        if (!CollectionUtils.isEmpty(tasks)) {
            log.warn("发现超时任务：{}", JsonUtil.toJsonStr(tasks));
            grabAndAsyncExecTask(tasks);
        }
    }

    /**
     * 【长任务】
     * 对长任务做一次saveCheckPoint
     * 目前没有长任务，先注释定时任务
     * TODO长任务还未经过线上打磨，可能存在风险点
     */
    @Override
    //@Scheduled(fixedDelay = 1000 * 60 * 5)
    public void longTaskReport() {
        List<Long> needDelete = Lists.newArrayList();
        for (Map.Entry<Long, TaskMeta> tmp : tasksForReport.entrySet()) {
            long taskId = tmp.getKey();
            TaskDO task = tmp.getValue().getTask();
            Future<?> future = tmp.getValue().getFuture();
            int version = tmp.getValue().getVersion();
            if (future.isDone() || future.isCancelled()) {
                needDelete.add(taskId);
            } else {
                int affect = taskMapper.saveCheckpoint(taskId, version, task.getTaskCheckpoint().getAsString());
                if (affect != 1) {
                    //任务运行时间超过上限或者版本号不符合期望
                    future.cancel(true);
                }
            }
        }
        needDelete.forEach(tasksForReport::remove);
    }

    @Override
    public boolean reRun(long id) {
        return taskMapper.reRun(id) == 1;
    }

    @Override
    public boolean existUnFinished(TaskType taskType, String taskKey) {
        return taskMapper.count(TaskQuery.from(taskType, TaskStatus.unFinish(), taskKey)) > 0;
    }

    @Override
    public boolean cancel(long id) {
        return taskMapper.cancel(id) == 1;
    }

    /**
     * 抢占并异步处理task任务
     */
    private void grabAndAsyncExecTask(List<TaskDO> tasks) {
        for (TaskDO task : tasks) {
            if (task == null) {
                continue;
            }
            int affect = taskMapper.grab(task.getId(), task.getVersion(), EnvUtil.getLocalIp());
            if (affect == 0) {
                continue;
            }
            try {
                //保证跟数据库字段值同步
                int version = task.getVersion() + 1;
                TaskHandler handler = handlerContext.getTaskHandlerByType(task.getType()).orElse(null);
                if (handler == null) {
                    throw new RuntimeException("不应该出现:在抢任务时只处理本机支持的任务类型");
                }
                Future<?> future = executor.submit(() -> wrappedTask(task, version));
                heartbeatIfNecessary(task, version, future);
            } catch (Exception e) {
                log.error("execute task {} exception and continue next task", task.getId(), e);
            }
        }
    }

    /**
     * 长任务心跳
     */
    private void heartbeatIfNecessary(TaskDO task, int version, Future<?> future) {
        TaskHandler handler = handlerContext.getTaskHandlerByType(task.getType()).orElseThrow(() -> new RuntimeException("不应该出现:在抢任务时只处理本机支持的任务类型"));
        //判断当前是否是长任务。A.isAssignableFrom(B)：A是B的父类；A instanceof B：A是B的子类
        if (LongRunTaskHandler.class.isAssignableFrom(handler.getClass())) {
            TaskMeta taskMeta = tasksForReport.putIfAbsent(task.getId(), new TaskMeta(task, future, version));
            if (taskMeta != null) {
                // 表示这个任务又被获取回来重新执行了 取消旧任务
                taskMeta.getFuture().cancel(true);
            }
        }
    }

    private void wrappedTask(TaskDO task, int version) {
        try {
            TaskHandler taskHandler = handlerContext.getTaskHandlerByType(task.getType()).orElseThrow(() -> new RuntimeException("不应该出现:在抢任务时只处理本机支持的任务类型"));
            TaskStatus status = TaskStatus.from(task.getStatus());
            // 这里对参数double-check 防止SQL语句不小心被改动出bug
            Validates.checkArgument(status == TaskStatus.WAIT_FOR_PROCESS || status == TaskStatus.PROCESSING, "task status wrong");
            LocalDateTime deadline = task.getExpectExecuteTime().plusDays(taskHandler.timeoutDay());
            if (LocalDateTime.now().isBefore(deadline)) {
                TaskResult result = taskHandler.handleTask(task);
                String memo = StringUtils.trimToEmpty(result.memo);
                if (TaskResult.isSucc(result)) {
                    terminalCheck(taskMapper.success(task.getId(), version, memo), task);
                } else if (TaskResult.isFail(result)) {
                    terminalCheck(taskMapper.fail(task.getId(), memo, version), task);
                } else if (TaskResult.isRetry(result)) {
                    terminalCheck(taskMapper.retry(task.getId(), result.getDelaySecond(), version), task);
                }
            } else {
                String memo = String.format("task timeout over threshold %s day(s)", taskHandler.timeoutDay());
                log.error("task {}, {} ", task.getId(), memo);
                terminalCheck(taskMapper.fail(task.getId(), memo, version), task);
            }
        } catch (Exception ex) {
            log.error("task {} exception.", task.getId(), ex);
            if (!Thread.interrupted()) {
                String message = ex.getMessage() == null ? "exception.getMessage() empty" : ex.getMessage();
                String err = message.length() < 100 ? message : message.substring(0, 100);
                String memo = err + ".Detail in log";
                terminalCheck(taskMapper.fail(task.getId(), memo, version), task);
            }
        }
    }

    /**
     * 正常case:任务执行者负责结束任务(成功、失败)
     * 异常情况可能超时时还在执行,但是被其他机器抢到了超时任务
     */
    private void terminalCheck(int affect, TaskDO task) {
        if (affect != 1) {
            log.error("task {} not terminate, perhaps the other host terminate it", task.getId());
        }
    }
}
