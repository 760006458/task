package com.github.xuan.task.handler;

import com.github.xuan.task.dao.domain.TaskDO;
import com.github.xuan.task.result.TaskResult;
import com.github.xuan.task.util.StringUtil;

public interface TaskHandler {

    /**
     * 处理任务
     */
    TaskResult handleTask(final TaskDO task);

    default String getHandlerName() {
        //返回类名首字母小写
        return StringUtil.getLowerFirstChar(this.getClass().getSimpleName());
    }

    /**
     * 任务创建后,不论down机超时重试还是返回的TaskResult的显示重试,默认不能超过7天否则失败
     */
    default int timeoutDay() {
        return 7;
    }
}