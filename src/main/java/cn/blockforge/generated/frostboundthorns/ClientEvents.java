package cn.blockforge.generated.frostboundthorns;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientEvents {
    /** Z：释放当前选中的法术（与菜单开合无关）。 */
    public static final KeyMapping SNOW_BURIAL_KEY = new KeyMapping(
            "key.deltamagic.snow_burial", InputConstants.KEY_Z, KeyMapping.CATEGORY_GAMEPLAY);
    /** X：打开 / 关闭屏幕顶部的法术菜单（菜单只是选择法术的界面，释放不再需要打开它）。 */
    public static final KeyMapping SPELL_MENU_KEY = new KeyMapping(
            "key.deltamagic.spell_menu", InputConstants.KEY_X, KeyMapping.CATEGORY_GAMEPLAY);
    /** ←：切到本页上一个法术（菜单开着关着都生效）。 */
    public static final KeyMapping SPELL_PREV_KEY = new KeyMapping(
            "key.deltamagic.spell_prev", InputConstants.KEY_LEFT, KeyMapping.CATEGORY_GAMEPLAY);
    /** →：切到本页下一个法术（菜单开着关着都生效）。 */
    public static final KeyMapping SPELL_NEXT_KEY = new KeyMapping(
            "key.deltamagic.spell_next", InputConstants.KEY_RIGHT, KeyMapping.CATEGORY_GAMEPLAY);
    /** ↑：法术分页时切到上一页（菜单开着关着都生效）。 */
    public static final KeyMapping SPELL_PAGE_UP_KEY = new KeyMapping(
            "key.deltamagic.spell_page_up", InputConstants.KEY_UP, KeyMapping.CATEGORY_GAMEPLAY);
    /** ↓：法术分页时切到下一页（菜单开着关着都生效）。 */
    public static final KeyMapping SPELL_PAGE_DOWN_KEY = new KeyMapping(
            "key.deltamagic.spell_page_down", InputConstants.KEY_DOWN, KeyMapping.CATEGORY_GAMEPLAY);

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(SNOW_BURIAL_KEY);
        event.register(SPELL_MENU_KEY);
        event.register(SPELL_PREV_KEY);
        event.register(SPELL_NEXT_KEY);
        event.register(SPELL_PAGE_UP_KEY);
        event.register(SPELL_PAGE_DOWN_KEY);
    }

    @SubscribeEvent
    public static void registerParticleProvider(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(GeneratedMod.SNOW_BURIAL_PARTICLE.get(), SnowflakeParticle.Provider::new);
        event.registerSpriteSet(GeneratedMod.ROUGH_SMASH_TRAIL_PARTICLE.get(), RoughSmashTrailParticle.Provider::new);
        // 朱红碎击的拖尾行为与粗暴碎击完全相同，只是换了红色一套贴图
        event.registerSpriteSet(GeneratedMod.CRIMSON_SMASH_TRAIL_PARTICLE.get(), RoughSmashTrailParticle.Provider::new);
        // 冰震术的雪花：行为由服务端下发的参数决定（悬停几刻、往哪散、多大）
        event.registerSpriteSet(GeneratedMod.ICE_SHOCK_SNOWFLAKE.get(), IceShockSnowflakeParticle.Provider::new);
        // 治疗祷言的绿色烟花火花：原版烟花贴图染绿、从头顶下落
        event.registerSpriteSet(GeneratedMod.HEAL_PRAYER_SPARK.get(), HealPrayerSparkParticle.Provider::new);
        // 睡眠喷雾的雾气：定在生物身上抖动放大渐隐 1 秒
        event.registerSpriteSet(GeneratedMod.SLEEP_MIST.get(), SleepMistParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GeneratedMod.ROUGH_SMASH_ENTITY.get(), RoughSmashRenderer::new);
        // Jevil Boss 与它的四种弹幕
        event.registerEntityRenderer(GeneratedMod.JEVIL.get(), JevilRenderer::new);
        event.registerEntityRenderer(GeneratedMod.JEVIL_BULLET.get(), JevilBulletRenderer::new);
        // 玩家法术「螺旋桃击 / 方钻上冲」的弹幕
        event.registerEntityRenderer(GeneratedMod.SPELL_BULLET.get(), SpellBulletRenderer::new);
        // OK治疗的治疗光球
        event.registerEntityRenderer(GeneratedMod.HEAL_SMALL.get(), HealSmallRenderer::new);
    }

    /**
     * 牢房栏用的是原版铁栅栏贴图（带透明镂空），必须走 cutout 渲染层，
     * 否则镂空部分会变成黑色方块。这里显式登记一次，和模型 JSON 里的
     * "render_type": "cutout" 互为保险。
     */
    @SubscribeEvent
    public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                GeneratedMod.PRISON_BARS.get(), net.minecraft.client.renderer.RenderType.cutout());
    }

    @Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeEvents {
        private static boolean lastSnowBurialKeyDown;
        private static boolean lastMenuKeyDown;
        private static boolean lastPrevKeyDown;
        private static boolean lastNextKeyDown;
        private static boolean lastPageUpKeyDown;
        private static boolean lastPageDownKeyDown;
        /** 上一次客户端刻里"右键使用"键是否按下（射治疗光球只在按下的那一下触发）。 */
        private static boolean lastUseKeyDown;

        private ForgeEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            boolean snowBurialDown = SNOW_BURIAL_KEY.isDown();
            boolean menuDown = SPELL_MENU_KEY.isDown();
            boolean prevDown = SPELL_PREV_KEY.isDown();
            boolean nextDown = SPELL_NEXT_KEY.isDown();
            boolean pageUpDown = SPELL_PAGE_UP_KEY.isDown();
            boolean pageDownDown = SPELL_PAGE_DOWN_KEY.isDown();
            boolean useDown = minecraft.options.keyUse.isDown();
            if (minecraft.player == null || minecraft.level == null) {
                SpellMenuState.reset();
                rememberEdges(snowBurialDown, menuDown, prevDown, nextDown, pageUpDown, pageDownDown, useDown);
                return;
            }
            if (minecraft.screen != null) {
                // 打开任何界面（背包、聊天框等）时收起菜单，避免按键被界面吃掉
                SpellMenuState.close();
                rememberEdges(snowBurialDown, menuDown, prevDown, nextDown, pageUpDown, pageDownDown, useDown);
                return;
            }

            // X：切换菜单开/关（开、关都播 snd_select.ogg）
            if (menuDown && !lastMenuKeyDown) {
                SpellMenuState.toggle();
                playSelectSound();
            }
            // ←→：在当前页内切换选中的法术（菜单开着关着都生效，选中真的换人了才响）
            if (prevDown && !lastPrevKeyDown) {
                int before = SpellMenuState.selectedSpell();
                SpellMenuState.moveSelection(-1);
                if (SpellMenuState.selectedSpell() != before) {
                    playSelectSound();
                }
            }
            if (nextDown && !lastNextKeyDown) {
                int before = SpellMenuState.selectedSpell();
                SpellMenuState.moveSelection(1);
                if (SpellMenuState.selectedSpell() != before) {
                    playSelectSound();
                }
            }
            // ↑↓：法术超过 3 个（分页）时切换上/下一页；只有一页时按下无效果
            if (pageUpDown && !lastPageUpKeyDown) {
                int before = SpellMenuState.selectedSpell();
                SpellMenuState.movePage(-1);
                if (SpellMenuState.selectedSpell() != before) {
                    playSelectSound();
                }
            }
            if (pageDownDown && !lastPageDownKeyDown) {
                int before = SpellMenuState.selectedSpell();
                SpellMenuState.movePage(1);
                if (SpellMenuState.selectedSpell() != before) {
                    playSelectSound();
                }
            }

            // Z：释放当前选中的法术——不再要求打开法术菜单，菜单开或关都释放上一次选中的法术
            // （没有可用法术时按 Z 什么都不做；菜单关闭时右侧徽章会显示当前选中的是哪一个）
            if (snowBurialDown && !lastSnowBurialKeyDown) {
                castSelectedSpell();
            }

            // OK治疗：右键按下的"那一下"对准非方块处 → 请求服务端射出一枚环绕中的治疗光球。
            // 不再监听 PlayerInteractEvent.RightClickEmpty/RightClickItem：原版在按住右键期间
            // 每刻都会重新触发这两个事件，一次"稍微按久一点"的右键会发两三个包 → 一下射出两枚。
            // 沿用自己的按下沿检测（和 X/Z 同一套路），保证一次物理点击只发一个包。
            if (useDown && !lastUseKeyDown) {
                HitResult hit = minecraft.hitResult;
                if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
                    requestOkHealFire(minecraft.player);
                }
            }
            rememberEdges(snowBurialDown, menuDown, prevDown, nextDown, pageUpDown, pageDownDown, useDown);
        }

        /** 把选中的法术转成对应的服务端释放包（粗暴碎击 / 朱红碎击 / 雪葬 / 冰震术 / 治疗祷言）。 */
        private static void castSelectedSpell() {
            int spell = SpellMenuState.selectedSpell();
            if (spell == SpellMenuState.SPELL_ROUGH_SMASH) {
                NetworkHandler.sendRoughSmashInput(true);
            } else if (spell == SpellMenuState.SPELL_CRIMSON_SMASH) {
                NetworkHandler.sendCrimsonSmashInput(true);
            } else if (spell == SpellMenuState.SPELL_SNOW_BURIAL) {
                NetworkHandler.sendSnowBurialInput(true);
            } else if (spell == SpellMenuState.SPELL_ICE_SHOCK) {
                NetworkHandler.sendIceShockInput(true);
            } else if (spell == SpellMenuState.SPELL_HEAL_PRAYER) {
                NetworkHandler.sendHealPrayerInput(true);
            } else if (spell == SpellMenuState.SPELL_SLEEP_SPRAY) {
                NetworkHandler.sendSleepSprayInput(true);
            } else if (spell == SpellMenuState.SPELL_SPIRAL_CLUBS) {
                NetworkHandler.sendSpiralClubsInput(true);
            } else if (spell == SpellMenuState.SPELL_DIAMOND_UPPERCUT) {
                NetworkHandler.sendDiamondUppercutInput(true);
            } else if (spell == SpellMenuState.SPELL_JEVIL_SCYTHE) {
                NetworkHandler.sendJevilScytheInput(true);
            } else if (spell == SpellMenuState.SPELL_OK_HEAL) {
                NetworkHandler.sendOkHealInput(true);
            }
        }

        /** 播 snd_select.ogg：UI 音（SimpleSoundInstance.forUI），只在本地响、不占世界声源、不随距离衰减。 */
        private static void playSelectSound() {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(GeneratedMod.SELECT_SOUND.get(), 1.0F));
        }

        /**
         * 右键射光球的统一入口：只有客户端、戴了带 OK治疗 附魔的头盔才发包（服务端再查有没有光球）。
         * 调用点在 onClientTick 的"右键按下那一下"分支里，一次物理点击只会走到这里一回。
         */
        private static void requestOkHealFire(Player player) {
            if (!player.level().isClientSide || player.isSpectator()) {
                return;
            }
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.OK_HEAL.get(), helmet) <= 0) {
                return;
            }
            NetworkHandler.sendOkHealFireInput(true);
        }

        private static void rememberEdges(boolean snow, boolean menu, boolean prev, boolean next,
                                         boolean pageUp, boolean pageDown, boolean use) {
            lastSnowBurialKeyDown = snow;
            lastMenuKeyDown = menu;
            lastPrevKeyDown = prev;
            lastNextKeyDown = next;
            lastPageUpKeyDown = pageUp;
            lastPageDownKeyDown = pageDown;
            lastUseKeyDown = use;
        }
    }
}
