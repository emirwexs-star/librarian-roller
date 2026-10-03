package com.roller.automation;

import com.roller.config.RollerConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;

import java.util.List;

public class RollerStateMachine {
    private static final RollerStateMachine INSTANCE = new RollerStateMachine();

    public static RollerStateMachine get() {
        return INSTANCE;
    }

    private RollerState currentState = RollerState.IDLE;
    private VillagerEntity targetVillager = null;
    private BlockPos lecternPos = null;

    private long stateStartTime = 0;
    private long actionTimer = 0;
    private int attemptCount = 0;
    private int consecutiveRetries = 0;
    private static final int MAX_RETRIES = 5;

    public RollerState getCurrentState() {
        return currentState;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public VillagerEntity getTargetVillager() {
        return targetVillager;
    }

    public BlockPos getLecternPos() {
        return lecternPos;
    }

    public boolean isRunning() {
        return currentState != RollerState.IDLE 
            && currentState != RollerState.TARGET_FOUND 
            && currentState != RollerState.ERROR_TIMEOUT;
    }

    public void start(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        attemptCount = 0;
        consecutiveRetries = 0;
        lecternPos = null;
        targetVillager = null;

        // 1. Oyuncunun baktığı veya en yakınındaki köylüyü bul
        HitResult hit = client.crosshairTarget;
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof VillagerEntity villager) {
            targetVillager = villager;
        } else {
            // 4.5 blok çevredeki en yakın köylüyü tara
            Box searchBox = client.player.getBoundingBox().expand(4.5);
            List<VillagerEntity> villagers = client.world.getEntitiesByClass(VillagerEntity.class, searchBox, v -> v.isAlive());
            if (!villagers.isEmpty()) {
                villagers.sort((v1, v2) -> Double.compare(v1.squaredDistanceTo(client.player), v2.squaredDistanceTo(client.player)));
                targetVillager = villagers.get(0);
            }
        }

        if (targetVillager == null) {
            sendFeedback(client, "§c[LibrarianRoller] Yakında uygun bir köylü bulunamadı! Lütfen bir köylüye yaklaşın.", true);
            setState(RollerState.IDLE);
            return;
        }

        // Köylünün daha önce ticaret yapıp yapmadığını kontrol et (XP > 0 ise meslek değişmez)
        if (targetVillager.getExperience() > 0) {
            sendFeedback(client, "§c[LibrarianRoller] Bu köylüyle daha önce ticaret yapılmış (XP > 0). Mesleği değiştirilemez!", true);
            setState(RollerState.IDLE);
            return;
        }

        // 2. Kürsü yerini belirle
        // Eğer köylünün yanında zaten bir kürsü varsa o konumu kullan
        BlockPos villagerBlockPos = targetVillager.getBlockPos();
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos checkPos = villagerBlockPos.offset(dir);
            if (client.world.getBlockState(checkPos).isOf(Blocks.LECTERN)) {
                lecternPos = checkPos;
                break;
            }
        }

