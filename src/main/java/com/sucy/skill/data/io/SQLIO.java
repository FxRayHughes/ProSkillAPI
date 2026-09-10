/**
 * SkillAPI
 * com.sucy.skill.data.io.SQLIO
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.data.io;

import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.config.parse.YAMLParser;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.serialization.gson.GsonUtils;
import com.sucy.skill.storage.sql.RemoteJsonStore;

/**
 * Remote player data manager backed by a pooled SQL connection and Gson JSON.
 *
 * <p>Existing rows containing the historical YAML payload are accepted as
 * migration input. Every successful save, including that migration write,
 * stores only the JSON returned by GsonUtils.</p>
 */
public class SQLIO extends JsonStoreIO {
    public static final String ID = "id";
    public static final String DATA = "data";

    /** Delimiter MCCore used to escape strings inside its packed YAML rows. */
    public static final char STRING = '√';

    private final RemoteJsonStore store;

    /**
     * @param api API reference
     */
    public SQLIO(SkillAPI api) {
        this(api, new RemoteJsonStore(
                SkillAPI.getSettings().getSQLType(),
                SkillAPI.getSettings().getSQLHost(),
                SkillAPI.getSettings().getSQLPort(),
                SkillAPI.getSettings().getSQLDatabase(),
                SkillAPI.getSettings().getSQLUser(),
                SkillAPI.getSettings().getSQLPass(),
                api.getName()));
    }

    private SQLIO(SkillAPI api, RemoteJsonStore store) {
        super(api, store);
        this.store = store;
    }

    @Override
    protected DataSection decodePayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return null;
        }
        String value = payload.trim();
        try {
            if (value.startsWith("{")) {
                return GsonUtils.toDataSection(value);
            }
            // YAMLParser is restricted to legacy reads. It is never used by
            // new writes, backups, or the remote storage module.
            return YAMLParser.parseText(payload, STRING);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * @return remote SQL table name
     */
    public String getTableName() {
        return store.getTableName();
    }

    /**
     * @return identifier of the configured SQL server type
     */
    public String getDialectId() {
        return store.getDialectId();
    }
}
