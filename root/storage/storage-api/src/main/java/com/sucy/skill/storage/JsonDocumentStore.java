/**
 * SkillAPI
 * com.sucy.skill.storage.JsonDocumentStore
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.storage;

import java.sql.SQLException;
import java.util.Map;

/**
 * Transactional store for opaque player JSON documents.
 *
 * <p>The contract every implementation owes its caller:</p>
 * <ul>
 *     <li><b>Atomicity.</b> {@link #saveAll} either commits every document or
 *     none of them. A partial batch is never observable, including after a
 *     crash mid-write.</li>
 *     <li><b>Consistency.</b> A write only replaces the revision it claims to
 *     replace. When the stored revision has moved on, the batch is rolled back
 *     and {@link StaleDocumentException} names the players involved, so a stale
 *     in-memory copy can never silently destroy a newer save.</li>
 *     <li><b>Isolation.</b> Concurrent writers do not interleave. The check and
 *     the write happen as one indivisible operation, so two servers saving the
 *     same player cannot both conclude they are the first.</li>
 *     <li><b>Durability.</b> Once {@code saveAll} returns, the data has been
 *     committed, not just buffered.</li>
 * </ul>
 *
 * <p>Implementations are safe for concurrent use, and {@link #close()} is
 * terminal: after it returns, every further operation fails rather than
 * silently re-opening the backend during plugin shutdown.</p>
 */
public interface JsonDocumentStore extends AutoCloseable {
    /**
     * Prepares the backend and its schema. Safe to call more than once.
     *
     * @throws SQLException when the backend cannot be prepared
     */
    void initialize() throws SQLException;

    /**
     * @param playerId stable player identifier
     * @return the document, including the revision needed to save it back, or
     *         null when the player has no row
     * @throws SQLException when the backend cannot be read
     */
    JsonDocument load(String playerId) throws SQLException;

    /**
     * @return every stored document keyed by player identifier
     * @throws SQLException when the backend cannot be read
     */
    Map<String, JsonDocument> loadAll() throws SQLException;

    /**
     * Writes one document under the same guarantees as a batch.
     *
     * @param document document carrying the revision it expects to replace
     * @return the revision the document now has in the store
     * @throws StaleDocumentException when the stored revision moved on
     * @throws SQLException when the write failed for any other reason
     */
    long save(JsonDocument document) throws SQLException;

    /**
     * Writes a batch of documents as a single transaction.
     *
     * <p>The returned revisions are what makes a repeated save safe: the caller
     * records them and passes them back on the next write, so a long-lived
     * in-memory copy stays synchronized with the store without re-reading it.</p>
     *
     * @param documents documents keyed by player identifier
     * @return the new revision of every written document, keyed by identifier
     * @throws StaleDocumentException when any document lost its revision check;
     *         no document in the batch is written in that case
     * @throws SQLException when the write failed for any other reason
     */
    Map<String, Long> saveAll(Map<String, JsonDocument> documents) throws SQLException;

    /**
     * Releases resources permanently.
     */
    @Override
    void close();
}
