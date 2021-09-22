package com.github.xuan.task.param;

import com.github.xuan.task.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定时任务类型，code值不能相同，因为作为map的key存在，相同会发生覆盖
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskType {

    /**
     * handler类的名字，默认是simpleClassName
     */
    public String handlerName;

    /**
     * task任务的类型
     */
    public Integer type;

    public String getHandlerName() {
        return StringUtil.getLowerFirstChar(handlerName);
    }
}