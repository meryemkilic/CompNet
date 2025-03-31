# Savaş Gemisi Oyunu (Battleship Game)

## Proje Tanımı
Savaş Gemisi, klasik masa oyunu Battleship'in Java dilinde uygulanmış ve ağ üzerinden oynanabilecek bir versiyonudur. Client-server mimarisi üzerine kurulu bu uygulama, iki oyuncunun birbirine karşı oynayabileceği bir platform sunar.

## Gereksinimler
- Java 8 veya üzeri
- Ağ bağlantısı (aynı ağ üzerinde veya internet üzerinden oyun için)

## Özellikleri
- Sunucu-İstemci mimarisi ile ağ üzerinden oyun desteği
- Grafiksel kullanıcı arayüzü (GUI)
- Rastgele gemi yerleşimi
- Sezgisel oyun tahtası ve hamle sistemi
- Oyun durumu güncellemeleri
- Yeniden oyun başlatma seçeneği

## Kurulum
1. Proje dosyalarını indirin veya klonlayın
2. Java IDE'niz ile projeyi açın veya komut satırı ile derleyin:
   ```
   mvn clean package
   ```
3. Oluşturulan JAR dosyasını çalıştırın:
   ```
   java -jar target/savasgemisi-1.0-SNAPSHOT.jar
   ```

## Kullanım

### Sunucu Başlatma
1. Ana menüden "1" seçeneğini seçin
2. Sunucunun çalışacağı port numarasını girin (varsayılan: 5000)
3. Sunucu başlatıldıktan sonra, istemcilerin bağlanmasını bekleyin
4. Sunucuyu kapatmak için "quit" yazın

### İstemci Olarak Bağlanma
1. Ana menüden "2" seçeneğini seçin
2. Açılan grafiksel arayüzde:
   - Sunucu IP adresini girin
   - Port numarasını girin
   - "Bağlan" düğmesine tıklayın
3. Diğer oyuncunun bağlanmasını bekleyin
4. İki oyuncu da bağlandığında oyun otomatik olarak başlar

## Oyun Kuralları
1. Oyun, 10x10 kareden oluşan iki oyun tahtası üzerinde oynanır
2. Her oyuncunun gemileri şu boyutlardadır:
   - 1 adet 5 birimlik gemi
   - 1 adet 4 birimlik gemi
   - 2 adet 3 birimlik gemi
   - 1 adet 2 birimlik gemi
3. Oyuncular sırayla rakiplerinin tahtasındaki bir kareye ateş ederler
4. İsabet olursa kare kırmızı, iska olursa beyaz renkle işaretlenir
5. Tüm gemileri ilk batıran oyuncu kazanır

## Oynanış Adımları

### İki Oyunculu Oyun İçin Adımlar:

1. **Sunucu Kurulumu**
   - Bilgisayarlardan birinde "1" seçeneğiyle sunucu başlatılır
   - Sunucunun IP adresi not edilir

2. **Birinci Oyuncu Bağlantısı**
   - Oyuncu uygulamayı başlatır ve "2" seçeneğini seçer
   - Sunucu IP ve port bilgilerini girer
   - "Bağlan" düğmesine tıklar
   - "Rakip bekleniyor..." mesajı görüntülenir

3. **İkinci Oyuncu Bağlantısı**
   - İkinci oyuncu da uygulamayı başlatır ve "2" seçeneğini seçer
   - Aynı sunucu bilgilerini girer
   - "Bağlan" düğmesine tıklar

4. **Oyun Başlangıcı**
   - İki oyuncu bağlandığında oyun otomatik başlar
   - Gemiler rastgele yerleştirilir
   - İlk oyuncuya "Sıra sizde" mesajı görüntülenir

5. **Oyun Sırasında**
   - Sırası gelen oyuncu, rakip tahtasında bir hücreye tıklar
   - Hamle sonucu tüm oyunculara bildirilir
   - Sıra diğer oyuncuya geçer

6. **Oyun Sonu**
   - Bir oyuncunun tüm gemileri batırıldığında oyun sona erer
   - Kazanan oyuncuya "Tebrikler!" mesajı görüntülenir
   - Yeni oyun başlatma seçeneği sunulur

## Proje Mimarisi
Proje, üç ana bileşenden oluşur:

1. **Common (Ortak) Paket**:
   - Hem istemci hem de sunucu tarafından kullanılan ortak sınıflar
   - Message işleme ve Move (hamle) sınıfları

2. **Server (Sunucu) Paket**:
   - Oyun oturumlarını ve istemci bağlantılarını yöneten sunucu sınıfları
   - Oyun mantığını uygulayan sınıflar

3. **Client (İstemci) Paket**:
   - Grafiksel kullanıcı arayüzü
   - Sunucu ile iletişim sınıfları
   - Oyun durumunu görselleştirme

## Teknik Detaylar
- Java Swing kütüphanesi ile grafiksel arayüz
- Thread yapısı ile asenkron iletişim
- Socket programlama ile ağ iletişimi
- MVC (Model-View-Controller) tasarım deseni kullanımı 