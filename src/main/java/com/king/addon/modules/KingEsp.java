package com.king.addon.modules;

import com.king.addon.KingAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class KingEsp extends Module {
    private final SettingGroup sgBlocks = settings.createGroup("Blocks");
    private final SettingGroup sgEntities = settings.createGroup("Entities");

    // ---- Blocks ----
    private final Setting<List<Block>> blocks = sgBlocks.add(new BlockListSetting.Builder()
        .name("blocks").description("Block can soi.")
        .defaultValue(
            Blocks.AMETHYST_BLOCK, Blocks.BUDDING_AMETHYST,
            Blocks.AMETHYST_CLUSTER, Blocks.LARGE_AMETHYST_BUD,
            Blocks.MEDIUM_AMETHYST_BUD, Blocks.SMALL_AMETHYST_BUD,
            Blocks.OBSERVER, Blocks.PISTON, Blocks.STICKY_PISTON,
            Blocks.CRAFTER, Blocks.BEE_NEST, Blocks.BEEHIVE)
        .build());

    private final Setting<Integer> range = sgBlocks.add(new IntSetting.Builder()
        .name("range").description("Ban kinh quet block.")
        .defaultValue(32).min(8).max(96).sliderRange(8, 96).build());

    private final Setting<Boolean> fullVertical = sgBlocks.add(new BoolSetting.Builder()
        .name("full-vertical").description("Quet tu day the gioi (-64) toi tran, xuyen dat.")
        .defaultValue(true).build());

    private final Setting<Integer> interval = sgBlocks.add(new IntSetting.Builder()
        .name("scan-interval").description("So tick giua moi lan quet.")
        .defaultValue(20).min(5).max(100).sliderRange(5, 100).build());

    private final Setting<ShapeMode> shapeMode = sgBlocks.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode").defaultValue(ShapeMode.Both).build());

    private final Setting<SettingColor> blockSide = sgBlocks.add(new ColorSetting.Builder()
        .name("block-side-color").defaultValue(new SettingColor(170, 90, 255, 40)).build());

    private final Setting<SettingColor> blockLine = sgBlocks.add(new ColorSetting.Builder()
        .name("block-line-color").defaultValue(new SettingColor(170, 90, 255, 255)).build());

    private final Setting<Boolean> blockTracers = sgBlocks.add(new BoolSetting.Builder()
        .name("block-tracers").description("Ve duong noi toi block.")
        .defaultValue(false).build());

    // ---- Entities ----
    private final Setting<Set<EntityType<?>>> entities = sgEntities.add(new EntityTypeListSetting.Builder()
        .name("entities").description("Entity can soi.")
        .defaultValue(EntityType.PLAYER, EntityType.BEE, EntityType.ITEM)
        .build());

    private final Setting<SettingColor> entitySide = sgEntities.add(new ColorSetting.Builder()
        .name("entity-side-color").defaultValue(new SettingColor(255, 60, 60, 40)).build());

    private final Setting<SettingColor> entityLine = sgEntities.add(new ColorSetting.Builder()
        .name("entity-line-color").defaultValue(new SettingColor(255, 60, 60, 255)).build());

    private final Setting<Boolean> entityTracers = sgEntities.add(new BoolSetting.Builder()
        .name("entity-tracers").description("Ve duong noi toi entity.")
        .defaultValue(true).build());

    private final List<BlockPos> found = new ArrayList<>();
    private int timer = 0;

    public KingEsp() {
        super(KingAddon.CATEGORY, "king-esp", "ESP cho amethyst, observer, piston, crafter, bee nest va entity.");
    }

    @Override
    public void onActivate() {
        found.clear();
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;
        if (timer-- > 0) return;
        timer = interval.get();

        List<BlockPos> result = new ArrayList<>();
        BlockPos center = mc.player.getBlockPos();
        int r = range.get();
        BlockPos.Mutable p = new BlockPos.Mutable();
        List<Block> wanted = blocks.get();

        int worldMin = mc.world.getBottomY();
        int worldMax = worldMin + mc.world.getHeight() - 1;
        int minY = fullVertical.get() ? worldMin : Math.max(worldMin, center.getY() - r);
        int maxY = fullVertical.get() ? worldMax : Math.min(worldMax, center.getY() + r);

        for (int x = center.getX() - r; x <= center.getX() + r; x++) {
            for (int z = center.getZ() - r; z <= center.getZ() + r; z++) {
                if (!mc.world.isChunkLoaded(x >> 4, z >> 4)) continue;
                for (int y = minY; y <= maxY; y++) {
                    p.set(x, y, z);
                    if (wanted.contains(mc.world.getBlockState(p).getBlock())) result.add(p.toImmutable());
                }
            }
        }

        synchronized (found) {
            found.clear();
            found.addAll(result);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        double tx = mc.player.getX(), ty = mc.player.getEyeY(), tz = mc.player.getZ();

        synchronized (found) {
            for (BlockPos pos : found) {
                event.renderer.box(pos, blockSide.get(), blockLine.get(), shapeMode.get(), 0);
                if (blockTracers.get()) {
                    event.renderer.line(tx, ty, tz, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, blockLine.get());
                }
            }
        }

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!entities.get().contains(entity.getType())) continue;

            Box box = entity.getBoundingBox();
            event.renderer.box(box, entitySide.get(), entityLine.get(), ShapeMode.Both, 0);
            if (entityTracers.get()) {
                event.renderer.line(tx, ty, tz, entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(), entityLine.get());
            }
        }
    }
}
