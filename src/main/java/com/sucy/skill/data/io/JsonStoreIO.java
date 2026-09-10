/**
 * SkillAPI
 * com.sucy.skill.data.io.JsonStoreIO
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.data.io;

import com.rit.sucy.config.CommentedConfig;
import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.version.VersionManager;
import com.rit.sucy.version.VersionPlayer;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerAccounts;
import com.sucy.skill.listener.MainListener;
import com.sucy.skill.log.Logger;
import com.sucy.skill.storage.JsonDocument;
import com.sucy.skill.storage.JsonDocumentStore;
import com.sucy.skill.storage.StaleDocumentException;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared player-data manager for every {@link JsonDocumentStore} backend.
 *
 * <p>Local SQLite and remote SQL differ only in which store they open and
 * which legacy payloads they have to accept; the account lifecycle around them
 * is identical, so it lives here rather than being duplicated per backend.</p>
 *
 * <p>This class owns the plugin half of the concurrency contract. The store
 * guarantees that a write cannot overwrite a newer revision; keeping the
 * revision each player was loaded at is what lets this layer state which
 * revision it is replacing. A rejected save is reported and the in-memory copy
 * is left untouched, because discarding the newer stored data is exactly what
 * the check exists to prevent.</p>
 */
abstract class JsonStoreIO extends IOManager {
    protected static final Charset UTF_8 = Charset.forName("UTF-8");
    protected static final String FORMAT = "gson-v1";

    /**
     * Revision each loaded player is expected to still be at.
     *
     * <p>Concurrent because saves run on async tasks while joins and quits run
     * on the main thread.</p>
     */
    private final Map<String, Long> revisions = new ConcurrentHashMap<String, Long>();

    private final JsonDocumentStore store;

    protected JsonStoreIO(SkillAPI api, JsonDocumentStore store) {
        super(api);
        this.store = store;
        try {
            store.initialize();
        } catch (Exception ex) {
            Logger.bug("Failed to initialize the player data store - " + ex.getMessage());
        }
    }

    /**
     * @return the backend store
     */
    protected JsonDocumentStore store() {
        return store;
    }

    /**
     * Converts a stored payload into the business tree.
     *
     * <p>Backends override this because they accept different historical
     * payloads: SQLite only ever wrote JSON, while a remote table may still
     * hold MCCore's YAML text.</p>
     *
     * @param payload stored payload
     * @return parsed tree, or null when the payload is unusable
     */
    protected abstract DataSection decodePayload(String payload);

    /**
     * @param payload stored payload
     * @param format stored format marker
     * @return true when the row is already in the current JSON format
     */
    protected boolean isCurrentFormat(String payload, String format) {
        return FORMAT.equalsIgnoreCase(format)
                && payload != null
                && payload.trim().startsWith("{");
    }

    @Override
    public HashMap<String, PlayerAccounts> loadAll() {
        HashMap<String, PlayerAccounts> result = new HashMap<String, PlayerAccounts>();
        for (Player player : VersionManager.getOnlinePlayers()) {
            result.put(new VersionPlayer(player).getIdString(), loadData(player));
        }
        return result;
    }

    @Override
    public PlayerAccounts loadData(OfflinePlayer player) {
        if (player == null) {
            return null;
        }

        String playerKey = new VersionPlayer(player).getIdString();
        try {
            JsonDocument document = store.load(playerKey);
            if (document == null && player.getName() != null && !playerKey.equals(player.getName())) {
                // Pre-UUID installations keyed rows by player name.
                document = store.load(player.getName());
            }
            if (document != null) {
                DataSection section = decodePayload(document.getJson());
                if (section != null) {
                    // Record the revision before the business loader runs, so a
                    // save triggered during migration still guards correctly.
                    revisions.put(playerKey, document.getRevision());
                    PlayerAccounts data = load(player, section);
                    if (!isCurrentFormat(document.getJson(), document.getFormat())) {
                        // Rewrite the legacy row now that the loader has
                        // validated and normalized its fields.
                        saveData(data);
                    }
                    return data;
                }
                Logger.bug("Failed to parse stored player data for " + playerKey);
            }
        } catch (Exception ex) {
            Logger.bug("Failed to load player data for " + playerKey + " - " + ex.getMessage());
            // A read failure is not proof the player has no data. Returning
            // empty data here would let the next save overwrite a real account
            // with an empty one, so refuse to serve a blank profile instead.
            return null;
        }

        DataSection legacy = loadLegacyConfig(player, playerKey);
        if (legacy != null) {
            try {
                revisions.put(playerKey, JsonDocument.NEW);
                PlayerAccounts data = load(player, legacy);
                saveData(data);
                return data;
            } catch (Exception ex) {
                Logger.bug("Failed to migrate legacy player data for " + playerKey
                        + " - " + ex.getMessage());
            }
        }

        revisions.put(playerKey, JsonDocument.NEW);
        return emptyData(player);
    }

