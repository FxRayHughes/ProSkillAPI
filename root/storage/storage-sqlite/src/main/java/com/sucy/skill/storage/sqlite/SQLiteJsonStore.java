/**
 * SkillAPI
 * com.sucy.skill.storage.sqlite.SQLiteJsonStore
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.storage.sqlite;

import com.sucy.skill.storage.JsonDocument;
import com.sucy.skill.storage.JsonDocumentStore;
import com.sucy.skill.storage.StaleDocumentException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Local SQLite implementation of {@link JsonDocumentStore}.
 *
 * <p>The storage module deliberately knows nothing about Bukkit or player
 * objects. This keeps the database contract reusable while the root plugin
 * retains ownership of business fields and Gson-based document conversion.</p>
 *
 * <p>How the ACID guarantees are met here:</p>
 * <ul>
 *     <li><b>Atomicity and durability</b> come from an explicit transaction per
 *     batch, committed once, plus WAL journaling so a crash leaves either the
 *     old or the new state and never a half-written file.</li>
 *     <li><b>Consistency and isolation</b> come from the same revision guard
 *     the remote store uses. Even a single server writes concurrently — the
 *     autosave task, a quit handler, and a manual save can overlap — so a
 *     conditional {@code UPDATE ... WHERE revision = ?} is what stops a stale
 *     in-memory copy from erasing a newer one. {@code BEGIN IMMEDIATE} takes
 *     the write lock up front so a batch cannot fail halfway on a busy
 *     file.</li>
 * </ul>
 */
public final class SQLiteJsonStore implements JsonDocumentStore {
    private static final String TABLE = "player_data";
    private static final String FORMAT = "gson-v1";

    private final File databaseFile;

    private volatile boolean initialized;

    /**
     * Set once by {@link #close()} and never cleared. Shutdown has to be a
     * terminal state: an async save still queued when the plugin disables would
     * otherwise walk through initialization and re-create the database after
     * the server considered it closed.
     */
    private volatile boolean closed;

    /**
     * @param dataFolder plugin data folder
     * @param fileName database file name
     */
    public SQLiteJsonStore(File dataFolder, String fileName) {
        this.databaseFile = new File(dataFolder, fileName);
    }

