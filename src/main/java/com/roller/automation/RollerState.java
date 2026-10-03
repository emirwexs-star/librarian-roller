package com.roller.automation;

public enum RollerState {
    IDLE("DURDURULDU", 0xFFAAAAAA),
    SEARCHING_VILLAGER("KÖYLÜ VE KÜRSÜ YERİ ARANIYOR...", 0xFFFFFF55),
    SELECTING_LECTERN("KÜRSÜ SEÇİLİYOR...", 0xFF55FFFF),
    PRE_PLACE_WAIT("YERLEŞTİRME ÖNCESİ BEKLENİYOR...", 0xFFFFFF55),
    PLACING_LECTERN("KÜRSÜ YERLEŞTİRİLİYOR...", 0xFF55FFFF),
    WAITING_PROFESSION("KÖYLÜNÜN MESLEK ALMASI BEKLENİYOR...", 0xFFFFAA00),
    PRE_TRADE_WAIT("TİCARET KONTROLÜ ÖNCESİ BEKLENİYOR...", 0xFFFFFF55),
    INTERACTING_VILLAGER("KÖYLÜ İLE ETKİLEŞİME GEÇİLİYOR...", 0xFF55FF55),
    CHECKING_TRADES("TİCARET KONTROL EDİLİYOR...", 0xFF55FF55),
    BREAKING_LECTERN("KÜRSÜ KIRILIYOR...", 0xFFFF5555),
    POST_BREAK_WAIT("KÜRSÜ KIRILDIKTAN SONRA BEKLENİYOR...", 0xFFFF8888),
    WAITING_RESET("MESLEK SIFIRLANMASI BEKLENİYOR...", 0xFFFFAA00),
    TARGET_FOUND("HEDEF KİTAP BULUNDU!", 0xFF55FF55),
    ERROR_TIMEOUT("ZAMAN AŞIMI VEYA HATA OLUŞTU!", 0xFFFF3333);

    private final String description;
    private final int color;

    RollerState(String description, int color) {
        this.description = description;
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public int getColor() {
        return color;
    }
}
