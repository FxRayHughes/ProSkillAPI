/**
 * SkillAPI
 * com.sucy.skill.serialization.GsonConfig
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.serialization;

import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.config.parse.YAMLParser;
import com.sucy.skill.serialization.gson.GsonUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * JSON-backed replacement for the former CommentedConfig usage in dynamic
 * skill and class data. A matching YAML file is read once as migration input,
 * but save always writes the JSON file through GsonUtils.
 */
public final class GsonConfig {
    private final File jsonFile;
    private final File legacyYamlFile;
    private final DataSection config;

    /**
     * @param plugin plugin owning the data folder
     * @param path relative path without an extension
     */
    public GsonConfig(JavaPlugin plugin, String path) {
        String normalized = path.replace('\\', File.separatorChar)
                .replace('/', File.separatorChar);
        File base = new File(plugin.getDataFolder(), normalized);
        jsonFile = new File(base.getPath() + ".json");
        legacyYamlFile = new File(base.getPath() + ".yml");
        config = load();
    }

    /**
     * @return the current JSON file
     */
    public File getConfigFile() {
        return jsonFile;
    }

    /**
     * @return in-memory configuration tree
     */
    public DataSection getConfig() {
        return config;
    }

    /**
     * Clears the in-memory tree before a full rewrite.
     */
    public void clear() {
        config.clear();
    }

    /**
     * Writes only JSON. The legacy YAML source is intentionally never modified
     * so a failed migration can be diagnosed or rolled back manually.
     */
    public void save() {
        try {
            GsonUtils.writeJson(jsonFile, config);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to save JSON config " + jsonFile, ex);
        }
    }

    private DataSection load() {
        try {
            if (jsonFile.exists()) {
                DataSection section = GsonUtils.toDataSection(GsonUtils.readJson(jsonFile));
                return section == null ? new DataSection() : section;
            }
            if (legacyYamlFile.exists()) {
                DataSection section = YAMLParser.parseFile(legacyYamlFile);
                return section == null ? new DataSection() : section;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read config " + jsonFile, ex);
        }
        return new DataSection();
    }
}