    /**
     * Creates the schema and applies the database-level settings.
     *
     * <p>{@code journal_mode} is durable state stored in the file header, not a
     * per-connection option, so WAL is set exactly once here. Repeating it on
     * every connection would force a journal switch on each query, and a switch
     * needs an exclusive lock that a concurrent reader can block.</p>
     */
    @Override
    public void initialize() throws SQLException {
        synchronized (this) {
            ensureOpen();
            if (initialized) {
                return;
            }
            File parent = databaseFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
                throw new SQLException("Unable to create SQLite directory: " + parent);
            }
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ex) {
                throw new SQLException("SQLite JDBC driver is missing", ex);
            }
            try (Connection connection = openConnection()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("PRAGMA journal_mode = WAL");
                    statement.execute("PRAGMA wal_autocheckpoint = 1000");
                    statement.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                                    + "player_id TEXT PRIMARY KEY,"
                                    + "player_name TEXT,"
                                    + "format TEXT NOT NULL,"
                                    + "data TEXT NOT NULL,"
                                    + "updated_at INTEGER NOT NULL,"
                                    + "revision INTEGER NOT NULL DEFAULT 0"
                                    + ")");
                    statement.executeUpdate(
                            "CREATE INDEX IF NOT EXISTS idx_" + TABLE + "_updated "
                                    + "ON " + TABLE + "(updated_at)");
                }
                addRevisionColumn(connection);
            }
            initialized = true;
        }
    }

    @Override
    public JsonDocument load(String playerId) throws SQLException {
        ensureInitialized();
        try (Connection connection = openConnection()) {
            configure(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT data, format, revision, player_name FROM " + TABLE
                            + " WHERE player_id = ?")) {
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
    }

    @Override
    public Map<String, JsonDocument> loadAll() throws SQLException {
        ensureInitialized();
        try (Connection connection = openConnection()) {
            configure(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT player_id, data, format, revision, player_name FROM " + TABLE);
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
    }

    @Override
    public long save(JsonDocument document) throws SQLException {
        Map<String, JsonDocument> values = new HashMap<String, JsonDocument>();
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
        ensureInitialized();
        Set<String> conflicts = new LinkedHashSet<String>();

        try (Connection connection = openConnection()) {
            configure(connection);
            // The driver issues its own BEGIN here, and the connection URL made
            // that a BEGIN IMMEDIATE: the write lock is taken up front, so a
            // busy database fails at the start rather than halfway through a
            // batch with rows already applied. Issuing BEGIN by hand as well
            // would nest transactions and be rejected.
            connection.setAutoCommit(false);
            try (PreparedStatement update = connection.prepareStatement(
                         "UPDATE " + TABLE
                                 + " SET data = ?, format = ?, updated_at = ?, player_name = ?,"
                                 + " revision = revision + 1"
                                 + " WHERE player_id = ? AND revision = ?");
                 PreparedStatement force = connection.prepareStatement(
                         "UPDATE " + TABLE
                                 + " SET data = ?, format = ?, updated_at = ?, player_name = ?,"
                                 + " revision = revision + 1"
                                 + " WHERE player_id = ?");
                 PreparedStatement insert = connection.prepareStatement(
                         "INSERT INTO " + TABLE
                                 + " (player_id, data, format, updated_at, player_name, revision)"
                                 + " VALUES (?, ?, ?, ?, ?, 1)")) {
                long now = System.currentTimeMillis();
                for (Map.Entry<String, JsonDocument> entry : documents.entrySet()) {
                    String id = entry.getKey();
                    JsonDocument document = entry.getValue();
                    if (id == null || document == null || document.getJson() == null) {
                        continue;
                    }
                    Long revision = write(connection, update, force, insert, id, document, now);
                    if (revision == null) {
                        conflicts.add(id);
                    } else {
                        revisions.put(id, revision);
                    }
                }

                if (!conflicts.isEmpty()) {
                    // One stale document invalidates the batch; rolling back
                    // leaves the file exactly as the caller found it.
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
     * @return the document's new revision, or null when it lost its check
     */
    private Long write(
            Connection connection,
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
            statement.setString(1, document.getJson());
            statement.setString(2, format);
            statement.setLong(3, now);
            statement.setString(4, document.getPlayerName());
            statement.setString(5, id);
            if (expected != JsonDocument.ANY) {
                statement.setLong(6, expected);
            }
            if (statement.executeUpdate() > 0) {
                // An unconditional write did not start from a known revision,
                // so the stored value is the only source of truth for it.
                return expected == JsonDocument.ANY ? readRevision(connection, id) : expected + 1;
            }
            if (expected != JsonDocument.ANY) {
                // The stored revision moved on, or the row was deleted. Either
                // way, writing would destroy newer state.
                return null;
            }
        }

        // The whole batch holds the write lock, so no other writer can create
        // this row between the failed update and this insert.
        insert.setString(1, id);
        insert.setString(2, document.getJson());
        insert.setString(3, format);
        insert.setLong(4, now);
        insert.setString(5, document.getPlayerName());
        insert.executeUpdate();
        return 1L;
    }

    private static Long readRevision(Connection connection, String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT revision FROM " + TABLE + " WHERE player_id = ?")) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : 1L;
            }
        }
    }

    /**
     * @return the database file used by this store
     */
    public File getDatabaseFile() {
        return databaseFile;
    }

    @Override
    public void close() {
        // Connections are scoped to operations, so there is no pool to drain;
        // what matters is that the store can never re-initialize afterwards.
        closed = true;
        initialized = false;
    }

    /*
     * The operation-scoped connection is intentional. Keeping one JDBC
     * connection in a plugin singleton would make async save tasks share
     * mutable JDBC state and would defeat SQLite's reader/writer behavior.
     */
    private Connection openConnection() throws SQLException {
        // transaction_mode=IMMEDIATE makes the driver's implicit BEGIN take the
        // write lock immediately. SQLite's default DEFERRED acquires it on the
        // first write instead, which can fail mid-batch with earlier rows of
        // the same transaction already applied.
        return DriverManager.getConnection("jdbc:sqlite:"
                + databaseFile.getAbsolutePath()
                + "?transaction_mode=IMMEDIATE");
    }

    /**
     * Applies the per-connection settings only. Database-level state such as
     * {@code journal_mode} belongs in {@link #initialize()} and must not be
     * re-issued per query.
     */
    private void configure(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout = 5000");
            statement.execute("PRAGMA synchronous = NORMAL");
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA temp_store = MEMORY");
        }
    }

    /**
     * Adds the revision column to a database created before optimistic
     * concurrency existed. Existing rows default to revision 0, which is also
     * what a freshly loaded document reports, so the first save after an
     * upgrade still passes its check.
     */
    private void addRevisionColumn(Connection connection) throws SQLException {
        try (ResultSet columns = connection.getMetaData()
                .getColumns(null, null, TABLE, "revision")) {
            if (columns.next()) {
                return;
            }
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "ALTER TABLE " + TABLE + " ADD COLUMN revision INTEGER NOT NULL DEFAULT 0");
        }
    }

    private static void safeRollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // The connection is already broken; the original error is reported.
        }
    }

    private void ensureInitialized() throws SQLException {
        ensureOpen();
        if (!initialized) {
            initialize();
        }
    }

    private void ensureOpen() throws SQLException {
        if (closed) {
            throw new SQLException("The local SQLite store has been closed");
        }
    }
}
