package com.fatihyilmazer.vitascanpro;

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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Tasarımı yükle
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            // Elemanları güvenli bir şekilde bağla
            EditText etWeight = view.findViewById(R.id.etWeightHome);
            EditText etHeight = view.findViewById(R.id.etHeightHome);
            Button btnCalculate = view.findViewById(R.id.btnCalculateHome);
            TextView tvResult = view.findViewById(R.id.tvResultHome);
            CardView cardQuickScan = view.findViewById(R.id.cardQuickScan);

            // BMI Hesaplama
            if (btnCalculate != null) {
                btnCalculate.setOnClickListener(v -> {
                    String w = etWeight.getText().toString();
                    String h = etHeight.getText().toString();

                    if (!w.isEmpty() && !h.isEmpty()) {
                        float weight = Float.parseFloat(w);
                        float height = Float.parseFloat(h) / 100;
                        float bmi = weight / (height * height);

                        String status = (bmi < 18.5) ? "Zayıf" : (bmi < 25) ? "Normal" : (bmi < 30) ? "Kilolu" : "Obez";
                        tvResult.setText(String.format("Sonuç: %.1f - %s", bmi, status));
                    } else {
                        Toast.makeText(getActivity(), "Lütfen boş alan bırakmayın", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Tarayıcıya Geçiş
            if (cardQuickScan != null) {
                cardQuickScan.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                        if (nav != null) nav.setSelectedItemId(R.id.nav_scanner);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Eğer bir hata olursa en azından uygulama kapanmasın diye try-catch ekledik
        }

        return view;
    }
}