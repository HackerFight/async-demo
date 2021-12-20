package com.qiuguan.springboot.async.custom.config;

import com.qiuguan.springboot.async.custom.ann.MyAsync;
import com.qiuguan.springboot.async.custom.processor.AsyncAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author created by qiuguan on 2021/12/15 16:50
 */
@Configuration
public class ProxyAsyncConfiguration {

    @Bean
    public AsyncAnnotationBeanPostProcessor asyncAnnotationBeanPostProcessor(){
        AsyncAnnotationBeanPostProcessor pbp = new AsyncAnnotationBeanPostProcessor(null, null);
        pbp.setAnnotationType(MyAsync.class);

        return pbp;
    }

    @Bean
    public Executor executor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(40);
        executor.setThreadNamePrefix("qiuguan-custom-thread-");

        return executor;
    }
}
