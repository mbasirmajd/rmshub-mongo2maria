package com.fadax.migratemongotomaria;
import com.fadax.migratemongotomaria.service.MongoToSqlService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MongoToSqlScheduler {
    private final MongoToSqlService mongoToSqlService;

    public MongoToSqlScheduler(MongoToSqlService mongoToSqlService) {
        this.mongoToSqlService = mongoToSqlService;
    }

    @Scheduled(cron = "0 0 * * * ?") // Runs every hour
    public void scheduleMigration() {
        System.out.println("🔄 Starting scheduled migration...");
        mongoToSqlService.migrateCollection("amazement", "amazement");
    }
}