package com.nhnacademy.marketgg.batch.batchstep;

import static com.nhnacademy.marketgg.batch.domain.constant.CouponName.GVIP;

import com.nhnacademy.marketgg.batch.domain.entity.Coupon;
import com.nhnacademy.marketgg.batch.domain.entity.GivenCoupon;
import com.nhnacademy.marketgg.batch.domain.entity.Member;
import com.nhnacademy.marketgg.batch.exception.CouponNotFoundException;
import com.nhnacademy.marketgg.batch.repository.coupon.CouponRepository;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gvip 회원을 조회하여 등급 쿠폰을 지급하는 Batch Step 과 Step Process(Reader, Processor, Writer) 입니다.
 *
 * @author 민아영
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class GiveCouponGVIPMemberStep {

    private final EntityManagerFactory entityManagerFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final CouponRepository couponRepository;

    private static final int CHUNK_SIZE = 5;

    /**
     * gVip 회원을 모두 조회하고 등급 쿠폰을 발급하는 Step 입니다.
     *
     * @return Step - Process(Reader, Processor, Writer) 설정을 stepBuilderFactory 가 빌드하여 반환한다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public Step gVipGivenCouponMemberStep() {
        return stepBuilderFactory.get("gVipGivenCouponMemberStep")
                                 .<Member, GivenCoupon>chunk(CHUNK_SIZE)
                                 .reader(gVIPMemberReader())
                                 .processor(gVipGivenCouponProcessor())
                                 .writer(gVIPMemberWriter())
                                 .allowStartIfComplete(true)
                                 .build();
    }

    /**
     * Gvip 회원을 모두 조회하는 Reader 입니다.
     * Page_size 와 Chunk_size 는 똑같은 값으로 설정 했습니다.
     *
     * @return 조회한 Member 리스트를 JpaPagingItemReaderBuilder 로 빌드하여 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public JpaPagingItemReader<Member> gVIPMemberReader() {
        return new JpaPagingItemReaderBuilder<Member>()
            .queryString("SELECT m FROM Member m WHERE m.memberGrade = 1")
            .pageSize(CHUNK_SIZE)
            .entityManagerFactory(entityManagerFactory)
            .name("gVIPMemberReader")
            .build();
    }

    /**
     * 회원에게 Gvip 쿠폰을 발급하는 Processor 입니다.
     *
     * @return 발급한 Gvip 쿠폰을 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public ItemProcessor<Member, GivenCoupon> gVipGivenCouponProcessor() {
        Coupon gVipCoupon = couponRepository.findCouponByName(GVIP.couponName())
                                            .orElseThrow(CouponNotFoundException::new);
        return member -> new GivenCoupon(gVipCoupon, member);
    }

    /**
     * 발급한 Gvip 쿠폰을 DB 에 저장하는 Writer 입니다.
     *
     * @return 발급한 Gvip 쿠폰의 정보를 담은 Writer 를 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public JpaItemWriter<GivenCoupon> gVIPMemberWriter() {
        JpaItemWriter<GivenCoupon> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

}
