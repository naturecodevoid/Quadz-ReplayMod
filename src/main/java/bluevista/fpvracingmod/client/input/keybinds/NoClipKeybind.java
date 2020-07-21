package bluevista.fpvracingmod.client.input.keybinds;

import bluevista.fpvracingmod.network.EMPPacketToServer;
import bluevista.fpvracingmod.network.NoClipPacketToServer;
import bluevista.fpvracingmod.server.ServerInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class NoClipKeybind {
    private static KeyBinding key;

    public static void callback(MinecraftClient client) {
        if (key.wasPressed()) NoClipPacketToServer.send();
    }

    public static void register() {
        key = new KeyBinding(
                "key." + ServerInitializer.MODID + ".noclip",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category." + ServerInitializer.MODID + ".keys"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickCallback.EVENT.register(NoClipKeybind::callback);
    }
}
