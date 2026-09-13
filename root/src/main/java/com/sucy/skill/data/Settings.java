package com.sucy.skill.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rit.sucy.config.CommentedConfig;
import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.config.parse.NumberParser;
import com.rit.sucy.text.TextFormatter;
import com.rit.sucy.version.VersionManager;
import com.sucy.party.Parties;
import com.sucy.party.Party;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.CombatProtection;
import com.sucy.skill.api.DefaultCombatProtection;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.cast.IndicatorSettings;
import com.sucy.skill.data.formula.Formula;
import com.sucy.skill.data.formula.value.CustomValue;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.gui.tool.GUITool;
import com.sucy.skill.log.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class Settings {
   private HashMap<String, GroupSettings> groups = new HashMap<>();
   private SkillAPI plugin;
   private DataSection config;
   private static final String DEFAULT_YIELD = "default";
   private Map<String, Map<String, Double>> breakYields;
   private Map<String, Map<String, Double>> placeYields;
   private Map<String, Map<String, Double>> craftYields;
   private boolean trackBreak;
   private boolean yieldsEnabled;
   private static final String ACCOUNT_BASE = "Accounts.";
   private static final String ACCOUNT_MAIN = "Accounts.main-class-group";
   private static final String ACCOUNT_EACH = "Accounts.one-per-class";
   private static final String ACCOUNT_MAX = "Accounts.max-accounts";
   private static final String ACCOUNT_PERM = "Accounts.perm-accounts";
   private String mainGroup;
   private boolean onePerClass;
   private int maxAccounts;
   private HashMap<String, Integer> permAccounts = new HashMap<>();
   private static final String TARGET_BASE = "Targeting.";
   private static final String TARGET_MONSTER = "Targeting.monsters-enemy";
   private static final String TARGET_PASSIVE = "Targeting.passive-ally";
   private static final String TARGET_PLAYER = "Targeting.player-ally";
   private static final String TARGET_PARTIES = "Targeting.parties-ally";
   private static final String TARGET_NPC = "Targeting.affect-npcs";
   private static final String TARGET_STANDS = "Targeting.affect-armor-stands";
   private ArrayList<String> monsterWorlds = new ArrayList<>();
   private ArrayList<String> passiveWorlds = new ArrayList<>();
   private ArrayList<String> playerWorlds = new ArrayList<>();
   private boolean monsterEnemy;
   private boolean passiveAlly;
   private boolean playerAlly;
   private boolean partiesAlly;
   private boolean affectNpcs;
   private boolean affectArmorStands;
   private CombatProtection combatProtection = new DefaultCombatProtection();
   private static final String SAVE_BASE = "Saving.";
   private static final String SAVE_AUTO = "Saving.auto-save";
   private static final String SAVE_MINS = "Saving.minutes";
   private static final String SAVE_SQL = "Saving.sql-database";
   private static final String SAVE_SQLD = "Saving.sql-details";
   private boolean auto;
   private boolean useSql;
   private int minutes;
   private int sqlDelay;
   private String sqlHost;
   private String sqlPort;
   private String sqlDatabase;
   private String sqlUser;
   private String sqlPass;
   private static final String CLASS_BASE = "Classes.";
   private static final String CLASS_MODIFY = "Classes.modify-health";
   private static final String CLASS_HP = "Classes.classless-hp";
   private static final String CLASS_SHOW = "Classes.show-auto-skills";
   private static final String CLASS_ATTRIB = "Classes.attributes-enabled";
   private static final String CLASS_REFUND = "Classes.attributes-downgrade";
   private static final String CLASS_LEVEL = "Classes.level-up-skill";
   private boolean modifyHealth;
   private boolean attributesHeal;
   private boolean skillModelData;
   private boolean oldDurability;
   private boolean attributeMobEnabled;
   private String sqlType = "mysql";
   /** 双向 SX-Attribute 桥接开关；默认关闭，避免单侧启用造成半集成。 */
   private boolean sxAttributeEnabled;
   private int defaultHealth;
   private boolean showAutoSkills;
   private boolean attributesEnabled;
   private boolean attributesDowngrade;
   private String levelUpSkill;
   private static final String MANA_BASE = "Mana.";
   private static final String MANA_ENABLED = "Mana.enabled";
   private static final String MANA_FREQ = "Mana.freq";
   private boolean manaEnabled;
   private int gainFreq;
   private static final String SKILL_BASE = "Skills.";
   private static final String SKILL_DOWNGRADE = "Skills.allow-downgrade";
   private static final String SKILL_MESSAGE = "Skills.show-messages";
   private static final String SKILL_RADIUS = "Skills.message-radius";
   private static final String SKILL_BLOCKS = "Skills.block-filter";
   private static final String SKILL_KNOCKBACK = "Skills.knockback-no-damage";
   private ArrayList<Material> filteredBlocks;
   private boolean allowDowngrade;
   private boolean showSkillMessages;
   private boolean knockback;
   private int messageRadius;
   private static final String ITEM_BASE = "Items.";
   private static final String ITEM_LORE = "Items.lore-requirements";
   private static final String ITEM_DROP = "Items.drop-weapon";
   private static final String ITEM_SKILLS = "Items.skill-requirements";
   private static final String ITEM_ATTRIBS = "Items.lore-attributes";
   private static final String ITEM_CLASS = "Items.lore-class-text";
   private static final String ITEM_SKILL = "Items.lore-skill-text";
   private static final String ITEM_LEVEL = "Items.lore-level-text";
   private static final String ITEM_EXCLUDE = "Items.lore-exclude-text";
   private static final String ITEM_ATTR = "Items.lore-attribute-text";
   private static final String ITEM_STATS = "Items.attribute-text";
   private static final String ITEM_SLOTS = "Items.slots";
   private boolean checkLore;
   private boolean checkAttribs;
   private boolean checkSkills;
   private boolean dropWeapon;
   private String loreClassText;
   private String loreLevelText;
   private String loreExcludeText;
   private int[] slots;
   private String skillPre;
   private String skillPost;
   private String attrReqPre;
   private String attrReqPost;
   private String attrPre;
   private String attrPost;
   private static final String GUI_BASE = "GUI.";
   private static final String GUI_OLD = "GUI.old-health-bar";
   private static final String GUI_FORCE = "GUI.force-scaling";
   private static final String GUI_LVLBAR = "GUI.level-bar";
   private static final String GUI_FOOD = "GUI.food-bar";
   private static final String GUI_ACTION = "GUI.use-action-bar";
   private static final String GUI_TEXT = "GUI.action-bar-text";
   private static final String GUI_BOARD = "GUI.scoreboard-enabled";
   private static final String GUI_NAME = "GUI.show-class-name";
   private static final String GUI_LEVEL = "GUI.show-class-level";
   private static final String GUI_BINDS = "GUI.show-binds";
   private static final String GUI_BIND_TEXT = "GUI.show-bind-text";
   private static final String GUI_LVLTXT = "GUI.class-level-text";
   private static final String GUI_TITLE = "GUI.title-enabled";
   private static final String GUI_DUR = "GUI.title-duration";
   private static final String GUI_FADEI = "GUI.title-fade-in";
   private static final String GUI_FADEO = "GUI.title-fade-out";
   private static final String GUI_LIST = "GUI.title-messages";
   private List<String> titleMessages;
   private boolean oldHealth;
   private boolean forceScaling;
   private String levelBar;
   private String foodBar;
   private String levelText;
   private boolean useActionBar;
   private String actionText;
   private boolean showScoreboard;
   private boolean showClassName;
   private boolean showClassLevel;
   private boolean showBinds;
   private String bindText;
   private boolean useTitle;
   private int titleDuration;
   private int titleFadeIn;
   private int titleFadeOut;
   private static final String CAST_BASE = "Casting.";
   private static final String CAST_ENABLED = "Casting.enabled";
   private static final String CAST_BARS = "Casting.bars";
   private static final String CAST_COMBAT = "Casting.combat";
   private static final String CAST_INDICATOR = "Casting.cast-indicator";
   private static final String CAST_SLOT = "Casting.slot";
   private static final String CAST_ITEM = "Casting.item";
   private static final String CAST_COOLDOWN = "Casting.cooldown";
   private static final String CAST_HOVER = "Casting.hover-item";
   private static final String CAST_INSTANT = "Casting.instant-item";
   private boolean castEnabled;
   private boolean castBars;
   private boolean combatEnabled;
   private int castSlot;
   private long castCooldown;
   private ItemStack castItem;
   private ItemStack hoverItem;
   private ItemStack instantItem;
   private static final String COMBO_BASE = "Click Combos.";
   private static final String COMBO_ENABLED = "Click Combos.enabled";
   private static final String COMBO_CUSTOM = "Click Combos.allow-custom";
   private static final String COMBO_CLICK = "Click Combos.use-click-";
   private static final String COMBO_SIZE = "Click Combos.combo-size";
   private static final String COMBO_TIME = "Click Combos.click-time";
   private static final String COMBO_AUTO = "Click Combos.auto-assign";
   private boolean[] clicks;
   private boolean combosEnabled;
   private boolean customCombos;
   private boolean autoAssignCombos;
   private int comboSize;
   private int clickTime;
   private final HashMap<String, Double> yields = new HashMap<>();
   private ExpFormula expFormula;
   private Formula expCustom;
   private boolean useCustomExp;
   private boolean useOrbs;
   private boolean blockSpawner;
   private boolean blockEgg;
   private boolean blockCreative;
   private boolean showExpMessages;
   private boolean showLevelMessages;
   private boolean showLossMessages;
   private Set<String> expLostBlacklist;
   private static final String EXP_BASE = "Experience.";
   private boolean skillBarEnabled;
   private boolean skillBarCooldowns;
   private ItemStack unassigned;
   private boolean[] defaultBarLayout = new boolean[9];
   private boolean[] lockedSlots = new boolean[9];
   // 防具自动装配技能栏
   private boolean armorAutoBindEnabled;
   private String armorDetectPre;
   private String armorDetectPost;
   private int armorSlotHelmet = 1;
   private int armorSlotChestplate = 2;
   private int armorSlotLeggings = 3;
   private int armorSlotBoots = 4;
   private int armorSlotOffhand = 5;
   private int armorSlotMainhand = 6;
   private String offhandWhitelistLore = "";
   // 套装效果：套装名 -> 检测lore + 件数->技能列表
   private final java.util.LinkedHashMap<String, SetBonusEntry> setBonuses = new java.util.LinkedHashMap<>();

   public static class SetBonusEntry {
      public final String detectLore;
      // 件数 -> 技能名列表（向下包含，4件套同时享有2件奖励）
      public final java.util.TreeMap<Integer, java.util.List<String>> bonuses = new java.util.TreeMap<>();
      // 件数 -> 属性名 -> 加成数值（向下包含，累计累加）
      // 例：count-2 health=5, count-4 health=10, mana=10 → 穿4件时 health+15, mana+10
      public final java.util.TreeMap<Integer, java.util.Map<String, Integer>> attributeBonuses = new java.util.TreeMap<>();
      public SetBonusEntry(String detectLore) { this.detectLore = detectLore; }
   }

   public java.util.Map<String, SetBonusEntry> getSetBonuses() {
      return this.setBonuses;
   }
   private static final String WORLD_BASE = "Worlds.";
   private static final String WORLD_ENABLE = "Worlds.enable";
   private static final String WORLD_TYPE = "Worlds.use-as-enabling";
   private static final String WORLD_LIST = "Worlds.worlds";
   private List<String> worlds;
   private boolean worldEnabled;
   private boolean worldEnableList;
   private static final String WG_SKILLS = "disable-skills";
   private static final String WG_EXP = "disable-exp";
   private Set<String> skillDisabledRegions;
   private Set<String> expDisabledRegions;

   // 战斗属性设置
   private double baseCritDamage = 1.75;
   private double critRateBase = 100.0;
   private double critDamageBase = 100.0;
   private double dodgeBase = 100.0;
   private double lifestealBase = 100.0;
   private double armorPenBase = 100.0;

   public Settings(SkillAPI var1) {
      // GUI item defaults may call SkillAPI.getSettings while this object is loading.
      SkillAPI.setSettingsDuringBootstrap(this);
      this.plugin = var1;// 86
      CommentedConfig var2 = new CommentedConfig(var1, "config");// 87
      var2.checkDefaults();// 88
      // 补全新增的配置字段（保留用户已有配置）
      ensureDefaults(var2.getConfig());
      var2.save();// 89
      this.config = var2.getConfig();// 90
      this.reload();// 91
   }// 92

   /**
    * 保留用户已有配置的前提下，把新增字段写入配置文件。
    * 仅当字段缺失时才设置默认值；已有值不会被覆盖。
    */
   private static void ensureDefaults(com.rit.sucy.config.parse.DataSection cfg) {
      // Combat.threat 段（仇恨系统）
      setIfAbsent(cfg, "Combat.threat.enabled", true);
      setIfAbsent(cfg, "Combat.threat.heal-range", 25.0);
      setIfAbsent(cfg, "Combat.threat.heal-multiplier", 0.5);
      setIfAbsent(cfg, "Combat.threat.combat-timeout", 5.0);

      // Combat.base-values.threat-power（仇恨强度基础值）
      setIfAbsent(cfg, "Combat.base-values.threat-power", "100.0");
      // Combat.calc-mode.threat-power（仇恨强度计算模式，默认线性）
      setIfAbsent(cfg, "Combat.calc-mode.threat-power", "additive");
      // Combat.attributes.threat-power（仇恨强度属性显示名）
      setIfAbsent(cfg, "Combat.attributes.threat-power", "仇恨强度");

      // 让 AttributeListener/KillListener 的 MM 属性查找可通过 config 覆盖名字
      setIfAbsent(cfg, "Combat.attributes.physical-defense", "防御力");
      setIfAbsent(cfg, "Combat.attributes.armor-penetration-flat", "固定穿透");
      setIfAbsent(cfg, "Combat.attributes.mob-level", "怪物等级");

      // 越级作战曲线：只在首次（section 完全缺失）时 seed；用户已配置则一概不动
      ensureLevelGapDefaults(cfg);
   }

   /**
    * 只在 Combat.level-gap.damage.curve 完全缺失时 seed 默认伤害曲线；
    * 只在 Combat.level-gap.exp.curve 完全缺失时 seed 默认经验曲线。
    * 若用户已经写过任一条目，即便只剩 1 项，也保持原样不再补齐 —— 避免用户删除的键被自动恢复。
    */
   private static void ensureLevelGapDefaults(com.rit.sucy.config.parse.DataSection cfg) {
      setIfAbsent(cfg, "Combat.level-gap.damage.enabled", true);
      setIfAbsent(cfg, "Combat.level-gap.exp.enabled", true);

      com.rit.sucy.config.parse.DataSection damageCurve =
         cfg.getSection("Combat.level-gap.damage") != null
            ? cfg.getSection("Combat.level-gap.damage").getSection("curve")
            : null;
      if (damageCurve == null || damageCurve.keys().isEmpty()) {
         cfg.set("Combat.level-gap.damage.curve.0", 1.0);
         cfg.set("Combat.level-gap.damage.curve.1", 0.8);
         cfg.set("Combat.level-gap.damage.curve.2", 0.5);
         cfg.set("Combat.level-gap.damage.curve.3", 0.1);
         cfg.set("Combat.level-gap.damage.curve.5", 0.01);
      }

      com.rit.sucy.config.parse.DataSection expCurve =
         cfg.getSection("Combat.level-gap.exp") != null
            ? cfg.getSection("Combat.level-gap.exp").getSection("curve")
            : null;
      if (expCurve == null || expCurve.keys().isEmpty()) {
         cfg.set("Combat.level-gap.exp.curve.0", 1.0);
         cfg.set("Combat.level-gap.exp.curve.5", 0.7);
         cfg.set("Combat.level-gap.exp.curve.10", 0.3);
         cfg.set("Combat.level-gap.exp.curve.15", 0.0);
      }
   }

   /**
    * 如果配置路径不存在则设置默认值。
    * 利用 DataSection 的 getDouble 返回 0 / getString 返回默认值来检测缺失。
    * 原理：如果 key 不存在，getBoolean(path, SENTINEL) 返回 SENTINEL。
    */
   private static void setIfAbsent(com.rit.sucy.config.parse.DataSection cfg, String path, Object value) {
      // DataSection.has 可用于单层 key，嵌套路径用 getSection 拆分判定
      String[] parts = path.split("\\.");
      com.rit.sucy.config.parse.DataSection section = cfg;
      for (int i = 0; i < parts.length - 1; i++) {
         com.rit.sucy.config.parse.DataSection sub = section.getSection(parts[i]);
         if (sub == null) {
            // 父 section 不存在 → 整条路径缺失，直接 set
            cfg.set(path, value);
            return;
         }
         section = sub;
      }
      String key = parts[parts.length - 1];
      if (!section.has(key)) {
         cfg.set(path, value);
      }
   }

   public void reload() {
      this.loadExperienceSettings();// 100
      this.loadAccountSettings();// 101
      this.loadClassSettings();// 102
      this.loadManaSettings();// 103
      this.loadSkillSettings();// 104
      this.loadItemSettings();// 105
      this.loadGUISettings();// 106
      this.loadCastSettings();// 107
      this.loadComboSettings();// 108
      this.loadExpSettings();// 109
      this.loadSkillBarSettings();// 110
      this.loadLoggingSettings();// 111
      this.loadWorldSettings();// 112
      this.loadSaveSettings();// 113
      this.loadTargetingSettings();// 114
      this.loadWorldGuardSettings();// 115
      this.loadCombatSettings();
   }// 116

   public void loadExperienceSettings() {
      CommentedConfig var1 = new CommentedConfig(this.plugin, "exp");// 134
      var1.checkDefaults();// 135
      var1.save();// 136
      DataSection var2 = var1.getConfig();// 137
      DataSection var3 = var2.getSection("break");// 139
      this.yieldsEnabled = var2.getBoolean("enabled", false);// 140
      this.trackBreak = var3.getBoolean("allow-replace", true);// 141
      this.breakYields = this.loadYields(var3.getSection("types"));// 142
      this.placeYields = this.loadYields(var2.getSection("place"));// 143
      this.craftYields = this.loadYields(var2.getSection("craft"));// 144
   }// 145

   private Map<String, Map<String, Double>> loadYields(DataSection var1) {
      HashMap var2 = new HashMap();// 148

      for (String var4 : var1.keys()) {// 149
         HashMap var5 = new HashMap();// 150
         DataSection var6 = var1.getSection(var4);// 151

         for (String var8 : var6.keys()) {// 152
            var5.put(var8.toUpperCase().replace(" ", "_"), var6.getDouble(var8));// 153
         }

         var2.put(var4, var5);// 155
      }

      return var2;// 157
   }

   public boolean trackBreaks() {
      return this.trackBreak;// 161
   }

   public boolean yieldsEnabled() {
      return this.yieldsEnabled;// 165
   }

   public double getBreakYield(PlayerClass var1, Material var2) {
      return this.getYield(this.breakYields, var1, var2.name());// 169
   }

   public double getPlaceYield(PlayerClass var1, Material var2) {
      return this.getYield(this.placeYields, var1, var2.name());// 173
   }

   public double getCraftYield(PlayerClass var1, Material var2) {
      return this.getYield(this.craftYields, var1, var2.name());// 177
   }

   private double getYield(Map<String, Map<String, Double>> var1, PlayerClass var2, String var3) {
      double var4 = this.getYield((Map<String, Double>)var1.get(var2.getData().getName()), var3);// 181
      return var4 > 0.0 ? var4 : this.getYield((Map<String, Double>)var1.get("default"), var3);// 182
   }

   private double getYield(Map<String, Double> var1, String var2) {
      return var1 == null ? 0.0 : (var1.containsKey(var2) ? (Double)var1.get(var2) : 0.0);// 186
   }

   public void loadGroupSettings() {
      CommentedConfig var1 = new CommentedConfig(this.plugin, "groups");// 196
      DataSection var2 = var1.getConfig();// 197
      this.groups.clear();// 198

      for (String var4 : var2.keys()) {// 200
         this.groups.put(var4.toLowerCase(), new GroupSettings(var2.getSection(var4)));// 201
      }

      for (String var7 : SkillAPI.getGroups()) {// 203
         if (!this.groups.containsKey(var7.toLowerCase())) {// 204
            GroupSettings var5 = new GroupSettings();// 205
            this.groups.put(var7.toLowerCase(), var5);// 206
            var5.save(var2.createSection(var7.toLowerCase()));// 207
         }

         var2.setComments(// 209
            var7.toLowerCase(),
            ImmutableList.of(
               "",
               " Settings for classes with the group " + var7,
               " If new classes are loaded with different groups,",
               " the new groups will show up in this file after the first load."
            )
         );
      }

      var1.save();// 216
   }// 217

   public GroupSettings getGroupSettings(String var1) {
      return !this.groups.containsKey(var1.toLowerCase()) ? new GroupSettings() : this.groups.get(var1.toLowerCase());// 227 230
   }

   public String getMainGroup() {
      return this.mainGroup;// 258
   }

   public boolean isOnePerClass() {
      return this.onePerClass;// 268
   }

   public int getMaxAccounts() {
      return this.maxAccounts;// 277
   }

   public int getMaxAccounts(Player var1) {
      if (var1 == null) {// 289
         return this.maxAccounts;// 290
      }

      int var2 = this.maxAccounts;// 292

      for (Entry var4 : this.permAccounts.entrySet()) {// 293
         if (var1.hasPermission((String)var4.getKey())) {// 294
            var2 = Math.max(var2, (Integer)var4.getValue());// 295
         }
      }

      return var2;// 298
   }

   private void loadAccountSettings() {
      this.mainGroup = this.config.getString("Accounts.main-class-group");// 302
      this.onePerClass = this.config.getBoolean("Accounts.one-per-class");// 303
      this.maxAccounts = this.config.getInt("Accounts.max-accounts");// 304

      for (String var3 : this.config.getList("Accounts.perm-accounts")) {// 307 308
         if (var3.contains(":")) {// 309
            String[] var4 = var3.split(":");// 313
            if (var4.length == 2) {// 314
               try {
                  this.permAccounts.put(var4[0], Integer.parseInt(var4[1]));// 319
               } catch (Exception var6) {// 320
               }
            }
         }
      }
   }// 324

   public boolean canAttack(LivingEntity var1, LivingEntity var2) {
      if (var1 instanceof Player) {// 362
         Player var7 = (Player)var1;// 363
         if (var2 instanceof Animals && !(var2 instanceof Tameable)) {// 364
            if (this.passiveAlly || this.passiveWorlds.contains(var1.getWorld().getName())) {// 365
               return false;
            }
         } else if (var2 instanceof Monster) {// 366
            if (this.monsterEnemy || this.monsterWorlds.contains(var1.getWorld().getName())) {// 367
               return true;
            }
         } else if (var2 instanceof Player) {// 368
            if (!this.playerAlly && !this.playerWorlds.contains(var1.getWorld().getName())) {// 369
               if (!this.partiesAlly) {// 371
                  return this.combatProtection.canAttack(var7, (Player)var2);// 377
               }

               Parties var4 = (Parties)Parties.getPlugin(Parties.class);// 372
               Party var5 = var4.getJoinedParty(var7);// 373
               Party var6 = var4.getJoinedParty((Player)var2);// 374
               return var5 == null || var5 != var6;// 375
            }

            return false;
         }

         return this.combatProtection.canAttack(var7, var2);// 379
      } else {
         if (!(var1 instanceof Tameable)) {// 380
            return !(var2 instanceof Monster);// 386
         }

         Tameable var3 = (Tameable)var1;// 381
         return var3.isTamed() && var3.getOwner() instanceof LivingEntity// 382
            ? var3.getOwner() != var2 && this.canAttack((LivingEntity)var3.getOwner(), var2)// 383 384
            : this.combatProtection.canAttack(var1, var2);// 388
      }
   }

   public boolean isAlly(LivingEntity var1, LivingEntity var2) {
      return !this.canAttack(var1, var2);// 400
   }

   public boolean isValidTarget(LivingEntity var1) {
      return (!var1.hasMetadata("NPC") || this.affectNpcs) && (!var1.getType().name().equals("ARMOR_STAND") || this.affectArmorStands);// 410 411
   }

   public void setCombatProtection(CombatProtection var1) {
      this.combatProtection = var1;// 420
   }// 421

   private void loadTargetingSettings() {
      if (this.config.isList("Targeting.monsters-enemy")) {// 424
         this.monsterWorlds.addAll(this.config.getList("Targeting.monsters-enemy"));// 425
         this.monsterEnemy = false;// 426
      } else {
         this.monsterEnemy = this.config.getBoolean("Targeting.monsters-enemy");// 427
      }

      if (this.config.isList("Targeting.passive-ally")) {// 429
         this.passiveWorlds.addAll(this.config.getList("Targeting.passive-ally"));// 430
         this.passiveAlly = false;// 431
      } else {
         this.passiveAlly = this.config.getBoolean("Targeting.passive-ally");// 432
      }

      if (this.config.isList("Targeting.player-ally")) {// 434
         this.playerWorlds.addAll(this.config.getList("Targeting.player-ally"));// 435
         this.playerAlly = false;// 436
      } else {
         this.playerAlly = this.config.getBoolean("Targeting.player-ally");// 437
      }

      this.partiesAlly = this.config.getBoolean("Targeting.parties-ally");// 439
      this.affectArmorStands = this.config.getBoolean("Targeting.affect-armor-stands");// 440
      this.affectNpcs = this.config.getBoolean("Targeting.affect-npcs");// 441
   }// 442

   public boolean isAutoSave() {
      return this.auto;// 473
   }

   public int getSaveFreq() {
      return this.minutes * 60 * 20;// 482
   }

   public boolean isUseSql() {
      return this.useSql;// 491
   }

   public String getSQLHost() {
      return this.sqlHost;// 500
   }

   public String getSQLPort() {
      return this.sqlPort;// 509
   }

   public String getSQLDatabase() {
      return this.sqlDatabase;// 518
   }

   public String getSQLUser() {
      return this.sqlUser;// 527
   }

   public String getSQLPass() {
      return this.sqlPass;// 536
   }

   public int getSqlDelay() {
      return this.sqlDelay;// 543
   }

   private void loadSaveSettings() {
      this.auto = this.config.getBoolean("Saving.auto-save");// 547
      this.minutes = this.config.getInt("Saving.minutes");// 548
      this.useSql = this.config.getBoolean("Saving.sql-database");// 549
      DataSection var1 = this.config.getSection("Saving.sql-details");// 551
      this.sqlDelay = var1.getInt("delay");// 552
      if (this.useSql) {// 554
         this.sqlHost = var1.getString("host");// 555
         this.sqlPort = var1.getString("port");// 556
         this.sqlDatabase = var1.getString("database");// 557
         this.sqlUser = var1.getString("username");// 558
         this.sqlPass = var1.getString("password");// 559
      }
   }// 561

   public boolean isModifyHealth() {
      return this.modifyHealth;// 590
   }

   public int getDefaultHealth() {
      return this.defaultHealth;// 599
   }

   public boolean isShowingAutoSkills() {
      return this.showAutoSkills;// 608
   }

   public boolean isAttributesEnabled() {
      return this.attributesEnabled;// 617
   }

   /** 返回是否允许与 SX-Attribute 建立双向握手。 */
   public boolean isSxAttributeEnabled() {
      return this.sxAttributeEnabled;
   }
   /** 兼容旧配置访问器，控制属性生命自动恢复。 */
   public boolean isAttributesHeal() { return attributesHeal; }
   /** 是否使用 CustomModelData。 */
   public boolean useSkillModelData() { return skillModelData; }
   /** GUI 物品也沿用同一 CustomModelData 开关。 */
   public boolean useGUIModelData() { return skillModelData; }
   /** 是否使用旧耐久字段。 */
   public boolean useOldDurability() { return oldDurability; }
   /** 是否启用非玩家属性。 */
   public boolean isAttributeMobEnabled() { return attributeMobEnabled; }
   /** 远程数据库类型。 */
   public String getSQLType() { return sqlType; }

   public boolean isAttributesDowngrade() {
      return this.attributesDowngrade;// 626
   }

   public double getBaseCritDamage() {
      return this.baseCritDamage;
   }

   public boolean hasLevelUpEffect() {
      return this.getLevelUpSkill() != null;// 636
   }

   public DynamicSkill getLevelUpSkill() {
      Skill var1 = SkillAPI.getSkill(this.levelUpSkill);// 645
      return var1 instanceof DynamicSkill ? (DynamicSkill)var1 : null;// 646
   }

   private void loadClassSettings() {
      this.modifyHealth = this.config.getBoolean("Classes.modify-health");// 650
      this.attributesHeal = this.config.getBoolean("Classes.attributes-heal", true);
      this.attributeMobEnabled = this.config.getBoolean("Attributes.mob-enabled", true);
      this.sxAttributeEnabled = this.config.getBoolean("SX-Attribute.enabled", false);
      this.defaultHealth = this.config.getInt("Classes.classless-hp");// 651
      this.showAutoSkills = this.config.getBoolean("Classes.show-auto-skills");// 652
      this.attributesEnabled = this.config.getBoolean("Classes.attributes-enabled");// 653
      this.attributesDowngrade = this.config.getBoolean("Classes.attributes-downgrade");// 654
      this.levelUpSkill = this.config.getString("Classes.level-up-skill");// 655
   }// 656

   public boolean isManaEnabled() {
      return this.manaEnabled;// 677
   }

   public int getGainFreq() {
      return this.gainFreq;// 686
   }

   private void loadManaSettings() {
      this.manaEnabled = this.config.getBoolean("Mana.enabled");// 690
      this.gainFreq = (int)(this.config.getDouble("Mana.freq") * 20.0);// 691
   }// 692

   public boolean isAllowDowngrade() {
      return this.allowDowngrade;// 720
   }

   public boolean isShowSkillMessages() {
      return this.showSkillMessages;// 729
   }

   public boolean isKnockback() {
      return this.knockback;// 736
   }

   public int getMessageRadius() {
      return this.messageRadius;// 745
   }

   public List<Material> getFilteredBlocks() {
      return this.filteredBlocks;// 754
   }

   private void loadSkillSettings() {
      this.allowDowngrade = this.config.getBoolean("Skills.allow-downgrade");// 758
      this.showSkillMessages = this.config.getBoolean("Skills.show-messages");// 759
      this.messageRadius = this.config.getInt("Skills.message-radius");// 760
      this.knockback = this.config.getBoolean("Skills.knockback-no-damage");// 761
      this.filteredBlocks = new ArrayList<>();// 763

      for (String var3 : this.config.getList("Skills.block-filter")) {// 764 765
         var3 = var3.toUpperCase().replace(' ', '_');// 766
         if (var3.endsWith("*")) {// 767
            var3 = var3.substring(0, var3.length() - 1);// 768

            for (Material var7 : Material.values()) {// 769
               if (var7.name().contains(var3)) {// 770
                  this.filteredBlocks.add(var7);// 771
               }
            }
         } else {
            try {
               Material var4 = Material.valueOf(var3);// 776
               this.filteredBlocks.add(var4);// 777
            } catch (Exception var8) {// 778
               Logger.invalid("Invalid block type \"" + var3 + "\"");// 779
            }
         }
      }
   }// 783

   public boolean isCheckLore() {
      return this.checkLore;// 823
   }

   public boolean isCheckSkillLore() {
      return this.checkSkills;// 830
   }

   public boolean isCheckAttributes() {
      return this.checkAttribs;// 837
   }

   public boolean isDropWeapon() {
      return this.dropWeapon;// 844
   }

   public String getSkillText(String var1) {
      return this.skillPre + var1 + this.skillPost;// 851
   }

   public String getLoreClassText() {
      return this.loreClassText;// 860
   }

   public String getLoreLevelText() {
      return this.loreLevelText;// 869
   }

   public String getLoreExcludeText() {
      return this.loreExcludeText;// 878
   }

   public String getAttrReqText(String var1) {
      return this.attrReqPre + var1 + this.attrReqPost;// 887
   }

   public String getAttrGiveText(String var1) {
      return this.attrPre + var1 + this.attrPost;// 894
   }

   public int[] getSlots() {
      return this.slots;// 901
   }

   private void loadItemSettings() {
      this.checkLore = this.config.getBoolean("Items.lore-requirements");// 905
      this.dropWeapon = this.config.getBoolean("Items.drop-weapon");// 906
      this.checkSkills = this.config.getBoolean("Items.skill-requirements");// 907
      this.checkAttribs = this.config.getBoolean("Items.lore-attributes");// 908
      this.loreClassText = this.config.getString("Items.lore-class-text").toLowerCase();// 909
      this.loreLevelText = this.config.getString("Items.lore-level-text").toLowerCase();// 910
      this.loreExcludeText = this.config.getString("Items.lore-exclude-text").toLowerCase();// 911
      String var1 = this.config.getString("Items.lore-skill-text").toLowerCase();// 913
      int var2 = var1.indexOf(123);// 914
      this.skillPre = var1.substring(0, var2);// 915
      this.skillPost = var1.substring(var2 + 7);// 916
      var1 = this.config.getString("Items.lore-attribute-text").toLowerCase();// 918
      var2 = var1.indexOf(123);// 919
      this.attrReqPre = var1.substring(0, var2);// 920
      this.attrReqPost = var1.substring(var2 + 6);// 921
      var1 = this.config.getString("Items.attribute-text").toLowerCase();// 923
      var2 = var1.indexOf(123);// 924
      this.attrPre = var1.substring(0, var2);// 925
      this.attrPost = var1.substring(var2 + 6);// 926
      List var3 = this.config.getList("Items.slots");// 928
      if (!VersionManager.isVersionAtLeast(VersionManager.V1_9_0)) {// 929
         var3.remove("40");
      }

      this.slots = new int[var3.size()];// 930

      for (int var4 = 0; var4 < this.slots.length; var4++) {// 931
         this.slots[var4] = NumberParser.parseInt((String)var3.get(var4));
      }
   }// 932

   public boolean isOldHealth() {
      return this.oldHealth;// 985
   }

   public boolean isForceScaling() {
      return this.forceScaling;// 992
   }

   public String getLevelBar() {
      return this.levelBar;// 1001
   }

   public String getFoodBar() {
      return this.foodBar;// 1010
   }

   public boolean isUseActionBar() {
      return this.useActionBar;// 1019
   }

   public String getActionText() {
      return this.actionText;// 1028
   }

   public boolean isShowScoreboard() {
      return this.showScoreboard;// 1037
   }

   public boolean isShowClassName() {
      return this.showClassName;// 1047
   }

   public boolean isShowClassLevel() {
      return this.showClassLevel;// 1057
   }

   public boolean isShowBinds() {
      return this.showBinds;// 1061
   }

   public String getBindText() {
      return this.bindText;// 1065
   }

   public String getLevelText() {
      return this.levelText;// 1072
   }

   public boolean useTitle(TitleType var1) {
      return this.useTitle && var1 != null && this.titleMessages.contains(var1.name().toLowerCase());// 1084
   }

   public int getTitleDuration() {
      return this.titleDuration;// 1091
   }

   public int getTitleFadeIn() {
      return this.titleFadeIn;// 1098
   }

   public int getTitleFadeOut() {
      return this.titleFadeOut;// 1105
   }

   private void loadGUISettings() {
      this.oldHealth = this.config.getBoolean("GUI.old-health-bar");// 1109
      this.forceScaling = this.config.getBoolean("GUI.force-scaling");// 1110
      this.levelBar = this.config.getString("GUI.level-bar");// 1111
      this.levelText = TextFormatter.colorString(this.config.getString("GUI.class-level-text", "Level"));// 1112
      this.foodBar = this.config.getString("GUI.food-bar");// 1113
      this.useActionBar = this.config.getBoolean("GUI.use-action-bar");// 1114
      this.actionText = this.config.getString("GUI.action-bar-text");// 1115
      this.showScoreboard = this.config.getBoolean("GUI.scoreboard-enabled");// 1116
      this.showClassName = this.config.getBoolean("GUI.show-class-name");// 1117
      this.showClassLevel = this.config.getBoolean("GUI.show-class-level");// 1118
      this.showBinds = this.config.getBoolean("GUI.show-binds");// 1119
      this.bindText = this.config.getString("GUI.show-bind-text");// 1120
      this.useTitle = this.config.getBoolean("GUI.title-enabled");// 1121
      this.titleDuration = (int)(20.0F * this.config.getFloat("GUI.title-duration"));// 1122
      this.titleFadeIn = (int)(20.0F * this.config.getFloat("GUI.title-fade-in"));// 1123
      this.titleFadeOut = (int)(20.0F * this.config.getFloat("GUI.title-fade-out"));// 1124
      this.titleMessages = this.config.getList("GUI.title-messages");// 1125
   }// 1126

   public boolean isCastEnabled() {
      return this.castEnabled;// 1158
   }

   public boolean isUsingBars() {
      return this.castEnabled && this.castBars && !this.combatEnabled;// 1165
   }

   public boolean isUsingWand() {
      return this.castEnabled && !this.castBars && !this.combatEnabled;// 1169
   }

   public boolean isUsingCombat() {
      return this.castEnabled && this.combatEnabled;// 1173
   }

   public int getCastSlot() {
      return this.castSlot;// 1180
   }

   public long getCastCooldown() {
      return this.castCooldown;// 1187
   }

   public ItemStack getCastItem() {
      return this.castItem;// 1194
   }

   public ItemStack getHoverItem() {
      return this.hoverItem;// 1198
   }

   public ItemStack getInstantItem() {
      return this.instantItem;// 1202
   }

   private void loadCastSettings() {
      this.castEnabled = this.config.getBoolean("Casting.enabled");// 1206
      this.castBars = this.config.getBoolean("Casting.bars");// 1207
      this.combatEnabled = this.config.getBoolean("Casting.combat");// 1208
      this.castSlot = this.config.getInt("Casting.slot") - 1;// 1209
      this.castCooldown = (long)(this.config.getDouble("Casting.cooldown") * 1000.0);// 1210
      this.castItem = GUITool.parseItem(this.config.getSection("Casting.item"));// 1211
      this.hoverItem = GUITool.parseItem(this.config.getSection("Casting.hover-item"));// 1212
      this.instantItem = GUITool.parseItem(this.config.getSection("Casting.instant-item"));// 1213
      this.castEnabled = this.castEnabled && this.castItem != null;// 1214
      IndicatorSettings.load(this.config.getSection("Casting.cast-indicator"));// 1215
   }// 1216

   public boolean isCombosEnabled() {
      return this.combosEnabled;// 1245
   }

   public boolean isCustomCombosAllowed() {
      return this.customCombos;// 1254
   }

   public boolean shouldAutoAssignCombos() {
      return this.autoAssignCombos;// 1258
   }

   public boolean[] getEnabledClicks() {
      return this.clicks;// 1265
   }

   public int getComboSize() {
      return this.comboSize;// 1274
   }

   public int getClickTime() {
      return this.clickTime;// 1283
   }

   private void loadComboSettings() {
      this.combosEnabled = this.config.getBoolean("Click Combos.enabled");// 1287
      this.customCombos = this.combosEnabled && this.config.getBoolean("Click Combos.allow-custom");// 1288
      this.autoAssignCombos = this.combosEnabled && this.config.getBoolean("Click Combos.auto-assign", true);// 1289
      this.comboSize = this.config.getInt("Click Combos.combo-size");// 1290
      this.clickTime = (int)(1000.0 * this.config.getDouble("Click Combos.click-time"));// 1291
      this.clicks = new boolean[Click.values().length + 1];// 1293

      for (int var1 = 1; var1 <= Click.values().length; var1++) {// 1294
         String var2 = "Click Combos.use-click-" + Click.getById(var1).name().toLowerCase().replace('_', '-');// 1295
         this.clicks[var1] = this.config.getBoolean(var2);// 1296
      }

      if (this.clicks[Click.RIGHT_SHIFT.getId()] || this.clicks[Click.LEFT_SHIFT.getId()]) {// 1298
         this.clicks[Click.SHIFT.getId()] = false;// 1299
      }
   }// 1301

   public int getRequiredExp(int var1) {
      return this.useCustomExp ? (int)this.expCustom.compute(var1, 0.0) : this.expFormula.calculate(var1);// 1331
   }

   public double getYield(String var1) {
      var1 = var1.toLowerCase();// 1342
      return !this.yields.containsKey(var1) ? 0.0 : this.yields.get(var1);// 1343 1344 1346
   }

   public boolean isUseOrbs() {
      return this.useOrbs;// 1357
   }

   public boolean isBlockSpawner() {
      return this.blockSpawner;// 1367
   }

   public boolean isBlockEgg() {
      return this.blockEgg;// 1377
   }

   public boolean isBlockCreative() {
      return this.blockCreative;// 1387
   }

   public boolean isShowExpMessages() {
      return this.showExpMessages;// 1397
   }

   public boolean isShowLevelMessages() {
      return this.showLevelMessages;// 1407
   }

   public boolean isShowLossMessages() {
      return this.showLossMessages;// 1417
   }

   public boolean shouldIgnoreExpLoss(World var1) {
      return this.expLostBlacklist.contains(var1.getName());// 1425
   }

   private void loadExpSettings() {
      this.useOrbs = this.config.getBoolean("Experience.use-exp-orbs");// 1431
      this.blockSpawner = this.config.getBoolean("Experience.block-mob-spawner");// 1432
      this.blockEgg = this.config.getBoolean("Experience.block-mob-egg");// 1433
      this.blockCreative = this.config.getBoolean("Experience.block-creative");// 1434
      this.showExpMessages = this.config.getBoolean("Experience.exp-message-enabled");// 1435
      this.showLevelMessages = this.config.getBoolean("Experience.level-message-enabled");// 1436
      this.showLossMessages = this.config.getBoolean("Experience.lose-exp-message");// 1437
      this.expLostBlacklist = new HashSet<>(this.config.getList("Experience.lose-exp-blacklist"));// 1438
      DataSection var1 = this.config.getSection("Experience.formula");// 1440
      int var2 = var1.getInt("x");// 1441
      int var3 = var1.getInt("y");// 1442
      int var4 = var1.getInt("z");// 1443
      this.expFormula = new ExpFormula(var2, var3, var4);// 1444
      this.expCustom = new Formula(this.config.getString("Experience.custom-formula"), new CustomValue("lvl"));// 1446
      this.useCustomExp = this.config.getBoolean("Experience.use-custom") && this.expCustom.isValid();// 1447
      DataSection var5 = this.config.getSection("Experience.yields");// 1449
      this.yields.clear();// 1450

      for (String var7 : var5.keys()) {// 1451
         this.yields.put(var7, var5.getDouble(var7));// 1452
      }
   }// 1454

   public boolean isSkillBarEnabled() {
      return this.skillBarEnabled;// 1474
   }

   public boolean isSkillBarCooldowns() {
      return this.skillBarCooldowns;// 1483
   }

   public ItemStack getUnassigned() {
      return this.unassigned;// 1492
   }

   public boolean[] getDefaultBarLayout() {
      return this.defaultBarLayout;// 1501
   }

   public boolean isArmorAutoBindEnabled() {
      return this.armorAutoBindEnabled;
   }

   public String getArmorDetectPre() {
      return this.armorDetectPre;
   }

   public String getArmorDetectPost() {
      return this.armorDetectPost;
   }

   public int getArmorSlot(String type) {
      switch (type) {
         case "helmet": return this.armorSlotHelmet;
         case "chestplate": return this.armorSlotChestplate;
         case "leggings": return this.armorSlotLeggings;
         case "boots": return this.armorSlotBoots;
         case "offhand": return this.armorSlotOffhand;
         case "mainhand": return this.armorSlotMainhand;
         default: return -1;
      }
   }

   public String getOffhandWhitelistLore() {
      return this.offhandWhitelistLore;
   }

   public boolean[] getLockedSlots() {
      return this.lockedSlots;// 1510
   }

   private void loadSkillBarSettings() {
      DataSection var1 = this.config.getSection("Skill Bar");// 1514
      this.skillBarEnabled = var1.getBoolean("enabled", false) && !this.castEnabled;// 1515
      this.skillBarCooldowns = var1.getBoolean("show-cooldown", true);// 1516
      DataSection var2 = var1.getSection("empty-icon");// 1518
      Material var3 = Material.matchMaterial(var2.getString("material", "PUMPKIN_SEEDS"));// 1519
      if (var3 == null) {// 1520
         var3 = Material.PUMPKIN_SEEDS;
      }

      this.unassigned = new ItemStack(var3);// 1521
      int var4 = var2.getInt("data", 0);// 1523
      this.unassigned.setDurability((short)var4);// 1524
      this.unassigned.setData(new MaterialData(var3, (byte)var4));// 1525
      ItemMeta var5 = this.unassigned.getItemMeta();// 1527
      if (var2.isList("text")) {// 1528
         List var6 = TextFormatter.colorStringList(var2.getList("text"));// 1529
         var5.setDisplayName((String)var6.remove(0));// 1530
         var5.setLore(var6);// 1531
      } else {
         var5.setDisplayName(TextFormatter.colorString(var2.getString("text", "&7Unassigned")));// 1532
      }

      this.unassigned.setItemMeta(var5);// 1533
      DataSection var10 = var1.getSection("layout");// 1535
      int var7 = 0;// 1536

      for (int var8 = 0; var8 < 9; var8++) {// 1537
         DataSection var9 = var10.getSection(var8 + 1 + "");// 1538
         this.defaultBarLayout[var8] = var9.getBoolean("skill", var8 <= 5);// 1539
         this.lockedSlots[var8] = var9.getBoolean("locked", false);// 1540
         if (this.isUsingCombat() && var8 == this.castSlot) {// 1541
            this.lockedSlots[var8] = true;// 1542
            this.defaultBarLayout[var8] = false;// 1543
         }

         if (this.defaultBarLayout[var8]) {// 1545
            var7++;// 1546
         }
      }

      if (var7 == 9) {// 1549
         Logger.invalid("Invalid Skill Bar Setup - Cannot have all 9 skill slots!");// 1550
         Logger.invalid("  -> Setting last slot to be a weapon slot");// 1551
         this.defaultBarLayout[8] = false;// 1552
      }

      // 防具自动装配
      DataSection armorBind = var1.getSection("armor-auto-bind");
      if (armorBind != null) {
         this.armorAutoBindEnabled = armorBind.getBoolean("enabled", false);
         String detectText = armorBind.getString("detect-text", "附带技能: {skill}");
         int idx = detectText.indexOf("{skill}");
         if (idx >= 0) {
            this.armorDetectPre = detectText.substring(0, idx);
            this.armorDetectPost = detectText.substring(idx + 7);
         } else {
            this.armorDetectPre = detectText;
            this.armorDetectPost = "";
         }
         DataSection mapping = armorBind.getSection("slot-mapping");
         if (mapping != null) {
            this.armorSlotHelmet = mapping.getInt("helmet", 1);
            this.armorSlotChestplate = mapping.getInt("chestplate", 2);
            this.armorSlotLeggings = mapping.getInt("leggings", 3);
            this.armorSlotBoots = mapping.getInt("boots", 4);
            this.armorSlotOffhand = mapping.getInt("offhand", 5);
            this.armorSlotMainhand = mapping.getInt("mainhand", 6);
         }
         this.offhandWhitelistLore = armorBind.getString("offhand-whitelist-lore", "");
         if ("NONE".equalsIgnoreCase(this.offhandWhitelistLore)) {
            this.offhandWhitelistLore = "";
         }
         // 加载套装效果（从独立文件 set-bonus.yml 读取，使用 Bukkit YAML 避免 MCCore 解析问题）
         this.setBonuses.clear();
         loadSetBonusFromFile();
      }
   }// 1554

   /**
    * Load set-bonus config from plugins/SkillAPI/set-bonus.yml using Bukkit YAML
    */
   private void loadSetBonusFromFile() {
      try {
         org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("SkillAPI");
         java.io.File file = new java.io.File(plugin.getDataFolder(), "set-bonus.yml");
         // 文件已存在 → 一字不动，直接读取（保护用户已修改的内容）
         if (!file.exists()) {
            // 文件不存在 → 优先从 jar 内嵌资源拷贝带注释完整示例
            // 失败再降级写最简 stub，保证插件首启不会崩
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            java.io.InputStream in = null;
            try {
               in = plugin.getResource("set-bonus.yml");
               if (in != null) {
                  java.nio.file.Files.copy(in, file.toPath());
               } else {
                  file.createNewFile();
                  org.bukkit.configuration.file.YamlConfiguration def =
                        new org.bukkit.configuration.file.YamlConfiguration();
                  org.bukkit.configuration.ConfigurationSection example = def.createSection("example_set");
                  example.set("detect-lore", "NONE");
                  example.set("bonuses.count-2", java.util.Arrays.asList("skill_name"));
                  def.save(file);
               }
            } finally {
               if (in != null) try { in.close(); } catch (java.io.IOException ignored) {}
            }
         }
         org.bukkit.configuration.file.YamlConfiguration yaml =
               org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
         for (String setName : yaml.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection sec = yaml.getConfigurationSection(setName);
            if (sec == null) continue;
            String detectLore = sec.getString("detect-lore", "");
            if (detectLore.isEmpty() || "NONE".equalsIgnoreCase(detectLore)) continue;
            SetBonusEntry entry = new SetBonusEntry(detectLore);
            org.bukkit.configuration.ConfigurationSection bonusSec = sec.getConfigurationSection("bonuses");
            if (bonusSec != null) {
               for (String countKey : bonusSec.getKeys(false)) {
                  try {
                     String numStr = countKey.startsWith("count-") ? countKey.substring(6) : countKey;
                     int count = Integer.parseInt(numStr);
                     java.util.List<String> skills = bonusSec.getStringList(countKey);
                     if (skills != null && !skills.isEmpty()) {
                        entry.bonuses.put(count, skills);
                     }
                  } catch (NumberFormatException ignore) {}
               }
            }
            // 新增：加载属性加成（attribute-bonuses.count-N.属性名 = 数值）
            //   count-2:
            //     health: 5
            //     physical-damage: 3
            //   count-4:
            //     mana: 10
            org.bukkit.configuration.ConfigurationSection attrBonusSec = sec.getConfigurationSection("attribute-bonuses");
            if (attrBonusSec != null) {
               for (String countKey : attrBonusSec.getKeys(false)) {
                  try {
                     String numStr = countKey.startsWith("count-") ? countKey.substring(6) : countKey;
                     int count = Integer.parseInt(numStr);
                     org.bukkit.configuration.ConfigurationSection attrSec = attrBonusSec.getConfigurationSection(countKey);
                     if (attrSec != null) {
                        java.util.Map<String, Integer> attrs = new java.util.HashMap<>();
                        for (String attrKey : attrSec.getKeys(false)) {
                           int val = attrSec.getInt(attrKey, 0);
                           if (val != 0) attrs.put(attrKey.toLowerCase(), val);
                        }
                        if (!attrs.isEmpty()) {
                           entry.attributeBonuses.put(count, attrs);
                        }
                     }
                  } catch (NumberFormatException ignore) {}
               }
            }
            if (!entry.bonuses.isEmpty() || !entry.attributeBonuses.isEmpty()) {
               this.setBonuses.put(setName, entry);
            }
         }
      } catch (Exception e) {
         org.bukkit.Bukkit.getLogger().warning("[SkillAPI] Failed to load set-bonus.yml: " + e.getMessage());
      }
   }

   private void loadLoggingSettings() {
      Logger.loadLevels(this.config.getSection("Logging"));// 1563
   }// 1564

   public boolean isWorldEnabled(World var1) {
      return this.isWorldEnabled(var1.getName());// 1589
   }

   public boolean isWorldEnabled(String var1) {
      return !this.worldEnabled || this.worldEnableList == this.worlds.contains(var1);// 1601
   }

   private void loadWorldSettings() {
      this.worldEnabled = this.config.getBoolean("Worlds.enable");// 1605
      this.worldEnableList = this.config.getBoolean("Worlds.use-as-enabling");// 1606
      this.worlds = this.config.getList("Worlds.worlds");// 1607
   }// 1608

   public boolean areSkillsDisabledForRegion(String var1) {
      return this.skillDisabledRegions.contains(var1);// 1623
   }

   public boolean isExpDisabledForRegion(String var1) {
      return this.expDisabledRegions.contains(var1);// 1627
   }

   private void loadWorldGuardSettings() {
      CommentedConfig var1 = new CommentedConfig(this.plugin, "worldGuard");// 1631
      var1.checkDefaults();// 1632
      var1.trim();// 1633
      var1.save();// 1634
      DataSection var2 = var1.getConfig();// 1635
      this.skillDisabledRegions = ImmutableSet.copyOf(var2.getList("disable-skills"));// 1637
      this.expDisabledRegions = ImmutableSet.copyOf(var2.getList("disable-exp"));// 1638
   }// 1639

   private void loadCombatSettings() {
      this.baseCritDamage = this.config.getDouble("Combat.base-crit-damage", 1.75);
      this.critRateBase = this.config.getDouble("Combat.base-values.crit-rate", 100.0);
      this.critDamageBase = this.config.getDouble("Combat.base-values.crit-damage", 100.0);
      this.dodgeBase = this.config.getDouble("Combat.base-values.dodge", 100.0);
      this.lifestealBase = this.config.getDouble("Combat.base-values.lifesteal", 100.0);
      this.armorPenBase = this.config.getDouble("Combat.base-values.armor-penetration", 100.0);

      this.combatMsgEnabled = this.config.getBoolean("Combat.message.enabled", true);
      this.msgDealDamage = this.config.getString("Combat.message.deal-damage", "&8>> &7你对 &f{target} &7造成了 &c{damage} &7点伤害");
      this.msgDealSkillDamage = this.config.getString("Combat.message.deal-skill-damage", "&8>> &7你使用 &b{skill} &7对 &f{target} &7造成了 &c{damage} &7点伤害");
      this.msgTakeDamage = this.config.getString("Combat.message.take-damage", "&8<< &f{attacker} &7对你造成了 &c{damage} &7点伤害");
      this.msgTakeSkillDamage = this.config.getString("Combat.message.take-skill-damage", "&8<< &f{attacker} &7使用 &b{skill} &7对你造成了 &c{damage} &7点伤害");
      this.msgCritTag = this.config.getString("Combat.message.crit-tag", " &4*暴击!");
      this.msgDodgeAttacker = this.config.getString("Combat.message.dodge-attacker", "&8- &f{target} &7闪避了你的攻击");
      this.msgDodgeTarget = this.config.getString("Combat.message.dodge-target", "&8- &a你闪避了 &f{attacker} &a的攻击");
      this.msgLifestealTag = this.config.getString("Combat.message.lifesteal-tag", " &a+{heal}");
      this.msgStunApply = this.config.getString("Combat.message.stun-apply", "&8# &7你对 &f{target} &7施加了 &e眩晕 &8| &f{duration}秒");
      this.msgStunTarget = this.config.getString("Combat.message.stun-target", "&8# &f{attacker} &7对你施加了 &e眩晕 &8| &f{duration}秒");
      this.msgRootApply = this.config.getString("Combat.message.root-apply", "&8# &7你对 &f{target} &7施加了 &e禁锢 &8| &f{duration}秒");
      this.msgRootTarget = this.config.getString("Combat.message.root-target", "&8# &f{attacker} &7对你施加了 &e禁锢 &8| &f{duration}秒");
      this.msgSilenceApply = this.config.getString("Combat.message.silence-apply", "&8# &7你对 &f{target} &7施加了 &e沉默 &8| &f{duration}秒");
      this.msgSilenceTarget = this.config.getString("Combat.message.silence-target", "&8# &f{attacker} &7对你施加了 &e沉默 &8| &f{duration}秒");
      this.msgInvisEnter = this.config.getString("Combat.message.invis-enter", "&8* &7你进入了 &f隐身 &8| &7持续 &f{duration}秒");
      this.msgInvisEnd = this.config.getString("Combat.message.invis-end", "&8* &7你的 &f隐身 &7已解除");
      this.msgMortalWoundApply = this.config.getString("Combat.message.mortal-wound-apply", "&8# &7你对 &f{target} &7施加了 &c减疗 &8| &f{duration}秒");
      this.msgMortalWoundTarget = this.config.getString("Combat.message.mortal-wound-target", "&8# &f{attacker} &7对你施加了 &c减疗 &8| &f{duration}秒");
      this.msgMortalWoundExpire = this.config.getString("Combat.message.mortal-wound-expire", "&8* &7你的 &c减疗 &7已解除");
      this.msgNaturalDamage = this.config.getString("Combat.message.natural-damage", "&8<< &7你受到了 &e{type} &7伤害 &c{damage}");
      this.msgHealDeal = this.config.getString("Combat.message.heal-deal", "&8>> &7你对 &f{target} &7恢复了 &a{amount} &7点生命值");
      this.msgHealReceive = this.config.getString("Combat.message.heal-receive", "&8<< &f{healer} &7为你恢复了 &a{amount} &7点生命值");

      // 仇恨系统配置
      boolean threatEnabled = this.config.getBoolean("Combat.threat.enabled", true);
      double healRange = this.config.getDouble("Combat.threat.heal-range", 25.0);
      double healMultiplier = this.config.getDouble("Combat.threat.heal-multiplier", 0.5);
      long combatTimeout = (long) (this.config.getDouble("Combat.threat.combat-timeout", 5.0) * 1000);
      com.sucy.skill.combat.threat.ThreatManager.configure(threatEnabled, healRange, healMultiplier, combatTimeout);

      // 越级作战曲线
      this.levelGapDamageEnabled = this.config.getBoolean("Combat.level-gap.damage.enabled", true);
      this.levelGapExpEnabled = this.config.getBoolean("Combat.level-gap.exp.enabled", true);
      this.levelGapDamageCurve = loadCurve("Combat.level-gap.damage.curve");
      this.levelGapExpCurve = loadCurve("Combat.level-gap.exp.curve");
   }

   private TreeMap<Integer, Double> loadCurve(String path) {
      TreeMap<Integer, Double> curve = new TreeMap<>();
      DataSection section = this.config.getSection(path);
      if (section == null) return curve;
      for (String key : section.keys()) {
         try {
            int diff = Integer.parseInt(key);
            double mult = parseCurveValue(section, key);
            curve.put(diff, mult);
         } catch (NumberFormatException ignored) {}
      }
      return curve;
   }

   /**
    * 兼容旧版本残留的带内联注释的字符串值（如 "0.01   # 高 5 级以上：1%"）：
    * 取 '#' 前的部分再 parseDouble；解析失败返回 1.0（不削减）。
    */
   private static double parseCurveValue(DataSection section, String key) {
      String raw = section.getString(key, "1.0");
      if (raw == null) return 1.0;
      int hash = raw.indexOf('#');
      if (hash >= 0) raw = raw.substring(0, hash);
      raw = raw.trim();
      if (raw.isEmpty()) return 1.0;
      try {
         return Double.parseDouble(raw);
      } catch (NumberFormatException ignored) {
         return 1.0;
      }
   }

   public double getCritRateBase() { return this.critRateBase; }
   public double getCritDamageBase() { return this.critDamageBase; }
   public double getDodgeBase() { return this.dodgeBase; }
   public double getLifestealBase() { return this.lifestealBase; }
   public double getArmorPenBase() { return this.armorPenBase; }

   // 战斗消息
   private boolean combatMsgEnabled;
   private String msgDealDamage, msgDealSkillDamage, msgTakeDamage, msgTakeSkillDamage;
   private String msgCritTag, msgDodgeAttacker, msgDodgeTarget, msgLifestealTag;
   private String msgStunApply, msgStunTarget, msgRootApply, msgRootTarget, msgSilenceApply, msgSilenceTarget;
   private String msgInvisEnter, msgInvisEnd;
   private String msgMortalWoundApply, msgMortalWoundTarget, msgMortalWoundExpire;
   private String msgNaturalDamage;
   private String msgHealDeal, msgHealReceive;

   public boolean isCombatMsgEnabled() { return this.combatMsgEnabled; }
   public String getMsgDealDamage() { return this.msgDealDamage; }
   public String getMsgDealSkillDamage() { return this.msgDealSkillDamage; }
   public String getMsgTakeDamage() { return this.msgTakeDamage; }
   public String getMsgTakeSkillDamage() { return this.msgTakeSkillDamage; }
   public String getMsgCritTag() { return this.msgCritTag; }
   public String getMsgDodgeAttacker() { return this.msgDodgeAttacker; }
   public String getMsgDodgeTarget() { return this.msgDodgeTarget; }
   public String getMsgLifestealTag() { return this.msgLifestealTag; }
   public String getMsgStunApply() { return this.msgStunApply; }
   public String getMsgStunTarget() { return this.msgStunTarget; }
   public String getMsgRootApply() { return this.msgRootApply; }
   public String getMsgRootTarget() { return this.msgRootTarget; }
   public String getMsgSilenceApply() { return this.msgSilenceApply; }
   public String getMsgSilenceTarget() { return this.msgSilenceTarget; }
   public String getMsgInvisEnter() { return this.msgInvisEnter; }
   public String getMsgInvisEnd() { return this.msgInvisEnd; }
   public String getMsgMortalWoundApply() { return this.msgMortalWoundApply; }
   public String getMsgMortalWoundTarget() { return this.msgMortalWoundTarget; }
   public String getMsgMortalWoundExpire() { return this.msgMortalWoundExpire; }
   public String getMsgNaturalDamage() { return this.msgNaturalDamage; }
   public String getMsgHealDeal() { return this.msgHealDeal; }
   public String getMsgHealReceive() { return this.msgHealReceive; }

   public String getCombatAttrName(String key, String defaultName) {
      return this.config.getString("Combat.attributes." + key, defaultName);
   }

   // 越级作战曲线（TreeMap: diff → multiplier；查表用 floorEntry(diff)）
   private boolean levelGapDamageEnabled;
   private boolean levelGapExpEnabled;
   private TreeMap<Integer, Double> levelGapDamageCurve = new TreeMap<>();
   private TreeMap<Integer, Double> levelGapExpCurve = new TreeMap<>();

   public boolean isLevelGapDamageEnabled() { return this.levelGapDamageEnabled; }
   public boolean isLevelGapExpEnabled() { return this.levelGapExpEnabled; }

   /**
    * 查询伤害削减倍率。diff = 怪物等级 - 玩家等级。
    * 返回 curve 中键 <= diff 的最大项对应值；曲线为空或 diff < 首项时返回 1.0（全额）。
    */
   public double getLevelGapDamageMult(int diff) {
      if (!this.levelGapDamageEnabled || this.levelGapDamageCurve.isEmpty()) return 1.0;
      Map.Entry<Integer, Double> entry = this.levelGapDamageCurve.floorEntry(diff);
      return entry == null ? 1.0 : entry.getValue();
   }

   /**
    * 查询经验削减倍率。diff = 玩家等级 - 怪物等级。
    */
   public double getLevelGapExpMult(int diff) {
      if (!this.levelGapExpEnabled || this.levelGapExpCurve.isEmpty()) return 1.0;
      Map.Entry<Integer, Double> entry = this.levelGapExpCurve.floorEntry(diff);
      return entry == null ? 1.0 : entry.getValue();
   }

   /**
    * 获取战斗属性的计算模式
    * @param statKey 属性key（如"lifesteal"）
    * @return "additive" 或 "inverse"（默认inverse）
    */
   public String getCombatCalcMode(String statKey) {
      return this.config.getString("Combat.calc-mode." + statKey, "inverse");
   }
}
