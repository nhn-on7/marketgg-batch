package com.nhnacademy.marketgg.batch.batchstep;


import static com.nhnacademy.marketgg.batch.domain.constant.CouponName.VIP;

import com.nhnacademy.marketgg.batch.domain.dto.GivenCouponDto;
import com.nhnacademy.marketgg.batch.domain.dto.MemberDto;
import com.nhnacademy.marketgg.batch.domain.entity.Coupon;
import com.nhnacademy.marketgg.batch.exception.CouponNotFoundException;
import com.nhnacademy.marketgg.batch.repository.coupon.CouponRepository;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

/**
 * vip 회원을 조회하여 등급 쿠폰을 지급하는 Batch Step 과 step process(reader, processor, writer) 입니다.
 *
 * @author 민아영
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class GiveCouponVIPMemberStep {

    private final DataSource dataSource;

    private final StepBuilderFactory stepBuilderFactory;
    private final CouponRepository couponRepository;

    private static final int CHUNK_SIZE = 100;

    /**
     * vip 회원을 모두 조회하고 등급 쿠폰을 발급하는 Step 입니다.
     *
     * @return Step - process(reader, processor, writer) 설정을 stepBuilderFactory 가 빌드하여 반환한다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public Step vipGivenCouponMemberStep() throws Exception {
        return stepBuilderFactory.get("vipGivenCouponMemberStep")
                                 .<MemberDto, GivenCouponDto>chunk(CHUNK_SIZE)
                                 .reader(vipMemberReader())
                                 .processor(vipGivenCouponProcessor())
                                 .writer(vipMemberWriter())
                                 .allowStartIfComplete(true)
                                 .build();
    }

    /**
     * vip 회원을 모두 조회하는 reader 입니다.
     * page_size 와 chunk_size 는 똑같은 값으로 설정 했습니다.
     *
     * @return 조회한 Member 리스트를 JpaPagingItemReaderBuilder 로 빌드하여 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public JdbcPagingItemReader<MemberDto> vipMemberReader() throws Exception {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("memberGradeNo", 2);

        return new JdbcPagingItemReaderBuilder<MemberDto>()
            .name("vipMemberReader")
            .pageSize(CHUNK_SIZE)
            .dataSource(this.dataSource)
            .queryProvider(createQueryProvider())
            .parameterValues(parameterValues)
            .rowMapper(new BeanPropertyRowMapper<>(MemberDto.class))
            .build();
    }

    /**
     * 회원에서 Gvip 등급인 회원만 조회하는 쿼리를 설정합니다.
     *
     * @return 작성된 쿼리를 반환합니다.
     * @throws Exception - 데이터를 객체로 변환할 때 발생할 수 있는 에러입니다.
     */
    private PagingQueryProvider createQueryProvider() throws Exception {
        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("member_no", Order.ASCENDING);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(this.dataSource);
        queryProvider.setSelectClause("select member_no");
        queryProvider.setFromClause("from members");
        queryProvider.setWhereClause("where member_grade_no = :memberGradeNo");
        queryProvider.setSortKeys(sortKey);

        return queryProvider.getObject();
    }

    /**
     * 회원에게 vip 쿠폰을 발급하는 processor 입니다.
     *
     * @return 발급한 vip 쿠폰을 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public ItemProcessor<MemberDto, GivenCouponDto> vipGivenCouponProcessor() {
        Coupon vipCoupon = couponRepository.findCouponByName(VIP.couponName())
                                           .orElseThrow(CouponNotFoundException::new);
        return memberDto -> new GivenCouponDto(memberDto.getMemberNo(), vipCoupon.getId());
    }

    /**
     * 발급한 vip 쿠폰을 DB 에 저장하는 writer 입니다.
     *
     * @return 발급한 vip 쿠폰의 정보를 담은 writer 를 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public JdbcBatchItemWriter<GivenCouponDto> vipMemberWriter() {

        return new JdbcBatchItemWriterBuilder<GivenCouponDto>()
            .dataSource(this.dataSource)
            .sql("insert into given_coupons" +
                "(coupon_no, member_no, created_at) " +
                "values (:couponNo, :memberNo, now())")
            .beanMapped()
            .build();
    }

}
