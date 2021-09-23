package com.github.xuan.task.register;

import com.github.xuan.task.handler.TaskHandler;
import com.github.xuan.task.service.HandlerContext;
import com.github.xuan.task.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 可以把任务注册放在boot模块，这样可指定任务被哪个应用消费，从而避免不同应用消费Task产生的隐患
 * 实现CommandLineRunner接口，会在springboot启动时执行run()
 */
@Slf4j
@Component
@AllArgsConstructor
public class TaskHandlerRegister implements CommandLineRunner, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final HandlerContext handlerContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... strings) {
        //代码中TaskHandler的实例bean：类名首字母小写
        Map<String, TaskHandler> handlerMap = applicationContext.getBeansOfType(TaskHandler.class);
        //校验handler的type之间不会重复
        handlerMap.values().stream().collect(Collectors.groupingBy(TaskHandler::getType, Collectors.counting()))
                .forEach((key, value) -> {
                    if (value > 1) {
                        List<String> duplicateHandlerNames = handlerMap.values().stream().filter(e -> e.getType() == key).map(e -> e.getClass().getName()).collect(Collectors.toList());
                        throw new RuntimeException(String.format("[task]handler之间的type类型不能重复！type=%s存在%s个重复handler=[%s]", key, value, JsonUtil.toJsonStr(duplicateHandlerNames)));
                    }
                });

        //注册TaskHandler
        for (TaskHandler handler : handlerMap.values()) {
            handlerContext.registerTaskHandler(handler);
        }
    }
}