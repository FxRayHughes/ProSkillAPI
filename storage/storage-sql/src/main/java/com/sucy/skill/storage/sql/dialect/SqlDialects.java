/**
 * SkillAPI
 * com.sucy.skill.storage.sql.dialect.SqlDialects
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.storage.sql.dialect;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resolves the configured database type to a dialect.
 */
public final class SqlDialects {
    private static final Map<String, SqlDialect> BY_ID;

    static {
        Map<String, SqlDialect> values = new LinkedHashMap<String, SqlDialect>();
        register(values, new MySqlDialect());
        register(values, new MariaDbDialect());
        register(values, new PostgreSqlDialect());
        // Aliases people actually type in configuration files.
        values.put("postgres", values.get("postgresql"));
        values.put("psql", values.get("postgresql"));
        values.put("pgsql", values.get("postgresql"));
        values.put("maria", values.get("mariadb"));
        BY_ID = Collections.unmodifiableMap(values);
    }

    private SqlDialects() {
        // Utility class: dialects are stateless singletons.
    }

    /**
     * @param id configured database type, case-insensitive; blank means MySQL
     * @return matching dialect
     * @throws IllegalArgumentException when the type is not supported
     */
    public static SqlDialect of(String id) {
        if (id == null || id.trim().isEmpty()) {
            return BY_ID.get("mysql");
        }
        SqlDialect dialect = BY_ID.get(id.trim().toLowerCase(java.util.Locale.ROOT));
        if (dialect == null) {
            throw new IllegalArgumentException(
                    "Unsupported database type \"" + id + "\". Supported values: " + supported());
        }
        return dialect;
    }

    /**
     * @return comma separated list of accepted configuration values
     */
    public static String supported() {
        StringBuilder builder = new StringBuilder();
        for (String key : BY_ID.keySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(key);
        }
        return builder.toString();
    }

    private static void register(Map<String, SqlDialect> values, SqlDialect dialect) {
        values.put(dialect.id(), dialect);
    }
}
