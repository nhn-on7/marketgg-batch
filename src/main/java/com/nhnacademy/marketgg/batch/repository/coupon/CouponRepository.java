package com.nhnacademy.marketgg.batch.repository.coupon;

import com.nhnacademy.marketgg.batch.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryCustom {

}
