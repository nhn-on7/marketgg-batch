package com.nhnacademy.marketgg.batch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;


/**
 * Spring Scheduler 설정 파일입니다.
 *
 * @author 민아영
 * @version 1.0.0
 */
@EnableScheduling
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    private static final int SCHEDULE_THREAD_SIZE = 5;

    /**
     * Scheduler Thread Pool 설정 관련 메소드 입니다.
     * Scheduler Thread default 값은 1개 입니다.
     * 동시에 실행되는 Scheduler 를 고려해 설정한 값입니다.
     *
     * @param taskRegistrar - 설정 값을 등록해주는 매개변수
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setPoolSize(SCHEDULE_THREAD_SIZE);
        threadPoolTaskScheduler.initialize();

        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }

}
