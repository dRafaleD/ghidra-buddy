# Ghidra Companion - Kullanıcı ve Başlangıç Rehberi

Bu rehber, Ghidra'yı ilk defa kullanan bir analistin **Ghidra Companion** eklentisiyle ilk analizini nasıl yapacağını adım adım açıklar.

---

## 1. İlk Analiz Yolculuğu (5 Adımlı İş Akışı)

### Adım 1: İkili Dosyayı Açma ve Otomatik Analiz
1. Ghidra Proje Yöneticisinde `File -> Import File` seçeneği ile analiz etmek istediğiniz çalıştırılabilir dosyayı (örneğin `samples/beginner_sample/main.c` derlemesini veya herhangi bir PE/ELF dosyasını) projeye ekleyin.
2. Dosyaya çift tıklayarak **CodeBrowser** aracını açın.
3. Ghidra size *"Would you like to analyze it now?"* sorusunu yönelttiğinde **Yes** deyin.
4. Varsayılan analiz seçeneklerini işaretli bırakıp **Analyze** butonuna basın.
5. Sağ alt köşedeki analiz göstergesinin tamamlanmasını bekleyin.

### Adım 2: Giriş Noktası (`entry` / `main`) Tespiti
1. Sol taraftaki **Symbol Tree** penceresini açın.
2. `Functions` klasörünü genişletin.
3. C/C++ programlarında başlangıç noktası genellikle `entry` sembolüdür. `entry` fonksiyonuna tıkladığınızda sağdaki **Öğrenme Asistanı** paneli:
   - Fonksiyonun başlangıç adresini,
   - Kaç adet parametre aldığını,
   - Çağrı kuralını (Calling Convention) anında gösterir.
4. `entry` içindeki çağrıları (CALL) inceleyerek gerçek `main` fonksiyonunu bulun.

### Adım 3: Metin Sabitlerini (Strings) ve XREF'leri İnceleme
1. Üst menüden `Window -> Defined Strings` penceresini açın.
2. Filtre kutusuna uygulamanın ekrana basabileceği kelimeleri (örneğin `"Parola"`, `"Lisans"`, `"Hata"`, `"Basarili"`) yazın.
3. Bulduğunuz bir metne tıkladığınızda **Öğrenme Asistanı**:
   - Bu metnin uzunluğunu ve karakter kodlamasını açıklar.
   - Bu metne yapılan çapraz referans (XREF) sayısını gösterir.
   - Paneldeki **"İlk Referansa Git"** butonuna basarak bu metni kullanan kod satırına 1 tıkla geçmenizi sağlar.

### Adım 4: Assembly ve Decompiler Karşılaştırması
1. Listing penceresinde bir satıra tıkladığınızda sağ taraftaki Decompiler penceresi eşzamanlı olarak karşılık gelen C satırını vurgular.
2. Örneğin bir `CMP eax, 0x1` komutunun decompiler'da `if (uVar1 == 1)` ifadesine nasıl dönüştüğünü görün.
3. Öğrenme Asistanı kartındaki **"Bu Nedir?"** ve **"Neden Önemli?"** açıklamalarını okuyarak komutun işlemci bayraklarına (ZF, SF) etkisini öğrenin.

### Adım 5: Değişkenleri Yeniden Adlandırma (L Tuşu)
1. Decompiler veya Listing'de `local_10` veya `uVar1` gibi anlamsız bir değişkenin üzerine gelin.
2. Klavyeden **`L` (Rename)** tuşuna basarak değişkene anlamlı bir isim verin (örneğin `kullanici_anahtari`).
3. Kodun üzerine not bırakmak için **`;` (Semicolon)** tuşuna basarak açıklama ekleyin.
4. Ghidra tüm bu değişiklikleri anında projenize kaydeder; mod değiştirseniz bile bu veriler asla kaybolmaz.

---

## 2. Çalışma Alanı Modları (Beginner vs Advanced)

Öğrenme Asistanı penceresinin üst tarafındaki **"Modu Değiştir"** butonuyla:
* **Başlangıç Modu:** İncelediğiniz her nesne için eğitici açıklamalar, kontrol listesi ve sözlük ipuçları aktiftir.
* **Uzman Modu:** Ghidra'nın tüm araçlarına doğrudan, tam ve kısıtlamasız erişim devam eder.

Her iki mod da aynı proje veritabanı üzerinde çalışır; mod geçişi sırasında analiz sonuçları, yorumlar ve etiketler **hiçbir kayba uğramaz**.
