package com.nhnacademy.marketgg.batch.batchstep;

import static com.nhnacademy.marketgg.batch.domain.constant.CouponName.GVIP;
import static com.nhnacademy.marketgg.batch.domain.constant.CouponName.VIP;

import com.nhnacademy.marketgg.batch.config.BatchJobParameter;
import com.nhnacademy.marketgg.batch.domain.dto.GivenCouponDto;
import com.nhnacademy.marketgg.batch.domain.dto.MemberDto;
import com.nhnacademy.marketgg.batch.domain.entity.Coupon;
import com.nhnacademy.marketgg.batch.exception.CouponNotFoundException;
import com.nhnacademy.marketgg.batch.repository.coupon.CouponRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

/**
 * Gvip 회원을 조회하여 등급 쿠폰을 지급하는 Batch Step 과 Step Process(Reader, Processor, Writer) 입니다.
 *
 * @author 민아영
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class GiveCouponMemberStep {

    private final DataSource dataSource;
    private final StepBuilderFactory stepBuilderFactory;
    private final CouponRepository couponRepository;

    private static final int CHUNK_SIZE = 100;


    @Bean
    @JobScope
    public BatchJobParameter jobParameter() {
        return new BatchJobParameter();
    }


    /**
     * gVip 회원을 모두 조회하고 등급 쿠폰을 발급하는 Step 입니다.
     *
     * @return Step - Process(Reader, Processor, Writer) 설정을 stepBuilderFactory 가 빌드하여 반환한다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    @JobScope
    public Step gVipGivenCouponMemberStep() throws Exception {
        jobParameter().setGradeNo(1L);
        jobParameter().setCouponName(GVIP);
        return stepBuilderFactory.get("gVipGivenCouponMemberStep")
                                 .<MemberDto, GivenCouponDto>chunk(CHUNK_SIZE)
                                 .reader(gradeMemberReader())
                                 .processor(givenCouponProcessor())
                                 .writer(gradeMemberWriter())
                                 .allowStartIfComplete(true)  // Job 이 Complete 된 상태여도 다시 시작하는 옵션
                                 .build();
    }

    /**
     * vip 회원을 모두 조회하고 등급 쿠폰을 발급하는 Step 입니다.
     *
     * @return Step - process(reader, processor, writer) 설정을 stepBuilderFactory 가 빌드하여 반환한다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    @JobScope
    public Step vipGivenCouponMemberStep() throws Exception {
        jobParameter().setGradeNo(2L);
        jobParameter().setCouponName(VIP);
        return stepBuilderFactory.get("vipGivenCouponMemberStep")
                                 .<MemberDto, GivenCouponDto>chunk(CHUNK_SIZE)
                                 .reader(gradeMemberReader())
                                 .processor(givenCouponProcessor())
                                 .writer(gradeMemberWriter())
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
    @StepScope
    public JdbcPagingItemReader<MemberDto> gradeMemberReader() throws Exception {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("memberGradeNo", jobParameter().getGradeNo());

        return new JdbcPagingItemReaderBuilder<MemberDto>()
            .name("gradeMemberReader")
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
     * 회원에게 Gvip 쿠폰을 발급하는 Processor 입니다.
     *
     * @return 발급한 Gvip 쿠폰을 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    @StepScope
    public CompositeItemProcessor<MemberDto, GivenCouponDto> givenCouponProcessor() {

        Coupon coupon = couponRepository.findCouponByName(jobParameter().getCouponName().couponName())
                                        .orElseThrow(CouponNotFoundException::new);

        List<ItemProcessor<MemberDto, GivenCouponDto>> delegates = new ArrayList<>(1);
        delegates.add(processor1(coupon.getId()));

        CompositeItemProcessor<MemberDto, GivenCouponDto> processor = new CompositeItemProcessor<>();

        processor.setDelegates(delegates);
        return processor;
    }

    public ItemProcessor<MemberDto, GivenCouponDto> processor1(Long couponId) {

        return memberDto -> new GivenCouponDto(memberDto.getMemberNo(), couponId);
    }

    /**
     * 발급한 Gvip 쿠폰을 DB 에 저장하는 Writer 입니다.
     *
     * @return 발급한 Gvip 쿠폰의 정보를 담은 Writer 를 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    @StepScope
    public JdbcBatchItemWriter<GivenCouponDto> gradeMemberWriter() {

        return new JdbcBatchItemWriterBuilder<GivenCouponDto>()
            .dataSource(this.dataSource)
            .sql("insert into given_coupons" +
                     "(coupon_no, member_no, created_at) " +
                     "values (:couponNo, :memberNo, now())")
            .beanMapped()  // GivenCouponDto 의 프로퍼티 네임으로 매핑
            .build();
    }

}
