package cn.blockforge.generated.frostboundthorns;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(GeneratedMod.MOD_ID)
public final class GeneratedMod {
    public static final String MOD_ID = "deltamagic";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<net.minecraft.world.item.enchantment.Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MOD_ID);
    public static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MOD_ID);
    /** 状态效果（目前是「继续前进」buff）。 */
    public static final DeferredRegister<net.minecraft.world.effect.MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
    /** 「继续前进」：1 分钟内连放 3 次冰震术获得，持有期间 TrueIce 伤害翻倍。 */
    public static final RegistryObject<net.minecraft.world.effect.MobEffect> KEEP_GOING =
            MOB_EFFECTS.register("keep_going", KeepGoingEffect::new);
    /** 全局战利品修正器（牙刷附魔书的宝箱 10% 来源）。 */
    public static final DeferredRegister<com.mojang.serialization.Codec<? extends net.minecraftforge.common.loot.IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final RegistryObject<com.mojang.serialization.Codec<ToothbrushBookLootModifier>> TOOTHBRUSH_BOOK_MODIFIER =
            LOOT_MODIFIERS.register("toothbrush_book", () -> ToothbrushBookLootModifier.CODEC);
    /** 钥匙碎片 a/b/c：每个原版宝箱各 15% 独立刷新。 */
    public static final RegistryObject<com.mojang.serialization.Codec<KeyShardLootModifier>> KEY_SHARD_MODIFIER =
            LOOT_MODIFIERS.register("key_shards", () -> KeyShardLootModifier.CODEC);
    /** 方块（目前只有牢房栏）。 */
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    /** 结构类型（1.20.1 的 template 结构必须走 Java StructureType + Codec）。 */
    public static final DeferredRegister<net.minecraft.world.level.levelgen.structure.StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MOD_ID);
    /** 结构方块件类型（存档重载时反序列化 jevil_room 用）。 */
    public static final DeferredRegister<net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, MOD_ID);
    /** 配方序列化器（目前只有「丑魔锭斧头升级」这一种自定义锻造配方）。 */
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
    /** 丑魔锭锻造升级配方的 JSON / 网络格式。 */
    public static final RegistryObject<net.minecraft.world.item.crafting.RecipeSerializer<?>> JEVIL_INGOT_SMITHING_SERIALIZER =
            RECIPE_SERIALIZERS.register("jevil_ingot_smithing", JevilIngotSmithingRecipe.Serializer::new);

    public static final RegistryObject<Item> THORN_RING = ITEMS.register("thorn_ring", ThornRingItem::new);
    public static final RegistryObject<Item> SNOWFLAKE_RING = ITEMS.register("snowflake_ring", SnowflakeRingItem::new);
    public static final RegistryObject<Item> SNOW_BURIAL_BOOK = ITEMS.register("snow_burial_book",
            () -> new EnchantedBookItem(new Item.Properties().stacksTo(1)));
    /** 紧迫一点：宝石碎片，右键使用恢复 32 TP。 */
    public static final RegistryObject<Item> TENSION_BIT = ITEMS.register("tension_bit",
            () -> new TensionItem(32));
    /** 紧迫宝石：完整宝石，右键使用恢复 50 TP。 */
    public static final RegistryObject<Item> TENSION_GEM = ITEMS.register("tension_gem",
            () -> new TensionItem(50));
    /** 紧迫满点：带附魔纹路的大宝石，右键使用恢复 100 TP。 */
    public static final RegistryObject<Item> TENSION_FULL = ITEMS.register("tension_full",
            () -> new TensionItem(100, true));
    public static final RegistryObject<EntityType<RoughSmashProjectile>> ROUGH_SMASH_ENTITY =
            ENTITIES.register("rough_smash", () -> EntityType.Builder.<RoughSmashProjectile>of(
                            RoughSmashProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("rough_smash"));
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> SNOW_BURIAL =
            ENCHANTMENTS.register("snow_burial", SnowBurialEnchantment::new);
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> TOOTHBRUSH =
            ENCHANTMENTS.register("toothbrush", ToothbrushEnchantment::new);
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> ROUGH_SMASH =
            ENCHANTMENTS.register("rough_smash", RoughSmashEnchantment::new);
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> CRIMSON_SMASH =
            ENCHANTMENTS.register("crimson_smash", CrimsonSmashEnchantment::new);
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> ICE_SHOCK =
            ENCHANTMENTS.register("ice_shock", IceShockEnchantment::new);
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> HEAL_PRAYER =
            ENCHANTMENTS.register("heal_prayer", HealPrayerEnchantment::new);
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> SLEEP_SPRAY =
            ENCHANTMENTS.register("sleep_spray", SleepSprayEnchantment::new);
    /** 螺旋桃击：锄头专属法术附魔（暂不做生存获取途径）。 */
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> SPIRAL_CLUBS =
            ENCHANTMENTS.register("spiral_clubs", SpiralClubsEnchantment::new);
    /** 方钻上冲：锄头专属法术附魔（暂不做生存获取途径）。 */
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> DIAMOND_UPPERCUT =
            ENCHANTMENTS.register("diamond_uppercut", DiamondUppercutEnchantment::new);
    /** 恶魔镰刀：锄头专属法术附魔（Jevil 掉落获取）。 */
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> JEVIL_SCYTHE =
            ENCHANTMENTS.register("jevil_scythe", JevilScytheEnchantment::new);
    /** OK治疗：头盔专属法术附魔（工作台合成获取，附魔台/宝箱/村民一律不刷）。 */
    public static final RegistryObject<net.minecraft.world.item.enchantment.Enchantment> OK_HEAL =
            ENCHANTMENTS.register("ok_heal", OkHealEnchantment::new);
    /** 治疗光球（heal_small）：OK治疗召唤的环绕/射出光点，纯特效实体。 */
    public static final RegistryObject<EntityType<HealSmallEntity>> HEAL_SMALL =
            ENTITIES.register("heal_small",
                    () -> EntityType.Builder.<HealSmallEntity>of(HealSmallEntity::new, MobCategory.MISC)
                            .sized(0.4F, 0.4F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("heal_small"));
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> OMINOUS_SOUND = SOUNDS.register("ominous",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "ominous")));
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> OMINOUS_CANCEL_SOUND = SOUNDS.register("ominous_cancel",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "ominous_cancel")));
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SNOWGRAVE_4_SOUND = SOUNDS.register("snowgrave_4",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "snowgrave_4")));
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> RUDEBUSTER_HIT_SOUND = SOUNDS.register("rudebuster_hit",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "rudebuster_hit")));
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> RUDEBUSTER_SWING_SOUND = SOUNDS.register("rudebuster_swing",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "rudebuster_swing")));
    /** 盾格挡有源伤害时播放（snd_graze.ogg）。 */
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> GRAZE_SOUND = SOUNDS.register("graze",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "graze")));
    /** 菜单开关 / 左右切换法术时播放（snd_select.ogg），UI 音不随距离衰减。 */
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SELECT_SOUND = SOUNDS.register("select",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "select")));
    /** 释放冰震术时在世界中播放（snd_icespell.ogg）。 */
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> ICE_SPELL_SOUND = SOUNDS.register("icespell",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "icespell")));
    /** 使用「紧迫」系列物品回复 TP 时播放（snd_tensionhorn.ogg）。 */
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> TENSION_HORN_SOUND = SOUNDS.register("tensionhorn",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "tensionhorn")));
    /** 释放治疗祷言 / OK治疗时在世界中播放（snd_power_1.ogg，即上传的 snd_power__1_.ogg）。 */
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> POWER_SOUND = SOUNDS.register("power",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "power")));
    /** 释放睡眠喷雾时在世界中播放（snd_spell_pacify.ogg）。 */
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> PACIFY_SOUND = SOUNDS.register("spell_pacify",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "spell_pacify")));
    /** 唱片声效「Ohhhhohohoho」（ohhhhohohoho.ogg，约 9.1 秒），供唱片机播放。 */
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> OHHHH_OHOHOHO_SOUND =
            SOUNDS.register("ohhhhohohoho",
                    () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                    MOD_ID, "ohhhhohohoho")));
    /** 唱片声效「Lost Girl」（lost_girl.ogg，约 76.9 秒），供唱片机播放。 */
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> LOST_GIRL_SOUND =
            SOUNDS.register("lost_girl",
                    () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                    MOD_ID, "lost_girl")));
    /**
     * 音乐唱片 4「Ohhhhohohoho」：拿在手里右键唱片机即可播放。
     * RecordItem(比较器输出, 声效懒加载, 属性, 时长刻数)：183 刻 ≈ 9.14 秒。
     * 注意：想放进唱片机，物品还必须进原版 #minecraft:music_discs 标签
     * （见 data/minecraft/tags/items/music_discs.json），否则唱片机静默拒绝。
     * 声效用 Supplier 形式引用，避免 ITEMS / SOUNDS 两个注册表的先后顺序问题。
     */
    public static final RegistryObject<Item> MUSIC_DISC_OHHHH_OHOHOHO = ITEMS.register("music_disc_ohhhhohohoho",
            () -> new net.minecraft.world.item.RecordItem(15, OHHHH_OHOHOHO_SOUND::get,
                    new Item.Properties().stacksTo(1), 183));
    /** 音乐唱片 30「Lost Girl」：1538 刻 ≈ 76.9 秒。 */
    public static final RegistryObject<Item> MUSIC_DISC_LOST_GIRL = ITEMS.register("music_disc_lost_girl",
            () -> new net.minecraft.world.item.RecordItem(15, LOST_GIRL_SOUND::get,
                    new Item.Properties().stacksTo(1), 1538));
    public static final RegistryObject<SimpleParticleType> SNOW_BURIAL_PARTICLE = PARTICLES.register(
            "snow_burial", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ROUGH_SMASH_TRAIL_PARTICLE = PARTICLES.register(
            "rough_smash_trail", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> CRIMSON_SMASH_TRAIL_PARTICLE = PARTICLES.register(
            "crimson_smash_trail", () -> new SimpleParticleType(true));
    /** 冰震术的雪花：带自定义参数（环上/中央、散开角度、悬停刻数、大小）。 */
    public static final RegistryObject<IceShockSnowflakeType> ICE_SHOCK_SNOWFLAKE = PARTICLES.register(
            "ice_shock_snowflake", IceShockSnowflakeType::new);
    /** 治疗祷言的绿色烟花火花：复用原版烟花贴图、染绿、从头顶下落。 */
    public static final RegistryObject<SimpleParticleType> HEAL_PRAYER_SPARK = PARTICLES.register(
            "heal_prayer_spark", () -> new SimpleParticleType(true));
    /** 睡眠喷雾的雾气：spr_icespell_mist.png，抖动放大渐隐 1 秒。 */
    public static final RegistryObject<SimpleParticleType> SLEEP_MIST = PARTICLES.register(
            "sleep_mist", () -> new SimpleParticleType(true));

    // ------------------------------------------------------------------
    // 牢房栏 / 牢房钥匙 / 钥匙碎片 / Jevil Boss / jevil_room 结构
    // ------------------------------------------------------------------
    /**
     * 牢房栏：直接继承原版铁栅栏（方块状态、碰撞箱、连接判定全部同源），
     * 差别只有两点——生存模式挖不动、手持牢房钥匙右键可立刻拆掉一格。
     * 外观复用原版铁栅栏贴图（见 assets/deltamagic/models/block/prison_bars.json）。
     */
    public static final RegistryObject<PrisonBarsBlock> PRISON_BARS =
            BLOCKS.register("prison_bars", PrisonBarsBlock::new);
    /** 牢房栏的物品形态：放置方块与创造模式图标都走它。 */
    public static final RegistryObject<Item> PRISON_BARS_ITEM = ITEMS.register("prison_bars",
            () -> new net.minecraft.world.item.BlockItem(PRISON_BARS.get(), new Item.Properties()));
    /** 牢房钥匙：右键牢房栏 → 那格方块立刻消失（16 点耐久，能拆 16 格）。 */
    public static final RegistryObject<Item> PRISON_KEY = ITEMS.register("prison_key", PrisonKeyItem::new);
    /**
     * 钥匙碎片 A / B / C：三片一起丢进工作台合成牢房钥匙。
     * 配方是无序合成（crafting_shapeless），所以摆放位置、朝向随便，只要能凑齐三种碎片。
     * 碎片来源：原版宝箱，每片各自 15% 独立刷新（见 KeyShardLootModifier）。
     */
    public static final RegistryObject<Item> KEY_SHARD_A = ITEMS.register("key_shard_a",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> KEY_SHARD_B = ITEMS.register("key_shard_b",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> KEY_SHARD_C = ITEMS.register("key_shard_c",
            () -> new Item(new Item.Properties()));
    /**
     * 暗影水晶：Jevil 的纪念掉落物——每个玩家只有第一次击败 Jevil 才会掉这一枚。
     * 贴图参考 spr_diamondbullet_form 做成暗紫水晶质感，竖菱形摆放；稀有度为 EPIC。
     */
    public static final RegistryObject<Item> SHADOW_CRYSTAL = ITEMS.register("shadow_crystal",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    /**
     * 丑魔锭：Jevil 的掉落物（每次击杀 80% 掉一个）。
     * 用途：锻造台里配下界合金升级模板升级任意斧头 → 攻击力 +8、
     * 粗暴碎击 TP 消耗降到 40（见 JevilIngotSmithingRecipe）。
     */
    public static final RegistryObject<Item> JEVIL_INGOT = ITEMS.register("jevil_ingot",
            () -> new Item(new Item.Properties()));
    /** Jevil（小丑）Boss：血量 350，只由 jevil_room 结构里的脚本生成，不自然刷新。 */
    public static final RegistryObject<EntityType<JevilEntity>> JEVIL = ENTITIES.register("jevil",
            () -> EntityType.Builder.<JevilEntity>of(JevilEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build("jevil"));
    /**
     * 玩家法术弹幕（螺旋桃击的桃弹 / 方钻上冲的方钻弹共用一个实体类型，按 kind 区分）。
     * updateInterval 取 1：桃弹起飞后每刻飞约 0.45 格，位置同步密一点才不会看着卡。
     */
    public static final RegistryObject<EntityType<SpellBullet>> SPELL_BULLET =
            ENTITIES.register("spell_bullet",
                    () -> EntityType.Builder.<SpellBullet>of(SpellBullet::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("spell_bullet"));
    /** Jevil 的弹幕（梅花 / 菱形 / 镰刀 / 旋转木马共用一个实体类型，按 kind 区分外观）。 */
    public static final RegistryObject<EntityType<JevilBullet>> JEVIL_BULLET =
            ENTITIES.register("jevil_bullet",
                    () -> EntityType.Builder.<JevilBullet>of(JevilBullet::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("jevil_bullet"));
    /** jevil_room 的结构类型：1.20.1 的数据驱动结构必须先在 Java 侧注册 Codec。 */
    public static final RegistryObject<net.minecraft.world.level.levelgen.structure.StructureType<?>>
            JEVIL_ROOM_STRUCTURE_TYPE = STRUCTURE_TYPES.register("jevil_room",
                    () -> (net.minecraft.world.level.levelgen.structure.StructureType<JevilRoomStructure>)
                            () -> JevilRoomStructure.CODEC);
    /** jevil_room 的方块件类型：存档重载时结构靠它把房间读回来。 */
    public static final RegistryObject<net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType>
            JEVIL_ROOM_PIECE = STRUCTURE_PIECES.register("jevil_room",
                    () -> (net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType)
                            (context, tag) -> new JevilRoomPiece(context.structureTemplateManager(), tag));
    /** Jevil 的十个音效：注册名 = sounds.json 里的键，指向用户给的 snd_joker_*.ogg。 */
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> JEVIL_HA0 = registerJevilSound("joker_ha0");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> JEVIL_HA1 = registerJevilSound("joker_ha1");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> JEVIL_ANYTHING = registerJevilSound("joker_anything");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> JEVIL_OH = registerJevilSound("joker_oh");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> JEVIL_CHAOS = registerJevilSound("joker_chaos");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> JEVIL_BYEBYE = registerJevilSound("joker_byebye");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> JEVIL_NEOCHAOS = registerJevilSound("joker_neochaos");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> JEVIL_LAUGH1 = registerJevilSound("joker_laugh1");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> JEVIL_METAMORPHOSIS = registerJevilSound("joker_metamorphosis");
    /** 战斗曲（joker.ogg），进战时以 RECORDS 声道在世界里奏响。 */
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> JEVIL_MUSIC = registerJevilSound("joker_music");

    /** 生成一个 deltamagic:&lt;id&gt; 的音效事件（sounds.json 里同名键指向 ogg 文件）。 */
    private static RegistryObject<net.minecraft.sounds.SoundEvent> registerJevilSound(String id) {
        return SOUNDS.register(id, () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, id)));
    }

    // ------------------------------------------------------------------
    // 进度页图标物品：只给 deltamagic 进度当图标用，故意不进创造页、不出现在生存里。
    // 图标贴图取自用户提供的 Undertale 风素材（见 textures/item/adv/）。
    // ------------------------------------------------------------------
    /** ①欢迎来到deltamagic!（spr_heart_0）。 */
    public static final RegistryObject<Item> ICON_HEART = ITEMS.register("adv_icon_heart",
            () -> new Item(new Item.Properties().stacksTo(1)));
    /** ②COOL术士（spr_icespell_snowflake）。 */
    public static final RegistryObject<Item> ICON_ICE_SNOWFLAKE = ITEMS.register("adv_icon_ice_snowflake",
            () -> new Item(new Item.Properties().stacksTo(1)));
    /** ③*Proceed（spr_noelleb_spell_special_0）。 */
    public static final RegistryObject<Item> ICON_NOELLEB_SPELL = ITEMS.register("adv_icon_noelleb_spell",
            () -> new Item(new Item.Properties().stacksTo(1)));
    /** ④冰冻鸡块（spr_berdly_ice）。 */
    public static final RegistryObject<Item> ICON_BERDLY_ICE = ITEMS.register("adv_icon_berdly_ice",
            () -> new Item(new Item.Properties().stacksTo(1)));
    /** ⑤*Just joking（spr_noelleb_hurt）。 */
    public static final RegistryObject<Item> ICON_NOELLEB_HURT = ITEMS.register("adv_icon_noelleb_hurt",
            () -> new Item(new Item.Properties().stacksTo(1)));
    /** ⑥那是斧头吗？（spr_susie_idle_0）。 */
    public static final RegistryObject<Item> ICON_SUSIE_IDLE = ITEMS.register("adv_icon_susie_idle",
            () -> new Item(new Item.Properties().stacksTo(1)));
    /** ⑦仁慈之心（spr_btspare_1-2）。 */
    public static final RegistryObject<Item> ICON_BT_SPARE = ITEMS.register("adv_icon_bt_spare",
            () -> new Item(new Item.Properties().stacksTo(1)));
    /** ⑧打瞌睡 / ⑨紧迫时间到（spr_tiredmark）。 */
    public static final RegistryObject<Item> ICON_TIREDMARK = ITEMS.register("adv_icon_tiredmark",
            () -> new Item(new Item.Properties().stacksTo(1)));

    /** 模组物品专属创造模式标签页（放在“战斗”页之后）。 */
    public static final RegistryObject<CreativeModeTab> MOD_ITEMS_TAB = CREATIVE_TABS.register("mod_items",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.deltamagic"))
                    .icon(() -> new ItemStack(THORN_RING.get()))
                    .withTabsBefore(CreativeModeTabs.COMBAT.location())
                    .displayItems((parameters, output) -> {
                        output.accept(new ItemStack(THORN_RING.get()));
                        output.accept(new ItemStack(SNOWFLAKE_RING.get()));
                        // 三本附魔书与「原材料」标签页里系统自动生成的完全一致：
                        // 普通附魔书 + 对应附魔，不加自定义名称
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(SNOW_BURIAL.get(), 1)));
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(TOOTHBRUSH.get(), 1)));
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(ROUGH_SMASH.get(), 1)));
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(CRIMSON_SMASH.get(), 1)));
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(ICE_SHOCK.get(), 1)));
                        // 治疗祷言：创造限定，任何生存获取渠道都不出
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(HEAL_PRAYER.get(), 1)));
                        // 睡眠喷雾：创造里也给一本，方便测试（生存另有宝箱 5% / 图书管理员 15% 两条渠道）
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(SLEEP_SPRAY.get(), 1)));
                        // 螺旋桃击 / 方钻上冲：暂不做生存获取途径，创造里各给一本用于测试
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(SPIRAL_CLUBS.get(), 1)));
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(DIAMOND_UPPERCUT.get(), 1)));
                        // 恶魔镰刀：生存由 Jevil 掉落，创造里也给一本用于测试
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(JEVIL_SCYTHE.get(), 1)));
                        // OK治疗：生存由工作台合成，创造里也给一本用于测试
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(OK_HEAL.get(), 1)));
                        // 「紧迫」系列 TP 补给品：从小到大排在一起
                        output.accept(new ItemStack(TENSION_BIT.get()));
                        output.accept(new ItemStack(TENSION_GEM.get()));
                        output.accept(new ItemStack(TENSION_FULL.get()));
                        // 两张音乐唱片：放进唱片机播放
                        output.accept(new ItemStack(MUSIC_DISC_OHHHH_OHOHOHO.get()));
                        output.accept(new ItemStack(MUSIC_DISC_LOST_GIRL.get()));
                        // 牢房栏 + 牢房钥匙 + 三片钥匙碎片
                        output.accept(new ItemStack(PRISON_BARS_ITEM.get()));
                        output.accept(new ItemStack(PRISON_KEY.get()));
                        output.accept(new ItemStack(KEY_SHARD_A.get()));
                        output.accept(new ItemStack(KEY_SHARD_B.get()));
                        output.accept(new ItemStack(KEY_SHARD_C.get()));
                        // 暗影水晶：Jevil 首杀纪念掉落物（创造里也给一枚用于查看贴图）
                        output.accept(new ItemStack(SHADOW_CRYSTAL.get()));
                        // 丑魔锭：Jevil 掉落，用于锻造台升级斧头
                        output.accept(new ItemStack(JEVIL_INGOT.get()));
                    })
                    .build());

    public GeneratedMod() {
        // 进度页自定义触发器（必须在任何进度 JSON 解析前登记）
        DeltamagicAdvancements.initialize();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // 方块先于物品注册：牢房栏的物品形态要取用方块注册对象
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        ENCHANTMENTS.register(bus);
        SOUNDS.register(bus);
        PARTICLES.register(bus);
        MOB_EFFECTS.register(bus);
        CREATIVE_TABS.register(bus);
        LOOT_MODIFIERS.register(bus);
        STRUCTURE_TYPES.register(bus);
        STRUCTURE_PIECES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        bus.addListener(GeneratedMod::enqueueCuriosSlot);
        bus.addListener(GeneratedMod::registerEntityAttributes);
        NetworkHandler.initialize();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ModEvents.INSTANCE);
    }

    /**
     * 实体属性（血量、速度…）必须在 mod 总线上的 EntityAttributeCreationEvent 里登记，
     * 否则 Jevil 一被生成就会因为没有属性表而崩溃。
     */
    private static void registerEntityAttributes(
            net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(JEVIL.get(), JevilEntity.createAttributes().build());
    }

    private static void enqueueCuriosSlot(InterModEnqueueEvent event) {
        try {
            Class<?> messageClass = Class.forName("top.theillusivec4.curios.api.SlotTypeMessage");
            Class<?> builderClass = Class.forName("top.theillusivec4.curios.api.SlotTypeMessage$Builder");
            Object builder = builderClass.getConstructor(String.class).newInstance("ring");
            builderClass.getMethod("size", int.class).invoke(builder, 1);
            builderClass.getMethod("priority", int.class).invoke(builder, 160);
            builderClass.getMethod("icon", net.minecraft.resources.ResourceLocation.class).invoke(builder,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                            "curios", "slot/empty_ring_slot"));
            Object message = builderClass.getMethod("build").invoke(builder);
            String registerType = messageClass.getField("REGISTER_TYPE").get(null).toString();
            InterModComms.sendTo("curios", registerType, () -> message);
        } catch (ReflectiveOperationException ignored) {
            // Curios is a mandatory runtime dependency; the data resource remains as a fallback.
        }
    }
}
