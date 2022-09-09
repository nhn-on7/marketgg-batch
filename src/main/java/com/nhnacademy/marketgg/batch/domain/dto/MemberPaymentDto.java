package com.nhnacademy.marketgg.batch.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 총 구매 금액 조회 결과 DTO 입니다.
 *
 * @author 민아영
 * @version 1.0.0
 */
@NoArgsConstructor
@Setter
@Getter
public class MemberPaymentDto {

    private Long memberNo;

    private Long totalAmount;

}
