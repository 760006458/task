package com.github.xuan.task.handler;

import com.github.xuan.task.dao.domain.TaskDO;
import com.github.xuan.task.param.TaskContext;
import com.github.xuan.task.result.TaskResult;

public interface ShortRunTaskHandler extends TaskHandler {

    @Override
    default TaskResult handleTask(final TaskDO task) {
        return handleShortTask(new TaskContext(task));
    }

    TaskResult handleShortTask(final TaskContext taskContext);
}
