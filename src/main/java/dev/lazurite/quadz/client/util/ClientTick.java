package dev.lazurite.quadz.client.util;

import com.google.gson.stream.JsonWriter;
import com.mojang.math.Quaternion;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.common.bindable.Bindable;
import dev.lazurite.quadz.common.quadcopter.Quadcopter;
import dev.lazurite.quadz.common.quadcopter.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.input.InputFrame;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickButtons;

/**
 * This class is responsible for transmitting the player's controller input
 * information to the server each tick. It will send input data from either
 * the player's camera entity or whatever entity they happen to be controlling
 * within range.
 * @see InputTick
 * @see InputFrame
 */
@Environment(EnvType.CLIENT)
public class ClientTick {
    public static boolean isUsingKeyboard = false;
    private static boolean recordingButtonLastFrame = false;
    private static boolean isRecording = false;
    private static int ticksSinceLastRecord = 0;
    private static ArrayList<Position> recordedPositions = new ArrayList<>();
    public static final File EXPORT_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "quadz_recordings");

    public static void tickInput(ClientLevel level) {
        final var client = Minecraft.getInstance();

        if (!client.isPaused()) {
            isUsingKeyboard = false;

            if (client.options.keyShift.isDown() && client.cameraEntity instanceof QuadcopterEntity) {
                ((CameraTypeAccess) (Object) client.options.getCameraType()).reset();
            }

            ByteBuffer buttons = glfwGetJoystickButtons(Config.getInstance().controllerId);

            // Ensure that isRecording is only toggled for 1 tick
            boolean toggleRecording = false;
            boolean fromButton = (Config.getInstance().toggleRecording != -1 && (Config.getInstance().toggleRecording <= buttons.limit() && buttons.get(Config.getInstance().toggleRecording) == GLFW_PRESS));
            if (recordingButtonLastFrame != fromButton) {
                toggleRecording = fromButton;
                recordingButtonLastFrame = fromButton;
            }

            if (toggleRecording)
                if (!isRecording)
                    if (client.getCameraEntity() instanceof QuadcopterEntity) {
                        isRecording = true;
                        Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("message.quadz.recording.start").withStyle(ChatFormatting.GREEN), false);
                    } else {
                        Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("message.quadz.recording.must_be_in_drone_view").withStyle(ChatFormatting.RED), false);
                    }
                else {
                    isRecording = false;
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("message.quadz.recording.stop").withStyle(ChatFormatting.RED), false);
                    // If we don't do this, the actionbar text will take some time to fade out
                    Minecraft.getInstance().player.displayClientMessage(new TextComponent(""), true);
                    saveRecordedToFile();
                }

            if (isRecording)
                if (client.getCameraEntity() instanceof QuadcopterEntity p) {
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("message.quadz.recording.currently_recording").withStyle(ChatFormatting.GREEN), true);
                    if (Config.getInstance().recordingRate != 1) ticksSinceLastRecord++;
                    if (Config.getInstance().recordingRate == 1 || ticksSinceLastRecord >= Config.getInstance().recordingRate) {
                        ticksSinceLastRecord = 0;
                        Quaternion rotation = Convert.toMinecraft(p.getRigidBody().getPhysicsRotation(null));
                        recordedPositions.add(new Position(
                                p.getX(),
                                p.getY() - p.getBbHeight(),
                                p.getZ(),
                                QuaternionHelper.getPitch(rotation),
                                QuaternionHelper.getRoll(rotation),
                                QuaternionHelper.getYaw(rotation)
                        ));
                    }
                } else {
                    isRecording = false;
                    recordedPositions = new ArrayList<>();
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("message.quadz.recording.must_be_in_drone_view").withStyle(ChatFormatting.RED), false);
                    // If we don't do this, the actionbar text will take some time to fade out
                    Minecraft.getInstance().player.displayClientMessage(new TextComponent(""), true);
                }

            Bindable.get(client.player.getMainHandItem()).ifPresent(transmitter -> {
                if (client.getCameraEntity() instanceof QuadcopterEntity quadcopter && quadcopter.isBoundTo(transmitter)) {
                    InputTick.getInstance().tickKeyboard(client);
                    quadcopter.getInputFrame().set(InputTick.getInstance().getInputFrame());
                    quadcopter.sendInputFrame();
                } else {
                    Quadcopter.getQuadcopterByBindId(
                            level,
                            client.cameraEntity.position(),
                            transmitter.getBindId(),
                            (int) client.gameRenderer.getRenderDistance())
                    .ifPresent(quadcopter -> {
                        if (Config.getInstance().followLOS) {
                            InputTick.getInstance().tickKeyboard(client);
                        }

                        quadcopter.getInputFrame().set(
                                Config.getInstance().controllerId == -1 ?
                                        new InputFrame() :
                                        InputTick.getInstance().getInputFrame());
                        quadcopter.sendInputFrame();
                    });
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static void saveRecordedToFile() {
        ArrayList<Position> positions = (ArrayList<Position>) recordedPositions.clone();
        recordedPositions = new ArrayList<>();
        Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("message.quadz.recording.saving").withStyle(ChatFormatting.YELLOW), false);
        new Thread(() -> {
            try {
                JsonWriter writer = new JsonWriter(new FileWriter(new File(EXPORT_DIR, new SimpleDateFormat("MM-dd-yyyy_hh_mm_ss_a").format(Calendar.getInstance().getTime()) + ".json")));
                writer.beginObject();
                writer.name("Quadz Export " + new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(Calendar.getInstance().getTime())).beginArray();

                // Object used for timestamps (which the user will add later when editing the replay)
                writer.beginObject();
                writer.name("keyframes").beginArray().endArray();
                writer.name("segments").beginArray().endArray();
                writer.name("interpolators").beginArray().endArray();
                writer.endObject();

                // The actual camera path
                writer.beginObject();

                writer.name("keyframes").beginArray();
                int time = 0;
                for (Position pos : positions) {
                    writer.beginObject();
                    writer.name("time").value(time);
                    writer.name("properties").beginObject();

                    writer.name("camera:rotation").beginArray();
                    writer.value(pos.yaw);
                    writer.value(pos.pitch);
                    writer.value(pos.roll);
                    writer.endArray();

                    writer.name("camera:position").beginArray();
                    writer.value(pos.x);
                    writer.value(pos.y);
                    writer.value(pos.z);
                    writer.endArray();

                    writer.endObject();
                    writer.endObject();

                    // 50 milliseconds per tick
                    time += Config.getInstance().recordingRate * 50;
                }
                writer.endArray();

                writer.name("segments").beginArray();
                for (int i = 0; i < positions.size() - 1; i++) {
                    writer.value(0);
                }
                writer.endArray();

                writer.name("interpolators").beginArray();
                writer.beginObject();
                writer.name("type").beginObject();
                writer.name("type").value("catmull-rom-spline");
                writer.name("alpha").value(0.5);
                writer.endObject();
                writer.name("properties").beginArray();
                writer.value("camera:rotation");
                writer.value("camera:position");
                writer.endArray();
                writer.endObject();
                writer.endArray();

                writer.endObject();
                writer.endArray();
                writer.endObject();
                writer.flush();

                Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("message.quadz.recording.done_saving").withStyle(ChatFormatting.GREEN), false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private record Position(double x, double y, double z, double yaw, double pitch, double roll) {}
}
