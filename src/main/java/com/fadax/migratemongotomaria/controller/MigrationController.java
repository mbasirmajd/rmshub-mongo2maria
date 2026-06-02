package com.fadax.migratemongotomaria.controller;
import com.fadax.migratemongotomaria.service.MongoToSqlService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/migrate")
public class MigrationController {

    private final MongoToSqlService migrationService;

    public MigrationController(MongoToSqlService migrationService) {
        this.migrationService = migrationService;
    }

    @GetMapping("/{mongoCollection}/{sqlTable}")
    public String migrate(@PathVariable String mongoCollection, @PathVariable String sqlTable) {
        migrationService.migrateCollection(mongoCollection, sqlTable);
        return "Migration from MongoDB `" + mongoCollection + "` to MariaDB `" + sqlTable + "` completed!";
    }
}
