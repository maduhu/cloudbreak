package com.sequenceiq.periscope.config;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;

import org.quartz.simpl.SimpleJobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.sequenceiq.cloudbreak.client.ConfigKey;
import com.sequenceiq.cloudbreak.client.IdentityClient;
import com.sequenceiq.cloudbreak.client.RestClientUtil;

@Configuration
@EnableAsync
@EnableScheduling
public class AppConfig implements AsyncConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    @Value("${periscope.threadpool.core.size:50}")
    private int corePoolSize;

    @Value("${periscope.threadpool.max.size:500}")
    private int maxPoolSize;

    @Value("${periscope.threadpool.queue.size:1000}")
    private int queueCapacity;

    @Value("${periscope.client.id}")
    private String clientId;

    @Inject
    @Named("identityServerUrl")
    private String identityServerUrl;

    @Value("${rest.debug:false}")
    private boolean restDebug;

    @Value("${cert.validation:true}")
    private boolean certificateValidation;

    @Bean
    public ThreadPoolExecutorFactoryBean getThreadPoolExecutorFactoryBean() {
        ThreadPoolExecutorFactoryBean executorFactoryBean = new ThreadPoolExecutorFactoryBean();
        executorFactoryBean.setCorePoolSize(corePoolSize);
        executorFactoryBean.setMaxPoolSize(maxPoolSize);
        executorFactoryBean.setQueueCapacity(queueCapacity);
        return executorFactoryBean;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setTaskExecutor(getAsyncExecutor());
        scheduler.setAutoStartup(true);
        scheduler.setJobFactory(new SimpleJobFactory());
        return scheduler;
    }

    @Bean
    public IdentityClient identityClient() {
        return new IdentityClient(identityServerUrl, clientId, new ConfigKey(certificateValidation, restDebug));
    }

    @Bean
    public Client restClient() {
        return RestClientUtil.get(new ConfigKey(certificateValidation, restDebug));
    }

    @Override
    public Executor getAsyncExecutor() {
        try {
            return getThreadPoolExecutorFactoryBean().getObject();
        } catch (Exception e) {
            LOGGER.error("Error creating task executor.", e);
        }
        return null;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}