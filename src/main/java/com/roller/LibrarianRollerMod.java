package com.roller;

import com.roller.automation.RollerStateMachine;
import com.roller.config.RollerConfig;
import com.roller.gui.RollerConfigScreen;
import com.roller.gui.RollerHudOverlay;
import com.roller.input.RollerKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class LibrarianRollerMod implements ClientModInitializer {
    public static final String MOD_ID = "librarian_roller";

    @Override
    public void onInitializeClient() {
        System.out.println("[LibrarianRoller] Mod başlatılıyor...");

        // 1. Ayarları yükle
        RollerConfig.load();

        // 2. Tuş atamalarını kaydet (Right Shift & Toggle tuşu)
        RollerKeybinds.register();

        // 3. Ekranda durum gösteren HUD arayüzünü kaydet
        HudRenderCallback.EVENT.register(new RollerHudOverlay());

        // 4. Her istemci tick'inde çalışacak döngüyü kaydet
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null) {
                return;
            }

            // Right Shift basıldığında ayar menüsünü aç
            while (RollerKeybinds.openConfigKey.wasPressed()) {
                client.setScreen(new RollerConfigScreen());
            }

            // Başlat / Durdur kısayol tuşu basıldığında
            while (RollerKeybinds.toggleAutomationKey.wasPressed()) {
                if (RollerStateMachine.get().isRunning()) {
                    RollerStateMachine.get().stop(client, "Kısayol tuşuyla durduruldu.");
                } else {
                    RollerStateMachine.get().start(client);
                }
            }

            // Otomasyon durum makinesini işlet
            RollerStateMachine.get().tick(client);
        });

        System.out.println("[LibrarianRoller] Mod başarıyla yüklendi! Menü için 'Right Shift' tuşuna basın.");
    }
}
