/**
 * SkillAPI
 * com.sucy.skill.data.io.SQLiteIO
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.data.io;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.serialization.gson.GsonUtils;
import com.sucy.skill.storage.sqlite.SQLiteJsonStore;

import java.io.File;

/**
 * Local player data manager backed by SQLite JSON.
 *
 * <p>The former ConfigIO YAML files are read only when a player row is
 * missing. Once loaded, the same business tree is serialized with GsonUtils and
 * stored in players.db; the source YAML is left untouched as a rollback
 * artifact.</p>
 */
public class SQLiteIO extends JsonStoreIO {
    private final SQLiteJsonStore store;

    /**
     * @param api API reference
     */
    public SQLiteIO(SkillAPI api) {
        this(api, new SQLiteJsonStore(api.getDataFolder(), "players.db"));
    }

    private SQLiteIO(SkillAPI api, SQLiteJsonStore store) {
        super(api, store);
        this.store = store;
    }

    @Override
    protected DataSection decodePayload(String payload) {
        // Only this plugin ever wrote to players.db, and it has only ever
        // written JSON, so there is no legacy text format to accept here.
        return GsonUtils.toDataSection(payload);
    }

    /**
     * @return local SQLite file
     */
    public File getDatabaseFile() {
        return store.getDatabaseFile();
    }
}
