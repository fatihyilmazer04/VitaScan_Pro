package com.fatihyilmazer.vitascanpro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScannerFragment extends Fragment {

    EditText etBarcode;
    Button btnAnalyze;
    TextView tvResultTitle, tvResultBody;
    RecyclerView rvHistory;
    TextView tvClearHistory;
    HistoryAdapter adapter;
    List<ScanHistoryItem> historyList = new ArrayList<>();
    ProductApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        try {
            etBarcode = view.findViewById(R.id.etBarcode);
            btnAnalyze = view.findViewById(R.id.btnAnalyze);
            tvResultTitle = view.findViewById(R.id.tvResultTitle);
            tvResultBody = view.findViewById(R.id.tvResultBody);
            rvHistory = view.findViewById(R.id.rvScanHistory);
            tvClearHistory = view.findViewById(R.id.tvClearHistory);

            // --- GÜNCELLEME: DAHA STABİL RETROFİT KURULUMU ---
            Gson gson = new GsonBuilder().setLenient().create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://world.openfoodfacts.org/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiService = retrofit.create(ProductApiService.class);

            if (rvHistory != null) {
                rvHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
                gecmisiYukle();
                adapter = new HistoryAdapter(historyList);
                rvHistory.setAdapter(adapter);
            }

            if (tvClearHistory != null) {
                tvClearHistory.setOnClickListener(v -> {
                    historyList.clear();
                    if (adapter != null) adapter.notifyDataSetChanged();
                    getActivity().getSharedPreferences("VitaScanPrefs", Context.MODE_PRIVATE)
                            .edit().remove("scan_history").apply();
                    Toast.makeText(getActivity(), "Geçmiş temizlendi!", Toast.LENGTH_SHORT).show();
                });
            }

            if (btnAnalyze != null) {
                btnAnalyze.setOnClickListener(v -> {
                    String input = etBarcode.getText().toString().trim();
                    if (input.isEmpty()) {
                        Toast.makeText(getActivity(), "Lütfen bir değer giriniz!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tvResultTitle.setText("Analiz Ediliyor...");
                    tvResultBody.setText("Sunucuya bağlanılıyor, lütfen bekleyin...");

                    gecmiseKaydet(input);

                    if (input.matches("\\d+")) {
                        barkodlaAra(input);
                    } else {
                        isimleAra(input);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Başlatma Hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return view;
    }

    private void gecmiseKaydet(String barkod) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.getDefault());
            String tarih = sdf.format(new Date());

            historyList.add(0, new ScanHistoryItem(barkod, tarih));
            if (adapter != null) adapter.notifyDataSetChanged();

            SharedPreferences prefs = getActivity().getSharedPreferences("VitaScanPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(historyList);
            editor.putString("scan_history", json);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gecmisiYukle() {
        try {
            SharedPreferences prefs = getActivity().getSharedPreferences("VitaScanPrefs", Context.MODE_PRIVATE);
            String json = prefs.getString("scan_history", null);
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<ScanHistoryItem>>() {}.getType();
                historyList = gson.fromJson(json, type);
            }
        } catch (Exception e) {
            historyList = new ArrayList<>();
        }
        if (historyList == null) historyList = new ArrayList<>();
    }

    private void barkodlaAra(String barkod) {
        Call<ProductResponse> call = apiService.getProduct(barkod);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getProducts();
                    if (products != null && !products.isEmpty()) {
                        sonucuGoster(products.get(0));
                    } else {
                        tvResultTitle.setText("Ürün Bulunamadı");
                        tvResultBody.setText("Bu barkod OpenFoodFacts veritabanında mevcut değil.");
                    }
                } else {
                    tvResultTitle.setText("Sunucu Yanıt Vermedi");
                    tvResultBody.setText("Kod: " + response.code() + "\nLütfen daha sonra tekrar deneyin.");
                }
            }
            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                tvResultTitle.setText("Bağlantı Başarısız");
                tvResultBody.setText("Hata detayı: " + t.getLocalizedMessage());
            }
        });
    }

    private void isimleAra(String isim) {
        Call<ProductResponse> call = apiService.searchProduct(isim);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getProducts();
                    if (products != null && !products.isEmpty()) {
                        sonucuGoster(products.get(0));
                    } else {
                        tvResultTitle.setText("Sonuç Yok");
                        tvResultBody.setText("'" + isim + "' araması için ürün bulunamadı.");
                    }
                } else {
                    tvResultTitle.setText("Sorgu Hatası");
                    tvResultBody.setText("Sunucu kodu: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                tvResultTitle.setText("Bağlantı Kesildi");
                tvResultBody.setText("Hata: " + t.getLocalizedMessage());
            }
        });
    }

    private void sonucuGoster(Product urun) {
        if (urun == null) return;

        tvResultTitle.setText(urun.getTitle());

        StringBuilder bilgi = new StringBuilder();
        bilgi.append("🏭 Marka: ").append(urun.getBrand()).append("\n");
        bilgi.append("📦 Kategori: ").append(urun.getCategory().split(",")[0]).append("\n\n");
        bilgi.append("ℹ️ İçindekiler:\n").append(urun.getDescription());

        tvResultBody.setText(bilgi.toString());
    }
}