package com.fatihyilmazer.vitascanpro;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

public class ProductResponse {

    @SerializedName("product")
    private Product product;

    @SerializedName("products")
    private List<Product> products;

    @SerializedName("status")
    private int status;

    public List<Product> getProducts() {
        if (products != null) {
            return products;
        } else if (product != null) {
            List<Product> singleList = new ArrayList<>();
            singleList.add(product);
            return singleList;
        }
        return new ArrayList<>();
    }
}