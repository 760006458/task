package com.github.xuan.task.enums;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum TaskStatus {
    WAIT_FOR_PROCESS(1, "待处理"),
    PROCESSING(2, "处理中"),
    SUCCESS(3, "已完成"),
    FAILED(4, "已失败");

    public final int code;

    public final String meaning;

    public boolean isNew() {
        return this == WAIT_FOR_PROCESS;
    }

    public boolean isProcessing() {
        return this == PROCESSING;
    }

    public boolean isFinish() {
        return isSuccess() || isFail();
    }

    public boolean isFail() {
        return this == FAILED;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public static Set<TaskStatus> unFinish() {
        return Sets.newHashSet(WAIT_FOR_PROCESS, PROCESSING);
    }

    public static TaskStatus from(Integer code) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMeaning() {
        return meaning;
    }
}