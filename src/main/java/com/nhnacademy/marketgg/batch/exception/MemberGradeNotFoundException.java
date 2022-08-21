package com.nhnacademy.marketgg.batch.exception;

public class MemberGradeNotFoundException extends IllegalArgumentException {

    private static final String ERROR = "회원 등급을 찾을 수 없습니다.";

    /**
     * 예외처리 시, 지정한 메세지를 보냅니다.
     *
     * @since 1.0.0
     */
    public MemberGradeNotFoundException() {
        super(ERROR);
    }

}
