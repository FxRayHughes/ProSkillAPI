package com.sucy.skill.hook;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Method;

/**
 * Resolves Vault's economy service only when called. Keeping its API out of
 * node signatures lets the same plugin start when Vault is absent.
 */
public final class EconomyService {
    private EconomyService() { }

    private static Object provider() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) return null;
        try {
            Class<?> type = Class.forName("net.milkbowl.vault.economy.Economy");
            Object registration = Bukkit.getServicesManager().getRegistration((Class) type);
            return registration == null ? null : registration.getClass().getMethod("getProvider").invoke(registration);
        } catch (ReflectiveOperationException | LinkageError ex) {
            Bukkit.getLogger().warning("[SkillAPI] Vault economy unavailable: " + ex.getMessage());
            return null;
        }
    }

    /** A missing provider is represented by null, never by a zero balance. */
    public static Double balance(OfflinePlayer player) {
        Object economy = provider();
        if (economy == null) return null;
        try {
            Object value = economy.getClass().getMethod("getBalance", OfflinePlayer.class).invoke(economy, player);
            return value instanceof Number ? ((Number) value).doubleValue() : null;
        } catch (ReflectiveOperationException ex) {
            Bukkit.getLogger().warning("[SkillAPI] Vault balance failed: " + ex.getMessage());
            return null;
        }
    }

    /** Deposits and withdrawals are accepted only when Vault reports success. */
    public static boolean adjust(OfflinePlayer player, double delta) {
        if (!Double.isFinite(delta) || delta == 0) return false;
        Object economy = provider();
        if (economy == null) return false;
        try {
            String method = delta > 0 ? "depositPlayer" : "withdrawPlayer";
            Object response = economy.getClass().getMethod(method, OfflinePlayer.class, double.class)
                    .invoke(economy, player, Math.abs(delta));
            Method success = response.getClass().getMethod("transactionSuccess");
            return Boolean.TRUE.equals(success.invoke(response));
        } catch (ReflectiveOperationException ex) {
            Bukkit.getLogger().warning("[SkillAPI] Vault transaction failed: " + ex.getMessage());
            return false;
        }
    }
}
