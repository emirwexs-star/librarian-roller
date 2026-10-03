package com.roller.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RollerKeybinds {
    public static KeyBinding openConfigKey;
    public static KeyBinding toggleAutomationKey;

    public static void register() {
        // Varsayılan tuş: Sağ Shift (Right Shift)
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.librarian_roller.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.category.librarian_roller"
        ));

        // Hızlı Başlat/Durdur tuşu: B tuşu
        toggleAutomationKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.librarian_roller.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.category.librarian_roller"
        ));
    }
}
