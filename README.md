# Task任务系统

## 背景&痛点
1. MQ消息不可见，不能支持SQL查询，排错困难
2. MySQL控制不了分布式事务，需要异步解耦的场景只能依赖MQ事务消息
3. 长任务(耗时>10秒)：发版中断等场景下无法保证可靠性，需要引入自动重试、存档点等概念
4. MQ消息一般不支持优先级
5. 达到最大重试次数的MQ消息，需要额外引入死信队列或者落表，未来才能做代码补救
6. 延迟队列有最大延迟时间限制，延迟的消息无法任意条件查询，延迟的消息无法批量撤销
7. 一个任务类型接入一个MQ，MQ越接越多
8. 无法获取到某条消息的处理机器IP，对于中断的任务，无法让原机器继续执行
9. 任务如何做到肉眼可见，并且人工可以随时干预：终止任务、修改执行时间、修改最大重试次数等
10. 无法统计消息处理的首次/二次成功率和耗时


## Task介绍

1. MySQL建1张task表，有任务类型、任务key、任务文本内容、执行时间、任务状态、重试次数、优先级、失败原因、存档点等字段；
2. 业务需要用到task的地方，insert一条记录(status=待执行)：设置上述字段值；
3. 写一个handler类，实现ShortRunTaskHandler或者LongRunTaskHandler，在handleShortTask()方法中写任务逻辑；
3. 定时任务A：扫描这张表，按照"优先级倒叙、执行时间升序"的方式分页获取一批任务，先通过乐观锁的方式抢占任务(status => 执行中)，然后交由具体的handler类去做逻辑；
4. 执行成功，status改为"成功"；执行失败，status改为"失败"并保存原因；需要重试，则status重置为"待执行"，且重试次数+1；
5. 定时任务B：扫描超过15min还没有结束的任务，默认由开启定时任务B的线程重跑；如果需要原机器去跑，需要取出ip，拼上项目端口和后门接口URL，http回调一把；
6. 超长任务：假设已经无法再拆分成更小的任务了，但任务耗时还很长(eg:10分钟或者几个小时)，可以创建LongRunTask，它在执行过程中会自动存档；
7. 如果在事务中：基于业务操作MySQL，然后insert一条task记录，事务可以保证两者同时成功，实现了强一致的分布式事务
8. 有了MySQL表，各种统计随心所欲


## Task表结构

```mysql
CREATE TABLE `task` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `type` int(11) NOT NULL default 0 COMMENT '类型',
    `task_key` varchar(50) NOT NULL default '' COMMENT '任务key:一般存业务id等有含义的值方便查询',
    `context` text COMMENT '任务内容',
    `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `expect_execute_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '期望执行时间',
    `timeout_time` datetime not NULL DEFAULT '2999-12-30' COMMENT '超时时间',
    `delay` int(11) NOT NULL DEFAULT '10' COMMENT '重试延迟秒数',
    `multiplier` int(11) NOT NULL DEFAULT '2' COMMENT '重试延迟乘数',
    `max_attempts` int(11) NOT NULL DEFAULT '3' COMMENT '最多执行次数',
    `attempts` int(11) NOT NULL DEFAULT '0' COMMENT '已执行次数',
    `checkpoint` varchar(255) NOT NULL default 0 COMMENT '存档点',
    `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态[{"待锁定":1,"已锁定":2,"已完成":3,"已失败":4}]',
    `locked_time` datetime NOT NULL DEFAULT '1970-01-01' COMMENT '锁定时间',
    `version` int(11) NOT NULL default 0 COMMENT '执行版本',
    `host` varchar(50) NOT NULL default '127.0.0.1' COMMENT '执行者ip',
    `memo` text COMMENT '失败备注',
    `prior` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '优先级。9级最高，1级最低(默认)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' comment '逻辑删除标记。0-正常，1-删除',
    PRIMARY KEY (`id`),
    KEY `idx_type_taskKey` (type, task_key) USING BTREE,
    KEY `idx_expectExecuteTime` (status, expect_execute_time, type, max_attempts, attempts, prior) USING BTREE,
    KEY `idx_timeoutTime` (status, timeout_time, type, max_attempts, attempts, prior) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT '定时任务表';
