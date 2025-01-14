package dev.lazurite.quadz.client.input;

import com.google.common.collect.Maps;
import dev.lazurite.quadz.api.event.JoystickEvents;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.util.ClientTick;
import dev.lazurite.quadz.common.util.input.InputFrame;
import dev.lazurite.toolbox.api.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.nio.FloatBuffer;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * This class is responsible for polling the user's controller each
 * time the class is ticked. It dynamically values stored in {@link Config}.
 * @see Config
 */
public final class InputTick {
    private static final InputTick instance = new InputTick();
    private final Map<Integer, String> joysticks = Maps.newConcurrentMap();
    private final InputFrame frame = new InputFrame();
    private boolean loaded = false;
    private long next;

    private InputTick() { }

    public static Event<Click> RIGHT_CLICK_EVENT = Event.create();
    public static Event<Click> LEFT_CLICK_EVENT = Event.create();

    public interface Click {
        void onClick();
    }

    public static InputTick getInstance() {
        return instance;
    }

    public void tick() {
        if (System.currentTimeMillis() > next) {
            Map<Integer, String> lastJoysticks = Maps.newHashMap(joysticks);
            this.joysticks.clear();

            for (int i = 0; i < 16; i++) {
                if (glfwJoystickPresent(i)) {
                    joysticks.put(i, getJoystickName(i));

                    if (!lastJoysticks.containsKey(i) && loaded) {
                        JoystickEvents.JOYSTICK_CONNECT.invoke(i, getJoystickName(i));
                    }
                } else if (lastJoysticks.containsKey(i) && loaded) {
                    JoystickEvents.JOYSTICK_DISCONNECT.invoke(i, lastJoysticks.get(i));
                }
            }

            next = System.currentTimeMillis() + 500;
            loaded = true;
        }

        if (controllerExists()) {
            FloatBuffer axes = glfwGetJoystickAxes(Config.getInstance().controllerId);

            frame.set(
                    (Config.getInstance().throttle == -1 ? 0 : (Config.getInstance().throttle > axes.limit() ? 0 : axes.get(Config.getInstance().throttle))),
                    (Config.getInstance().pitch == -1 ? 0 : (Config.getInstance().pitch > axes.limit() ? 0 : axes.get(Config.getInstance().pitch))),
                    (Config.getInstance().yaw == -1 ? 0 : (Config.getInstance().yaw > axes.limit() ? 0 : axes.get(Config.getInstance().yaw))),
                    (Config.getInstance().roll == -1 ? 0 : (Config.getInstance().roll > axes.limit() ? 0 : axes.get(Config.getInstance().roll))),
                    Config.getInstance().rate,
                    Config.getInstance().superRate,
                    Config.getInstance().expo,
                    Config.getInstance().maxAngle,
                    Config.getInstance().mode);

            if (Config.getInstance().invertThrottle) {
                frame.setThrottle(-frame.getThrottle());
            }

            if (Config.getInstance().invertPitch) {
                frame.setPitch(-frame.getPitch());
            }

            if (Config.getInstance().invertRoll) {
                frame.setRoll(-frame.getRoll());
            }

            if (Config.getInstance().invertYaw) {
                frame.setYaw(-frame.getYaw());
            }

            if (Config.getInstance().throttleInCenter) {
                if (frame.getThrottle() < 0) {
                    frame.setThrottle(0);
                }
            } else {
                frame.setThrottle((frame.getThrottle() + 1) / 2.0f);
            }

            if (Config.getInstance().deadzone != 0.0f) {
                float halfDeadzone = Config.getInstance().deadzone / 2.0f;

                if (frame.getPitch() < halfDeadzone && frame.getPitch() > -halfDeadzone) {
                    frame.setPitch(0);
                }

                if (frame.getYaw() < halfDeadzone && frame.getYaw() > -halfDeadzone) {
                    frame.setYaw(0);
                }

                if (frame.getRoll() < halfDeadzone && frame.getRoll() > -halfDeadzone) {
                    frame.setRoll(0);
                }
            }
        } else if (Minecraft.getInstance().level != null){
            Config.getInstance().controllerId = -1;
        }
    }

    private final float rate = 0.3f;
    private float pitch;
    private float roll;

    public void tickKeyboard(Minecraft minecraft) {
        if (Config.getInstance().controllerId == -1) {
            ClientTick.isUsingKeyboard = true;

            float throttle = getInputFrame().getThrottle();
            float yaw = 0.0f;

            if (minecraft.options.keyRight.isDown()) {
                yaw = -0.5f;
            } else if (minecraft.options.keyLeft.isDown()) {
                yaw = 0.5f;
            }

            if (minecraft.options.keyUp.isDown()) {
                throttle += 0.025f;
            } else if (minecraft.options.keyDown.isDown()) {
                throttle -= 0.025f;
            }

            throttle = Mth.clamp(throttle, 0.0f, 1.0f);
            pitch = Mth.clamp(pitch, -1.0f, 1.0f);
            roll = Mth.clamp(roll, -1.0f, 1.0f);

            frame.set(throttle, pitch, yaw, roll,
                    Config.getInstance().rate,
                    Config.getInstance().superRate,
                    Config.getInstance().expo,
                    Config.getInstance().maxAngle,
                    Mode.ANGLE);
        }
    }

    public InputFrame getInputFrame() {
        return new InputFrame(frame);
    }

    public Map<Integer, String> getJoysticks() {
        Map<Integer, String> out = Maps.newHashMap(joysticks);
        out.put(-1, "keyboard");
        return out;
    }

    public static String getJoystickName(int id) {
        if (glfwJoystickPresent(id)) {
            return glfwGetJoystickName(id);
        }

        return null;
    }

    public static boolean controllerExists() {
        return Config.getInstance().controllerId != -1 && glfwJoystickPresent(Config.getInstance().controllerId);
    }
}