        // Yoksa köylünün yanında yerleştirmeye uygun boş bir yer seç
        if (lecternPos == null) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos checkPos = villagerBlockPos.offset(dir);
                if (client.world.getBlockState(checkPos).isAir() && 
                    client.world.getBlockState(checkPos.down()).isSolidBlock(client.world, checkPos.down())) {
                    lecternPos = checkPos;
                    break;
                }
            }
        }

        if (lecternPos == null) {
            sendFeedback(client, "§c[LibrarianRoller] Köylünün yanına kürsü koyacak uygun boş zemin bulunamadı!", true);
            setState(RollerState.IDLE);
            return;
        }

        sendFeedback(client, "§a[LibrarianRoller] Otomasyon başlatıldı! Hedef: §e" + 
                RollerConfig.get().targetEnchantmentName + " " + RollerConfig.get().targetLevel + 
                " §a(Fiyat: " + RollerConfig.get().minEmeralds + "-" + RollerConfig.get().maxEmeralds + " Zümrüt)", false);

        // Zaten kürsü varsa ve köylü kütüphaneciyse doğrudan ticarete geç
        if (client.world.getBlockState(lecternPos).isOf(Blocks.LECTERN)) {
            if (targetVillager.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN) {
                setState(RollerState.PRE_TRADE_WAIT);
            } else {
                setState(RollerState.WAITING_PROFESSION);
            }
        } else {
            setState(RollerState.SELECTING_LECTERN);
        }
    }

    public void stop(MinecraftClient client, String reason) {
        setState(RollerState.IDLE);
        if (client != null && client.player != null && reason != null) {
            sendFeedback(client, "§e[LibrarianRoller] Otomasyon durduruldu: " + reason, false);
        }
    }

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || !isRunning()) {
            return;
        }

        RollerConfig config = RollerConfig.get();
        long now = System.currentTimeMillis();

        // Güvenlik Watchdog Zaman Aşımı (Sonsuz takılmayı ve desync'i engeller)
        if (now - stateStartTime > config.maxTimeoutMs) {
            handleTimeout(client);
            return;
        }

        // Hedef köylü hala geçerli ve yakın mı?
        if (targetVillager == null || !targetVillager.isAlive() || 
            targetVillager.squaredDistanceTo(client.player) > 36.0) {
            stop(client, "Köylü kayboldu veya çok uzaklaştı!");
            return;
        }

        switch (currentState) {
            case SELECTING_LECTERN:
                // Hotbar'da kürsü var mı kontrol et
                int lecternSlot = findItemSlot(client, Items.LECTERN);
                if (lecternSlot == -1) {
                    stop(client, "Envanterde/Hotbarda Kürsü (Lectern) kalmadı!");
                    return;
                }
                client.player.getInventory().selectedSlot = lecternSlot;
                actionTimer = now;
                setState(RollerState.PRE_PLACE_WAIT);
                break;

            case PRE_PLACE_WAIT:
                if (now - actionTimer >= config.prePlaceWaitMs) {
                    setState(RollerState.PLACING_LECTERN);
                }
                break;

            case PLACING_LECTERN:
                // Kürsü zaten var mı?
                if (client.world.getBlockState(lecternPos).isOf(Blocks.LECTERN)) {
                    setState(RollerState.WAITING_PROFESSION);
                    actionTimer = now;
                    return;
                }

                // Kürsüyü yerleştir
                int slot = findItemSlot(client, Items.LECTERN);
                if (slot == -1) {
                    stop(client, "Kürsü tükendi!");
                    return;
                }
                client.player.getInventory().selectedSlot = slot;

                // Kürsü bloğunun altındaki bloğa sağ tıkla
                BlockPos placeTarget = lecternPos.down();
                Vec3d hitVec = new Vec3d(lecternPos.getX() + 0.5, lecternPos.getY(), lecternPos.getZ() + 0.5);
                BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, placeTarget, false);

                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
                client.player.swingHand(Hand.MAIN_HAND);

                // Yerleştirildiğini doğrula
                if (client.world.getBlockState(lecternPos).isOf(Blocks.LECTERN)) {
                    consecutiveRetries = 0;
                    actionTimer = now;
                    setState(RollerState.WAITING_PROFESSION);
                }
                break;

            case WAITING_PROFESSION:
                // Kürsünün hala orada olduğunu doğrula
                if (!client.world.getBlockState(lecternPos).isOf(Blocks.LECTERN)) {
                    setState(RollerState.SELECTING_LECTERN);
                    return;
                }

                // Köylünün meslek edinip edinmediğini kontrol et
                VillagerData villagerData = targetVillager.getVillagerData();
                if (villagerData.getProfession() == VillagerProfession.LIBRARIAN) {
                    // Meslek alındı, ticaret kontrolü öncesi bekleme
                    actionTimer = now;
                    setState(RollerState.PRE_TRADE_WAIT);
                }
                break;

            case PRE_TRADE_WAIT:
                if (now - actionTimer >= config.tradeCheckWaitMs) {
                    setState(RollerState.INTERACTING_VILLAGER);
                }
                break;

            case INTERACTING_VILLAGER:
                // Köylüye sağ tıklayıp ticaret menüsünü aç
                client.interactionManager.interactEntity(client.player, targetVillager, Hand.MAIN_HAND);
                actionTimer = now;
                setState(RollerState.CHECKING_TRADES);
                break;

            case CHECKING_TRADES:
                if (client.currentScreen instanceof MerchantScreen merchantScreen) {
                    TradeOfferList offers = merchantScreen.getScreenHandler().getRecipes();
                    if (offers != null && !offers.isEmpty()) {
                        attemptCount++;
                        boolean matched = checkOffersMatch(client, offers, config);

                        if (matched) {
                            // Başarılı! Aranan kitap bulundu
                            setState(RollerState.TARGET_FOUND);
                            if (client.player != null) {
                                client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                sendFeedback(client, "§a§l[LibrarianRoller] HEDEF KİTAP BULUNDU! (" + attemptCount + ". deneme)", false);
                            }
                        } else {
                            // Uygun kitap değil, menüyü kapat ve kürsüyü kır
                            client.player.closeHandledScreen();
                            actionTimer = now;
                            setState(RollerState.BREAKING_LECTERN);
                        }
                    }
                }
                break;

            case BREAKING_LECTERN:
                // Kürsü zaten kırılmış mı?
                if (client.world.getBlockState(lecternPos).isAir()) {
                    consecutiveRetries = 0;
                    actionTimer = now;
                    setState(RollerState.POST_BREAK_WAIT);
                    return;
                }

                // Elimize tercihen balta al
                int axeSlot = findBestAxeSlot(client);
                if (axeSlot != -1) {
                    client.player.getInventory().selectedSlot = axeSlot;
                }

                // Kürsüyü kırma paketini gönder
                client.interactionManager.updateBlockBreakingProgress(lecternPos, Direction.UP);
                client.player.swingHand(Hand.MAIN_HAND);

                // Kırma süresi tamamlandıktan sonra anında kırılma kontrolü
                if (now - actionTimer >= config.breakDurationMs) {
                    if (client.world.getBlockState(lecternPos).isAir()) {
                        actionTimer = now;
                        setState(RollerState.POST_BREAK_WAIT);
                    }
                }
                break;

            case POST_BREAK_WAIT:
                if (now - actionTimer >= config.postBreakWaitMs) {
                    actionTimer = now;
                    setState(RollerState.WAITING_RESET);
                }
                break;

            case WAITING_RESET:
                // Köylünün mesleğini sıfırlayıp sıfırlamadığını doğrula
                if (targetVillager.getVillagerData().getProfession() == VillagerProfession.NONE) {
                    // Meslek sıfırlandı, bekleme süresinden sonra tekrar kürsü koymaya geç
                    if (now - actionTimer >= config.professionResetWaitMs) {
                        setState(RollerState.SELECTING_LECTERN);
                    }
                }
                break;

            default:
                break;
        }
    }

    private boolean checkOffersMatch(MinecraftClient client, TradeOfferList offers, RollerConfig config) {
        for (TradeOffer offer : offers) {
            ItemStack sellItem = offer.getSellItem();
            if (sellItem.isOf(Items.ENCHANTED_BOOK)) {
                ItemEnchantmentsComponent enchants = sellItem.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
                
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchants.getEnchantmentEntries()) {
                    RegistryEntry<Enchantment> enchEntry = entry.getKey();
                    int level = entry.getIntValue();
                    String enchId = enchEntry.getKey().map(k -> k.getValue().toString()).orElse("");

                    if (enchId.equalsIgnoreCase(config.targetEnchantmentId) && level >= config.targetLevel) {
                        // Zümrüt maliyetini kontrol et
                        int emeraldPrice = offer.getDisplayedFirstBuyItem().getCount();
                        if (emeraldPrice >= config.minEmeralds && emeraldPrice <= config.maxEmeralds) {
                            sendFeedback(client, "§6[LibrarianRoller] Eşleşme: §b" + config.targetEnchantmentName + 
                                    " " + level + " §f- §a" + emeraldPrice + " Zümrüt", false);
                            return true;
                        } else {
                            sendFeedback(client, "§7[LibrarianRoller] Büyü uydu ancak fiyat aralık dışı: " + emeraldPrice + " zümrüt (İstenen: " + config.minEmeralds + "-" + config.maxEmeralds + ")", false);
                        }
                    }
                }
            }
        }
        return false;
    }

    private void handleTimeout(MinecraftClient client) {
        consecutiveRetries++;
        sendFeedback(client, "§e[LibrarianRoller] Senkronizasyon uyarısı: Adım zaman aşımına uğradı, yeniden deneniyor (" + consecutiveRetries + "/" + MAX_RETRIES + ")...", false);

        if (consecutiveRetries >= MAX_RETRIES) {
            setState(RollerState.ERROR_TIMEOUT);
            stop(client, "Köylü veya kürsü ile senkronizasyon sağlanamadı. Güvenlik gereği durduruldu.");
            return;
        }

        // Güvenli kurtarma: Ekran açıksa kapat, kürsü varsa kır, başa dön
        if (client.currentScreen != null) {
            client.player.closeHandledScreen();
        }

        if (client.world.getBlockState(lecternPos).isOf(Blocks.LECTERN)) {
            setState(RollerState.BREAKING_LECTERN);
        } else {
            setState(RollerState.SELECTING_LECTERN);
        }
        stateStartTime = System.currentTimeMillis();
    }

    private int findItemSlot(MinecraftClient client, net.minecraft.item.Item item) {
        // Öncelikli olarak hotbar (0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                return i;
            }
        }
        return -1;
    }

    private int findBestAxeSlot(MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(Items.NETHERITE_AXE) || stack.isOf(Items.DIAMOND_AXE) || 
                stack.isOf(Items.IRON_AXE) || stack.isOf(Items.GOLDEN_AXE) || 
                stack.isOf(Items.STONE_AXE) || stack.isOf(Items.WOODEN_AXE)) {
                return i;
            }
        }
        return -1;
    }

    private void setState(RollerState newState) {
        this.currentState = newState;
        this.stateStartTime = System.currentTimeMillis();
    }

    private void sendFeedback(MinecraftClient client, String message, boolean overlay) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), overlay);
        }
    }
}
