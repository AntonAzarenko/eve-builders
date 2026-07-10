package com.azarenka.evebuilders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = "com.azarenka.evebuilders",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.azarenka\\.evebuilders\\.migration\\..*"
    )
)
@ConfigurationPropertiesScan(basePackages = "com.azarenka.evebuilders.config.database")
@EnableScheduling
public class EveBuildersApplication {

    public static void main(String[] args) {
        SpringApplication.run(EveBuildersApplication.class, args);
    }
}
