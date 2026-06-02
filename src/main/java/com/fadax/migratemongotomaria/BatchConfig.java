package com.fadax.migratemongotomaria;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.sql.DataSource;
import java.util.Collections;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    @Bean
    public MongoItemReader<Document> reader(MongoTemplate mongoTemplate) {
        MongoItemReader<Document> reader = new MongoItemReader<>();
        reader.setTemplate(mongoTemplate);
        reader.setQuery("{}");
        reader.setTargetType(Document.class);
        reader.setSort(Collections.singletonMap("_id", 1));
        reader.setCollection("amazement");
        return reader;
    }

    @Bean
    public ItemProcessor<Document, Document> processor() {
        return new PassThroughItemProcessor<>();
    }

    @Bean
    public JdbcBatchItemWriter<Document> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Document>()
                .sql("INSERT INTO amazement (mongo_id, field1, field2) VALUES (:id, :field1, :field2)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public Step migrationStep(StepBuilderFactory stepBuilderFactory, MongoItemReader<Document> reader,
                              ItemProcessor<Document, Document> processor, JdbcBatchItemWriter<Document> writer) {
        return stepBuilderFactory.get("migrationStep")
                .<Document, Document>chunk(100)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job migrateMongoToSqlJob(JobBuilderFactory jobBuilderFactory, Step migrationStep) {
        return jobBuilderFactory.get("migrateMongoToSqlJob")
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        System.out.println("🔄 Starting migration job...");
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        System.out.println("✅ Migration job completed!");
                    }
                })
                .start(migrationStep)
                .build();
    }
}
