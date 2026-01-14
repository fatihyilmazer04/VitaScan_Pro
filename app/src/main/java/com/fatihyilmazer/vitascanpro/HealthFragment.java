package com.fatihyilmazer.vitascanpro;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HealthFragment extends Fragment {

    // --- DEĞİŞKENLER ---

    // 1. BMI (Vücut Kitle İndeksi)
    EditText etWeight, etHeight;
    Button btnCalculateBMI;
    TextView tvBMIResult;

    // 2. Su Takibi
    TextView tvWaterCount;
    Button btnAddWater;
    int currentWater = 0;
    final int TARGET_WATER = 2500;

    // 3. Makro & Kalori (YENİ)
    EditText etYas;
    Spinner spCinsiyet, spHareket, spHedef;
    Button btnHesaplaMakro;
    LinearLayout layoutMakroSonuc;
    TextView tvKaloriSonuc, tvProtein, tvKarb, tvYag;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);

        // --- TANIMLAMALAR (XML ile Bağlantı) ---

        // BMI Kısmı
        etWeight = view.findViewById(R.id.etWeight);
        etHeight = view.findViewById(R.id.etHeight);
        btnCalculateBMI = view.findViewById(R.id.btnCalculateBMI);
        tvBMIResult = view.findViewById(R.id.tvBMIResult);

        // Su Kısmı
        tvWaterCount = view.findViewById(R.id.tvWaterCount);
        btnAddWater = view.findViewById(R.id.btnAddWater);

        // Makro Kısmı (YENİ)
        etYas = view.findViewById(R.id.etYas);
        spCinsiyet = view.findViewById(R.id.spCinsiyet);
        spHareket = view.findViewById(R.id.spHareket);
        spHedef = view.findViewById(R.id.spHedef);
        btnHesaplaMakro = view.findViewById(R.id.btnHesaplaMakro);
        layoutMakroSonuc = view.findViewById(R.id.layoutMakroSonuc);
        tvKaloriSonuc = view.findViewById(R.id.tvKaloriSonuc);
        tvProtein = view.findViewById(R.id.tvProtein);
        tvKarb = view.findViewById(R.id.tvKarb);
        tvYag = view.findViewById(R.id.tvYag);

        // --- HAZIRLIKLAR ---
        spinnerlariDoldur(); // Seçenek kutularını doldur

        // --- BUTON TIKLAMALARI ---

        // 1. BMI Hesapla Butonu
        btnCalculateBMI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hesaplaBMI();
            }
        });

        // 2. Su Ekle Butonu
        btnAddWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suEkle();
            }
        });

        // 3. Makro Planı Oluştur Butonu (YENİ)
        btnHesaplaMakro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hesaplaMakro();
            }
        });

        return view;
    }

    // --- FONKSİYONLAR ---

    private void hesaplaBMI() {
        String kiloStr = etWeight.getText().toString();
        String boyStr = etHeight.getText().toString();

        if (!kiloStr.isEmpty() && !boyStr.isEmpty()) {
            float kilo = Float.parseFloat(kiloStr);
            float boy = Float.parseFloat(boyStr) / 100; // cm'yi metreye çevir
            float bmi = kilo / (boy * boy);

            String durum;
            if (bmi < 18.5) durum = "Zayıf";
            else if (bmi < 25) durum = "Normal Kilolu";
            else if (bmi < 30) durum = "Fazla Kilolu";
            else durum = "Obez";

            tvBMIResult.setText(String.format("BMI: %.1f\nDurum: %s", bmi, durum));
        } else {
            Toast.makeText(getActivity(), "Lütfen boy ve kilonuzu girin!", Toast.LENGTH_SHORT).show();
        }
    }

    private void suEkle() {
        if (currentWater < TARGET_WATER) {
            currentWater += 200;
            tvWaterCount.setText(currentWater + " / " + TARGET_WATER + " ml");

            if (currentWater >= TARGET_WATER) {
                Toast.makeText(getActivity(), "Tebrikler! Günlük hedefe ulaştın! 💧", Toast.LENGTH_LONG).show();
                tvWaterCount.setTextColor(Color.parseColor("#4CAF50")); // Yeşil yap
            }
        } else {
            Toast.makeText(getActivity(), "Zaten hedefe ulaştın! Fazla su içme :)", Toast.LENGTH_SHORT).show();
        }
    }

    private void spinnerlariDoldur() {
        // Cinsiyet
        ArrayAdapter<String> cinsiyetAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Erkek", "Kadın"});
        spCinsiyet.setAdapter(cinsiyetAdapter);

        // Hareket Seviyesi
        String[] hareketler = {"Hareketsiz (Masa başı)", "Az Hareketli (Haftada 1-3 spor)", "Orta Hareketli (Haftada 3-5 spor)", "Çok Hareketli (Her gün spor)"};
        ArrayAdapter<String> hareketAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, hareketler);
        spHareket.setAdapter(hareketAdapter);

        // Hedef
        String[] hedefler = {"Kilo Ver (-500 kcal)", "Kilomu Koru", "Kas Yap / Kilo Al (+400 kcal)"};
        ArrayAdapter<String> hedefAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, hedefler);
        spHedef.setAdapter(hedefAdapter);
    }

    private void hesaplaMakro() {
        // Kilo ve Boy bilgisini yukarıdaki kutulardan alıyoruz
        String kiloStr = etWeight.getText().toString();
        String boyStr = etHeight.getText().toString();
        String yasStr = etYas.getText().toString();

        if (kiloStr.isEmpty() || boyStr.isEmpty() || yasStr.isEmpty()) {
            Toast.makeText(getActivity(), "Lütfen Boy, Kilo ve Yaş alanlarını doldurun.", Toast.LENGTH_SHORT).show();
            return;
        }

        double kilo = Double.parseDouble(kiloStr);
        double boy = Double.parseDouble(boyStr);
        int yas = Integer.parseInt(yasStr);
        String cinsiyet = spCinsiyet.getSelectedItem().toString();

        // 1. ADIM: BMR (Bazal Metabolizma) Hesapla (Mifflin-St Jeor Formülü)
        double bmr;
        if (cinsiyet.equals("Erkek")) {
            bmr = (10 * kilo) + (6.25 * boy) - (5 * yas) + 5;
        } else {
            bmr = (10 * kilo) + (6.25 * boy) - (5 * yas) - 161;
        }

        // 2. ADIM: Aktivite Çarpanı
        double aktiviteCarpani = 1.2;
        int hareketSecim = spHareket.getSelectedItemPosition();
        if (hareketSecim == 1) aktiviteCarpani = 1.375;
        else if (hareketSecim == 2) aktiviteCarpani = 1.55;
        else if (hareketSecim == 3) aktiviteCarpani = 1.725;

        double gunlukKalori = bmr * aktiviteCarpani;

        // 3. ADIM: Hedefe Göre Ayarla
        int hedefSecim = spHedef.getSelectedItemPosition();
        if (hedefSecim == 0) gunlukKalori -= 500; // Kilo Ver
        else if (hedefSecim == 2) gunlukKalori += 400; // Kilo Al

        int sonKalori = (int) gunlukKalori;

        // 4. ADIM: Makroları Böl (Protein %30, Karb %50, Yağ %20)
        // Protein ve Karb: 1 gram = 4 kalori
        // Yağ: 1 gram = 9 kalori

        int proteinGr = (int) ((sonKalori * 0.30) / 4);
        int karbGr = (int) ((sonKalori * 0.50) / 4);
        int yagGr = (int) ((sonKalori * 0.20) / 9);

        // Sonuçları Ekrana Bas
        tvKaloriSonuc.setText(sonKalori + " kcal");
        tvProtein.setText(proteinGr + "g");
        tvKarb.setText(karbGr + "g");
        tvYag.setText(yagGr + "g");

        // Kutuyu görünür yap
        layoutMakroSonuc.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "Planınız Oluşturuldu! 🚀", Toast.LENGTH_SHORT).show();
    }
}