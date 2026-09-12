package com.sucy.skill.dynamic;

import org.bukkit.entity.LivingEntity;

/**
 * 标记键名的占位符解析。
 *
 * <p>标记键是给标记加"作用域"的手段：写死一个键会让所有施法者共享同一个标记，
 * 因此需要能把施法者身份嵌进键名。支持两个占位符：</p>
 *
 * <ul>
 *   <li>{@code {uuid}} —— 施法者的 UUID，唯一且不随改名变化，适合做真正的隔离</li>
 *   <li>{@code {player}} —— 施法者名称，可读性好，便于用指令排查；
 *       但玩家改名后旧标记会失联，且非玩家实体没有名称</li>
 * </ul>
 *
 * <p>这里刻意不走 {@code EffectComponent.filter}：那个方法会把花括号内容当作
 * cast data 键去解析，还会处理特殊字符转义——对标记键名而言都是多余且有害的。</p>
 */
public final class FlagKeys {

    private FlagKeys() {
    }

    /**
     * 把标记键里的占位符替换成施法者信息。
     *
     * @param key    配置里写的键名，可为 null
     * @param caster 施法者，可为 null
     * @return 替换后的键名；入参为 null 时原样返回
     */
    public static String resolve(final String key, final LivingEntity caster) {
        if (key == null || caster == null) {
            return key;
        }
        String resolved = key;
        if (resolved.contains("{uuid}")) {
            resolved = resolved.replace("{uuid}", caster.getUniqueId().toString());
        }
        if (resolved.contains("{player}")) {
            // 非玩家实体可能没有自定义名称，此时退回实体类型名，
            // 保证替换后的键名仍然稳定，而不是留下未解析的占位符。
            resolved = resolved.replace("{player}", casterName(caster));
        }
        return resolved;
    }

    private static String casterName(final LivingEntity caster) {
        final String custom = caster.getCustomName();
        if (custom != null && !custom.isEmpty()) {
            return custom;
        }
        final String name = caster.getName();
        return name != null && !name.isEmpty() ? name : caster.getType().name();
    }
}
