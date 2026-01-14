package com.fatihyilmazer.vitascanpro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etUser = findViewById(R.id.etLoginUsername);
        EditText etPass = findViewById(R.id.etLoginPassword);
        Button btnLogin = findViewById(R.id.btnLoginSubmit);
        Button btnRegister = findViewById(R.id.btnRegisterSubmit);

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // --- GİRİŞ YAP BUTONU ---
        btnLogin.setOnClickListener(v -> {
            String username = etUser.getText().toString().trim();
            String password = etPass.getText().toString().trim();
            String savedPass = sp.getString("user_pass_" + username, "");

            if (!username.isEmpty() && savedPass.equals(password)) {
                // Giriş Başarılı -> Aktif Kullanıcıyı kaydet ve ana sayfaya git
                sp.edit().putString("active_user", username).apply();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish(); // Bu ekranı kapat ki geri tuşuyla dönülmesin
            } else {
                Toast.makeText(this, "Hatalı kullanıcı adı veya şifre!", Toast.LENGTH_SHORT).show();
            }
        });

        // --- KAYIT OL BUTONU ---
        btnRegister.setOnClickListener(v -> {
            String username = etUser.getText().toString().trim();
            String password = etPass.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen alanları doldurun!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Şifreyi kaydet (Senin profil kısmındaki mantıkla aynı)
            sp.edit().putString("user_pass_" + username, password).apply();
            Toast.makeText(this, "Kayıt Başarılı! Şimdi giriş yapabilirsin.", Toast.LENGTH_LONG).show();
        });
    }
}