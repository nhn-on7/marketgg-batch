package com.nhnacademy.marketgg.batch.repository.member;

import com.nhnacademy.marketgg.batch.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {


}
