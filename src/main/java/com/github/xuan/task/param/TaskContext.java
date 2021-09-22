package com.github.xuan.task.param;

import com.github.xuan.task.dao.domain.TaskDO;
import lombok.Data;

@Data
public class TaskContext {

    /**
     * 任务类型
     */
    private Integer type;

    /**
     * 任务的key：组合唯一索引(type+taskKey)
     */
    private String taskKey;

    /**
     * 任务有参数则放在这
     */
    private String context;

    /**
     * 重试延迟秒数
     */
    private Integer delay;

    /**
     * 重试延迟乘数
     */
    private Integer multiplier;

    /**
     * 最多执行次数
     */
    private Integer maxAttempts;

    /**
     * 已执行次数
     */
    private Integer attempts;

    /**
     * 执行ip
     */
    private String host;

    public TaskContext(TaskDO task) {
        this.context = task.getContext();
        this.host = task.getHost();
        this.type = task.getType();
        this.taskKey = task.getTaskKey();
        this.delay = task.getDelay();
        this.multiplier = task.getMultiplier();
        this.maxAttempts = task.getMaxAttempts();
        this.attempts = task.getAttempts();
    }

}