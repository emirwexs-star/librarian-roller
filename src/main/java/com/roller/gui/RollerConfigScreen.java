package com.roller.gui;

import com.roller.automation.EnchantmentDefinition;
import com.roller.automation.RollerStateMachine;
import com.roller.config.RollerConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public class RollerConfigScreen extends Screen {
    private int currentEnchantIndex = 0;
    private final List<EnchantmentDefinition> enchantments = EnchantmentDefinition.getAll();

    private ButtonWidget toggleStartButton;
    private ButtonWidget enchantButton;
    private ButtonWidget levelButton;

    private TextFieldWidget minPriceField;
    private TextFieldWidget maxPriceField;
    private TextFieldWidget breakDurationField;
    private TextFieldWidget postBreakWaitField;
    private TextFieldWidget resetWaitField;
    private TextFieldWidget prePlaceWaitField;
    private TextFieldWidget tradeWaitField;

    public RollerConfigScreen() {
        super(Text.literal("Librarian Roller Ayarları"));
    }

    @Override
    protected void init() {
        super.init();

        RollerConfig config = RollerConfig.get();

        // Mevcut seçili büyünün index'ini bul
        for (int i = 0; i < enchantments.size(); i++) {
            if (enchantments.get(i).getId().equalsIgnoreCase(config.targetEnchantmentId)) {
                currentEnchantIndex = i;
                break;
            }
        }

        int centerX = this.width / 2;
        int startY = 40;

        // 1. Başlat / Durdur Butonu
        updateStartButton(centerX, startY);

        // 2. Büyü Seçimi Butonu
        enchantButton = ButtonWidget.builder(
                Text.literal("Büyü: §e" + enchantments.get(currentEnchantIndex).getDisplayName()),
                button -> {
                    currentEnchantIndex = (currentEnchantIndex + 1) % enchantments.size();
                    EnchantmentDefinition selected = enchantments.get(currentEnchantIndex);
                    config.targetEnchantmentId = selected.getId();
                    config.targetEnchantmentName = selected.getDisplayName();
                    if (config.targetLevel > selected.getMaxLevel()) {
                        config.targetLevel = 1;
                    }
                    button.setMessage(Text.literal("Büyü: §e" + selected.getDisplayName()));
                    updateLevelButtonMessage();
                }
        ).dimensions(centerX - 155, startY + 25, 205, 20).build();
        this.addDrawableChild(enchantButton);

        // 3. Seviye Seçimi Butonu
        levelButton = ButtonWidget.builder(
                Text.literal("Seviye: §b" + toRoman(config.targetLevel)),
                button -> {
                    EnchantmentDefinition selected = enchantments.get(currentEnchantIndex);
                    config.targetLevel++;
                    if (config.targetLevel > selected.getMaxLevel()) {
                        config.targetLevel = 1;
                    }
                    updateLevelButtonMessage();
                }
        ).dimensions(centerX + 55, startY + 25, 100, 20).build();
        this.addDrawableChild(levelButton);

        // 4. Zümrüt Fiyat Filtresi (Min / Maks)
        int priceY = startY + 52;
        minPriceField = new TextFieldWidget(this.textRenderer, centerX - 50, priceY, 45, 18, Text.literal("Min Fiyat"));
        minPriceField.setText(String.valueOf(config.minEmeralds));
        this.addDrawableChild(minPriceField);

        maxPriceField = new TextFieldWidget(this.textRenderer, centerX + 105, priceY, 45, 18, Text.literal("Maks Fiyat"));
        maxPriceField.setText(String.valueOf(config.maxEmeralds));
        this.addDrawableChild(maxPriceField);

        // 5. Gecikme ve Zamanlama Ayarları Giriş Alanları
        int timingY = priceY + 30;
        int col1X = centerX - 155;
        int col2X = centerX + 10;

        // Kırma Süresi
        breakDurationField = createNumericField(col1X + 115, timingY, 40, config.breakDurationMs);
        // Kırma Sonrası Bekleme
        postBreakWaitField = createNumericField(col2X + 105, timingY, 40, config.postBreakWaitMs);

        // Meslek Sıfırlanma Bekleme
        resetWaitField = createNumericField(col1X + 115, timingY + 24, 40, config.professionResetWaitMs);
        // Yerleştirme Öncesi Bekleme
        prePlaceWaitField = createNumericField(col2X + 105, timingY + 24, 40, config.prePlaceWaitMs);

        // Ticaret Kontrol Bekleme
        tradeWaitField = createNumericField(col1X + 115, timingY + 48, 40, config.tradeCheckWaitMs);

        // 6. Kaydet ve Kapat Butonu
        ButtonWidget saveButton = ButtonWidget.builder(
                Text.literal("§aAyarları Kaydet ve Kapat"),
                button -> saveAndClose()
        ).dimensions(centerX - 100, this.height - 30, 200, 20).build();
        this.addDrawableChild(saveButton);
    }

    private void updateStartButton(int centerX, int startY) {
        boolean running = RollerStateMachine.get().isRunning();
        String label = running ? "§c§l[■] OTOMASYONU DURDUR" : "§a§l[▶] OTOMASYONU BAŞLAT";
        if (toggleStartButton != null) {
            this.remove(toggleStartButton);
        }
        toggleStartButton = ButtonWidget.builder(
                Text.literal(label),
                button -> {
                    if (RollerStateMachine.get().isRunning()) {
                        RollerStateMachine.get().stop(this.client, "Kullanıcı menüden durdurdu.");
                    } else {
                        saveInputsToConfig();
                        RollerStateMachine.get().start(this.client);
                    }
                    updateStartButton(centerX, startY);
                }
        ).dimensions(centerX - 155, startY, 310, 20).build();
        this.addDrawableChild(toggleStartButton);
    }

    private TextFieldWidget createNumericField(int x, int y, int width, long value) {
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, x, y, width, 18, Text.literal(""));
        field.setText(String.valueOf(value));
        this.addDrawableChild(field);
        return field;
    }

    private void updateLevelButtonMessage() {
        levelButton.setMessage(Text.literal("Seviye: §b" + toRoman(RollerConfig.get().targetLevel)));
    }

    private void saveInputsToConfig() {
        RollerConfig config = RollerConfig.get();
        try {
            config.minEmeralds = Math.max(1, Integer.parseInt(minPriceField.getText().trim()));
            config.maxEmeralds = Math.min(64, Integer.parseInt(maxPriceField.getText().trim()));

            config.breakDurationMs = Math.max(50, Long.parseLong(breakDurationField.getText().trim()));
            config.postBreakWaitMs = Math.max(50, Long.parseLong(postBreakWaitField.getText().trim()));
            config.professionResetWaitMs = Math.max(100, Long.parseLong(resetWaitField.getText().trim()));
            config.prePlaceWaitMs = Math.max(50, Long.parseLong(prePlaceWaitField.getText().trim()));
            config.tradeCheckWaitMs = Math.max(50, Long.parseLong(tradeWaitField.getText().trim()));
        } catch (NumberFormatException ignored) {
        }
        RollerConfig.save();
    }

    private void saveAndClose() {
        saveInputsToConfig();
        this.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Arka planı hafif karart
        this.renderBackground(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        // Başlık
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§6§lLibrarian Roller §f- Köylü Büyü Otomasyonu"), centerX, 10, 0xFFFFFF);

        // Mevcut Durum Göstergesi
        var state = RollerStateMachine.get().getCurrentState();
        int attempts = RollerStateMachine.get().getAttemptCount();
        String statusText = "Durum: " + state.getDescription() + (attempts > 0 ? " (Deneme: " + attempts + ")" : "");
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(statusText), centerX, 24, state.getColor());

        // Fiyat etiketleri
        int priceY = 40 + 52 + 5;
        context.drawTextWithShadow(this.textRenderer, Text.literal("§7Min Zümrüt:"), centerX - 150, priceY, 0xCCCCCC);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§7Maks Zümrüt:"), centerX + 5, priceY, 0xCCCCCC);

        // Zamanlama etiketleri
        int timingY = priceY + 25;
        int col1X = centerX - 155;
        int col2X = centerX + 10;

        context.drawTextWithShadow(this.textRenderer, Text.literal("Kürsü kırma (ms):"), col1X, timingY + 5, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Kırılma bekleme (ms):"), col2X, timingY + 5, 0xAAAAAA);

        context.drawTextWithShadow(this.textRenderer, Text.literal("Meslek sıfırlama (ms):"), col1X, timingY + 29, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Yerleştirme bekle (ms):"), col2X, timingY + 29, 0xAAAAAA);

        context.drawTextWithShadow(this.textRenderer, Text.literal("Ticaret bekle (ms):"), col1X, timingY + 53, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    private String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }
}
