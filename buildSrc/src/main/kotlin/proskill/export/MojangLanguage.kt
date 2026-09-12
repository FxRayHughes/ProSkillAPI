package proskill.export

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.net.URL
import java.util.Properties

/**
 * 拉取 Mojang 官方的 zh_cn 语言文件。
 *
 * 链路照搬 TabooLib 的 MinecraftLanguage.kt：
 *   version_manifest.json → <version>.json → assetIndex.json → resources/<hash前2位>/<hash>
 *
 * 语言文件在不同世代有三种路径与两种格式：
 *   - minecraft/lang/zh_CN.lang   老版本，properties 格式
 *   - minecraft/lang/zh_cn.lang   1.12 及更早，properties 格式
 *   - minecraft/lang/zh_cn.json   1.13 起，JSON 格式
 */
object MojangLanguage {

    // 官方源实测直连可用（经代理）；BMCLAPI 镜像当前返回 HTML 错误页，因此不作默认。
    private const val OFFICIAL_MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
    private const val OFFICIAL_RESOURCES = "https://resources.download.minecraft.net"
    private const val MIRROR_MANIFEST = "https://bmclapi2.bangbang93.com/mc/game/version_manifest.json"
    private const val MIRROR_RESOURCES = "https://bmclapi2.bangbang93.com/assets"

    /**
     * 取指定版本的 zh_cn 词条表；取不到时返回空表，调用方据此走词典兜底。
     *
     * @param cacheDir 缓存目录，同一版本只下载一次
     * @param useMirror 官方源不可达时可切到 BMCLAPI
     */
    fun fetch(version: String, cacheDir: File, useMirror: Boolean = false): Map<String, String> {
        val cached = File(cacheDir, "$version.json")
        if (cached.isFile && cached.length() > 0) {
            return parseJsonLang(cached.readText(Charsets.UTF_8))
        }

        val manifestUrl = if (useMirror) MIRROR_MANIFEST else OFFICIAL_MANIFEST
        val resourceUrl = if (useMirror) MIRROR_RESOURCES else OFFICIAL_RESOURCES

        val manifest = readJson(manifestUrl) ?: return emptyMap()
        val entry = manifest.getAsJsonArray("versions")
            ?.map { it.asJsonObject }
            ?.firstOrNull { it["id"].asString == version }
            ?: return emptyMap()

        val versionMeta = readJson(transfer(entry["url"].asString, useMirror)) ?: return emptyMap()
        val assetIndexUrl = versionMeta.getAsJsonObject("assetIndex")?.get("url")?.asString ?: return emptyMap()
        val assetIndex = readJson(transfer(assetIndexUrl, useMirror)) ?: return emptyMap()
        val objects = assetIndex.getAsJsonObject("objects") ?: return emptyMap()

        // 按世代顺序尝试三种路径
        val candidates = listOf(
            "minecraft/lang/zh_CN.lang",
            "minecraft/lang/zh_cn.lang",
            "minecraft/lang/zh_cn.json"
        )
        for (name in candidates) {
            val obj = objects.getAsJsonObject(name) ?: continue
            val hash = obj["hash"].asString
            val text = readText("$resourceUrl/${hash.substring(0, 2)}/$hash") ?: continue

            val parsed = if (name.endsWith(".json")) parseJsonLang(text) else parsePropertiesLang(text)
            if (parsed.isNotEmpty()) {
                cacheDir.mkdirs()
                // 统一以 JSON 缓存，后续读取无需再区分格式
                cached.writeText(toJson(parsed), Charsets.UTF_8)
                return parsed
            }
        }
        return emptyMap()
    }

    private fun transfer(url: String, useMirror: Boolean): String =
        if (useMirror) url.replace("piston-meta.mojang.com", "bmclapi2.bangbang93.com") else url

    /**
     * 读取远端文本。必须显式处理 gzip 与重定向：实测 Mojang 的资源服务器会对
     * 大文件启用 gzip，URL.readText() 拿到的是压缩字节流，直接解析会在中途报错。
     */
    private fun readText(url: String): String? = runCatching {
        // URL(String) 已弃用，改用 URI 解析后转换。
        var current = java.net.URI(url).toURL()
        repeat(5) {
            val conn = (current.openConnection() as java.net.HttpURLConnection).apply {
                setRequestProperty("User-Agent", "ProSkillAPI-Export")
                setRequestProperty("Accept-Encoding", "gzip")
                connectTimeout = 30_000
                readTimeout = 60_000
                instanceFollowRedirects = false
            }
            val code = conn.responseCode
            if (code in 300..399) {
                val location = conn.getHeaderField("Location") ?: return@runCatching null
                conn.disconnect()
                current = current.toURI().resolve(location).toURL()
                return@repeat
            }
            if (code != 200) {
                conn.disconnect()
                return@runCatching null
            }
            val raw = conn.inputStream
            val stream = if (conn.contentEncoding?.contains("gzip", true) == true) {
                java.util.zip.GZIPInputStream(raw)
            } else raw
            return@runCatching stream.use { it.readBytes().toString(Charsets.UTF_8) }
        }
        null
    }.getOrNull()

    private fun readJson(url: String): JsonObject? = runCatching {
        JsonParser.parseString(readText(url) ?: return null).asJsonObject
    }.getOrNull()

    private fun parseJsonLang(text: String): Map<String, String> = runCatching {
        val root = JsonParser.parseString(text).asJsonObject
        root.entrySet().associate { (k, v) -> k to v.asString }
    }.getOrElse { emptyMap() }

    private fun parsePropertiesLang(text: String): Map<String, String> = runCatching {
        // 老版本是 key=value 的 properties，且以 UTF-8 存储
        val props = Properties()
        props.load(text.reader())
        props.entries.associate { (k, v) -> k.toString() to v.toString() }
    }.getOrElse { emptyMap() }

    private fun toJson(map: Map<String, String>): String {
        val obj = JsonObject()
        map.forEach { (k, v) -> obj.addProperty(k, v) }
        return obj.toString()
    }
}
