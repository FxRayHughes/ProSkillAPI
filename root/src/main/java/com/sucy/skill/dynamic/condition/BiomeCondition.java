/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.BiomeCondition
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.condition;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;

import java.util.Set;
import java.util.stream.Collectors;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "biome",
        name = "Biome",
        nameZh = "检查生物群系",
        description = "Applies child components when in a specified biome.",
        descriptionZh = "检查目标所在方块的生物群系。勾选的群系名会转大写、空格换成下划线后与目标群系枚举名精确比对；“In Biome”要求命中任意一个勾选项，“Not In Biome”要求都不命中。",
        container = true)
public class BiomeCondition extends ConditionComponent
{
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] Whether or not the target should be in the biome. If checking for in the biome, they must be in any one of the checked biomes. If checking for the opposite, they must not be in any of the checked biomes.",
            tooltipZh = "选“In Biome”要求处于所选群系之一；只有值恰好等于“not in biome”（忽略大小写）才切换为取反判定，其余任何值都按“处于其中”处理。",
            options = {"In Biome", "Not In Biome"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "In Biome")
    private static final String TYPE  = "type";
    @SkillField(
            kind = FieldKind.MultiListValue,
            label = "Biome",
            labelZh = "生物群系",
            tooltip = "[biome] The biomes to check for. The expectation would be any of the selected biomes need to match",
            tooltipZh = "允许的生物群系列表，任一命中即算匹配。名称转大写并把空格换成下划线，须与服务端 Biome 枚举名一致，否则永远不命中。",
            options = {"Alpine", "Alpineconifermountain", "Alpineconifersnow", "Alpineforest", "Alpinem", "Alps", "Alpsedge", "Alpsforest", "Alpsmountain1", "Alpsmountain2", "Alpsmountain3", "Alpsmountainedge", "Alpsmountainedge2", "Alpsmountainhighlands", "Alpsmountainpeak", "Alpsmountainpeak2", "Archipelago", "Archipelagohigh", "Archipelagomid", "Arctic", "Arcticedge", "Autumnal Woods", "Autumnal Woods Hills", "Autumnal Woods M", "Beach Desert", "Beaches", "Birch Forest", "Birch Forest Hills", "Birch Forest Peak", "Birch Meadow", "Birchforesthighlands", "Birchforestmountain", "Birchforestmountainrange", "Birchforestrocky", "Birchforesttropical", "Birchforesttropicalisle", "Birchforesttropicalisle2", "Birchforesttropicalisle3", "Birchgrove", "Birchgroveborder", "Canyon", "Canyonhigh", "Canyonmid", "Caverns", "Caverns", "Caverns M", "Caverns Edge", "Caverns M", "Cliffssmall", "Cliffssmalledge", "Cliffssmallforest", "Cliffssmallforest2", "Cliffssmallmountain", "Cliffssmallmountainedge", "Cliffssmallplateau", "Cliffssmallplateau2", "Cliffssmallplateau3", "Cold Beach", "Cold Plains", "Coldforestfirthicket", "Coldforestfirthicketedge", "Coldtaigaborder", "Deep Ocean", "Desert", "Desert Cliffs", "Desert Cliffs Edge", "Desert Hills", "Desertbaobabs", "Desertcanyon", "Desertcanyonhigh", "Desertcanyonmid", "Desertcliffsoasis", "Desertcliffsoasisedge", "Desertflats", "Desertlowlands", "Desertlowlandsedge", "Desertlowlandsoasis", "Desertlowlandsoasisedge", "Desertmountain", "Desertoasis", "Desertoasisborder", "Desertred", "Desertredconifer", "Desertsmall", "Desertsmallred", "Desertsmallrededge", "Deserttropical", "Deserttropicaledge", "Deserttropicalm", "Deserttropicalplateau", "Dioritecliffs", "Dioritecliffsm", "Dioritecliffspeak", "Dry Plains", "Drylands", "Drylandsedge1", "Drylandsedge2", "Drylandsisle", "Dryplainsm", "Drysavanna", "Drysavannaborder", "Extreme Hills", "Extreme Hills Jungle", "Extreme Hills Jungle Edge", "Extreme Hills With Trees", "Floatingedge", "Floatinghole", "Floatingislands", "Floatingjungle", "Floatingjungleedge", "Floatingjunglehole", "Floatingplains", "Flyingforest", "Forest", "Forest Hills", "Forest M", "Forest Wetland", "Forestbaldcypress", "Forestbaldcypresssmall", "Forestcliffs", "Forestcliffsborder", "Forestcliffslowlands", "Forestcliffsmountain", "Forestcliffsmountainedge", "Forestconifer", "Forestconiferedge", "Forestconifermountain", "Forestconifersnow", "Forestconifersnow2", "Forestdry", "Forestdryedge", "Forestenchanted", "Forestenchantedborder", "Forestenchantedcaves", "Forestenchantedgrove", "Forestender", "Forestendermountain", "Forestendervalley", "Forestfir", "Forestfirmountain", "Forestfirmountainedge1", "Forestfirmountainedge2", "Forestfirthicket", "Forestfirthicketedge", "Forestgiantsprucesmall", "Forestgrandfir", "Forestgrandfirsmall", "Foresthighlands", "Forestlarch", "Forestlarchpeak", "Forestmountain", "Forestmountainrange", "Forestmountainrangeedge", "Forestoaksmall", "Forestpeak", "Forestpinyon", "Forestplateau", "Forestplateauedge", "Forestplateauroofed", "Forestplateauthicket", "Forestpoplar", "Forestsakura", "Forestsakurahills", "Forestwetlandm", "Forestwoodland", "Frozen Forest", "Frozen Forest Edge", "Frozen Ocean", "Frozen River", "Giantmushroomisland", "Glacier", "Glacierm", "Hell", "High Cliffs", "Highcliffsedge", "Highcliffssummit", "Highcliffsthicket", "Highcliffsthicketmountain", "Highlands", "Highlandspeaks", "Ice Flats", "Ice Mountains", "Icewall", "Icymountainpeak1", "Icymountainpeak2", "Icymountains", "Isleriver", "Jungle", "Jungle Edge", "Jungle Hills", "Jungleancient", "Jungleancientmountain", "Jungleancientmountaintall", "Jungleancientmushrooms", "Jungleancientrocks", "Junglecliffs", "Junglecliffscanyon", "Junglecliffscanyonhigh", "Junglecliffscanyonmid", "Junglecliffsedge", "Junglecliffsvolcano", "Jungleflat", "Junglehighlands", "Junglelonepeak", "Junglelowlands", "Junglemonument", "Junglemountainpeak", "Junglemountainrange", "Jungleplateau", "Jungleplateaulow", "Jungleswampy", "Mangrove Forest", "Marsh", "Marshborder", "Mega Taiga Mountains", "Mesa", "Mesa Bryce Edge", "Mesa Bryce Forest", "Mesa Bryce Rocks", "Mesa Clear Rock", "Mesa Desert", "Mesa River", "Mesa Rock", "Mesa Sandstone", "Mesa Sandstone Edge", "Mesa Sandstone M", "Mesa Sandstone Peak", "Mesa Small", "Mesa Small Edge", "Mesa Small M", "Mesa Verde", "Mesa Verde Edge", "Mesa Verde Forest", "Mesa Verde Mountain", "Mesacanyon", "Mesapaintedcanyon", "Mesapeak", "Mesaverdecanyon", "Mesaverdecanyonhigh", "Mesaverdecanyonmid", "Mesawhite", "Mesawhiteedge1", "Mesawhiteedge2", "Mesawhitelow", "Mesawhitepeak", "Monument", "Monumentm", "Monumentmountains", "Monumentmountainspeak1", "Monumentmountainspeak2", "Monumentsmall", "Monumentvalley", "Monumentvalleyedge", "Monumentvalleyfir", "Mountain Peak", "Mountain Peak2", "Mountain Peak3", "Mountain Range", "Mountainsteps", "Mountainstepsedge1", "Mountainstepsedge2", "Mountainstepsglacier", "Mountainstepsglacieredge1", "Mountainstepsglacieredge2", "Mushroom Island", "Mushroom Island Shore", "Mutated Birch Forest", "Mutated Birch Forest Hills", "Mutated Desert", "Mutated Extreme Hills", "Mutated Extreme Hills With Trees", "Mutated Forest", "Mutated Ice Flats", "Mutated Jungle", "Mutated Jungle Edge", "Mutated Mesa", "Mutated Mesa Clear Rock", "Mutated Mesa Rock", "Mutated Plains", "Mutated Redwood Taiga", "Mutated Redwood Taiga Hills", "Mutated Roofed Forest", "Mutated Savanna", "Mutated Savanna Rock", "Mutated Swampland", "Mutated Taiga", "Mutated Taiga Cold", "Ocean", "Ocean Island", "Oceancoral", "Oceandesertisland", "Oceanforesthighlands", "Oceanforesthighlandsborder", "Oceanforestisland", "Oceanislanddesert", "Oceanislandforest", "Oceanislandroofedforest", "Oceanplateau", "Oceanplateaulow", "Oceanplateaum", "Oceanrocks", "Oceanrocksdiorite", "Oceanrocksm", "Oceanrockyisland", "Oceanrockyislandpeak", "Plains", "Plainscliff", "Plainscliffm", "Pumpkin Plains", "Rainforest", "Rainforest M", "Rainforest Plateau", "Rainforest Plateau Low", "Rainforest Plateau M", "Rainforestbog", "Rainforesttropical", "Rainforesttropicaledge", "Rainforesttropicalmountain", "Rainforesttropicalmountainpeak", "Redmountain", "Redwood Forest", "Redwood Taiga", "Redwood Taiga Hills", "Redwoodsmall", "River", "Riveralpine", "Riverautumnal", "Riverbirchforest", "Riverclay", "Riverdesert", "Riverdiorite", "Riverenchantedforest", "Riverender", "Riverforest", "Riverforestconifer", "Riverhaunted", "Riverrocky", "Riverroofedforest", "Riversandy", "Riversavanna", "Riverspooky", "Riversteppe", "Riverswampland", "Rivertaiga", "Rivertropical", "Rivertundra", "Riveruplands", "Riververde", "Riverwetland", "Rocks", "Rockyhills", "Rockyhillscliff", "Rockyhillsedge", "Rockymountainpeak1", "Rockymountainpeak2", "Rockymountains", "Rockymountainspeakedge", "Rockyplains", "Rockyplainsm", "Rockyplainssmall", "Roofed Forest", "Roofed Fungi Forest", "Roofedforestenchanted", "Roofedforestflat", "Savanna", "Savanna Rock", "Savannaedge", "Savannahills", "Savannasmall", "Sky", "Smaller Extreme Hills", "Snowylonepeak", "Snowymesa", "Snowymesaedge", "Snowymesaforest", "Snowymesarocks", "Snowymountains", "Snowyplains", "Snowyplainsedge", "Snowyplainsm", "Spookygrove", "Spookygroveedge", "Steppe", "Steppeheathland", "Steppehill", "Stepperocks", "Stone Beach", "Swampland", "Swampland Bushes", "Swamplandborder", "Swamplandlarge", "Swamplandruins", "Swamplandsmall", "Swamplandspooky", "Taiga", "Taiga Cold", "Taiga Cold Hills", "Taiga Hills", "Taigahighlands", "Taigahighlandspeak", "Taigaplateau", "Taigaplateauedge", "Taigasmall", "Temperatehills", "Temperatehillsborder", "Temperatehillsforest", "Temperatelonepeak", "Temperatelowlands", "Temperatelowlandsedge", "Temperateplains", "Temperateplainsautumnal", "Temperateplainscypresshill", "Temperateplainsdarkmeadow", "Temperateplainsheathland", "Temperateplainsm", "Temperateplainsmeadow", "Temperateplainsmeadowborder", "Temperateplainsmeadowm", "Temperateplainsoakgrove", "Temperateplainsoakgroveborder", "Tropical Savanna", "Tropical Savanna Canyon", "Tropical Savanna Canyon High", "Tropical Savanna Canyon Mid", "Tropical Savanna Edge", "Tropical Savanna Summit", "Tropicalmountain", "Tropicalpillarlarge", "Tropicalpillars", "Tropicalpillarsmall", "Tundra", "Tundracliffs", "Tundraedge", "Tundraforest", "Uplands", "Uplandsblackspruce", "Uplandsedge", "Void", "Webbed Forest", "Webbed Forest Grove", "Webbed Hills", "Webbed Hills Edge", "Woodlandborder"},
            optionsZh = {"生物群系1", "生物群系2", "生物群系3", "生物群系4", "生物群系5", "生物群系6", "生物群系7", "生物群系8", "生物群系9", "生物群系10", "生物群系11", "生物群系12", "生物群系13", "生物群系14", "生物群系15", "生物群系16", "生物群系17", "生物群系18", "生物群系19", "生物群系20", "生物群系21", "生物群系22", "生物群系23", "生物群系24", "生物群系25", "生物群系26", "桦木", "桦木", "桦木", "桦木", "生物群系31", "生物群系32", "生物群系33", "生物群系34", "生物群系35", "生物群系36", "生物群系37", "生物群系38", "生物群系39", "生物群系40", "生物群系41", "生物群系42", "生物群系43", "生物群系44", "生物群系45", "生物群系46", "生物群系47", "生物群系48", "生物群系49", "生物群系50", "生物群系51", "生物群系52", "生物群系53", "生物群系54", "生物群系55", "生物群系56", "生物群系57", "生物群系58", "生物群系59", "生物群系60", "生物群系61", "生物群系62", "生物群系63", "生物群系64", "生物群系65", "生物群系66", "生物群系67", "生物群系68", "生物群系69", "生物群系70", "生物群系71", "生物群系72", "生物群系73", "生物群系74", "生物群系75", "生物群系76", "生物群系77", "生物群系78", "生物群系79", "生物群系80", "生物群系81", "生物群系82", "生物群系83", "生物群系84", "生物群系85", "生物群系86", "生物群系87", "生物群系88", "生物群系89", "生物群系90", "生物群系91", "生物群系92", "生物群系93", "生物群系94", "生物群系95", "生物群系96", "生物群系97", "生物群系98", "生物群系99", "生物群系100", "生物群系101", "生物群系102", "丛林木", "丛林木", "生物群系105", "生物群系106", "生物群系107", "生物群系108", "生物群系109", "生物群系110", "生物群系111", "生物群系112", "生物群系113", "生物群系114", "生物群系115", "生物群系116", "生物群系117", "生物群系118", "生物群系119", "生物群系120", "生物群系121", "生物群系122", "生物群系123", "生物群系124", "生物群系125", "生物群系126", "生物群系127", "生物群系128", "生物群系129", "生物群系130", "生物群系131", "生物群系132", "生物群系133", "生物群系134", "生物群系135", "生物群系136", "生物群系137", "生物群系138", "生物群系139", "生物群系140", "生物群系141", "生物群系142", "生物群系143", "生物群系144", "生物群系145", "生物群系146", "生物群系147", "生物群系148", "生物群系149", "生物群系150", "生物群系151", "生物群系152", "生物群系153", "生物群系154", "生物群系155", "生物群系156", "生物群系157", "生物群系158", "生物群系159", "生物群系160", "生物群系161", "生物群系162", "生物群系163", "生物群系164", "生物群系165", "生物群系166", "生物群系167", "生物群系168", "生物群系169", "生物群系170", "生物群系171", "生物群系172", "生物群系173", "生物群系174", "生物群系175", "生物群系176", "生物群系177", "生物群系178", "生物群系179", "生物群系180", "生物群系181", "生物群系182", "生物群系183", "生物群系184", "生物群系185", "生物群系186", "生物群系187", "丛林木", "丛林木", "丛林木", "生物群系191", "生物群系192", "生物群系193", "生物群系194", "生物群系195", "生物群系196", "生物群系197", "生物群系198", "生物群系199", "生物群系200", "生物群系201", "生物群系202", "生物群系203", "生物群系204", "生物群系205", "生物群系206", "生物群系207", "生物群系208", "生物群系209", "生物群系210", "生物群系211", "生物群系212", "生物群系213", "生物群系214", "生物群系215", "生物群系216", "生物群系217", "生物群系218", "生物群系219", "生物群系220", "生物群系221", "生物群系222", "生物群系223", "生物群系224", "生物群系225", "生物群系226", "生物群系227", "生物群系228", "生物群系229", "生物群系230", "生物群系231", "生物群系232", "生物群系233", "生物群系234", "生物群系235", "生物群系236", "生物群系237", "生物群系238", "生物群系239", "生物群系240", "生物群系241", "生物群系242", "生物群系243", "生物群系244", "生物群系245", "生物群系246", "生物群系247", "生物群系248", "生物群系249", "生物群系250", "生物群系251", "生物群系252", "生物群系253", "生物群系254", "生物群系255", "生物群系256", "生物群系257", "生物群系258", "生物群系259", "生物群系260", "生物群系261", "生物群系262", "生物群系263", "生物群系264", "生物群系265", "生物群系266", "桦木", "桦木", "生物群系269", "生物群系270", "生物群系271", "生物群系272", "生物群系273", "丛林木", "丛林木", "生物群系276", "生物群系277", "生物群系278", "生物群系279", "生物群系280", "生物群系281", "生物群系282", "生物群系283", "生物群系284", "生物群系285", "生物群系286", "生物群系287", "生物群系288", "生物群系289", "生物群系290", "生物群系291", "生物群系292", "生物群系293", "生物群系294", "生物群系295", "生物群系296", "生物群系297", "生物群系298", "生物群系299", "生物群系300", "生物群系301", "生物群系302", "生物群系303", "生物群系304", "生物群系305", "生物群系306", "生物群系307", "生物群系308", "生物群系309", "生物群系310", "生物群系311", "生物群系312", "生物群系313", "生物群系314", "生物群系315", "生物群系316", "生物群系317", "生物群系318", "生物群系319", "生物群系320", "生物群系321", "生物群系322", "生物群系323", "生物群系324", "生物群系325", "生物群系326", "生物群系327", "生物群系328", "生物群系329", "生物群系330", "生物群系331", "生物群系332", "生物群系333", "生物群系334", "生物群系335", "生物群系336", "生物群系337", "生物群系338", "生物群系339", "生物群系340", "生物群系341", "生物群系342", "生物群系343", "生物群系344", "生物群系345", "生物群系346", "生物群系347", "生物群系348", "生物群系349", "生物群系350", "生物群系351", "生物群系352", "生物群系353", "生物群系354", "生物群系355", "生物群系356", "生物群系357", "生物群系358", "生物群系359", "生物群系360", "生物群系361", "生物群系362", "生物群系363", "生物群系364", "生物群系365", "生物群系366", "生物群系367", "生物群系368", "生物群系369", "生物群系370", "生物群系371", "生物群系372", "生物群系373", "生物群系374", "生物群系375", "生物群系376", "生物群系377", "生物群系378", "生物群系379", "生物群系380", "生物群系381", "生物群系382", "生物群系383", "生物群系384", "生物群系385", "生物群系386", "石头", "生物群系388", "生物群系389", "生物群系390", "生物群系391", "生物群系392", "生物群系393", "生物群系394", "生物群系395", "生物群系396", "生物群系397", "生物群系398", "生物群系399", "生物群系400", "生物群系401", "生物群系402", "生物群系403", "生物群系404", "生物群系405", "生物群系406", "生物群系407", "生物群系408", "生物群系409", "生物群系410", "生物群系411", "生物群系412", "生物群系413", "生物群系414", "生物群系415", "生物群系416", "生物群系417", "生物群系418", "生物群系419", "生物群系420", "生物群系421", "生物群系422", "生物群系423", "生物群系424", "生物群系425", "生物群系426", "生物群系427", "生物群系428", "生物群系429", "生物群系430", "生物群系431", "生物群系432", "生物群系433", "生物群系434", "生物群系435", "生物群系436", "生物群系437", "生物群系438", "生物群系439", "生物群系440", "生物群系441", "生物群系442", "生物群系443"})
    private static final String BIOME = "biome";

    private Set<String> biomes;
    private boolean     requiresIn;

    @Override
    public String getKey() {
        return "biome";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        requiresIn = !settings.getString(TYPE, "in biome").toLowerCase().equals("not in biome");
        biomes = settings.getStringList(BIOME).stream()
                .map(s -> s.toUpperCase().replace(' ', '_'))
                .collect(Collectors.toSet());
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        return biomes.contains(target.getLocation().getBlock().getBiome().name()) == requiresIn;
    }
}
