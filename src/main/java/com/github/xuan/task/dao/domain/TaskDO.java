package com.github.xuan.task.dao.domain;

import com.github.xuan.task.dto.TaskCheckpoint;
import com.github.xuan.task.param.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDO {

    private Long id;

    /**
     * 任务类型
     *
     * @see TaskType
     */
    private Integer type;

    /* 组合唯一索引（type + taskKey） */
    private String taskKey;

    /* 任务有参数则放在这 */
    private String context;

    /* 任务提交时间 */
    private LocalDateTime submitTime;

    /* 期望execute时间，默认任务提交时间 */
    private LocalDateTime expectExecuteTime;

    /* 优先处理 */
    private Integer prior;

    /* 任务超时的时间点 */
    private LocalDateTime timeoutTime;

    /* 重试延迟秒数：expectExecuteTime = expectExecuteTime + delay */
    private Integer delay;

    /* 重试延迟乘数 */
    private Integer multiplier;

    /* 最多执行次数 */
    private Integer maxAttempts;

    /* 已执行次数 */
    private Integer attempts;

    /* 存档点 */
    private String checkpoint;

    /* 状态[{"待锁定"-1，"已锁定"-2，"已完成"-3，"已失败"-4}] */
    private Integer status;

    /* 抢到锁的时间 */
    private LocalDateTime lockedTime;

    /* 版本号,自增 */
    private Integer version;

    /* 执行备注 */
    private String memo;

    /* 执行ip */
    private String host;

    /* 任务检查点 */
    private volatile TaskCheckpoint taskCheckpoint;

    /**
     * 数据库该条记录的创建时间（数据库自动默认设置，代码中无需再手动设置）
     */
    private LocalDateTime createTime;

    /**
     * 记录修改时间（数据库自动默认修改，代码中无需再手动设置）
     */
    private LocalDateTime updateTime;

    private Boolean isDeleted;

    public synchronized TaskCheckpoint getTaskCheckpoint() {
        if (taskCheckpoint == null) {
            taskCheckpoint = new TaskCheckpoint(checkpoint);
        }
        return taskCheckpoint;
    }
}
