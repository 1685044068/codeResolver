package com.icbc.codeResolver.config;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @BelongsProject: project3
 * @BelongsPackage: com.icbc.codeResolver.config
 * @Author: zero
 * @CreateTime: 2024-07-31  09:15
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
@EnableAsync
public class AsyncThreadPoolConfig implements AsyncConfigurer {
    private static Logger logger = Logger.getLogger(AsyncThreadPoolConfig.class);
    private static ThreadPoolTaskExecutor executor;
    //获取单前机器cpu数量
    private static final int cpu = Runtime.getRuntime().availableProcessors();
    //设置核心线程数
    private static final int corePoolSize = 1;
    //设置最大线程数
    private static final int maxPoolSize = 1000;
    //设置线程空闲时间(秒)
    private static final int keepAliveTime = 1;
    //设置主线程等待时间
    private static final int awaitTerminationSeconds = 120;
    //缓存队列数
    private static final int queueCapacity = 200;
    //线程池前缀名

    public static ThreadPoolTaskExecutor getExecutor() {
        return executor;
    }

    //利用Spring容器进行自动注入
    @Bean("parseExecutor")
    @Lazy
    public ThreadPoolTaskExecutor taskExecutor() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        //等待队列大小
        executor.setQueueCapacity(queueCapacity);
        //空闲时间
        executor.setKeepAliveSeconds(keepAliveTime);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // RejectedExecutionHandler:当pool已经达到max-size的时候，如何处理新任务
        // CallerRunsPolicy:不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
