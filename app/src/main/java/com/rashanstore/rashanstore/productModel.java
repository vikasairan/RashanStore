package com.rashanstore.rashanstore;

import android.graphics.Bitmap;

public class productModel {
    public String name,qty,price,id,companName,description;

    productModel(String name,String companName,String description,String qty,String price, String id) {
        this.name = name;
        this.companName=companName;
        this.description=description;
        this.qty=qty;
        this.price=price;
        this.id=id;
    }
    public String getName() {
        return name;
    }
    public String getCompanyName() {
        return companName;
    }
    public String getDescription() {
        return description;
    }
}
