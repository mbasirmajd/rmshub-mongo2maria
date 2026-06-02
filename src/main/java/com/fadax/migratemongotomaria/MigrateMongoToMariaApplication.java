package com.fadax.migratemongotomaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MigrateMongoToMariaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MigrateMongoToMariaApplication.class, args);
    }

}
