package com.github.xuan.task.handler;

import com.github.xuan.task.param.TaskContext;
import com.github.xuan.task.result.TaskResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * taskHandler示例
 *
 * @author xuan
 * @create 2021-04-28 15:06
 **/
@Slf4j
@Component
@AllArgsConstructor
public class TestHandler implements ShortRunTaskHandler {

    @Override
    public TaskResult handleShortTask(TaskContext taskContext) {
        log.info("处理测试任务开始...");
        return TaskResult.successWith("test success");
    }
}
