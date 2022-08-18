package com.nhnacademy.marketgg.batch.repository.member;

import com.nhnacademy.marketgg.batch.domain.entity.Member;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MemberRepositoryCustom {

    List<Member> findAllMembersByBirthday(String birthday);

}
