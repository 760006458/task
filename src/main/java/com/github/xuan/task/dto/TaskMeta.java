package com.github.xuan.task.dto;

import com.github.xuan.task.dao.domain.TaskDO;
import lombok.Data;
import java.util.concurrent.Future;

/**
 * @author xuan
 * @create 2021-09-21 14:45
 **/
@Data
public class TaskMeta {

    private final int version;

    private final TaskDO task;

    private final Future<?> future;

    public TaskMeta(TaskDO task, Future<?> future, int version) {
        this.task = task;
        this.future = future;
        this.version = version;
    }
}
