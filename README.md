# Ghidra Companion

> **Tersine Mühendisliğe Yeni Başlayanlar İçin Yaklaşılabilir, Eğitici Ghidra Eklentisi**
> 
> *Ghidra için bağımsız ve açık kaynaklı bir öğrenme asistanı ve bağlam rehberi.*

---

## 🌟 Vizyon ve Amaç

Ghidra son derece güçlü bir tersine mühendislik aracıdır; ancak ilk defa açanlar için onlarca karmaşık pencere, yüzlerce kısayol ve anlaşılması güç semboller nedeniyle korkutucu olabilir.

**Ghidra Companion**, Ghidra'nın gelişmiş analiz yeteneklerini gizlemeden veya kısıtlamadan:
1. **Ne gördüğünüzü açıklar** (*Bu fonksiyon başlığı mı, API çağrısı mı, yığın değişkeni mi, metin sabiti mi?*).
2. **Neden önemli olduğunu gösterir** (*Bu kontrol akış kararı, parola kontrolü veya güvenlik kontrolü nasıl çalışıyor?*).
3. **Sıradaki analiz adımını önerir** (*1 tıkla referanslara gitme, sembol yeniden adlandırma veya yorum ekleme*).
4. **Başlangıç ve Uzman modları arasında anında geçiş sunar** (Veritabanını bozmadan arayüzü sadeleştirir).
5. **Çevrimdışı ve güvenlidir** (Hiçbir harici bulut/yapay zeka servisine bağlanmaz, tüm analizler yerel Ghidra modelleri üzerinden deterministik olarak çalışır).

---

## 🚀 Özellikler

### 1. 🎯 Canlı Bağlam Analizi (Context Inspector)
Ghidra'nın **Listing (Assembly)** veya **Decompiler (C Pseudocode)** penceresinde imlecinizi hareket ettirdiğinizde, yan paneldeki **Öğrenme Asistanı** anında güncellenir:
- **Fonksiyon Tanımları:** İmza, parametre sayısı, çağrı kuralı (Calling Convention), XREF sayısı.
- **Dış Kütüphane / API Çağrıları:** Windows/Linux sistem API'leri ve işlevleri.
- **Koşullu Atlama Komutları (Branches):** `JZ`, `JNZ`, `JE`, `JG`, `CMP`, `TEST` komutlarının anlamı.
- **Yığın Değişkenleri:** Stack ofseti, değişken tipi, yerel değişken amacı.
- **Sabit Metinler (Strings):** Metin içeriği ve bu metni kullanan kod bloklarına 1 tıkla gitme bağlantısı.

### 2. 📋 Başlangıç Rehberi & Analiz Kontrol Listesi
Tersine mühendislik adımlarını yapılandırılmış bir kontrol listesiyle öğrenin:
- Adım 1: İkili Dosyayı Yükleme ve Otomatik Analiz
- Adım 2: Giriş Noktası (`entry` / `main`) Tespiti
- Adım 3: Metin Sabitlerini (`Defined Strings`) ve XREF'leri İnceleme
- Adım 4: Assembly ve Decompiler Karşılaştırması
- Adım 5: Değişken ve Fonksiyonları Yeniden Adlandırma (`L` tuşu)

### 3. 📖 Arama Destekli Çevrimdışı RE Sözlüğü
Tersine mühendislik terimlerini doğrudan Ghidra içinden arayın:
- `XREF (Cross-Reference)`
- `Decompiler vs Disassembler`
- `Stack Frame (Yığın Çerçevesi)`
- `Calling Conventions (cdecl, stdcall, fastcall, x64)`
- `PE / ELF Bölümleri (.text, .data, .rdata)`
- `IAT (Import Address Table)`
- `Endianness`
- `Control Flow Graph (CFG)`

### 4. 🔄 Başlangıç ve Uzman Çalışma Alanı Modları
Tek tıkla **Başlangıç Modu** ve **Uzman Modu** arasında geçiş yapın. Geçiş sırasında Ghidra projesindeki analizler, eklenen yorumlar ve yeniden adlandırmalar **%100 korunur**.

---

## 🛠️ Kurulum

### Ön Koşullar
* **Ghidra:** 12.1.3 veya üzeri
* **Java:** OpenJDK 21 LTS

