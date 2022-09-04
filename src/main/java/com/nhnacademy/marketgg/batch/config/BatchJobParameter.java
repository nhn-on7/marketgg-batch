package com.nhnacademy.marketgg.batch.config;

import com.nhnacademy.marketgg.batch.domain.constant.CouponName;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
public class BatchJobParameter {

    @Value("#{jobParameters['startDate']}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Value("#{jobParameters['endDate']}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Value("#{jobParameters['gradeNo']}")
    private Long gradeNo;

    @Value("#{jobParameters['couponName']}")
    private CouponName couponName;

}
