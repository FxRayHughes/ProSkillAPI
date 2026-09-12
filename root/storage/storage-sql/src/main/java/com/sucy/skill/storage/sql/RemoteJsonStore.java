/**
 * SkillAPI
 * com.sucy.skill.storage.sql.RemoteJsonStore
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.storage.sql;

import com.sucy.skill.storage.JsonDocument;
import com.sucy.skill.storage.JsonDocumentStore;
import com.sucy.skill.storage.StaleDocumentException;
import com.sucy.skill.storage.sql.dialect.SqlDialect;
import com.sucy.skill.storage.sql.dialect.SqlDialects;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Remote SQL implementation of {@link JsonDocumentStore}.
 *
 * <p>The repository is deliberately independent from Bukkit and Gson. It owns
 * connection pooling, schema compatibility, transactions, and parameterized
 * statements; the plugin layer owns player business data and serialization.</p>
 *
 * <p>How the ACID guarantees in {@link JsonDocumentStore} are met here:</p>
 * <ul>
 *     <li><b>Atomicity and durability</b> come from running each batch inside
 *     one explicit transaction that is committed once, or rolled back
 *     entirely.</li>
 *     <li><b>Isolation and consistency</b> come from the revision column. Each
 *     document is written by a conditional {@code UPDATE ... WHERE key = ? AND
 *     revision = ?}, which the server evaluates atomically. Two servers writing
 *     the same player therefore cannot both succeed: the second sees zero
 *     affected rows and the batch is rejected rather than overwriting the
 *     first. A document believed to be new is inserted, and losing that race
 *     surfaces as a duplicate key, which is treated as the same conflict.</li>
 *     <li>The transaction runs at {@code READ COMMITTED}. A stricter level is
 *     unnecessary because correctness rests on the conditional update, not on
 *     the surrounding reads, and a looser one is never used.</li>
 * </ul>
 */
public final class RemoteJsonStore implements JsonDocumentStore {
    private static final String FORMAT = "gson-v1";

    /** Historical key column names, most likely first. */
    private static final String[] KEY_CANDIDATES = {
            "Name", "name", "id", "uuid", "player_id", "playerid", "player"
    };

    /** Historical payload column names, most likely first. */
    private static final String[] DATA_CANDIDATES = {
            "data", "Data", "value", "json", "payload"
    };

    private final SqlDialect dialect;
    private final String rawTableName;
    private final HikariDataSource dataSource;

    /** Resolved during initialization; read-only afterwards. */
    private volatile Schema schema;
    private volatile boolean closed;