```

## 快速接入

#### 步骤1
下载代码并deploy到公司私服库或其他仓库，在原项目引入task的maven坐标

#### 步骤2
根据task.sql文件建表

#### 步骤3
原项目的springboot配置(参见TaskConfig类)：
```yaml
task:
    github:
        fixedDelay:
            normal: 3000    #定时任务A的fixedDelay频次(毫秒)，根据集群机器数和分表数量合理评估
            timeout: 60000  #定时任务B的fixedDelay频次(毫秒)
        types:
          - handlerName: TestHandler    #handler类名。每新增一个handler，都需要配置一份handlerName & type
            type: 0                     #任务类型(数字)。eg: insert时如果type=0，则任务最终由TestHandler执行
          - hadlerName: xxx
            type: 100
```

#### 步骤4
业务代码中taskService.submitTask(xxx);
```java
public class BussinessClass {
    @Resource
    private HandlerContext handlerContext;
    
    @Transactional(rollbackFor = Exception.class)
    public void bussinessMethod(xxx x) {
        //1.业务逻辑
        
        //2.创建task任务
        taskService.submitTask(TaskCreateParam.builder()
                //TODO 这里还没有想好更优雅的方案
                .type(handlerContext.ofTaskType("TestHandler"))
                .taskKey(goodsId)
                .context(JsonUtils.toString(goods))
                .maxAttempts(3)
                .build());
    }
}

```

#### 步骤5
创建自定义handler，实现ShortRunTaskHandler或LongRunTaskHandler接口，在handleShortTask()方法中写任务逻辑。参见：TestHandler
```java
@Slf4j
@Component
public class TestHandler implements ShortRunTaskHandler {

    public TaskResult handleShortTask(TaskContext taskContext) {
        log.info("处理测试任务开始...");
        
        //TODO 业务逻辑
        
        return TaskResult.successWith("test success");
    }
}
```


## 注意事项
1. 合理评估业务和MySQL的TPS，task-insert频次别压垮MySQL
2. 评估task表的数据增长速度，B端系统一般不要超过几十万/天，如果量太大，需要分表并做好物理删除&备份。
   示例：某个定时任务要连续处理10万条数据，不要1条数据插1条task任务；可以每100条一个批次，共插入1000条task任务
3. 根据集群容器数量和task分表数量，合理设置定时任务频率
4. 由于定时任务A的扫描频次可能不高，可能导致task任务的实际执行时间滞后几秒
5. 可以把发生异常时的处理逻辑，交给task系统解耦和重试，可以减少task的创建数量


## 未来TODO

1. 引入分表&路由键route_key：
    a. 单个task表是无法满足高并发和大集群需求的，数据增长过快，单表成为集群定时任务争抢的热点，需要分表分而治之。
    b. 新增路由键字段，"其hash值 mod 分表数量" => 得到分表后缀。eg: task_19 
        i. 插入：根据route_key计算分表后缀
        ii.查询：每台机器内存中维护一个计数器，从0开始自增，第一次查询task_0，第二次查询task_1... 到最大值后重新置0。
                随着发版批次和每个任务执行耗时的不同，每台机器的轮询都会比较均匀。
    c. 说明：为何采用轮询机制，而不是随机机制，因为随机可能导致饥饿，极端场景下，某个task_x表里有高优任务，但迟迟没有定时任务扫描到x表      
2. 引入分库：拓展分库能力，由使用方定义分库规则
3. 数据物理删除：海量历史数据需要定时做物理删除，建议使用方自己做好备份
4. web页面报表统计：统计任务相关的各种指标。eg：成功率、耗时、不同任务类型的数量

