package com.roller.gui;

import com.roller.automation.RollerStateMachine;
import com.roller.config.RollerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class RollerHudOverlay implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        RollerStateMachine machine = RollerStateMachine.get();
        if (!machine.isRunning() && machine.getCurrentState() == com.roller.automation.RollerState.IDLE) {
            return;
        }

        RollerConfig config = RollerConfig.get();
        var state = machine.getCurrentState();

        int x = 8;
        int y = 8;

        // Arka plan kutusu çizimi (yarı saydam siyah)
        drawContext.fill(x - 4, y - 4, x + 210, y + 42, 0x90000000);

        // Başlık ve Durum
        drawContext.drawTextWithShadow(client.textRenderer, Text.literal("§6§l[Librarian Roller]"), x, y, 0xFFFFFF);
        drawContext.drawTextWithShadow(client.textRenderer, Text.literal("§7Durum: " + state.getDescription()), x, y + 11, state.getColor());

        // Hedef Büyü ve Zümrüt
        String targetInfo = "§7Hedef: §e" + config.targetEnchantmentName + " " + config.targetLevel + " §8(§a" + config.minEmeralds + "-" + config.maxEmeralds + "z§8)";
        drawContext.drawTextWithShadow(client.textRenderer, Text.literal(targetInfo), x, y + 22, 0xCCCCCC);

        // Deneme sayısı
        String attempts = "§7Deneme Sayısı: §f" + machine.getAttemptCount();
        drawContext.drawTextWithShadow(client.textRenderer, Text.literal(attempts), x, y + 33, 0xAAAAAA);
    }
}
