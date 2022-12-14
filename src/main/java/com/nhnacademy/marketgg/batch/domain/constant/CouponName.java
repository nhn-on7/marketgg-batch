package com.nhnacademy.marketgg.batch.domain.constant;

/**
 * 쿠폰의 이름 Enum 클래스입니다.
 *
 * @author 민아영
 * @version 1.0.0
 */
public enum CouponName {

    GVIP("GVIP 쿠폰"),
    VIP("VIP 쿠폰"),
    BIRTHDAY("생일 쿠폰");

    private final String name;

    CouponName(String name) {
        this.name = name;
    }

    public String couponName() {
        return this.name;
    }

}
