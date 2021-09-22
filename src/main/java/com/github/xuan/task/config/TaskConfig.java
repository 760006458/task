package com.github.xuan.task.config;

import com.github.xuan.task.param.TaskType;
import lombok.Data;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * MapperScan：扫描路径一定要精细，否则会对包下所有接口进行代理
 *
 * @author xuan
 * @create 2021-05-06 17:11
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "task.github")
@MapperScan("com.github.xuan.task.dao.mapper")
public class TaskConfig {

    /**
     * 定时任务频次：此处只做展示，真正使用在TaskServiceImpl的
     * ${task.fixedDelayString.normal}和${task.fixedDelayString.timeout}
     */
    private FixedDelay fixedDelay;

    /**
     * 注册任务类型
     */
    private List<TaskType> types;

    /**
     * 普通任务的MySQL抓取数量(pageSize)
     */
    private int normalBatchSize = 100;

    /**
     * 超时任务的MySQL抓取数量(pageSize)
     */
    private int timeoutBatchSize = 10;

    /**
     * 线程池配置queueSize
     */
    private int queueSize = 1000;

    /**
     * 线程池配置corePoolSize
     */
    private int corePoolSize = 4;

    /**
     * 线程池配置maxPoolSize
     */
    private int maxPoolSize = 8;

    @Data
    private static class FixedDelay {

        /**
         * 普通定时任务扫描的fixedDelay耗时
         * 单位：毫秒(ms)
         */
        private String normal;

        /**
         * 超时定时任务扫描的fixedDelay耗时
         * 单位：毫秒(ms)
         */
        private String timeout;
    }
}
