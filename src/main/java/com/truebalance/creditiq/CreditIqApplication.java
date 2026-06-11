package com.truebalance.creditiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CreditIqApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditIqApplication.class, args);
    }
}
