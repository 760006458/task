package com.github.xuan.task.register;

import com.github.xuan.task.config.TaskConfig;
import com.github.xuan.task.handler.TaskHandler;
import com.github.xuan.task.param.TaskType;
import com.github.xuan.task.service.HandlerContext;
import com.github.xuan.task.util.Validates;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import javax.annotation.PostConstruct;
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

    private final TaskConfig taskConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void postCheck() {
        List<TaskType> types = taskConfig.getTypes();
        if (CollectionUtils.isEmpty(types)) {
            return;
        }
        List<TaskType> taskTypes = types.stream().map(e -> new TaskType(e.getHandlerName(), e.getType())).collect(Collectors.toList());
        taskTypes.forEach(type -> {
            Validates.checkNotNullOrEmpty(type.getHandlerName(), "task.types[#].handlerName不能为空");
            Validates.checkNotNull(type.getType(), "task.types[#].type不能为空且必须是数字");
        });
        long count = taskTypes.stream().map(TaskType::getType).distinct().count();
        Validates.check(count == taskTypes.size(), "task.types[#].type值不能重复");
        handlerContext.registerTaskType(taskTypes);
    }

    @Override
    public void run(String... strings) {
        //代码中TaskHandler的实例bean：类名首字母小写
        Map<String, TaskHandler> handlerMap = applicationContext.getBeansOfType(TaskHandler.class);
        //读取和验证配置文件
        List<TaskType> types = taskConfig.getTypes();
        if (CollectionUtils.isEmpty(types)) {
            return;
        }
        Map<String, TaskType> taskTypeMap = types.stream().collect(Collectors.toMap(TaskType::getHandlerName, e -> e));
        taskTypeMap.keySet().forEach(handlerName -> Validates.check(handlerMap.containsKey(handlerName), String.format("配置文件中的handlerName=%s不存在", handlerName)));
        //注册TaskHandler
        for (TaskHandler handler : handlerMap.values()) {
            Validates.check(taskTypeMap.containsKey(handler.getHandlerName()), String.format("TaskHandler=%s在配置文件中不存在", handler.getHandlerName()));
            handlerContext.registerTaskHandler(handler);
        }
    }
}