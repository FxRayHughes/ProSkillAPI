/**
 * SkillAPI
 * com.sucy.skill.storage.sql.dialect.MariaDbDialect
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.storage.sql.dialect;

/**
 * MariaDB served through its own driver.
 *
 * <p>MariaDB speaks the MySQL grammar and reports the same duplicate-key error,
 * so only the JDBC scheme and driver name change. Deployments that keep using
 * Connector/J against MariaDB should configure {@code mysql} instead; this
 * entry exists for the ones that install the MariaDB driver and reject the
 * MySQL one.</p>
 */
public class MariaDbDialect extends MySqlDialect {
    public MariaDbDialect() {
        super("mariadb");
    }

    @Override
    public String driverClass() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    public String jdbcUrl(String host, String port, String database) {
        return "jdbc:mariadb://" + host + ":" + port + "/" + database
                + "?useUnicode=true&characterEncoding=UTF-8";
    }
}
