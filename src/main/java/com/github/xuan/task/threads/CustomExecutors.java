package com.github.xuan.task.threads;

import com.github.xuan.task.config.TaskConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义executorService
 * 注意事项(具体见 ThreadPoolExecutor 文档):
 * 1.注意这里采用的是有界队列(ArrayBlockingQueue),
 * 当提交任务时,线程数小于corePoolSize优先创建线程,
 * 等于corePoolSize则优先放入队列,等队列满了在创建新线程,请合理设置参数
 * 2.异常处理
 * 当调用execute方法,表示不用返回结果,任务异常时线程会抛出并退出,ThreadPoolExecutor会自动决定是否创建新线程
 * 当调用submit方法时,表示希望获取返回结果,线程不会抛出异常,异常抛出延迟到调用结果对象的get()抛出
 * 3.未来如需线程开始结束后执行某些逻辑,采用继承ThreadPoolExecutor方式并覆盖其hook方法
 *
 * @author xuan
 */
@Slf4j
@Configuration
public class CustomExecutors {

    @Resource
    private TaskConfig taskConfig;

    private static ExecutorService create(String prefix, int corePoolSize, int maximumPoolSize,
                                          int queueSize, RejectedExecutionHandler rejectedExecutionHandler) {
        return new ThreadPoolExecutor(
                corePoolSize, maximumPoolSize,
                100, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                new CustomNamedThreadFactory(prefix),
                rejectedExecutionHandler);
    }

    /**
     * 不采用提交线程执行任务的策略,防止任务执行时间过长导致其他worker获取不到新任务
     * 不可使用Runtime.getRuntime().availableProcessors()，获取的是物理机cpu核数，容器化部署会有问题
     */
    @Bean
    public ExecutorService taskThreadPool() {
        return CustomExecutors.create("task", taskConfig.getCorePoolSize(), taskConfig.getMaxPoolSize(), taskConfig.getQueueSize(),
                (r, executor) -> {
                    try {
                        //采用fixDelay的定时任务方案，理论队列是不会满的
                        log.warn("task-executor-queue-full... detail: {}", executor);
                        while (executor.getQueue().size() >= taskConfig.getQueueSize()) {
                            //让外部的任务提交线程慢一点
                            Thread.sleep(1000);
                        }
                        if (!executor.isShutdown()) {
                            //这里不采用submit,因为r没必要包装为future对象其本身已经是future类型
                            executor.execute(r);
                        }
                    } catch (InterruptedException e) {
                        log.warn("Task loop thread is interrupted and exit", e);
                    }
                });
    }

    @PreDestroy
    public void closeThreadPool() {
        taskThreadPool().shutdown();
    }
}