### Eklentiyi Ghidra'ya Yükleme
1. `dist/` klasöründeki derlenmiş eklenti arşivini (`ghidra_12.1.3_PUBLIC_*_GhidraCompanion.zip`) bulun.
2. Ghidra ana penceresinde üst menüden **File → Install Extensions...** seçeneğine tıklayın.
3. Sağ üstteki yeşil **+ (Add Extension)** butonuna basın ve `dist/` altındaki `.zip` dosyasını seçin.
4. Eklentiyi işaretleyip **OK** deyin ve Ghidra'yı yeniden başlatın.
5. Bir ikili dosya açıp `CodeBrowser` aracına girdiğinizde, sağ tarafta **Öğrenme Asistanı** panelini göreceksiniz. (Görünmüyorsa: `Window → Öğrenme Asistanı` menüsünden açabilirsiniz).

---

## 🧪 Kaynak Koddan Derleme ve Test

Önce Ghidra kurulum dizininizi ortam değişkeni olarak tanımlayın (PowerShell: `$env:GHIDRA_INSTALL_DIR = "C:\ghidra_12.1.3_PUBLIC"`). Ardından yerleşik Gradle wrapper'ı kullanabilirsiniz:

```bash
# Eklentiyi derleme ve testleri çalıştırma
.\gradlew.bat test

# Dağıtım ZIP paketini oluşturma
.\gradlew.bat buildExtension
```

Oluşturulan ZIP paketi `dist/` dizinine kaydedilir.

### Örnek program

`samples/beginner_sample/main.c` dosyasını kendiniz derleyin (ör. `gcc main.c -o beginner_sample.exe`) ve Ghidra'ya aktarın.

### Masaüstü arayüzü (`desktop/`)

`desktop/` klasörü, eklentinin yerel köprüsüne (`127.0.0.1:19842`) bağlanan isteğe bağlı bir React/Tauri arayüzüdür. Köprü yalnızca bu makineden ve izin verilen kaynaklardan (`localhost:5173`, Tauri) gelen isteklere yanıt verir; Ghidra çalışmıyorsa arayüz demo verisi gösterir.

> **Durum:** Beta. Birim testler geçmektedir ancak eklenti Ghidra içinde sınırlı olarak denenmiştir.

---

## 📁 Proje Mimarisi

```text
ghidragorunum/
├── build.gradle               # Ghidra 12.1.3 buildExtension entegrasyonu
├── extension.properties        # Eklenti metaverileri
├── Module.manifest            # Modül bildirimi
├── src/
│   ├── main/java/ghidracompanion/
│   │   ├── GhidraCompanionPlugin.java           # Eklenti yaşam döngüsü ve olay dinleyicisi
│   │   ├── model/
│   │   │   ├── CodeContext.java                 # Bağlam modeli ve eylem önerileri
│   │   │   ├── GlossaryEntry.java               # Sözlük kavram modeli
│   │   │   └── WalkthroughStep.java             # Analiz rehberi adım modeli
│   │   ├── service/
│   │   │   ├── ContextAnalysisService.java      # Deterministik kural motoru
│   │   │   ├── GlossaryService.java             # Çevrimdışı kavram veritabanı
│   │   │   ├── WalkthroughService.java          # Başlangıç yol haritası yönetimi
│   │   │   └── WorkspaceManager.java            # Sıfır veri kaybı ile mod geçişi
│   │   └── ui/
│   │       ├── ContextCard.java                 # Açıklama kartı Swing bileşeni
│   │       ├── LearningAssistantProvider.java   # Ghidra dockable pencere sağlayıcısı
│   │       └── LearningPanel.java               # 3 sekmeli ana kullanıcı arayüzü
│   └── test/java/ghidracompanion/              # JUnit 5 otomatik birim testleri
├── samples/beginner_sample/                    # Güvenli, zararsız eğitim C ikilisi
└── dist/                                       # Derlenmiş kuruluma hazır .zip paketi
```

---

## 📄 Lisans ve Sorumluluk Reddi

* Bu proje **Apache License 2.0** kapsamında lisanslanmıştır.
* **Ghidra**, National Security Agency (NSA) tarafından geliştirilmektedir. Bu proje resmi bir NSA veya Ghidra projesi değildir; bağımsız bir eğitim eklentisidir.
