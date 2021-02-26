package dev.lazurite.fpvracing.client.input.keybind.net;

import dev.lazurite.fpvracing.FPVRacing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PowerGogglesC2S {
    public static final Identifier PACKET_ID = new Identifier(FPVRacing.MODID, "power_goggles_c2s");

    public static void accept(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        ItemStack hat = player.inventory.armor.get(3);
        ItemStack hand = player.inventory.getMainHandStack();
        boolean enable = buf.readBoolean();

        server.execute(() -> {
            if (FPVRacing.GOGGLES_CONTAINER.maybeGet(hand).isPresent()) {
                FPVRacing.GOGGLES_CONTAINER.get(hand).setEnabled(enable);
            } else if (FPVRacing.GOGGLES_CONTAINER.maybeGet(hat).isPresent()) {
                FPVRacing.GOGGLES_CONTAINER.get(hat).setEnabled(enable);
            }

//            player.playSound(enable ? SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON : SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, 1.0f, 1.0f);
        });
    }

    public static void send(boolean enabled) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(enabled);
        ClientPlayNetworking.send(PACKET_ID, buf);
    }
}
