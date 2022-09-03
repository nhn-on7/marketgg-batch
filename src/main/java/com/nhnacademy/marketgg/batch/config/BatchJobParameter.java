package com.nhnacademy.marketgg.batch.config;

import java.time.LocalDate;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;


@Getter
public class BatchJobParameter {

    @Value("#{jobParameters['startDate']}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Value("#{jobParameters['endDate']}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

}
