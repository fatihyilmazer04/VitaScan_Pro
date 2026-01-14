package com.fatihyilmazer.vitascanpro;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast; // Mesaj göstermek için lazım
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GuideFragment extends Fragment {

    EditText etSearch;
    LinearLayout layoutContent;
    List<GuideItem> tumBilgiler = new ArrayList<>();

    Handler handler = new Handler();
    Runnable runnable;

    // --- FİLTRE KELİMELERİNİ ARTIRDIM ---
    // --- GENİŞLETİLMİŞ FİLTRE (Gıdalar + Sağlık) ---
    String[] saglikKelimeleri = {
            // 1. Tıbbi ve Biyolojik Terimler
            "sağlık", "hastalık", "tedavi", "vücut", "organ", "hücre", "genetik",
            "kan", "kemik", "kas", "beyin", "kalp", "mide", "bağırsak", "cilt", "deri",
            "semptom", "tanı", "ilaç", "ağrı", "enfeksiyon", "bakteri", "virüs",
            "metabolizma", "alerji", "sendrom", "zehir", "toksin", "yarar", "zarar",
            "kimyasal", "madde", "bileşik", "element", "molekül", "asit", "enzim",

            // 2. Besin Değerleri
            "vitamin", "mineral", "protein", "karbonhidrat", "yağ", "kalori", "enerji",
            "lif", "şeker", "tuz", "kolesterol", "glikoz", "kafein", "kalsiyum",

            // 3. Yiyecekler ve Yemekler (Pizza, Hamburger vb. için)
            "yemek", "yiyecek", "gıda", "besin", "mutfak", "sofra", "öğün", "kahvaltı",
            "tatlı", "tuzlu", "ekşi", "acı", "hamur", "un", "ekmek", "makarna", "pizza",
            "sandviç", "çorba", "sos", "baharat", "yağ", "kızartma", "haşlama",
            "et", "tavuk", "balık", "süt", "peynir", "yoğurt", "yumurta",

            // 4. İçecekler (Kola, Kahve, Çay vb. için)
            "içecek", "meşrubat", "gazlı", "sıvı", "su", "çay", "kahve", "alkol", "şarap", "bira",
            "meyve suyu", "soda", "şişe", "bardak",

            // 5. Bitkiler ve Doğal Ürünler
            "bitki", "meyve", "sebze", "tohum", "kök", "yaprak", "çiçek", "ağaç",
            "tahıl", "baklagil", "kuruyemiş", "tarım", "organik"
    };;

    class GuideItem {
        String baslik, aciklama, etiketler;
        int renkKodu;

        GuideItem(String b, String a, String e, int r) {
            this.baslik = b;
            this.aciklama = a;
            this.etiketler = e.toLowerCase();
            this.renkKodu = r;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide, container, false);

        etSearch = view.findViewById(R.id.etSearchGuide);
        layoutContent = view.findViewById(R.id.layoutContent);

        verileriDoldur();
        listeyiGuncelle(tumBilgiler);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Yerel arama
                filtrele(s.toString().toLowerCase());

                // İnternet araması (Gecikmeli)
                if (runnable != null) handler.removeCallbacks(runnable);
                runnable = () -> {
                    String aranan = s.toString().trim();
                    if (aranan.length() > 2) { // En az 3 harf
                        // Kullanıcıya arandığını hissettir
                        Toast.makeText(getActivity(), "İnternette aranıyor: " + aranan, Toast.LENGTH_SHORT).show();
                        wikipediaDanGetir(aranan);
                    }
                };
                handler.postDelayed(runnable, 1000);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void filtrele(String aranacak) {
        List<GuideItem> filtrelenmis = new ArrayList<>();
        if (aranacak.isEmpty()) {
            listeyiGuncelle(tumBilgiler);
            return;
        }
        for (GuideItem item : tumBilgiler) {
            if (item.baslik.toLowerCase().contains(aranacak) || item.etiketler.contains(aranacak)) {
                filtrelenmis.add(item);
            }
        }
        listeyiGuncelle(filtrelenmis);
    }

    // --- GÜNCELLENMİŞ VE KONUŞKAN WIKIPEDIA FONKSİYONU ---
    // --- KESİN ÇÖZÜM İÇİN YENİLENMİŞ FONKSİYON ---
    private void wikipediaDanGetir(String kelime) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://tr.wikipedia.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WikiApiService service = retrofit.create(WikiApiService.class);

        // "Grip" gibi kelimelerin ilk harfini büyüt (Wikipedia kuralı)
        String duzgunKelime = kelime.substring(0, 1).toUpperCase() + kelime.substring(1);

        service.getSummary(duzgunKelime).enqueue(new Callback<WikiResponse>() {
            @Override
            public void onResponse(Call<WikiResponse> call, Response<WikiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WikiResponse wiki = response.body();

                    // Gelen veri boş değilse ve "Anlam ayrımı" sayfası değilse
                    if (wiki.extract != null && !wiki.extract.isEmpty() && !wiki.type.equals("disambiguation")) {

                        if (sagliklaIlgiliMi(wiki.extract)) {
                            kartEkle(wiki.title + " (İnternet)", wiki.extract, 4);
                            Toast.makeText(getActivity(), "✅ Bilgi bulundu!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "⚠️ Bulundu ama sağlıkla ilgisiz: " + wiki.title, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "❌ Wikipedia'da tam karşılığı bulunamadı.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Eğer 404 hatası gelirse (Sayfa yoksa)
                    Toast.makeText(getActivity(), "🔍 Sonuç bulunamadı (Kod: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WikiResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "🚫 Bağlantı Hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean sagliklaIlgiliMi(String metin) {
        String kucukMetin = metin.toLowerCase();
        if (kucukMetin.contains("anlamına gelebilir")) return false;

        for (String anahtarKelime : saglikKelimeleri) {
            if (kucukMetin.contains(anahtarKelime)) return true;
        }
        return false;
    }

    private void listeyiGuncelle(List<GuideItem> liste) {
        if(layoutContent == null) return;
        layoutContent.removeAllViews();
        for (GuideItem item : liste) {
            kartEkle(item.baslik, item.aciklama, item.renkKodu);
        }
    }

    private void kartEkle(String baslik, String aciklama, int renkKodu) {
        if(getActivity() == null) return;

        CardView card = new CardView(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 40);
        card.setLayoutParams(params);
        card.setRadius(40);
        card.setCardElevation(8);

        LinearLayout innerLayout = new LinearLayout(getActivity());
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(50, 50, 50, 50);

        if (renkKodu == 1) innerLayout.setBackgroundResource(R.drawable.gradient_turuncu);
        else if (renkKodu == 2) innerLayout.setBackgroundResource(R.drawable.gradient_kirmizi);
        else if (renkKodu == 3) innerLayout.setBackgroundResource(R.drawable.gradient_mor);
        else innerLayout.setBackgroundColor(Color.parseColor("#1976D2"));

        TextView tvTitle = new TextView(getActivity());
        tvTitle.setText(baslik);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.WHITE);

        TextView tvDesc = new TextView(getActivity());
        tvDesc.setText(aciklama);
        tvDesc.setTextSize(15);
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setPadding(0, 20, 0, 0);

        innerLayout.addView(tvTitle);
        innerLayout.addView(tvDesc);
        card.addView(innerLayout);

        if (renkKodu == 4) layoutContent.addView(card, 0);
        else layoutContent.addView(card);
    }

    private void verileriDoldur() {
        tumBilgiler.clear();

        // 🔴 BÖLÜM 1: ZARARLI KATKI MADDELERİ VE E-KODLARI (Kırmızı)
        tumBilgiler.add(new GuideItem("⚠️ E621 (MSG)", "Çin tuzu. Beyni kandırarak doyma hissini yok eder. Obezite ve migren tetikleyicisidir.", "msg çin tuzu e621 glutamat", 2));
        tumBilgiler.add(new GuideItem("⚠️ E120 (Karmin)", "Böceklerden elde edilen kırmızı boyadır. Yoğurt ve kozmetikte kullanılır. Alerjiktir.", "böcek boyası karmin e120", 2));
        tumBilgiler.add(new GuideItem("⚠️ E951 (Aspartam)", "Yapay tatlandırıcı. Şekerden 200 kat tatlıdır. Kanserojen risk taşır.", "tatlandırıcı diyet aspartam", 2));
        tumBilgiler.add(new GuideItem("⚠️ E250 (Sodyum Nitrit)", "Salam, sosis gibi işlenmiş etlerde bulunur. Kanserojen riski artırır.", "salam sosis nitrit", 2));
        tumBilgiler.add(new GuideItem("⚠️ E102 (Tartrazin)", "Sarı gıda boyası. Çocuklarda hiperaktiviteye yol açabilir.", "boya sarı tartrazin", 2));
        tumBilgiler.add(new GuideItem("⚠️ E211 (Sodyum Benzoat)", "Gazlı içeceklerde koruyucudur. Hücre hasarına yol açabilir.", "asitli içecek koruyucu", 2));
        tumBilgiler.add(new GuideItem("⚠️ Yüksek Fruktozlu Şurup", "Mısır şurubu (NBŞ). Karaciğer yağlanmasının 1 numaralı sebebidir.", "mısır şurubu glikoz", 2));
        tumBilgiler.add(new GuideItem("⚠️ Trans Yağlar", "Damar tıkanıklığı yapan en tehlikeli yağdır. Margarinlerde bulunur.", "margarin damar kalp", 2));
        tumBilgiler.add(new GuideItem("⚠️ Akrilamid", "Yüksek ısıda kızaran nişastalı gıdalarda (Cips, patates) oluşan kanserojen maddedir.", "kızartma cips", 2));

        // 🧴 BÖLÜM 2: KOZMETİK & KİŞİSEL BAKIM (YENİ EKLENDİ!)
        tumBilgiler.add(new GuideItem("⚠️ SLS / SLES", "Şampuan ve diş macununda köpürtücü olarak kullanılır. Cildi tahriş eder, saç dökebilir.", "şampuan sabun köpük deterjan", 2));
        tumBilgiler.add(new GuideItem("⚠️ Parabenler", "Kozmetiklerde raf ömrünü uzatır. Hormon sistemini bozabilir (Endokrin bozucu).", "krem şampuan koruyucu", 2));
        tumBilgiler.add(new GuideItem("⚠️ Alüminyum (Deodorant)", "Ter gözeneklerini tıkar. Meme kanseri ve Alzheimer riskiyle ilişkilendirilmektedir.", "ter koku koltuk", 2));
        tumBilgiler.add(new GuideItem("🦷 Florür", "Diş çürüklerini önler ancak fazlası yutulursa zehirlidir ve beyne zarar verebilir.", "diş macun", 3));
        tumBilgiler.add(new GuideItem("⚠️ Triklosan", "Diş macunu ve sabunlarda antibakteriyel olarak kullanılır. Hormonları bozabilir.", "diş sabun bakteri", 2));
        tumBilgiler.add(new GuideItem("⚠️ Mineral Yağlar (Parafin)", "Petrolden elde edilir. Cildin nefes almasını engeller, gözenekleri tıkar. Bebek yağlarında bulunur.", "cilt krem petrol", 2));
        tumBilgiler.add(new GuideItem("⚠️ Formaldehit", "Ojelerde ve saç düzleştiricilerde bulunur. Kesin kanserojen maddelerdendir.", "oje saç", 2));
        tumBilgiler.add(new GuideItem("⚠️ PFOA (Teflon)", "Yapışmaz tavalarda çizilince ortaya çıkar. Kanserojendir.", "tava mutfak", 2));
        tumBilgiler.add(new GuideItem("🧴 Güneş Kremi (Kimyasal)", "Oksibenzon içeren kremler kana karışabilir. Fiziksel (Çinko oksit) koruyucular daha güvenlidir.", "güneş krem", 3));
        tumBilgiler.add(new GuideItem("🧼 Antibakteriyel Sabun", "Yararlı bakterileri de öldürür ve bağışıklığı zayıflatabilir.", "sabun temizlik", 3));
        tumBilgiler.add(new GuideItem("💄 Kurşun (Ruj)", "Bazı kalitesiz rujlarda ağır metal (kurşun) bulunabilir. Sinir sistemine zararlıdır.", "makyaj ruj", 2));

        // 🟠 BÖLÜM 3: VİTAMİNLER (Turuncu)
        tumBilgiler.add(new GuideItem("☀️ D Vitamini", "Bağışıklığın anahtarıdır. Kaynağı Güneş'tir.", "güneş kemik bağışıklık", 1));
        tumBilgiler.add(new GuideItem("💊 B12 Vitamini", "Unutkanlığı önler. Sadece hayvansal gıdalarda bulunur.", "hafıza et yumurta", 1));
        tumBilgiler.add(new GuideItem("🍊 C Vitamini", "Grip savardır. Portakal ve biberde boldur.", "bağışıklık grip meyve", 1));
        tumBilgiler.add(new GuideItem("🥕 A Vitamini", "Göz sağlığı için kritiktir. Havuçta bulunur.", "göz havuç", 1));
        tumBilgiler.add(new GuideItem("🥑 E Vitamini", "Cilt ve saç sağlığı için önemlidir.", "cilt saç güzellik", 1));
        tumBilgiler.add(new GuideItem("🩸 K Vitamini", "Kanın pıhtılaşmasını sağlar.", "kan ıspanak", 1));
        tumBilgiler.add(new GuideItem("🧠 B6 Vitamini", "Mutluluk hormonu üretimi için gereklidir.", "beyin mutluluk", 1));

        // 🟣 BÖLÜM 4: MİNERALLER (Mor)
        tumBilgiler.add(new GuideItem("💪 Magnezyum", "Kas kramplarını önler, uykuyu düzenler.", "kas uyku kramp", 3));
        tumBilgiler.add(new GuideItem("🦴 Kalsiyum", "Kemik ve diş sağlığı için şarttır.", "kemik diş süt", 3));
        tumBilgiler.add(new GuideItem("🔴 Demir", "Kansızlığı önler. Kırmızı ette bulunur.", "kan kansızlık", 3));
        tumBilgiler.add(new GuideItem("⚡ Çinko", "Yaraların iyileşmesi ve saç sağlığı için önemlidir.", "yara saç", 3));
        tumBilgiler.add(new GuideItem("🧂 İyot", "Tiroid bezinin çalışması ve zeka için gereklidir.", "tiroid tuz zeka", 3));

        // 🟠 BÖLÜM 5: SÜPER GIDALAR (Turuncu)
        tumBilgiler.add(new GuideItem("🥦 Brokoli", "Kanser savaşçısıdır.", "sebze kanser", 1));
        tumBilgiler.add(new GuideItem("🥚 Yumurta", "En kaliteli proteindir. Tok tutar.", "protein kahvaltı", 1));
        tumBilgiler.add(new GuideItem("🐟 Somon", "Omega-3 deposudur. Beyin dostudur.", "balık beyin", 1));
        tumBilgiler.add(new GuideItem("🥑 Avokado", "Sağlıklı yağlar içerir. Cildi güzelleştirir.", "yağ cilt", 1));
        tumBilgiler.add(new GuideItem("🌰 Ceviz", "Beyne iyi gelir. Hafızayı güçlendirir.", "beyin hafıza", 1));
        tumBilgiler.add(new GuideItem("🍵 Yeşil Çay", "Metabolizmayı hızlandırır, yağ yakar.", "zayıflama diyet", 1));
        tumBilgiler.add(new GuideItem("🫐 Yaban Mersini", "Yaşlanmayı geciktirir (Antioksidan).", "gençlik meyve", 1));
        tumBilgiler.add(new GuideItem("🥛 Kefir", "Bağırsak dostudur. Sindirimi düzenler.", "probiyotik bağırsak", 1));
        tumBilgiler.add(new GuideItem("🍫 Bitter Çikolata", "Kalp dostudur ve mutluluk verir.", "tatlı kalp", 1));
        tumBilgiler.add(new GuideItem("🧄 Sarımsak", "Doğal antibiyotiktir. Tansiyonu düşürür.", "antibiyotik tansiyon", 1));
        tumBilgiler.add(new GuideItem("🍎 Elma Sirkesi", "Kan şekerini dengeler ve yağ yakımını destekler.", "diyet zayıflama", 1));
        tumBilgiler.add(new GuideItem("🥥 Hindistan Cevizi Yağı", "Metabolizmayı hızlandırabilir. Alzheimer'a karşı koruyabilir.", "yağ diyet", 1));
        tumBilgiler.add(new GuideItem("🍵 Zencefil", "Mide bulantısına iyi gelir, bağışıklığı güçlendirir.", "mide grip", 1));
        tumBilgiler.add(new GuideItem("🍂 Zerdeçal", "Vücuttaki iltihabı kurutur.", "sağlık iltihap", 1));

        // 🟣 BÖLÜM 6: HASSASİYETLER & DİYET (Mor)
        tumBilgiler.add(new GuideItem("🍞 Gluten", "Çölyak hastaları yiyemez (Buğday, arpa).", "alerji ekmek", 3));
        tumBilgiler.add(new GuideItem("🥛 Laktoz", "Süt şekerini sindirememe durumudur. Şişkinlik yapar.", "süt alerji", 3));
        tumBilgiler.add(new GuideItem("🌱 Vegan", "Hayvansal ürün tüketilmez.", "diyet bitkisel", 3));
        tumBilgiler.add(new GuideItem("🥩 Keto Diyet", "Düşük karbonhidrat, yüksek yağ diyeti.", "zayıflama diyet", 3));
        tumBilgiler.add(new GuideItem("🍽️ Aralıklı Oruç", "Belirli saatlerde aç kalarak vücudu dinlendirme.", "if fasting", 3));
        tumBilgiler.add(new GuideItem("💉 İnsülin Direnci", "Şeker hastalığının öncüsüdür. Karbonhidrat azaltılmalıdır.", "diyabet şeker", 3));

        // 🔴 BÖLÜM 7: ZARARLI ALIŞKANLIKLAR (Kırmızı)
        tumBilgiler.add(new GuideItem("🧂 Aşırı Tuz", "Tansiyon ve böbrek hastası yapar.", "tansiyon böbrek", 2));
        tumBilgiler.add(new GuideItem("🍬 Şeker", "Obezite ve diyabetin baş sebebidir. Bağımlılık yapar.", "diyabet kilo", 2));
        tumBilgiler.add(new GuideItem("☕ Kafein Fazlası", "Çarpıntı ve uykusuzluk yapar.", "kahve kalp", 2));
        tumBilgiler.add(new GuideItem("🥤 Kola ve Gazlılar", "Mide asidini bozar, kemik erimesi yapar.", "mide kemik asit", 2));
        tumBilgiler.add(new GuideItem("🚬 Sigara", "Kanser sebebidir. Cildi yaşlandırır.", "zararlı kanser", 2));
        tumBilgiler.add(new GuideItem("🍺 Alkol", "Karaciğeri bitirir, beyin hücrelerini öldürür.", "karaciğer zararlı", 2));
        tumBilgiler.add(new GuideItem("🧊 İşlenmiş Gıda", "Paketli ürünler boş kaloridir.", "zararlı abur cubur", 2));
        tumBilgiler.add(new GuideItem("🌭 İşlenmiş Et", "Salam, sosis kanserojen gruptadır.", "kanser et", 2));

        // 🟠 BÖLÜM 8: YAŞAM TARZI (Turuncu)
        tumBilgiler.add(new GuideItem("💧 Su İçmek", "Metabolizmayı hızlandırır. Günde 2.5 litre içilmelidir.", "zayıflama su", 1));
        tumBilgiler.add(new GuideItem("🚶 Yürüyüş", "Günde 10.000 adım kalp riskini azaltır.", "spor kalp", 1));
        tumBilgiler.add(new GuideItem("💤 Uyku", "Vücudu yeniler. Günde 7-8 saat uyunmalı.", "dinlenme sağlık", 1));
        tumBilgiler.add(new GuideItem("🧊 Soğuk Duş", "Bağışıklığı artırır, cildi sıkılaştırır.", "sağlık cilt", 1));
        tumBilgiler.add(new GuideItem("🧴 Güneş Kremi", "Cilt kanserinden korur. Kışın bile sürülmeli.", "cilt kanser", 1));
        tumBilgiler.add(new GuideItem("🛌 Yastık Kılıfı", "Haftada bir değişmezse sivilce yapar.", "cilt", 3));
        tumBilgiler.add(new GuideItem("🦷 Diş İpi", "Arayüz çürüklerini ve ağız kokusunu önler.", "diş", 1));
        tumBilgiler.add(new GuideItem("🏋️ Kreatin", "En güvenli ve etkili spor takviyesidir. Kas gücünü artırır, beyne iyi gelir. Bol suyla tüketilmelidir.", "spor kas fitness", 1));
        tumBilgiler.add(new GuideItem("🥤 Whey Protein", "Peynir altı suyundan elde edilir. Antrenman sonrası hızlı kas onarımı sağlar. Pratik bir protein kaynağıdır.", "protein spor kas", 1));
        tumBilgiler.add(new GuideItem("⚠️ Pre-Workout", "Yüksek kafein içerir. Enerji verir ama fazlası çarpıntı, uykusuzluk ve anksiyete yapar. Gece içilmemeli.", "enerji spor kafein", 3));
        tumBilgiler.add(new GuideItem("💊 BCAA", "Dallı zincirli amino asitlerdir. Kas yıkımını önler ama yeterli protein alıyorsanız şart değildir.", "kas spor amino", 3));
        tumBilgiler.add(new GuideItem("🔥 L-Karnitin", "Yağ asitlerini enerjiye çevirmeye yardımcı olur. Spor yapmadan içilirse işe yaramaz.", "yağ yakıcı zayıflama", 3));
        tumBilgiler.add(new GuideItem("⚡ Elektrolitler", "Terle kaybedilen tuz ve minerallerdir. Uzun koşu ve antrenmanlarda krampları önlemek için şarttır.", "su spor koşu", 1));
        tumBilgiler.add(new GuideItem("🐟 Omega-3 (Balık Yağı)", "Eklemleri yağlar, kas ağrılarını azaltır ve beyin sağlığını korur. Sporcular için önemlidir.", "eklem spor", 1));
        tumBilgiler.add(new GuideItem("💊 ZMA", "Çinko, Magnezyum ve B6 kombinasyonudur. Uykuyu derinleştirir ve testosteronu dengeler.", "uyku kas erkek", 1));
        tumBilgiler.add(new GuideItem("⚠️ Steroidler", "Yapay hormonlardır. Kalıcı kısırlık, kalp krizi ve karaciğer iflasına yol açar. Kesinlikle uzak durulmalı.", "zararlı doping hormon", 2));
        tumBilgiler.add(new GuideItem("🥩 Kollajen Peptit", "Eklem, tendon ve cilt sağlığı için kullanılır. Sakatlık riskini azaltabilir.", "cilt eklem spor", 1));
        tumBilgiler.add(new GuideItem("🍫 Protein Barlar", "Dikkat edilmeli! Çoğu 'sağlıklı' görünse de çok yüksek şeker ve yapay tatlandırıcı içerir.", "ara öğün şeker", 3));
        tumBilgiler.add(new GuideItem("🏋️ Ağırlık Kemeri", "Sadece çok ağır kaldırırken takılmalı. Sürekli takmak bel kaslarını tembelleştirir.", "spor ekipman", 3));

        // 🧴 BÖLÜM 10: CİLT BAKIMI VE İÇERİKLER (Renk: 3-Mor / 2-Kırmızı)
        tumBilgiler.add(new GuideItem("✨ Retinol (A Vit)", "Yaşlanma karşıtı en güçlü maddedir. Hücreyi yeniler. Sadece gece sürülmeli ve gündüz güneş kremi şarttır.", "cilt kırışıklık sivilce", 1));
        tumBilgiler.add(new GuideItem("💧 Hyaluronik Asit", "Kendi ağırlığının 1000 katı su tutar. Cildi dolgunlaştırır ve nemlendirir.", "nem cilt", 1));
        tumBilgiler.add(new GuideItem("🍋 C Vitamini Serum", "Cildi parlatır, leke açar. Sabahları güneş kremi altına sürülürse korumayı artırır.", "leke cilt", 1));
        tumBilgiler.add(new GuideItem("🧪 Salisilik Asit (BHA)", "Gözeneklerin içine girip yağı temizler. Siyah nokta ve sivilce için birebirdir.", "sivilce siyah nokta", 1));
        tumBilgiler.add(new GuideItem("🧪 Glikolik Asit (AHA)", "Cildin üst ölü tabakasını soyar. Cildi pürüzsüzleştirir ama hassasiyet yapabilir.", "peeling cilt", 3));
        tumBilgiler.add(new GuideItem("🌿 Niasinamid (B3)", "Gözenekleri sıkılaştırır, leke açar ve cilt bariyerini onarır. Her cilt tipine uyar.", "gözenek leke", 1));
        tumBilgiler.add(new GuideItem("⚠️ Fiziksel Peeling", "Kayısı çekirdeği vb. tanecikli peelingler ciltte mikro çizikler oluşturur. Asitli peeling daha güvenlidir.", "cilt zarar", 2));
        tumBilgiler.add(new GuideItem("⚠️ Alkol (Tonik)", "İçeriğinde 'Denatüre Alkol' olan tonikler cildi kurutur ve bariyeri bozar.", "cilt zarar", 2));
        tumBilgiler.add(new GuideItem("🧴 Seramidler", "Cildin harcıdır. Bozulan cilt bariyerini onarır ve egzamaya iyi gelir.", "nem onarım", 1));
        tumBilgiler.add(new GuideItem("🐌 Salyangoz Özü", "Kulağa garip gelse de cildi onarır, nemlendirir ve lekeleri iyileştirir.", "kore cilt", 3));
        tumBilgiler.add(new GuideItem("🪵 Çay Ağacı Yağı", "Doğal antiseptiktir. Sivilcenin üzerine nokta kadar sürülürse kurutur.", "sivilce doğal", 1));

        // 🚿 BÖLÜM 11: KİŞİSEL HİJYEN & BAKIM (Renk: 1-Turuncu / 2-Kırmızı)
        tumBilgiler.add(new GuideItem("🚿 Soğuk Duş", "Kan dolaşımını hızlandırır, saçı parlatır ve depresyona iyi gelir.", "sağlık duş", 1));
        tumBilgiler.add(new GuideItem("🧼 Kese Yapmak", "Ölü deriyi atar ve kan dolaşımını hızlandırır. Ayda 1-2 kez yapılmalıdır.", "cilt temizlik", 1));
        tumBilgiler.add(new GuideItem("⚠️ Pamuklu Çubuk", "Kulağın içine sokmak kiri daha ileri iter ve zara zarar verebilir. Sadece dışı temizlenmeli.", "kulak zarar", 2));
        tumBilgiler.add(new GuideItem("👅 Dil Sıyırıcı", "Ağız kokusunun %90'ı dildeki bakterilerden gelir. Her sabah dil temizlenmelidir.", "diş ağız koku", 1));
        tumBilgiler.add(new GuideItem("🧽 Lif Bakımı", "Banyo lifleri bakteri yuvasıdır. Her kullanımdan sonra kurutulmalı ve sık sık değiştirilmelidir.", "temizlik bakteri", 2));
        tumBilgiler.add(new GuideItem("🦶 Topuk Taşı", "Ayak sağlığı için önemlidir. Nasırları temizler ve mantar oluşumunu engeller.", "ayak bakım", 1));
        tumBilgiler.add(new GuideItem("🧴 Nemlendirici", "Duştan hemen sonra, cilt hafif nemliyken sürülürse etkisi 2 katına çıkar.", "cilt nem", 1));
        tumBilgiler.add(new GuideItem("💅 Oje Sürmek", "Tırnakların nefes almasını engellemez (tırnak nefes almaz) ama sürekli sürmek sarartabilir. Ara verilmeli.", "tırnak makyaj", 3));
        tumBilgiler.add(new GuideItem("🪒 Jilet Yanığı", "Tıraş bıçağı körse veya kuru tıraş olunursa oluşur. Aloe vera ile yatıştırılabilir.", "tıraş cilt", 3));
        tumBilgiler.add(new GuideItem("💇 Kuru Şampuan", "Günü kurtarır ama sürekli kullanılırsa saç köklerini tıkar ve dökülme yapar.", "saç zarar", 2));
        tumBilgiler.add(new GuideItem("🧴 Roll-on vs Sprey", "Roll-on ter kokusunu önlemede daha etkilidir ama kıyafet lekeleyebilir. Alüminyumsuz tercih edin.", "ter koku", 3));

        // 🧘 BÖLÜM 12: ANTRENMAN VE HAREKET TÜRLERİ (Renk: 3-Mor)
        tumBilgiler.add(new GuideItem("🏃 Kardiyo", "Kalp sağlığı için şarttır. Yağ yakar ama çok fazlası kas kaybına yol açabilir.", "zayıflama kalp", 3));
        tumBilgiler.add(new GuideItem("🔥 HIIT", "Yüksek yoğunluklu aralıklı antrenman. Kısa sürede çok kalori yakar ve metabolizmayı 24 saat hızlandırır.", "zayıflama spor", 1));
        tumBilgiler.add(new GuideItem("🧘 Pilates", "Derin kasları çalıştırır, duruşu düzeltir ve esnekliği artırır.", "esneklik bel", 1));
        tumBilgiler.add(new GuideItem("🏋️ Bileşik Hareketler", "Squat, Deadlift gibi hareketler aynı anda çok kası çalıştırır ve büyüme hormonu salgılatır.", "kas fitness", 1));
        tumBilgiler.add(new GuideItem("🧘 Yoga", "Stresi azaltır, nefes kontrolü sağlar ve vücut farkındalığını artırır.", "stres esneklik", 1));
        tumBilgiler.add(new GuideItem("🚶 Soğuma (Cool Down)", "Spordan sonra aniden durmak baş dönmesi yapar. 5 dk yürüyüşle nabız düşürülmeli.", "kalp spor", 1));
        tumBilgiler.add(new GuideItem("🤸 Esneme (Stretching)", "Spordan önce 'Dinamik', spordan sonra 'Statik' esneme yapılmalıdır. Sakatlığı önler.", "esneklik sakatlık", 1));

        // 🧠 BÖLÜM 13: GÜNLÜK SAĞLIK TÜYOLARI (Renk: 1-Turuncu / 2-Kırmızı)
        tumBilgiler.add(new GuideItem("📱 Mavi Işık", "Telefon ekranından gelen ışık uyku hormonu melatonini bozar. Yatmadan 1 saat önce ekran bırakılmalı.", "uyku göz", 2));
        tumBilgiler.add(new GuideItem("🌬️ Burun Nefesi", "Ağızdan nefes almak diş çürümesi, horlama ve yorgunluk yapar. Her zaman burundan nefes alın.", "nefes sağlık", 1));
        tumBilgiler.add(new GuideItem("🪑 Duruş (Postür)", "Kambur durmak (Telefon boynu) boyun fıtığı sebebidir. Telefonu göz hizasında tutun.", "boyun fıtık", 2));
        tumBilgiler.add(new GuideItem("🧊 Buz Banyosu", "Profesyonel sporcular kullanır. İltihabı azaltır ve dopamini %250 artırır.", "recovery enerji", 3));
        tumBilgiler.add(new GuideItem("🍵 Matcha Çayı", "Yeşil çaydan 10 kat daha fazla antioksidan içerir. Sakin bir enerji verir.", "detoks enerji", 1));
        tumBilgiler.add(new GuideItem("🦶 Çıplak Ayak", "Evde veya çimende çıplak ayak yürümek ayak kaslarını güçlendirir ve stresi alır (Topraklanma).", "ayak stres", 1));
        tumBilgiler.add(new GuideItem("🍽️ Yemeği Çiğnemek", "Yemeği çok çiğnemek sindirimi ağızda başlatır, şişkinliği önler ve daha çabuk doymanızı sağlar.", "diyet mide", 1));
        tumBilgiler.add(new GuideItem("🛌 Yastık Seçimi", "Yan yatanlar yüksek, sırt üstü yatanlar orta, yüz üstü yatanlar alçak yastık seçmeli.", "uyku boyun", 3));
        tumBilgiler.add(new GuideItem("🦠 Probiyotikler", "Turşu, yoğurt, kefir. Mutluluk hormonunun %90'ı bağırsakta üretilir, onlara iyi bakın.", "bağırsak depresyon", 1));
    }
    // --- BU SINIFI EN ALTA EKLE (GuideFragment içinde) ---
    public class WikiResponse {
        String title;   // Başlık
        String extract; // Özet Bilgi
        String type;    // Tür (standard, disambiguation vs.)
    }
}