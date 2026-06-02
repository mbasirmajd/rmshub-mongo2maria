package com.fadax.migratemongotomaria;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduler {
    private final JobLauncher jobLauncher;
    private final Job migrateMongoToSqlJob;

    public BatchScheduler(JobLauncher jobLauncher, Job migrateMongoToSqlJob) {
        this.jobLauncher = jobLauncher;
        this.migrateMongoToSqlJob = migrateMongoToSqlJob;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Runs every day at 2 AM
    public void scheduleMigration() {
        try {
            jobLauncher.run(migrateMongoToSqlJob, new JobParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
