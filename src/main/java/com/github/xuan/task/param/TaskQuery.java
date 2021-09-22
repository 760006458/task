package com.github.xuan.task.param;

import com.github.xuan.task.enums.TaskStatus;
import javax.validation.constraints.Min;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskQuery {

    private TaskType type;

    private Set<TaskStatus> statusSet;

    private String taskKey;

    /**
     * 每页条数
     */
    @Min(1)
    private int pageSize = 1;

    /**
     * 当天页码 <br />
     * 从1开始,默认为1
     */
    @Min(1)
    private int currentPage = 1;

    private TaskQuery() {
    }

    public static TaskQuery from(TaskType type, Set<TaskStatus> statusSet, String taskKey) {
        TaskQuery query = new TaskQuery();
        query.type = type;
        if (statusSet != null) {
            query.statusSet = statusSet.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        query.taskKey = taskKey;
        return query;
    }

    public TaskType getType() {
        return type;
    }

    public Set<TaskStatus> getStatusSet() {
        return statusSet;
    }

    public String getTaskKey() {
        return taskKey;
    }
}