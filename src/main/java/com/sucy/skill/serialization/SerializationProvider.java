/**
 * SkillAPI
 * com.sucy.skill.serialization.SerializationProvider
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.serialization;

import com.sucy.skill.serialization.gson.GsonItemSerializationService;
import com.sucy.skill.serialization.legacy.LegacyNbtItemSerializationService;

import java.util.Arrays;

/**
 * Owns the item serialization module order for the main plugin. The provider is
 * intentionally small so future formats can be added without touching inventory
 * restore logic or NMS compatibility modules.
 */
public final class SerializationProvider {
    private static final ItemSerializationService SERVICE = new CompositeItemSerializationService(Arrays.asList(
            new GsonItemSerializationService(),
            new LegacyNbtItemSerializationService()
    ));

    private SerializationProvider() {
        // Utility class: module services are stateless and safe to reuse.
    }

    public static ItemSerializationService service() {
        return SERVICE;
    }
}
