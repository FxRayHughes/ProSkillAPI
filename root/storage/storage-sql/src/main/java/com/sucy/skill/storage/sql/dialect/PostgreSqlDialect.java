/**
 * SkillAPI
 * com.sucy.skill.storage.sql.dialect.PostgreSqlDialect
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.storage.sql.dialect;

import java.sql.SQLException;

/**
 * PostgreSQL.
 *
 * <p>Four things differ from MySQL and each is why a shared dialect would have
 * been wrong: identifiers are quoted with double quotes and become
 * case-sensitive once quoted, there is no {@code LONGTEXT} type, duplicate keys
 * report SQLSTATE {@code 23505} rather than a MySQL error code, and
 * {@code ADD COLUMN IF NOT EXISTS} is supported so the metadata pre-check can
 * be skipped.</p>
 */
public class PostgreSqlDialect implements SqlDialect {
    /** SQLSTATE 23505: unique_violation. */
    private static final String UNIQUE_VIOLATION = "23505";

    @Override
    public String id() {
        return "postgresql";
    }

    @Override
    public String driverClass() {
        return "org.postgresql.Driver";
    }

    @Override
    public String jdbcUrl(String host, String port, String database) {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }

    @Override
    public String defaultPort() {
        return "5432";
    }

    @Override
    public String quote(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    @Override
    public String createTable(String table, Columns columns) {
        return "CREATE TABLE IF NOT EXISTS " + table + " ("
                + columns.key() + " VARCHAR(64) NOT NULL PRIMARY KEY,"
                + columns.data() + " TEXT NOT NULL,"
                + columns.format() + " " + formatColumnDefinition() + ","
                + columns.updated() + " " + timestampColumnDefinition() + ","
                + columns.revision() + " " + revisionColumnDefinition() + ","
                + columns.name() + " " + nameColumnDefinition()
                + ")";
    }

    @Override
    public String addColumn(String table, String column, String definition) {
        return "ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS " + column + " " + definition;
    }

    @Override
    public boolean addColumnIsIdempotent() {
        return true;
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
            if (cause instanceof SQLException
                    && UNIQUE_VIOLATION.equals(((SQLException) cause).getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
