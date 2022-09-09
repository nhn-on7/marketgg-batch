package com.nhnacademy.marketgg.batch.config;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch 설정 파일 입니다.
 *
 * @author 민아영
 * @version 1.0.0
 */
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class BatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final Step memberGradeResetStep;
    private final Step memberGradeUpdateStep;
    private final Step gVipGivenCouponMemberStep;
    private final Step vipGivenCouponMemberStep;

    /**
     * Batch 의 Job 에 Step flow 을 설정합니다.
     * job 의 이름은 실행 시 중복되지 않고 실행 시간을 알 수 있도록 현재 시간으로 설정합니다.
     * Step flow 는 4단계로 구성되어 있습니다.
     *
     * @return jobBuilderFactory 로 build 한 Job 을 반환합니다.
     */
    @Bean
    public Job memberGradeJob() {
        return jobBuilderFactory.get(LocalDateTime.now().toString())
                                .start(memberGradeResetStep)
                                .next(memberGradeUpdateStep)
                                .next(gVipGivenCouponMemberStep)
                                .next(vipGivenCouponMemberStep)
                                .build();
    }

}
