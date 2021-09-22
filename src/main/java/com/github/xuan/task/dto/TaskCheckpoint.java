package com.github.xuan.task.dto;

import com.github.xuan.task.util.StringUtil;
import java.util.Optional;

/**
 * 存档点
 */
public class TaskCheckpoint {

    private volatile String checkpoint;

    public TaskCheckpoint(String checkpoint) {
        this.checkpoint = checkpoint == null ? "" : checkpoint;
    }

    public String getAsString() {
        return checkpoint;
    }

    public Optional<Long> getAsLong() {
        return StringUtil.isDigit(checkpoint) ? Optional.of(Long.parseLong(checkpoint)) : Optional.empty();
    }

    public void update(String checkpoint) {
        this.checkpoint = checkpoint;
    }

    public void update(long checkpoint) {
        this.checkpoint = String.valueOf(checkpoint);
    }
}