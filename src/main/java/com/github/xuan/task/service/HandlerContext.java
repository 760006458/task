package com.github.xuan.task.service;

import com.github.xuan.task.handler.TaskHandler;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * 任务handler的容器，项目启动时，会将所有handler注册进来
 *
 * @author xuan
 * @create 2021-09-21 15:13
 **/
@Slf4j
@Service
public class HandlerContext {

    /**
     * key: task的type值
     * value: TaskHandler的spring实例bean
     */
    private static final ConcurrentMap<Integer, TaskHandler> TASK_HANDLERS = Maps.newConcurrentMap();

    /**
     * 注册需要执行的任务类型，如果不注册，则batchGetTask时不会获取
     */
    public void registerTaskHandler(TaskHandler taskHandler) {
        if (taskHandler == null) {
            throw new RuntimeException("handler is null");
        }
        TaskHandler handler = TASK_HANDLERS.putIfAbsent(taskHandler.getType(), taskHandler);
        if (handler != null) {
            if (handler.getClass() == taskHandler.getClass()) {
                String template = "%s only support register once";
                String message = String.format(template, handler.getClass());
                throw new UnsupportedOperationException(message);
            } else {
                String template = "A task type does not support multiple handlers. Exist type & handler pair [%s]-[%s]";
                String message = String.format(template, taskHandler.getType(), taskHandler.getClass().getSimpleName());
                throw new UnsupportedOperationException(message);
            }
        } else {
            log.info("TaskHandler[{}] Registered!", taskHandler.getHandlerName());
        }
    }

    /**
     * 获取能处理的任务类型集合
     */
    Set<Integer> supportedTaskTypes() {
        return TASK_HANDLERS.keySet();
    }

    /**
     * 根据任务类型获取taskHandler
     */
    Optional<TaskHandler> getTaskHandlerByType(Integer taskType) {
        return Optional.ofNullable(TASK_HANDLERS.get(taskType));
    }

    /**
     * 根据handlerName获取任务类型
     */
    public Integer ofHandlerType(String handlerName) {
        return TASK_HANDLERS.values().stream().filter(e -> e.getHandlerName().equals(handlerName))
                .findFirst().orElseThrow(() -> new RuntimeException("handlerName=%s不存在")).getType();
    }

}
