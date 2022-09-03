package com.nhnacademy.marketgg.batch.batchstep;

import com.nhnacademy.marketgg.batch.domain.entity.Member;
import com.nhnacademy.marketgg.batch.domain.entity.MemberGrade;
import com.nhnacademy.marketgg.batch.exception.MemberGradeNotFoundException;
import com.nhnacademy.marketgg.batch.repository.membergrade.MemberGradeRepository;
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
 * 회원을 모두 조회하여 등급을 초기화하는 Batch Step 과 Step Process(Reader, Processor, Writer) 입니다.
 *
 * @author 민아영
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class ResetMemberGradeStep {

    private final EntityManagerFactory entityManagerFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MemberGradeRepository memberGradeRepository;

    private static final int CHUNK_SIZE = 1000;
    private static final long MEMBER = 3L;

    /**
     * 회원을 모두 조회하고 등급을 초기화하는 Step 입니다.
     * chunk 는 처리하는 수행단위 입니다. Commit 되는 트랜잭션 단위와 같습니다.
     *
     * @return Step - Process(Reader, Processor, Writer) 설정을 stepBuilderFactory 가 빌드하여 반환한다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public Step memberGradeResetStep() {
        return stepBuilderFactory.get("memberGradeResetStep")
                                 .<Member, Member>chunk(CHUNK_SIZE)
                                 .reader(allMemberReader())
                                 .processor(resetGradeProcessor())
                                 .writer(allMemberWriter())
                                 .allowStartIfComplete(true)
                                 .build();
    }

    /**
     * 회원을 모두 조회하는 Reader 입니다.
     * page_size 와 chunk_size 는 똑같은 값으로 설정 했습니다.
     *
     * @return 조회한 MemberByAmount 리스트를 JdbcPagingItemReaderBuilder 로 빌드하여 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public JpaPagingItemReader<Member> allMemberReader() {

        return new JpaPagingItemReaderBuilder<Member>()
            .queryString("SELECT m FROM Member m")
            .pageSize(CHUNK_SIZE)
            .entityManagerFactory(entityManagerFactory)
            .name("allMemberReader")
            .build();
    }

    /**
     * 회원의 등급 정보를 업데이트 하기 위한 비지니스 로직이 작성된 Processor 입니다.
     *
     * @return 등급 정보를 업데이트한 회원을 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public ItemProcessor<Member, Member> resetGradeProcessor() {

        return member -> {
            MemberGrade grade = memberGradeRepository.findById(MEMBER)
                                                     .orElseThrow(MemberGradeNotFoundException::new);
            member.updateGrade(grade);
            return member;
        };
    }

    /**
     * 등급이 업데이트 된 회원들을 DB 에 저장하는 Writer 입니다.
     *
     * @return 회원들의 정보를 담은 Writer 를 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    @Bean
    public JpaItemWriter<Member> allMemberWriter() {
        JpaItemWriter<Member> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
