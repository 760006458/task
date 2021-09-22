package com.github.xuan.task.param;

import com.github.xuan.task.util.JsonUtil;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Data
@Builder
public class TaskCreateParam {

    private static final String DEFAULT_CONTEXT = "";

    private static final Integer DEFAULT_PRIOR = 1;

    private static final Integer DEFAULT_DEALY = 10;

    private static final Integer DEFAULT_MULTIPLIER = 2;

    private static final Integer DEFAULT_MAX_ATTEMPTS = 3;

    private static final Integer DEFAULT_ATTEMPTS = 0;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 任务key。组合唯一索引(type+taskKey)
     * 注：如果违反唯一索引，则后边的任务被丢弃且不报错（insert ignore）
     * 建议：赋值业务id，如果业务id不能保证唯一索引，可以拼上当前时间戳后缀："-timestamp"
     * 如果没有业务id，可以直接赋值serialNo（前提：）
     */
    private String taskKey;

    /**
     * 任务有参数则放在这
     */
    private String context = DEFAULT_CONTEXT;

    /**
     * 期望execute时间:定时任务
     */
    private LocalDateTime expectExecuteTime = defaultExpectTime();

    /**
     * 优先处理
     */
    private Integer prior = DEFAULT_PRIOR;

    /**
     * 重试延迟秒数
     */
    private Integer delay = DEFAULT_DEALY;

    /**
     * 重试延迟乘数
     */
    private Integer multiplier = DEFAULT_MULTIPLIER;

    /**
     * 最多执行次数
     */
    private Integer maxAttempts = DEFAULT_MAX_ATTEMPTS;

    /**
     * 已执行次数
     */
    private Integer attempts = DEFAULT_ATTEMPTS;

    public static class TaskCreateParamBuilder {

        private LocalDateTime expectExecuteTime = defaultExpectTime();

        private Integer prior = DEFAULT_PRIOR;

        private Integer delay = DEFAULT_DEALY;

        private Integer multiplier = DEFAULT_MULTIPLIER;

        private Integer maxAttempts = DEFAULT_MAX_ATTEMPTS;

        private Integer attempts = DEFAULT_ATTEMPTS;

        /**
         * 期望任务在什么时间运行,默认提交后运行
         */
        public TaskCreateParamBuilder expectDelayExecute(long delay, TimeUnit unit) {
            LocalDateTime now = LocalDateTime.now();
            this.expectExecuteTime = now.plusSeconds(unit.toSeconds(delay));
            return this;
        }

        /**
         * 期望任务在什么时间运行,默认提交后运行
         */
        public TaskCreateParamBuilder expectExecuteTime(LocalDateTime executeTime) {
            this.expectExecuteTime = executeTime;
            return this;
        }

        public TaskCreateParamBuilder context(Object object) {
            String context = JsonUtil.toJsonStr(object);
            return this;
        }
    }

    private static LocalDateTime defaultExpectTime() {
        return LocalDateTime.now();
    }
}