package com.nhnacademy.marketgg.batch.batchstep;

import com.nhnacademy.marketgg.batch.domain.dto.MemberPaymentDto;
import com.nhnacademy.marketgg.batch.domain.entity.Member;
import com.nhnacademy.marketgg.batch.domain.entity.MemberGrade;
import com.nhnacademy.marketgg.batch.exception.MemberGradeNotFoundException;
import com.nhnacademy.marketgg.batch.exception.MemberNotFoundException;
import com.nhnacademy.marketgg.batch.repository.member.MemberRepository;
import com.nhnacademy.marketgg.batch.repository.membergrade.MemberGradeRepository;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

/**
 * 주문 내역을 조회하여 한달 동안 총 구매 금액 별 등급을 업데이트하는 Batch Step 과 Step process(reader, processor, writer) 입니다.
 *
 * @author 민아영
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class UpdateMemberGradeStep {

    private final EntityManagerFactory entityManagerFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MemberGradeRepository memberGradeRepository;
    private final MemberRepository memberRepository;
    private final DataSource dataSource;

    private static final int CHUNK_SIZE = 5;
    private static final long GVIP = 1L;
    private static final long VIP = 2L;
    private static final long MEMBER = 3L;

    /**
     * 주문 내역을 조회하고 구매 금액 별 등급을 업데이트하고 저장하는 Step 입니다.
     * Chunk 는 처리하는 수행단위 입니다. Commit 되는 트랜잭션 단위와 같습니다.
     *
     * @return Step - Process(Reader, Processor, Writer) 설정을 stepBuilderFactory 가 빌드하여 반환한다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public Step memberGradeUpdateStep() throws Exception {
        return stepBuilderFactory.get("memberGradeUpdateStep")
                                 .<MemberPaymentDto, Member>chunk(CHUNK_SIZE)
                                 .reader(memberReader())
                                 .processor(updateGradeProcessor())
                                 .writer(memberWriter())
                                 .allowStartIfComplete(true)
                                 .build();
    }

    /**
     * 조회 기간에 회원의 구매 금액을 조회하는 Reader 입니다.
     * Page_size 와 Chunk_size 는 똑같은 값으로 설정했습니다.
     *
     * @return 조회한 MemberByAmount 리스트를 JdbcPagingItemReaderBuilder 로 빌드하여 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public JdbcPagingItemReader<MemberPaymentDto> memberReader() throws Exception {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("start_date", "2022-08-01");
        parameterValues.put("end_date", "2022-08-31");

        return new JdbcPagingItemReaderBuilder<MemberPaymentDto>().name("memberReader")
                                                                  .pageSize(CHUNK_SIZE)
                                                                  .dataSource(this.dataSource)
                                                                  .queryProvider(createQueryProvider())
                                                                  .parameterValues(parameterValues)
                                                                  .rowMapper(new BeanPropertyRowMapper<>(MemberPaymentDto.class))
                                                                  .build();
    }

    /**
     * 회원과 주문 테이블을 조회하여 총 구매 금액을 조회하는 쿼리를 설정합니다.
     *
     * @return 작성된 쿼리를 반환합니다.
     * @throws Exception - 데이터를 객체로 변환할 때 발생할 수 있는 에러입니다.
     */
    private PagingQueryProvider createQueryProvider() throws Exception {
        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("member_no", Order.ASCENDING);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(this.dataSource);
        queryProvider.setSelectClause("select member_no, sum(total_amount) as total_amount");
        queryProvider.setFromClause("from orders");
        queryProvider.setWhereClause("where created_at >= :start_date and created_at <= :end_date");
        queryProvider.setGroupClause("group by member_no");
        queryProvider.setSortKeys(sortKey);

        return queryProvider.getObject();
    }

    /**
     * 회원의 등급 정보를 업데이트 하기 위한 비지니스 로직이 작성된 processor 입니다.
     *
     * @return 등급 정보를 업데이트한 회원을 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public ItemProcessor<MemberPaymentDto, Member> updateGradeProcessor() {

        return memberPaymentDto -> {
            long memberGrade;
            long account = memberPaymentDto.getTotalAmount();

            if (account < 300_000L) {
                memberGrade = MEMBER;
            } else if (account < 500_000L) {
                memberGrade = VIP;
            } else {
                memberGrade = GVIP;
            }
            MemberGrade grade = memberGradeRepository.findById(memberGrade)
                                                     .orElseThrow(MemberGradeNotFoundException::new);
            Member member = memberRepository.findById(memberPaymentDto.getMemberNo())
                                            .orElseThrow(MemberNotFoundException::new);
            member.updateGrade(grade);
            return member;
        };
    }

    /**
     * 등급이 업데이트 된 회원들을 DB 에 저장하는 writer 입니다.
     *
     * @return 회원들의 정보를 담은 writer 를 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public JpaItemWriter<Member> memberWriter() {
        JpaItemWriter<Member> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
