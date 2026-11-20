package dev.soulfire;

import dev.soulfire.block.BonfireBlock;
import dev.soulfire.command.SoulfireCommands;
import dev.soulfire.data.SoulfirePlayerData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoulfireMod implements ModInitializer {
    public static final String MOD_ID = "soulfire";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Block BONFIRE = Registry.register(
            Registries.BLOCK, id("bonfire"),
            new BonfireBlock(Block.Settings.create()
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, id("bonfire")))
                    .strength(2.0f).luminance(state -> 15).nonOpaque())
    );
    public static final Item BONFIRE_ITEM = Registry.register(
            Registries.ITEM, id("bonfire"),
            new BlockItem(BONFIRE, new Item.Settings()
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, id("bonfire"))))
    );

    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }

    @Override
    public void onInitialize() {
        SoulfirePlayerData.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SoulfireCommands.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                SoulfirePlayerData data = SoulfirePlayerData.get(player);
                data.tickStamina(player.isSprinting(), player.isOnGround());
                if (data.getStamina() <= 0.0f && player.isSprinting()) {
                    player.setSprinting(false);
                }
            }
        });
        LOGGER.info("SOULFIRE loaded.");
    }
}
