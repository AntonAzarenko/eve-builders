package com.azarenka.evebuilders.migration;

import com.azarenka.evebuilders.config.database.DatabaseTargetValidator;
import com.azarenka.evebuilders.config.database.MigrationDataSourceConfiguration;
import com.azarenka.evebuilders.config.database.MigrationLiquibaseConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication(
    scanBasePackages = "com.azarenka.evebuilders.migration",
    exclude = {
        LiquibaseAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
    }
)
@Import({MigrationDataSourceConfiguration.class, MigrationLiquibaseConfiguration.class, DatabaseTargetValidator.class})
public class MigrationApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(MigrationApplication.class)
            .web(WebApplicationType.NONE)
            .run(args);
        System.exit(org.springframework.boot.SpringApplication.exit(context));
    }
}
