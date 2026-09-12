/**
 * SkillAPI
 * com.sucy.skill.data.io.ConfigIO
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.data.io;

import com.sucy.skill.SkillAPI;

/**
 * Compatibility name for integrations that constructed the former YAML
 * manager directly. Player data now uses SQLiteIO's local JSON backend in the
 * same way the old configuration manager did, so this class delegates all
 * operations without writing YAML.
 */
@Deprecated
public class ConfigIO extends SQLiteIO
{
    /**
     * @param plugin SkillAPI reference
     */
    public ConfigIO(SkillAPI plugin)
    {
        super(plugin);
    }
}