    /**
     * @param type database type, see {@link SqlDialects#supported()}
     * @param host remote database host
     * @param port remote database port, blank for the dialect default
     * @param database database name
     * @param username database username
     * @param password database password
     * @param pluginName historical table prefix used by MCCore's SQL helper
     */
    public RemoteJsonStore(
            String type,
            String host,
            String port,
            String database,
            String username,
            String password,
            String pluginName
    ) {
        dialect = SqlDialects.of(type);
        rawTableName = sanitizeIdentifier(pluginName) + "_players";

        String resolvedPort = port == null || port.trim().isEmpty() ? dialect.defaultPort() : port.trim();
        loadDriver();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dialect.jdbcUrl(host, resolvedPort, database));
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("SkillAPI-SQL");
        config.setMaximumPoolSize(4);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(5000);
        config.setValidationTimeout(3000);
        config.setInitializationFailTimeout(-1);
        config.setAutoCommit(true);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        if ("mysql".equals(dialect.id()) || "mariadb".equals(dialect.id())) {
            // Server-side statement caching is a MySQL-family property and is
            // rejected as unknown by the PostgreSQL driver.
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
        }
        dataSource = new HikariDataSource(config);
    }

    /**
     * Discovers or creates the schema.
     *
     * <p>An existing table is adopted as-is: its real key and payload column
     * names are read from JDBC metadata, so an installation that stored player
     * UUIDs in an {@code id} column, or data in a {@code Data} column, keeps
     * working and keeps its rows visible. Only the columns the JSON format and
     * the revision check added are created when missing.</p>
     */
    @Override
    public void initialize() throws SQLException {
        synchronized (this) {
            ensureOpen();
            if (schema != null) {
                return;
            }
            try (Connection connection = dataSource.getConnection()) {
                Schema discovered = discover(connection);
                if (discovered == null) {
                    createTable(connection);
                    discovered = discover(connection);
                    if (discovered == null) {
                        throw new SQLException(
                                "Created table " + rawTableName + " but could not read it back");
                    }
                } else {
                    discovered = addMissingColumns(connection, discovered);
                }
                schema = discovered;
            }
        }
    }

    @Override
    public JsonDocument load(String playerId) throws SQLException {
        Schema active = active();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + active.columns.data()
                             + ", " + active.columns.format()
                             + ", " + active.columns.revision()
                             + ", " + active.nameSelect
                             + " FROM " + active.table
                             + " WHERE " + active.columns.key() + " = ?")) {
            statement.setString(1, playerId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return new JsonDocument(
                        playerId,
                        result.getString(4),
                        result.getString(1),
                        result.getString(2),
                        result.getLong(3));
            }
        }
    }

    @Override
    public Map<String, JsonDocument> loadAll() throws SQLException {
        Schema active = active();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + active.columns.key()
                             + ", " + active.columns.data()
                             + ", " + active.columns.format()
                             + ", " + active.columns.revision()
                             + ", " + active.nameSelect
                             + " FROM " + active.table);
             ResultSet result = statement.executeQuery()) {
            Map<String, JsonDocument> values = new LinkedHashMap<String, JsonDocument>();
            while (result.next()) {
                String id = result.getString(1);
                values.put(id, new JsonDocument(
                        id,
                        result.getString(5),
                        result.getString(2),
                        result.getString(3),
                        result.getLong(4)));
            }
            return values;
        }
    }

    @Override
    public long save(JsonDocument document) throws SQLException {
        Map<String, JsonDocument> values = new LinkedHashMap<String, JsonDocument>();
        values.put(document.getPlayerId(), document);
        Long revision = saveAll(values).get(document.getPlayerId());
        return revision == null ? document.getRevision() : revision;
    }

    @Override
    public Map<String, Long> saveAll(Map<String, JsonDocument> documents) throws SQLException {
        Map<String, Long> revisions = new LinkedHashMap<String, Long>();
        if (documents.isEmpty()) {
            return revisions;
        }
        Schema active = active();
        Set<String> conflicts = new LinkedHashSet<String>();

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement update = connection.prepareStatement(active.updateSql);
                 PreparedStatement force = connection.prepareStatement(active.forceUpdateSql);
                 PreparedStatement insert = connection.prepareStatement(active.insertSql)) {
                long now = System.currentTimeMillis();
                for (Map.Entry<String, JsonDocument> entry : documents.entrySet()) {
                    String id = entry.getKey();
                    JsonDocument document = entry.getValue();
                    if (id == null || document == null || document.getJson() == null) {
                        continue;
                    }
                    Long revision = write(connection, active, update, force, insert, id, document, now);
                    if (revision == null) {
                        conflicts.add(id);
                    } else {
                        revisions.put(id, revision);
                    }
                }

                if (!conflicts.isEmpty()) {
                    // Consistency over partial progress: one stale document
                    // invalidates the batch, and rolling back leaves the store
                    // exactly as the caller found it.
                    connection.rollback();
                    throw new StaleDocumentException(conflicts);
                }
                connection.commit();
            } catch (SQLException ex) {
                safeRollback(connection);
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        return revisions;
    }

    /**
     * Writes one document inside the caller's transaction.
     *
     * @return the document's new revision, or null when it lost its check
     */
    private Long write(
            Connection connection,
            Schema active,
            PreparedStatement update,
            PreparedStatement force,
            PreparedStatement insert,
            String id,
            JsonDocument document,
            long now) throws SQLException {
        long expected = document.getRevision();
        String format = document.getFormat() == null ? FORMAT : document.getFormat();

        if (expected != JsonDocument.NEW) {
            PreparedStatement statement = expected == JsonDocument.ANY ? force : update;
            int index = 1;
            statement.setString(index++, document.getJson());
            statement.setString(index++, format);
            statement.setLong(index++, now);
            if (active.writesName) {
                statement.setString(index++, document.getPlayerName());
            }
            statement.setString(index++, id);
            if (expected != JsonDocument.ANY) {
                statement.setLong(index, expected);
            }
            if (statement.executeUpdate() > 0) {
                // An unconditional write did not start from a known revision,
                // so the stored value is the only source of truth for it.
                return expected == JsonDocument.ANY
                        ? readRevision(connection, active, id)
                        : Long.valueOf(expected + 1);
            }
            if (expected == JsonDocument.ANY) {
                // Nothing to update: the row was deleted or never existed, so
                // fall through and recreate it.
                return insertNew(connection, active, insert, id, document, format, now);
            }
            // Zero rows means the stored revision moved on, or the row is gone.
            // Both are cases where overwriting would destroy newer state.
            return null;
        }

        return insertNew(connection, active, insert, id, document, format, now);
    }

    /**
     * Inserts a document believed to be new.
     *
     * <p>A savepoint wraps the attempt because PostgreSQL aborts the whole
     * transaction on a failed statement; without it a single lost insert race
     * would take the rest of the batch down with it.</p>
     *
     * @return the new revision, or null when another writer created the row first
     */
    private Long insertNew(
            Connection connection,
            Schema active,
            PreparedStatement insert,
            String id,
            JsonDocument document,
            String format,
            long now) throws SQLException {
        Savepoint savepoint = connection.setSavepoint("skillapi_insert");
        try {
            int index = 1;
            insert.setString(index++, id);
            insert.setString(index++, document.getJson());
            insert.setString(index++, format);
            insert.setLong(index++, now);
            if (active.writesName) {
                insert.setString(index, document.getPlayerName());
            }
            insert.executeUpdate();
            connection.releaseSavepoint(savepoint);
            return 1L;
        } catch (SQLException ex) {
            connection.rollback(savepoint);
            if (dialect.isDuplicateKey(ex)) {
                return null;
            }
            throw ex;
        }
    }

    private Long readRevision(Connection connection, Schema active, String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT " + active.columns.revision() + " FROM " + active.table
                        + " WHERE " + active.columns.key() + " = ?")) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Long.valueOf(result.getLong(1)) : Long.valueOf(1L);
            }
        }
    }

    /**
     * @return the JDBC table name without SQL quoting
     */
    public String getTableName() {
        Schema active = schema;
        return active == null ? rawTableName : active.rawTable;
    }

    /**
     * @return identifier of the configured SQL server type
     */
    public String getDialectId() {
        return dialect.id();
    }

    @Override
    public void close() {
        synchronized (this) {
            // Closing is permanent. A late async save must fail loudly rather
            // than re-open a pool during or after plugin shutdown.
            closed = true;
            schema = null;
        }
        dataSource.close();
    }

    private Schema active() throws SQLException {
        Schema active = schema;
        if (active != null) {
            ensureOpen();
            return active;
        }
        initialize();
        active = schema;
        if (active == null) {
            throw new SQLException("Remote SQL schema is unavailable");
        }
        return active;
    }

    private void ensureOpen() throws SQLException {
        if (closed) {
            throw new SQLException("The remote SQL store has been closed");
        }
    }

    private void loadDriver() {
        try {
            Class.forName(dialect.driverClass());
        } catch (ClassNotFoundException ignored) {
            // HikariCP can still resolve the driver through the JDBC service
            // loader; the eager attempt only improves the failure message.
        }
    }

    private static void safeRollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // The connection is already broken; the original error is reported.
        }
    }

    private void createTable(Connection connection) throws SQLException {
        SqlDialect.Columns columns = new SqlDialect.Columns(
                dialect.quote("Name"),
                dialect.quote("data"),
                dialect.quote("format"),
                dialect.quote("updated_at"),
                dialect.quote("revision"),
                dialect.quote("player_name"));
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(dialect.createTable(dialect.quote(rawTableName), columns));
        }
    }

    /**
     * @return the existing layout, or null when the table has to be created
     */
    private Schema discover(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String actualTable = findTable(metaData, connection);
        if (actualTable == null) {
            return null;
        }

        List<String> columns = new ArrayList<String>();
        try (ResultSet result = metaData.getColumns(
                catalog(connection), schemaName(connection), actualTable, null)) {
            while (result.next()) {
                columns.add(result.getString("COLUMN_NAME"));
            }
        }
        if (columns.isEmpty()) {
            return null;
        }

        String key = findPrimaryKey(metaData, connection, actualTable, columns);
        if (key == null) {
            key = match(columns, KEY_CANDIDATES);
        }
        String data = match(columns, DATA_CANDIDATES);
        if (key == null || data == null) {
            throw new SQLException("Table \"" + actualTable + "\" exists but has no recognizable"
                    + " player key and data columns. Found: " + columns
                    + ". Rename the table or point SkillAPI at an empty database.");
        }

        return new Schema(
                actualTable,
                key,
                data,
                match(columns, new String[]{"format"}),
                match(columns, new String[]{"updated_at", "updatedAt"}),
                match(columns, new String[]{"revision"}),
                match(columns, new String[]{"player_name", "playerName", "display_name"}));
    }

    private Schema addMissingColumns(Connection connection, Schema discovered) throws SQLException {
        String format = discovered.rawFormat;
        String updated = discovered.rawUpdated;
        String revision = discovered.rawRevision;
        String name = discovered.rawName;

        if (format == null) {
            addColumn(connection, discovered.rawTable, "format", dialect.formatColumnDefinition());
            format = "format";
        }
        if (updated == null) {
            addColumn(connection, discovered.rawTable, "updated_at", dialect.timestampColumnDefinition());
            updated = "updated_at";
        }
        if (revision == null) {
            // Existing rows default to revision 0, which is also the revision a
            // freshly loaded legacy document reports, so the first save after
            // an upgrade still passes its check.
            addColumn(connection, discovered.rawTable, "revision", dialect.revisionColumnDefinition());
            revision = "revision";
        }
        if (name == null && !discovered.rawKey.equalsIgnoreCase("player_name")) {
            addColumn(connection, discovered.rawTable, "player_name", dialect.nameColumnDefinition());
            name = "player_name";
        }
        return new Schema(
                discovered.rawTable, discovered.rawKey, discovered.rawData,
                format, updated, revision, name);
    }

    private void addColumn(Connection connection, String table, String column, String definition)
            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    dialect.addColumn(dialect.quote(table), dialect.quote(column), definition));
        } catch (SQLException ex) {
            if (dialect.addColumnIsIdempotent()) {
                throw ex;
            }
            // MySQL has no IF NOT EXISTS here. A concurrent server that added
            // the same column first is a benign race, not a failure.
            if (!columnExists(connection, table, column)) {
                throw ex;
            }
        }
    }

    private boolean columnExists(Connection connection, String table, String column)
            throws SQLException {
        try (ResultSet result = connection.getMetaData().getColumns(
                catalog(connection), schemaName(connection), table, null)) {
            while (result.next()) {
                if (column.equalsIgnoreCase(result.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Finds the table regardless of the case the server folded it to. MySQL on
     * Linux keeps the created case, PostgreSQL lower-cases unquoted names, and
     * an upgraded installation may have either.
     */
    private String findTable(DatabaseMetaData metaData, Connection connection) throws SQLException {
        String[] patterns = {
                rawTableName,
                rawTableName.toLowerCase(Locale.ROOT),
                rawTableName.toUpperCase(Locale.ROOT)
        };
        for (String pattern : patterns) {
            try (ResultSet result = metaData.getTables(
                    catalog(connection), schemaName(connection), pattern, new String[]{"TABLE"})) {
                if (result.next()) {
                    return result.getString("TABLE_NAME");
                }
            }
        }
        return null;
    }

    private String findPrimaryKey(
            DatabaseMetaData metaData,
            Connection connection,
            String table,
            List<String> columns) throws SQLException {
        List<String> keys = new ArrayList<String>();
        try (ResultSet result = metaData.getPrimaryKeys(
                catalog(connection), schemaName(connection), table)) {
            while (result.next()) {
                keys.add(result.getString("COLUMN_NAME"));
            }
        }
        // A composite primary key is not a player key; fall back to name matching.
        if (keys.size() != 1) {
            return null;
        }
        String key = keys.get(0);
        return columns.contains(key) ? key : null;
    }

    private static String catalog(Connection connection) {
        try {
            return connection.getCatalog();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String schemaName(Connection connection) {
        try {
            // Connection#getSchema is JDBC 4.1; some older drivers throw.
            return connection.getSchema();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String match(List<String> columns, String[] candidates) {
        for (String candidate : candidates) {
            for (String column : columns) {
                if (column.equalsIgnoreCase(candidate)) {
                    return column;
                }
            }
        }
        return null;
    }

    private static String sanitizeIdentifier(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "SkillAPI";
        }
        return value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    /**
     * Resolved table layout plus the statements built from it.
     *
     * <p>The raw names are kept for metadata lookups and the quoted names for
     * statements, so neither has to be recomputed per query.</p>
     */
    private final class Schema {
        private final String rawTable;
        private final String rawKey;
        private final String rawData;
        private final String rawFormat;
        private final String rawUpdated;
        private final String rawRevision;
        private final String rawName;

        private final String table;
        private final SqlDialect.Columns columns;
        private final boolean writesName;

        /** Conditional update; the revision guard is the last parameter. */
        private final String updateSql;
        /** Unconditional update used only by {@link JsonDocument#ANY}. */
        private final String forceUpdateSql;
        private final String insertSql;
        /** Column to read the display name from in select statements. */
        private final String nameSelect;

        private Schema(
                String table,
                String key,
                String data,
                String format,
                String updated,
                String revision,
                String name) {
            this.rawTable = table;
            this.rawKey = key;
            this.rawData = data;
            this.rawFormat = format;
            this.rawUpdated = updated;
            this.rawRevision = revision;
            this.rawName = name;

            this.table = dialect.quote(table);
            this.columns = new SqlDialect.Columns(
                    dialect.quote(key),
                    dialect.quote(data),
                    dialect.quote(format == null ? "format" : format),
                    dialect.quote(updated == null ? "updated_at" : updated),
                    dialect.quote(revision == null ? "revision" : revision),
                    name == null ? null : dialect.quote(name));

            // A table whose primary key already IS the display name has no
            // separate name column; writing one would overwrite the key.
            this.writesName = columns.name() != null;
            String nameSet = writesName ? " " + columns.name() + " = ?," : "";
            String nameColumn = writesName ? ", " + columns.name() : "";
            String namePlaceholder = writesName ? ", ?" : "";

            String set = columns.data() + " = ?,"
                    + " " + columns.format() + " = ?,"
                    + " " + columns.updated() + " = ?,"
                    + nameSet
                    + " " + columns.revision() + " = " + columns.revision() + " + 1";

            this.updateSql = "UPDATE " + this.table + " SET " + set
                    + " WHERE " + columns.key() + " = ? AND " + columns.revision() + " = ?";
            this.forceUpdateSql = "UPDATE " + this.table + " SET " + set
                    + " WHERE " + columns.key() + " = ?";
            this.insertSql = "INSERT INTO " + this.table
                    + " (" + columns.key()
                    + ", " + columns.data()
                    + ", " + columns.format()
                    + ", " + columns.updated()
                    + nameColumn
                    + ", " + columns.revision() + ")"
                    + " VALUES (?, ?, ?, ?" + namePlaceholder + ", 1)";
            // Without a dedicated name column the key doubles as the name.
            this.nameSelect = writesName ? columns.name() : columns.key();
        }
    }
}
