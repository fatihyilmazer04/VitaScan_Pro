package com.fatihyilmazer.vitascanpro;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductApiService {

    @Headers("User-Agent: VitaScanPro - Android")
    @GET("api/v2/product/{barcode}.json")
    Call<ProductResponse> getProduct(@Path("barcode") String barcode);

    @Headers("User-Agent: VitaScanPro - Android")
    @GET("cgi/search.pl?search_simple=1&action=process&json=1&page_size=20")
    Call<ProductResponse> searchProduct(@Query("search_terms") String query);
}