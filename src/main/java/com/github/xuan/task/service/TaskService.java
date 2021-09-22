package com.github.xuan.task.service;

import com.github.xuan.task.param.TaskType;
import com.github.xuan.task.param.TaskCreateParam;

public interface TaskService {

    /**
     * 创建task任务
     */
    boolean submitTask(TaskCreateParam taskCreateParam);

    /**
     * 重跑任务
     */
    boolean reRun(long id);

    /**
     * 执行普通任务：定时任务频次较高
     */
    void execute();

    /**
     * 执行超时任务：定时任务频次较低
     * 与execute()方法分离原因：
     * 1.对batchGetTask的SQL优化
     * 2.普通任务和超时任务频次分离
     */
    void executeTimeout();

    /**
     * 长任务的定时savePoint
     */
    void longTaskReport();

    /**
     * 判断是否有未完成的任务
     */
    boolean existUnFinished(TaskType taskType, String taskKey);

    /**
     * 取消任务
     */
    boolean cancel(long id);
}