package com.fadax.migratemongotomaria.service;
import com.fadax.migratemongotomaria.repository.GenericMongoRepository;
import org.bson.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MongoToSqlService {

    private final GenericMongoRepository mongoRepository;
    private final JdbcTemplate jdbcTemplate;

    public MongoToSqlService(GenericMongoRepository mongoRepository, JdbcTemplate jdbcTemplate) {
        this.mongoRepository = mongoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void migrateCollection(String mongoCollection, String sqlTable) {
        List<Document> documents = mongoRepository.findAll(mongoCollection);

        if (documents.isEmpty()) {
            throw new RuntimeException("No data found in MongoDB collection: " + mongoCollection);
        }

        // Create Main Table
        createTableIfNotExists(sqlTable, documents.get(0));

        // Create Join Tables for Array Fields
        createJoinTables(sqlTable, documents.get(0));

        // Insert Data
        for (Document doc : documents) {
            insertData(sqlTable, doc);
        }
    }

    /** Step 1: Create the Main Table */
    private void createTableIfNotExists(String tableName, Document document) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (id BIGINT AUTO_INCREMENT PRIMARY KEY");

        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (entry.getKey().equals("_id")) {
                sql.append(", mongo_id VARCHAR(50) UNIQUE");
            } else if (entry.getValue() instanceof Integer) {
                sql.append(", ").append(entry.getKey()).append(" INT");
            } else if (entry.getValue() instanceof Double) {
                sql.append(", ").append(entry.getKey()).append(" DOUBLE");
            } else if (!(entry.getValue() instanceof List)) {
                sql.append(", ").append(entry.getKey()).append(" VARCHAR(255)");
            }
        }

        sql.append(")");
        jdbcTemplate.execute(sql.toString());
    }

    /** Step 2: Create Join Tables for Array Fields */
    private void createJoinTables(String parentTable, Document document) {
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (entry.getValue() instanceof List) {
                createJoinTable(parentTable, entry.getKey());
            }
        }
    }

    private void createJoinTable(String parentTable, String columnName) {
        String sql = "CREATE TABLE IF NOT EXISTS " + parentTable + "_" + columnName + " ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "parent_id VARCHAR(50), "
                + "value VARCHAR(255), "
                + "FOREIGN KEY (parent_id) REFERENCES " + parentTable + "(mongo_id) ON DELETE CASCADE)";
        jdbcTemplate.execute(sql);
    }

    /** Step 3: Insert Data into the Main Table */
    private void insertData(String tableName, Document document) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder values = new StringBuilder(" VALUES (");

        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (entry.getKey().equals("_id")) {
                sql.append("mongo_id, ");
                values.append("'").append(entry.getValue().toString()).append("', ");
            } else if (!(entry.getValue() instanceof List)) { // Ignore lists for now
                sql.append(entry.getKey()).append(", ");
                values.append("'").append(entry.getValue().toString()).append("', ");
            }
        }

        sql.setLength(sql.length() - 2);
        values.setLength(values.length() - 2);
        sql.append(")").append(values).append(")");

        jdbcTemplate.update(sql.toString());

        // Insert related lists into join tables
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (entry.getValue() instanceof List) {
                insertJoinTableData(tableName, entry.getKey(), document.getObjectId("_id").toString(), (List<?>) entry.getValue());
            }
        }
    }

    /** Step 4: Insert Data into Join Tables */
    private void insertJoinTableData(String parentTable, String columnName, String parentId, List<?> values) {
        for (Object value : values) {
            String sql = "INSERT INTO " + parentTable + "_" + columnName + " (parent_id, value) VALUES (?, ?)";
            jdbcTemplate.update(sql, parentId, value.toString());
        }
    }
}
