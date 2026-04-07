package com.commerceos.taxcalculation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class TaxCalculationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaxCalculationApplication.class, args);
    }
}
