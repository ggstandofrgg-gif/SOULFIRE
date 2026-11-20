package dev.soulfire.block;

import dev.soulfire.data.SoulfirePlayerData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BonfireBlock extends Block {
    public BonfireBlock(Settings settings) { super(settings); }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            SoulfirePlayerData data = SoulfirePlayerData.get(serverPlayer);
            data.setLastBonfire(pos);
            serverPlayer.setHealth(serverPlayer.getMaxHealth());
            serverPlayer.getHungerManager().setFoodLevel(20);
            serverPlayer.getHungerManager().setSaturationLevel(20.0f);
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_AMBIENT,
                    SoundCategory.BLOCKS, 0.8f, 0.9f);
            serverPlayer.sendMessage(Text.literal("§6✦ Костёр активирован. Пламя помнит тебя."), false);
            serverPlayer.sendMessage(Text.literal("§7Души: §e" + data.getSouls()
                    + " §8| §7Уровень: §e" + data.getLevel()), false);
            serverPlayer.sendMessage(Text.literal("§7/soulfire help — список механик"), false);
        }
        return ActionResult.SUCCESS;
    }
}
