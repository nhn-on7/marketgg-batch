package com.nhnacademy.marketgg.batch.repository.coupon;

import com.nhnacademy.marketgg.batch.domain.entity.Coupon;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CouponRepositoryCustom {

    /**
     * 쿠폰 name 으로 일치하는 쿠폰 을 반환합니다.
     *
     * @param name - 쿠폰 name 입니다.
     * @return Coupon 을 Optional 로 반환합니다.
     * @author 민아영
     * @since 1.0.0
     */
    Optional<Coupon> findCouponByName(String name);

}

