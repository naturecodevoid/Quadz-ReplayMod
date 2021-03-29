package dev.lazurite.quadz.client.input.frame;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.item.container.TransmitterContainer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class InputFrameC2S {
    public static final Identifier PACKET_ID = new Identifier(Quadz.MODID, "input_frame_c2s");

    public static void accept(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int entityId = buf.readInt();
        InputFrame frame = new InputFrame(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readEnumConstant(Mode.class));

        server.execute(() -> {
            if (Quadz.TRANSMITTER_CONTAINER.maybeGet(player.getMainHandStack()).isPresent()) {
                TransmitterContainer transmitter = Quadz.TRANSMITTER_CONTAINER.get(player.getMainHandStack());
                Entity entity = player.getEntityWorld().getEntityById(entityId);

                if (entity instanceof QuadcopterEntity) {
                    if (((QuadcopterEntity) entity).isBoundTo(transmitter)) {
                        ((QuadcopterEntity) entity).getInputFrame().set(frame);
                    }
                }
            }
        });
    }

    public static void send(Entity entity, InputFrame frame) {
        if (!frame.isEmpty()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(entity.getEntityId());
            buf.writeFloat(frame.getThrottle());
            buf.writeFloat(frame.getPitch());
            buf.writeFloat(frame.getYaw());
            buf.writeFloat(frame.getRoll());
            buf.writeFloat(frame.getRate());
            buf.writeFloat(frame.getSuperRate());
            buf.writeFloat(frame.getExpo());
            buf.writeFloat(frame.getMaxAngle());
            buf.writeEnumConstant(frame.getMode());
            ClientPlayNetworking.send(PACKET_ID, buf);
        }
    }
}
