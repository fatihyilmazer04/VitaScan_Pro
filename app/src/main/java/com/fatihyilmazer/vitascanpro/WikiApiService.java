package com.fatihyilmazer.vitascanpro;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface WikiApiService {

    // YENİ ADRES: Artık "Arama" değil "Özet Getirme" servisini kullanıyoruz.
    // Bu servis çok daha kararlıdır.

    @Headers("User-Agent: VitaScanPro/1.0 (vitascanpro_app@gmail.com)")
    @GET("api/rest_v1/page/summary/{baslik}")
    Call<GuideFragment.WikiResponse> getSummary(@Path("baslik") String baslik);
}