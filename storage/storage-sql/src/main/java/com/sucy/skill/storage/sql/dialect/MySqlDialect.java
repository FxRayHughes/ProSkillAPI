/**
 * SkillAPI
 * com.sucy.skill.storage.sql.dialect.MySqlDialect
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.storage.sql.dialect;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * MySQL and MariaDB, which share the same grammar and error codes.
 */
public class MySqlDialect implements SqlDialect {
    /** MySQL error 1062: duplicate entry for a unique key. */
    private static final int ER_DUP_ENTRY = 1062;

    private final String id;

    public MySqlDialect() {
        this("mysql");
    }

    MySqlDialect(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String driverClass() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    public String jdbcUrl(String host, String port, String database) {
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useUnicode=true&characterEncoding=UTF-8"
                + "&useSSL=false&serverTimezone=UTC"
                + "&allowPublicKeyRetrieval=true";
    }

    @Override
    public String defaultPort() {
        return "3306";
    }

    @Override
    public String quote(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    @Override
    public String createTable(String table, Columns columns) {
        return "CREATE TABLE IF NOT EXISTS " + table + " ("
                + columns.key() + " VARCHAR(64) NOT NULL PRIMARY KEY,"
                + columns.data() + " LONGTEXT NOT NULL,"
                + columns.format() + " " + formatColumnDefinition() + ","
                + columns.updated() + " " + timestampColumnDefinition() + ","
                + columns.revision() + " " + revisionColumnDefinition() + ","
                + columns.name() + " " + nameColumnDefinition()
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
    }

    @Override
    public String addColumn(String table, String column, String definition) {
        return "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
    }

    @Override
    public boolean addColumnIsIdempotent() {
        // MySQL has no ADD COLUMN IF NOT EXISTS, so the caller has to check
        // metadata before issuing the statement.
        return false;
    }

    @Override
    public String formatColumnDefinition() {
        return "VARCHAR(32) NOT NULL DEFAULT 'legacy'";
    }

    @Override
    public String timestampColumnDefinition() {
        return "BIGINT NOT NULL DEFAULT 0";
    }

    @Override
    public String revisionColumnDefinition() {
        return "BIGINT NOT NULL DEFAULT 0";
    }

    @Override
    public String nameColumnDefinition() {
        return "VARCHAR(64) NULL";
    }

    @Override
    public boolean isDuplicateKey(SQLException ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            if (cause instanceof SQLException) {
                SQLException sql = (SQLException) cause;
                if (sql.getErrorCode() == ER_DUP_ENTRY || "23000".equals(sql.getSQLState())) {
                    return true;
                }
            }
        }
        return false;
    }
}
