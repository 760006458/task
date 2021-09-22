package com.github.xuan.task.dao.mapper;

import com.github.xuan.task.dao.domain.TaskDO;
import com.github.xuan.task.param.TaskQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

@Repository
public interface TaskMapper {

    int create(@Nonnull TaskDO taskDO);

    int batchCreate(@Nonnull List<TaskDO> taskDOList);

    int fail(@Param("id") long id, @Param("memo") String memo, @Param("version") int version);

    int success(@Param("id") long id, @Param("version") int version, @Param("memo") String memo);

    int retry(@Param("id") long id, @Param("delay") int delay, @Param("version") int version);

    int grab(@Param("id") long id, @Param("version") long version, @Param("host") String host);

    int reRun(@Param("id") long id);

    int saveCheckpoint(@Param("id") long id, @Param("version") long version, @Param("checkpoint") String checkpoint);

    List<TaskDO> batchGetTask(@Param("size") int size, @Param("types") Set<Integer> taskTypes);

    List<TaskDO> batchGetTimeoutTask(@Param("size") int size, @Param("types") Set<Integer> taskTypes);

    int count(@Nonnull TaskQuery query);

    int cancel(@Param("id") long id);
}