package com.fatihyilmazer.vitascanpro;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("product_name")
    private String title;

    @SerializedName("brands")
    private String brand;

    @SerializedName("categories")
    private String category;

    @SerializedName("ingredients_text")
    private String description;

    @SerializedName("image_url")
    private String imageUrl;

    // Getter Metodları
    public String getTitle() {
        return title != null ? title : "İsimsiz Ürün";
    }

    public String getBrand() {
        return brand != null ? brand : "Bilinmiyor";
    }

    public String getCategory() {
        return category != null ? category : "-";
    }

    public String getDescription() {
        return description != null ? description : "İçerik bilgisi bulunamadı.";
    }

    public String getIngredients() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}