package com.nhnacademy.marketgg.batch.repository.member;

import com.nhnacademy.marketgg.batch.domain.entity.Member;
import com.nhnacademy.marketgg.batch.domain.entity.QMember;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import java.util.List;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class MemberRepositoryImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom {

    public MemberRepositoryImpl() {
        super(Member.class);
    }

    QMember member = QMember.member;

    @Override
    public List<Member> findAllMembersByBirthday(String birthday) {

        StringTemplate dateFormat
            = Expressions.stringTemplate("DATE_FORMAT({0}, {1})", member.birthDate, ConstantImpl.create("%m-%d"));

        return from(member)
            .where(dateFormat.eq(birthday))
            .fetch();
    }
}
