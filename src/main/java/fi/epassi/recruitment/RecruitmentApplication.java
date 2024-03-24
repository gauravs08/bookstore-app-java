package fi.epassi.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableConfigurationProperties
//@EnableJpaAuditing
@EnableCaching
@EnableR2dbcRepositories
//@EntityScan
@EnableR2dbcAuditing
//@ComponentScan(basePackages = {"fi.epassi.recruitment.repository","fi.epassi.recruitment"})
public class RecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitmentApplication.class, args);
    }

}

