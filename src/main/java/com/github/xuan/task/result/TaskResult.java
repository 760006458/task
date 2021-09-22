package com.github.xuan.task.result;

/**
 * task执行的返回结果
 */
public class TaskResult {

    private static final int SUCC_TYPE = 1;

    private static final int RETRY_TYPE = 2;

    private static final int FAIL_TYPE = 3;

    public static final TaskResult DEFAULT_FAIL = new TaskResult(FAIL_TYPE, "fail");

    public static final TaskResult SUCC = new TaskResult(SUCC_TYPE, "succ");

    private final int type;

    public final String memo;

    private int delaySecond;

    private TaskResult(int type, String memo) {
        this.type = type;
        this.memo = memo;
    }

    private static boolean is(TaskResult result, int type) {
        return result != null && result.type == type;
    }

    public static boolean isSucc(TaskResult result) {
        return is(result, SUCC_TYPE);
    }

    public static boolean isFail(TaskResult result) {
        return is(result, FAIL_TYPE);
    }

    public static boolean isRetry(TaskResult result) {
        return is(result, RETRY_TYPE);
    }

    public static TaskResult failWith(String memo) {
        return new TaskResult(FAIL_TYPE, memo);
    }

    public static TaskResult successWith(String memo) {
        return new TaskResult(SUCC_TYPE, memo);
    }

    public int getDelaySecond() {
        return delaySecond;
    }

    public static TaskResult retryAfterMinutes(int delayMinute) {
        TaskResult result = new TaskResult(RETRY_TYPE, "retry");
        result.delaySecond = delayMinute * 60;
        return result;
    }

    public static TaskResult retryAfterSecond(int delaySecond) {
        TaskResult result = new TaskResult(RETRY_TYPE, "retry");
        result.delaySecond = delaySecond;
        return result;
    }
}
