package com.fatihyilmazer.vitascanpro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileFragment extends Fragment {

    // Arayüz Elemanları
    CheckBox cbSugar, cbAlcohol, cbGluten, cbSalt, cbDairy, cbNuts, cbVegan, cbCaffeine;
    RadioGroup rgSkinType;
    RadioButton rbDry, rbNormal;
    Button btnSave;
    SwitchMaterial switchDarkMode;
    CardView cardUser;
    TextView tvUserName, tvUserStatus;

    // Hafıza Ayarları
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_ACTIVE_USER = "active_user";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // --- ELEMANLARI BAĞLA ---
        cbSugar = view.findViewById(R.id.cbSugar);
        cbAlcohol = view.findViewById(R.id.cbAlcohol);
        cbGluten = view.findViewById(R.id.cbGluten);
        cbSalt = view.findViewById(R.id.cbSalt);
        cbDairy = view.findViewById(R.id.cbDairy);
        cbNuts = view.findViewById(R.id.cbNuts);
        cbVegan = view.findViewById(R.id.cbVegan);
        cbCaffeine = view.findViewById(R.id.cbCaffeine);

        rgSkinType = view.findViewById(R.id.rgSkinType);
        rbDry = view.findViewById(R.id.rbDry);
        rbNormal = view.findViewById(R.id.rbNormal);

        btnSave = view.findViewById(R.id.btnSaveProfile);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        cardUser = view.findViewById(R.id.cardUser);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserStatus = view.findViewById(R.id.tvUserStatus);

        // --- VERİLERİ YÜKLE ---
        verileriYukle();

        // --- TIKLAMA İŞLEMLERİ ---
        cardUser.setOnClickListener(v -> girisEkraniAc());
        btnSave.setOnClickListener(v -> verileriKaydet());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences sp = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sp.edit().putBoolean("darkMode", isChecked).commit();
            if (isChecked) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        });

        return view;
    }

    private void girisEkraniAc() {
        if (getActivity() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("🔐 Çoklu Üyelik Sistemi");
        builder.setMessage("Kayıtlı hesabınıza giriş yapın veya yeni hesap oluşturun.");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText inputName = new EditText(getActivity());
        inputName.setHint("Kullanıcı Adı");
        layout.addView(inputName);

        final EditText inputPass = new EditText(getActivity());
        inputPass.setHint("Şifre");
        inputPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPass);

        builder.setView(layout);

        // --- BUTON 1: GİRİŞ YAP ---
        builder.setPositiveButton("GİRİŞ YAP", (dialog, which) -> {
            String girilenIsim = inputName.getText().toString().trim();
            String girilenSifre = inputPass.getText().toString().trim();

            if (girilenIsim.isEmpty() || girilenSifre.isEmpty()) {
                Toast.makeText(getActivity(), "❌ İsim ve şifre boş olamaz!", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sp = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String kayitliSifre = sp.getString("user_pass_" + girilenIsim, "");

            if (kayitliSifre.equals("")) {
                Toast.makeText(getActivity(), "🚫 Böyle bir kullanıcı bulunamadı!", Toast.LENGTH_LONG).show();
            }
            else if (kayitliSifre.equals(girilenSifre)) {
                // ŞİFRE DOĞRU -> GİRİŞ YAP
                sp.edit().putString(KEY_ACTIVE_USER, girilenIsim).commit();

                // --- KRİTİK NOKTA: Giriş yapınca o kişinin verilerini ekrana getir ---
                verileriYukle();

                Toast.makeText(getActivity(), "Hoş geldin " + girilenIsim + " 👋", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "🚫 Şifre Hatalı!", Toast.LENGTH_LONG).show();
            }
        });

        // --- BUTON 2: KAYIT OL ---
        builder.setNeutralButton("KAYIT OL", (dialog, which) -> {
            String yeniIsim = inputName.getText().toString().trim();
            String yeniSifre = inputPass.getText().toString().trim();

            if (yeniIsim.isEmpty() || yeniSifre.isEmpty()) return;

            SharedPreferences sp = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String varMi = sp.getString("user_pass_" + yeniIsim, "");

            if (!varMi.isEmpty()) {
                Toast.makeText(getActivity(), "⚠️ Bu isim alınmış!", Toast.LENGTH_LONG).show();
            } else {
                SharedPreferences.Editor editor = sp.edit();
                // Kullanıcı şifresini kaydet
                editor.putString("user_pass_" + yeniIsim, yeniSifre);

                // Otomatik giriş yap
                editor.putString(KEY_ACTIVE_USER, yeniIsim);
                editor.commit();

                // Yeni kullanıcının boş/varsayılan ayarlarını yükle
                verileriYukle();

                Toast.makeText(getActivity(), "🎉 Hesap oluşturuldu: " + yeniIsim, Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("İptal", null);
        builder.show();
    }

    private void verileriKaydet() {
        if (getActivity() == null) return;
        SharedPreferences sp = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Aktif kullanıcının kim olduğunu bul
        String aktifUser = sp.getString(KEY_ACTIVE_USER, "Misafir");

        SharedPreferences.Editor editor = sp.edit();

        // --- İŞTE SİHİR BURADA: Anahtarların sonuna ismini ekliyoruz ---
        editor.putBoolean("sugar_" + aktifUser, cbSugar.isChecked());
        editor.putBoolean("alcohol_" + aktifUser, cbAlcohol.isChecked());
        editor.putBoolean("gluten_" + aktifUser, cbGluten.isChecked());
        editor.putBoolean("salt_" + aktifUser, cbSalt.isChecked());
        editor.putBoolean("dairy_" + aktifUser, cbDairy.isChecked());
        editor.putBoolean("nuts_" + aktifUser, cbNuts.isChecked());
        editor.putBoolean("vegan_" + aktifUser, cbVegan.isChecked());
        editor.putBoolean("caffeine_" + aktifUser, cbCaffeine.isChecked());

        if (rbDry.isChecked()) editor.putString("skinType_" + aktifUser, "Kuru");
        else editor.putString("skinType_" + aktifUser, "Normal");

        editor.commit();
        Toast.makeText(getActivity(), "✅ Ayarlar " + aktifUser + " için kaydedildi!", Toast.LENGTH_SHORT).show();
    }

    private void verileriYukle() {
        if (getActivity() == null) return;
        SharedPreferences sp = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Aktif Kullanıcıyı Bul
        String aktifUser = sp.getString(KEY_ACTIVE_USER, "Misafir");

        // Arayüzü güncelle
        if(aktifUser.equals("Misafir")) {
            tvUserName.setText("Misafir Kullanıcı");
            tvUserStatus.setText("Giriş yapmak için dokunun 👆");
        } else {
            tvUserName.setText(aktifUser);
            tvUserStatus.setText("✅ Oturum Açık - " + aktifUser);
        }

        // --- İŞTE SİHİR BURADA: Verileri çağırırken ismini kullanıyoruz ---
        cbSugar.setChecked(sp.getBoolean("sugar_" + aktifUser, false));
        cbAlcohol.setChecked(sp.getBoolean("alcohol_" + aktifUser, false));
        cbGluten.setChecked(sp.getBoolean("gluten_" + aktifUser, false));
        cbSalt.setChecked(sp.getBoolean("salt_" + aktifUser, false));
        cbDairy.setChecked(sp.getBoolean("dairy_" + aktifUser, false));
        cbNuts.setChecked(sp.getBoolean("nuts_" + aktifUser, false));
        cbVegan.setChecked(sp.getBoolean("vegan_" + aktifUser, false));
        cbCaffeine.setChecked(sp.getBoolean("caffeine_" + aktifUser, false));

        String skinType = sp.getString("skinType_" + aktifUser, "Kuru");
        if (skinType.equals("Kuru")) rbDry.setChecked(true);
        else rbNormal.setChecked(true);

        // Karanlık Mod herkese özel değil, genel cihaz ayarı olsun
        boolean isDark = sp.getBoolean("darkMode", false);
        switchDarkMode.setChecked(isDark);

        if (isDark) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}