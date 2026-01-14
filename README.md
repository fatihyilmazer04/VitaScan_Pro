🛡️ VitaScan Pro - Akıllı Gıda Analiz ve Sağlık Takip Sistemi
VitaScan Pro, tüketicilerin gıda okuryazarlığını artırmak ve kişisel sağlık verilerini takip etmelerini sağlamak amacıyla geliştirilmiş kapsamlı bir Android mobil uygulamasıdır. Kullanıcılar, ürün barkodlarını taratarak veya isimle aratarak gıda içeriklerine, alerjen uyarılarına ve detaylı ürün bilgilerine anlık olarak ulaşabilirler. [User context, cite: 1]

🚀 Öne Çıkan Özellikler
Anlık Barkod Tarama & Analiz: OpenFoodFacts API entegrasyonu ile küresel gıda veritabanına erişim ve saniyeler içinde ürün içeriği listeleme.

Kişiselleştirilmiş Sağlık Profili: Kullanıcıya özel alerjen tercihleri (Süt, Gluten, Yer Fıstığı vb.) ve bu tercihlere göre otomatik ürün uyarı sistemi. [User context]

Vücut Kitle İndeksi (VKE) Takibi: Kullanıcı verileri (boy/kilo) üzerinden anlık VKE hesaplaması ve sağlık durumu değerlendirmesi. [User context]

Wikipedia Entegreli Rehber: Gıda katkı maddeleri ve sağlık terimleri için Wikipedia REST API üzerinden canlı bilgi çekme özelliği.

Akıllı Geçmiş Yönetimi: Yapılan tüm taramaların tarih ve saat bilgisiyle birlikte yerel hafızada (offline) saklanması.

🛠️ Kullanılan Teknolojiler ve Kütüphaneler
Dil: Java

Mimari: MVC (Model-View-Controller) & Single Activity Design Pattern

Ağ İstekleri: Retrofit 2 & OkHttp

Veri İşleme: GSON (JSON Serialization/Deserialization)

Yerel Depolama: SharedPreferences

Arayüz Bileşenleri: Material Design, RecyclerView, CardView, BottomNavigationView

📊 Performans Değerlendirmesi
Android Studio Profiler araçları ile yapılan testlerde uygulamanın yüksek verimlilikle çalıştığı doğrulanmıştır: [User context, cite: 33]

Bellek (RAM) Kullanımı: Aktif kullanımda ortalama 105.1 MB (Optimize edilmiş bellek yönetimi). [User context]

İşlemci (CPU) Yükü: Asenkron veri işleme sayesinde boşta %0, yük altında maksimum %2. [User context]

Ağ Gecikmesi (Latency): API yanıtlarının işlenmesi ve ekrana yansıması ortalama 450-550 ms.

<img width="355" height="794" alt="Ekran görüntüsü 2026-01-14 050255" src="https://github.com/user-attachments/assets/0ca484ac-c464-4de0-88a2-17f9c65e895f" />
<img width="353" height="788" alt="Ekran görüntüsü 2026-01-14 050149 - Kopya" src="https://github.com/user-attachments/assets/cf540532-024f-4498-8a50-148597ef5c2a" />
<img width="356" height="784" alt="Ekran görüntüsü 2026-01-14 050133" src="https://github.com/user-attachments/assets/29555391-70e1-47c8-9b81-af526e7b6be1" />
<img width="357" height="784" alt="Ekran görüntüsü 2026-01-14 050049" src="https://github.com/user-attachments/assets/1818de83-2023-4201-96e6-976215979b0a" />
<img width="348" height="787" alt="Ekran görüntüsü 2026-01-14 045704 - Kopya" src="https://github.com/user-attachments/assets/2b44bcab-57b7-43fa-b608-328e4cbf99f9" />
<img width="359" height="788" alt="Ekran görüntüsü 2026-01-14 045423" src="https://github.com/user-attachments/assets/77c1c78b-e36d-454e-bd4e-b016b508aa2f" />
<img width="348" height="791" alt="Ekran görüntüsü 2026-01-14 045151" src="https://github.com/user-attachments/assets/f38fccd2-3818-4db2-b65c-e65ba48394cf" />
<img width="354" height="787" alt="Ekran görüntüsü 2026-01-14 050303 - Kopya" src="https://github.com/user-attachments/assets/102adca8-aa32-4624-a0a4-fdd840014765" />



🛠️ Kurulum
Bu depoyu klonlayın: git clone https://github.com/KULLANICI_ADIN/VitaScan_Pro.git

Android Studio'yu açın ve projeyi içe aktarın.

build.gradle dosyasındaki bağımlılıkların yüklenmesini bekleyin.

Bir emülatör veya gerçek Android cihaz üzerinde çalıştırın.

Geliştirici: Fatih Yılmazer

Proje Durumu: Tamamlandı / Eğitim Amaçlı Geliştirilmiştir.
