package com.nhnacademy.marketgg.batch.repository.coupon;

import com.nhnacademy.marketgg.batch.domain.entity.Coupon;
import com.nhnacademy.marketgg.batch.domain.entity.QCoupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class CouponRepositoryImpl extends QuerydslRepositorySupport implements CouponRepositoryCustom {

    public CouponRepositoryImpl() {
        super(Coupon.class);
    }

    QCoupon coupon = QCoupon.coupon;

    @Override
    public Optional<Coupon> findCouponByName(String name) {

        Coupon result = from(coupon)
            .where(coupon.name.eq(name))
            .orderBy(coupon.id.desc())
            .fetchFirst();

        return Optional.ofNullable(result);
    }

}
