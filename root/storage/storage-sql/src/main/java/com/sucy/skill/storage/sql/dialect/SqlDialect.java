/**
 * SkillAPI
 * com.sucy.skill.storage.sql.dialect.SqlDialect
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.storage.sql.dialect;

import java.sql.SQLException;

/**
 * Everything the remote store needs that differs between SQL servers.
 *
 * <p>The repository itself only knows "one revisioned JSON document per player
 * key". What is not portable is identifier quoting, column types, the
 * duplicate-key error signature, and whether {@code ADD COLUMN IF NOT EXISTS}
 * exists, so exactly those live here. Adding another server means adding one
 * implementation and one entry in {@link SqlDialects}, not touching the
 * repository.</p>
 */
public interface SqlDialect {
    /**
     * @return identifier used in configuration and log messages
     */
    String id();

    /**
     * @return JDBC driver class, loaded eagerly so a missing driver reports a
     *         clear error instead of a pool timeout
     */
    String driverClass();

    /**
     * @param host database host
     * @param port database port
     * @param database database name
     * @return complete JDBC URL including the connection parameters this
     *         server needs for UTF-8 and time-zone-safe operation
     */
    String jdbcUrl(String host, String port, String database);

    /**
     * @return port used when the configuration leaves it blank
     */
    String defaultPort();

    /**
     * @param identifier raw table or column name
     * @return identifier quoted for this server
     */
    String quote(String identifier);

    /**
     * @param table quoted table name
     * @param columns quoted column names in schema order
     * @return statement creating the table when it does not exist
     */
    String createTable(String table, Columns columns);

    /**
     * @param table quoted table name
     * @param column quoted column name
     * @param definition column type and constraints
     * @return statement adding one column
     */
    String addColumn(String table, String column, String definition);

    /**
     * @return true when {@link #addColumn} is a no-op on an existing column and
     *         therefore safe to run without checking metadata first
     */
    boolean addColumnIsIdempotent();

    /**
     * @return column definition for the format marker, including its default
     */
    String formatColumnDefinition();

    /**
     * @return column definition for the update timestamp, including its default
     */
    String timestampColumnDefinition();

    /**
     * @return column definition for the optimistic-concurrency revision
     */
    String revisionColumnDefinition();

    /**
     * @return column definition for the recorded display name
     */
    String nameColumnDefinition();

    /**
     * Decides whether a failed insert lost a race with a concurrent insert of
     * the same key, as opposed to failing for an unrelated reason. The two must
     * not be confused: the first is a revision conflict the caller can report,
     * the second is a real error that has to propagate.
     *
     * @param ex exception thrown by the insert
     * @return true when the cause is a duplicate primary key
     */
    boolean isDuplicateKey(SQLException ex);

    /**
     * Column names resolved against the live schema, already quoted.
     */
    final class Columns {
        private final String key;
        private final String data;
        private final String format;
        private final String updated;
        private final String revision;
        private final String name;

        public Columns(
                String key,
                String data,
                String format,
                String updated,
                String revision,
                String name) {
            this.key = key;
            this.data = data;
            this.format = format;
            this.updated = updated;
            this.revision = revision;
            this.name = name;
        }

        public String key() {
            return key;
        }

        public String data() {
            return data;
        }

        public String format() {
            return format;
        }

        public String updated() {
            return updated;
        }

        public String revision() {
            return revision;
        }

        public String name() {
            return name;
        }
    }
}
