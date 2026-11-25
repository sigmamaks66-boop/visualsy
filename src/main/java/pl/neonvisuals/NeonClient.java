package pl.neonvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public class NeonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NeonConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(Neon::clientTick);

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && player == MinecraftClient.getInstance().player) {
                Neon.onAttack(entity);
            }
            return ActionResult.PASS;
        });

        // /visuals -> otwiera menu
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("visuals").executes(ctx -> {
                    MinecraftClient mc = ctx.getSource().getClient();
                    mc.send(() -> mc.setScreen(new NeonScreen()));
                    return 1;
                })));
    }
}
