package com.github.xuan.task.handler;

import com.github.xuan.task.param.TaskContext;
import com.github.xuan.task.result.TaskResult;
import com.github.xuan.task.dao.domain.TaskDO;
import com.github.xuan.task.dto.TaskCheckpoint;

/**
 * 任务单次运行时间上限为1天,过期则被取消,重试的时候可以获取checkpoint继续work
 */
public interface LongRunTaskHandler extends TaskHandler {

    @Override
    default TaskResult handleTask(final TaskDO task) {
        return handleLongTask(new TaskContext(task), task.getTaskCheckpoint());
    }

    TaskResult handleLongTask(final TaskContext context, final TaskCheckpoint checkpoint);
}