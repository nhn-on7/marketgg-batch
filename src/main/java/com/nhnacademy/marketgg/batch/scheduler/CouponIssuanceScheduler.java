package com.nhnacademy.marketgg.batch.scheduler;

import static com.nhnacademy.marketgg.batch.domain.constant.CouponName.BIRTHDAY;

import com.nhnacademy.marketgg.batch.config.BatchJobConfig;
import com.nhnacademy.marketgg.batch.domain.entity.Coupon;
import com.nhnacademy.marketgg.batch.domain.entity.GivenCoupon;
import com.nhnacademy.marketgg.batch.domain.entity.Member;
import com.nhnacademy.marketgg.batch.exception.CouponNotFoundException;
import com.nhnacademy.marketgg.batch.repository.coupon.CouponRepository;
import com.nhnacademy.marketgg.batch.repository.givencoupon.GivenCouponRepository;
import com.nhnacademy.marketgg.batch.repository.member.MemberRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 생일 쿠폰 발급과 매월 회원 등급 관리에 관한 Scheduler 를 설정한 클래스입니다.
 *
 * @author 민아영
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssuanceScheduler {

    private final JobLauncher jobLauncher;
    private final BatchJobConfig jobConfig;

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final GivenCouponRepository givenCouponRepository;

    /**
     * 매일 자정에 생일인 회원을 조회하여 생일 쿠픈을 지급하는 메소드입니다.
     *
     * @author 민아영
     * @since 1.0.0
     */
    @Async
    @Scheduled(cron = "@daily", zone = "Asia/Seoul")
    public void scheduleBirthdayCoupon() {
        log.info("생일 쿠폰 스케줄러 시작 시간: {}", LocalDateTime.now());

        Coupon birthdayCoupon = couponRepository.findCouponByName(BIRTHDAY.couponName())
                                                .orElseThrow(CouponNotFoundException::new);

        String todayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd"));
        List<Member> members = memberRepository.findAllMembersByBirthday(todayDate);

        for (Member member : members) {
            log.info("오늘 생일인 회원: {}", member);
            GivenCoupon givenCoupon = new GivenCoupon(birthdayCoupon, member);
            givenCouponRepository.save(givenCoupon);
        }
        log.info("스케줄러 끝 시간: {}", LocalDateTime.now());
    }

    /**
     * 매월 1일 자정에 회원 등급을 업데이트하고 등급에 따른 쿠폰을 지급해주는 Batch Job 을 실행하는 Scheduler 입니다.
     *
     * @author 민아영
     * @since 1.0.0
     */
    @Async
    // @Scheduled(cron = "@monthly", zone = "Asia/Seoul")
    @Scheduled(fixedDelay = 10000)
    public void scheduleMemberGradeCoupon() {
        log.info("등급 쿠폰 스케줄러 시작 시간: {}", LocalDateTime.now());

        Job job = jobConfig.memberGradeJob();
        JobParameters jobParameters = new JobParameters();

        try {
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                 | JobParametersInvalidException | JobRestartException e) {
            log.error(e.getMessage());
        }

        log.info("등급 쿠폰 스케줄러 끝 시간: {}", LocalDateTime.now());
    }

}
