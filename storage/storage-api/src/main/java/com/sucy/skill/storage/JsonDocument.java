/**
 * SkillAPI
 * com.sucy.skill.storage.JsonDocument
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.storage;

/**
 * One stored player document.
 *
 * <p>The {@code revision} field is what makes concurrent saves safe. Every
 * successful write increments it, and every write states which revision it
 * believes it is replacing. A save whose expected revision no longer matches
 * the stored one is rejected instead of silently overwriting work that another
 * server, or another thread, committed in between.</p>
 */
public final class JsonDocument {
    /** Revision of a document that does not exist in the store yet. */
    public static final long NEW = 0L;

    /**
     * Revision meaning "write regardless of what is stored". Reserved for
     * administrative imports; normal saves must carry a real revision.
     */
    public static final long ANY = -1L;

    private final String playerId;
    private final String playerName;
    private final String json;
    private final String format;
    private final long revision;

    /**
     * @param playerId stable player identifier and primary key
     * @param playerName display name at the time of the write, may be null
     * @param json serialized payload
     * @param format payload format marker
     * @param revision revision this document was loaded at, or {@link #NEW}
     */
    public JsonDocument(String playerId, String playerName, String json, String format, long revision) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.json = json;
        this.format = format;
        this.revision = revision;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getJson() {
        return json;
    }

    public String getFormat() {
        return format;
    }

    public long getRevision() {
        return revision;
    }

    /**
     * @param json new payload
     * @param format new format marker
     * @return copy carrying a new payload at the same expected revision
     */
    public JsonDocument withPayload(String json, String format) {
        return new JsonDocument(playerId, playerName, json, format, revision);
    }

    /**
     * @param playerName display name to record
     * @return copy carrying an updated display name
     */
    public JsonDocument withPlayerName(String playerName) {
        return new JsonDocument(playerId, playerName, json, format, revision);
    }

    @Override
    public String toString() {
        return "JsonDocument{" + playerId + " @" + revision + "}";
    }
}
