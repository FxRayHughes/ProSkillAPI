package proskill.export

import com.google.gson.JsonParser
import java.io.File

/**
 * 把 Bukkit 枚举名译成中文。
 *
 * 优先用 Mojang 官方 zh_cn 词条，未命中再查本地词典；仍未命中则保留英文原名，
 * 绝不按词素拼凑译名——生硬的机器组合比保留英文更糟。
 *
 * 实测的键映射（1.21.11，8557 条词条）：
 *   Material STONE            → block.minecraft.stone        = 石头
 *   Material DIAMOND_SWORD    → item.minecraft.diamond_sword = 钻石剑
 *   Sound ENTITY_ZOMBIE_AMBIENT → subtitles.entity.zombie.ambient = 僵尸：低吼
 *   Particle                  → 原版仅 9 个相关键，基本要靠词典
 */
class EnumTranslator(
    private val vanilla: Map<String, String>,
    private val dictionary: Map<String, String>
) {

    /** 记录未命中的名字，供任务结束时输出覆盖率报告。 */
    val missing = linkedMapOf<String, MutableSet<String>>()

    fun translateMaterial(name: String): String? {
        val lower = name.lowercase()
        val camel = toCamel(name)
        return lookup("material", name, listOf(
            // 1.13 起的扁平化键
            "block.minecraft.$lower",
            "item.minecraft.$lower",
            // 1.12 及更早用驼峰，例如 BIRCH_FENCE → tile.birchFence.name
            "tile.$camel.name",
            "item.$camel.name"
        ))
    }

    fun translateSound(name: String): String? {
        // ENTITY_ZOMBIE_AMBIENT → subtitles.entity.zombie.ambient
        val path = name.lowercase().replace('_', '.')
        return lookup("sound", name, listOf(
            "subtitles.$path",
            // 少数音效的字幕键省略了类别前缀
            "subtitles.${path.substringAfter('.')}"
        ))
    }

    fun translateParticle(name: String): String? =
        lookup("particle", name, listOf(
            "particle.minecraft.${name.lowercase()}"
        ))

    /** STONE_SLAB → stoneSlab，用于 1.12 及更早的语言键。 */
    private fun toCamel(name: String): String {
        val parts = name.lowercase().split('_')
        return parts.first() + parts.drop(1).joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
    }

    private fun lookup(category: String, name: String, keys: List<String>): String? {
        for (key in keys) {
            vanilla[key]?.let { return it }
        }
        dictionary[name]?.let { return it }
        missing.getOrPut(category) { linkedSetOf() }.add(name)
        return null
    }

    companion object {
        /** 读取随仓库维护的补充词典，只用来补官方缺口。 */
        fun loadDictionary(file: File): Map<String, String> {
            if (!file.isFile) return emptyMap()
            return runCatching {
                JsonParser.parseString(file.readText(Charsets.UTF_8)).asJsonObject
                    .entrySet().associate { (k, v) -> k to v.asString }
            }.getOrElse { emptyMap() }
        }
    }
}