    @Override
    public void saveData(PlayerAccounts data) {
        if (data == null || data.getOfflinePlayer() == null) {
            return;
        }
        String json = saveJson(data);
        if (json == null) {
            return;
        }
        OfflinePlayer player = data.getOfflinePlayer();
        String key = new VersionPlayer(player).getIdString();
        try {
            long revision = store.save(new JsonDocument(
                    key, player.getName(), json, FORMAT, expectedRevision(key)));
            revisions.put(key, revision);
        } catch (StaleDocumentException ex) {
            reportConflict(ex);
        } catch (Exception ex) {
            Logger.bug("Failed to save player data for " + key + " - " + ex.getMessage());
        }
    }

    @Override
    public void saveAll() {
        Map<String, JsonDocument> documents = new LinkedHashMap<String, JsonDocument>();
        for (String key : new ArrayList<String>(SkillAPI.getPlayerAccountData().keySet())) {
            PlayerAccounts data = SkillAPI.getPlayerAccountData().get(key);
            if (data == null || data instanceof SavePlayer || data.getOfflinePlayer() == null) {
                continue;
            }
            // A player whose load is still in flight has no authoritative data
            // in memory yet; saving it would write a placeholder over real data.
            if (MainListener.isLoading(data.getOfflinePlayer().getUniqueId())) {
                continue;
            }
            String json = saveJson(data);
            if (json == null) {
                continue;
            }
            OfflinePlayer player = data.getOfflinePlayer();
            String documentKey = new VersionPlayer(player).getIdString();
            documents.put(documentKey, new JsonDocument(
                    documentKey, player.getName(), json, FORMAT, expectedRevision(documentKey)));
        }
        if (documents.isEmpty()) {
            return;
        }
        try {
            revisions.putAll(store.saveAll(documents));
        } catch (StaleDocumentException ex) {
            reportConflict(ex);
        } catch (Exception ex) {
            Logger.bug("Failed to save player data - " + ex.getMessage());
        }
    }

    @Override
    public void unloaded(OfflinePlayer player) {
        if (player != null) {
            revisions.remove(new VersionPlayer(player).getIdString());
        }
    }

    /**
     * Exports stored documents as individual JSON files for operational backup.
     *
     * @param directory destination directory
     * @return number of files written
     */
    @Override
    public int backupTo(File directory) throws Exception {
        if (!directory.exists() && !directory.mkdirs() && !directory.isDirectory()) {
            throw new IllegalStateException("Unable to create backup directory: " + directory);
        }
        int count = 0;
        for (Map.Entry<String, JsonDocument> entry : store.loadAll().entrySet()) {
            File target = new File(directory, entry.getKey() + ".json");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(target), UTF_8))) {
                writer.write(entry.getValue().getJson());
            }
            count++;
        }
        return count;
    }

    @Override
    public void close() {
        store.close();
        revisions.clear();
    }

    /**
     * @param key player identifier
     * @return revision this player was loaded at, or {@link JsonDocument#NEW}
     */
    private long expectedRevision(String key) {
        Long revision = revisions.get(key);
        return revision == null ? JsonDocument.NEW : revision;
    }

    /**
     * A conflict means the store holds data this server has not seen. Dropping
     * the in-memory copy would lose the player's current session, and
     * overwriting would lose the other writer's, so the operator is told and
     * both copies are left intact until the player reconnects.
     */
    private void reportConflict(StaleDocumentException ex) {
        Logger.bug("Player data was modified elsewhere and was NOT overwritten: "
                + ex.getConflicts()
                + ". This usually means the same player is loaded on another server"
                + " sharing this database.");
    }

    private PlayerAccounts emptyData(OfflinePlayer player) {
        PlayerAccounts data = new PlayerAccounts(player);
        // A missing row is a valid new account and must complete the same
        // initialization phase as the old empty DataSection path.
        data.getActiveData().endInit();
        return data;
    }

    private DataSection loadLegacyConfig(OfflinePlayer player, String playerKey) {
        DataSection data = readLegacyConfig("players/" + playerKey);
        if (data != null) {
            return data;
        }
        String name = player.getName();
        return name == null || playerKey.equals(name)
                ? null
                : readLegacyConfig("players/" + name);
    }

    private DataSection readLegacyConfig(String path) {
        try {
            CommentedConfig config = new CommentedConfig(api, path);
            if (!config.getConfigFile().exists()) {
                return null;
            }
            return config.getConfig();
        } catch (Exception ex) {
            Logger.bug("Failed to read legacy player file " + path + " - " + ex.getMessage());
            return null;
        }
    }
}
