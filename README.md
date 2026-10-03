# 📚 Librarian Roller - Minecraft 1.21.1 Köylü Büyü Otomasyon Modu

Bu mod, Minecraft 1.21.1 (Fabric) üzerinde köylülerin kürsü (Lectern) ile edindikleri kütüphaneci (Librarian) ticaretlerini otomatik olarak tarayan, istenen büyülü kitap ve zümrüt fiyatı bulunana kadar kürsüyü kırıp tekrar yerleştiren güvenli bir istemci otomasyon modudur.

---

## 🚀 Özellikler

1. **Akıllı Durum Makinesi (State Machine):**
   - **Kürsü Yerleştirme:** Oyuncunun hotbarındaki kürsüyü otomatik algılar ve köylünün yanındaki uygun zemine yerleştirir. Kürsünün gerçekten yerleştiğini blok seviyesinde doğrular.
   - **Meslek Doğrulama:** Köylünün kütüphaneci mesleğini aldığını doğrulamadan ticarete geçmez.
   - **Ticaret Okuma (1.21 Data Components):** Köylünün sunduğu büyülü kitabı, seviyesini ve zümrüt fiyatını okur.
   - **Kürsü Kırma:** İstenen kitap bulunamazsa kürsüyü kırar (varsa baltayı seçer) ve bloğun hava (Air) olduğunu doğrular.
   - **Meslek Sıfırlanma Kontrolü:** Köylünün mesleği sıfırlanmadan (Unemployed olmadan) yeni kürsü koymaz.
   - **Watchdog / Desync Güvenliği:** Herhangi bir adımda gecikme veya lag olursa takılıp kalmaz; zaman aşımı devreye girerek adımı güvenle baştan dener.

2. **Kapsamlı Ayar Menüsü (Right Shift):**
   - **Büyü Seçimi:** Mending, Unbreaking, Protection, Sharpness, Fortune, Silk Touch, Efficiency vb. tüm büyüler.
   - **Seviye Seçimi:** İstenen büyü seviyesi (I, II, III, IV, V).
   - **Zümrüt Fiyat Filtresi:** Minimum ve maksimum zümrüt aralığı (Örn: 10 - 20 zümrüt).
   - **Zamanlama & Gecikme Ayarları (ms):**
     - Kürsü kırma süresi
     - Kırıldıktan sonra bekleme
     - Meslek sıfırlanma bekleme
     - Yerleştirme öncesi bekleme
     - Ticaret kontrol bekleme

3. **Oyun İçi Canlı HUD:**
   - Ekranın sol üst köşesinde mevcut durumu (`KÜRSÜ KIRILIYOR`, `TİCARET KONTROL EDİLİYOR`, `KİTAP BULUNDU!` vb.) ve deneme sayısını canlı gösterir.

4. **Kalıcı Yapılandırma:**
   - Tüm ayarlar `.minecraft/config/librarian_roller.json` dosyasına GSON ile otomatik kaydedilir, oyun yeniden açıldığında korunur.

---

## 🎮 Tuş Atamaları
* **Right Shift (Sağ Shift):** Ayar menüsünü açar/kapatır.
* **B Tuşu:** Otomasyonu anında başlatır veya durdurur.
*(Minecraft Ayarlar -> Denetimler -> Tuş Atamaları menüsünden dilediğiniz gibi değiştirebilirsiniz).*

---

## 🛠️ Nasıl Derlenir (.jar Oluşturma)?

1. Proje klasöründeki `build.bat` dosyasına çift tıklayın veya bir komut satırında şu komutu çalıştırın:
   ```bash
   ./gradlew build
   ```
2. Derleme tamamlandığında mod dosyanız şu konumda hazır olacaktır:
   `build/libs/librarian-roller-1.0.0.jar`
3. Bu `.jar` dosyasını `.minecraft/mods` klasörünüze atıp Fabric Loader 1.21.1 ile oyuna girebilirsiniz!
