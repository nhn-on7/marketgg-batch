package com.nhnacademy.marketgg.batch.exception;

public class CouponNotFoundException extends IllegalArgumentException {

    /**
     * 쿠폰을 찾을 수 없을 때 예외처리입니다.
     *
     * @version 1.0.0
     */
    private static final String ERROR = "쿠폰을 찾을 수 없습니다.";

    /**
     * 예외처리 시, 지정한 메세지를 보냅니다.
     *
     * @since 1.0.0
     */
    public CouponNotFoundException() {
        super(ERROR);
    }

}
