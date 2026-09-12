package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.util.ParticleHelper;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/** Shared sampling rules for configured outlines; points are never randomly filled. */
final class ParticleGeometry {
    private ParticleGeometry() { }

    /** Bound per-target work so malformed density cannot stall the server tick. */
    static int samples(double count) {
        if (!Double.isFinite(count) || count < 1 || count > 16384) return 0;
        return (int) Math.ceil(count);
    }

    /**
     * 安全归一化：零长向量归一化会得到 NaN，整个图形随之消失。
     *
     * <p>玩家视线正上或正下看时，压平 Y 分量后就是零长向量，因此这里必须兜底。
     * 兜底方向取 +Z（世界南向），与压平朝向的取值域一致。</p>
     *
     * @return 单位向量；输入零长或含非有限分量时返回 (0,0,1)
     */
    static Vector normalizeOrDefault(Vector direction) {
        if (direction == null) {
            return new Vector(0, 0, 1);
        }
        double lengthSq = direction.lengthSquared();
        if (!Double.isFinite(lengthSq) || lengthSq < 1.0E-9) {
            return new Vector(0, 0, 1);
        }
        return direction.normalize();
    }

    /**
     * 两点间距离，跨世界时返回 -1 而不是抛异常。
     *
     * <p>Bukkit 的 {@code Location.distance} 在两点世界不同时直接抛
     * IllegalArgumentException。技能完全可能在传送途中结算，因此调用方
     * 需要能判断"这次不画"而不是让整个技能崩掉。</p>
     *
     * @return 距离；任一位置为 null、世界不同或结果非有限时返回 -1
     */
    static double safeDistance(Location from, Location to) {
        if (from == null || to == null) {
            return -1;
        }
        if (from.getWorld() == null || !from.getWorld().equals(to.getWorld())) {
            return -1;
        }
        double distance = from.distance(to);
        return Double.isFinite(distance) ? distance : -1;
    }

    /** Plane coordinates are world aligned; rotation is expressed in config degrees. */
    static Vector radial(String plane, double radius, double angle) {
        double a = radius * Math.cos(angle), b = radius * Math.sin(angle);
        if ("XY".equalsIgnoreCase(plane)) return new Vector(a, b, 0);
        if ("YZ".equalsIgnoreCase(plane)) return new Vector(0, a, b);
        if ("XZ".equalsIgnoreCase(plane)) return new Vector(a, 0, b);
        throw new IllegalArgumentException("Unsupported particle plane: " + plane);
    }

    /** Include the last endpoint only for open lines to avoid duplicate polygon corners. */
    static void line(Location origin, Vector start, Vector end, int steps, boolean endpoint, Settings settings) {
        for (int i = 0; i < steps + (endpoint ? 1 : 0); i++) {
            double fraction = (double) i / steps;
            Vector point = start.clone().multiply(1 - fraction).add(end.clone().multiply(fraction));
            ParticleHelper.play(origin.clone().add(point), settings.getString("particle", "invalid"), settings);
        }
    }
}
