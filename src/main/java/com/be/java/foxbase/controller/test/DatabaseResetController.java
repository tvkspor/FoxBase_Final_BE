package com.be.java.foxbase.controller.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@Profile("test")
public class DatabaseResetController {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @DeleteMapping("/reset-database")
  public String resetDatabase() {
    // Turn of foreign key check
    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

    // Get all tables in database test
    var tables = jdbcTemplate.queryForList(
        "SELECT table_name FROM information_schema.tables WHERE table_schema = 'foxbase_test_db'");

    // Delete all records
    for (var table : tables) {
      String tableName = (String) table.get("table_name");
      jdbcTemplate.execute("DELETE FROM `" + tableName + "`");
    }

    // Turn on foreign key check
    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

    return "Test database reset successfully";
  }
}