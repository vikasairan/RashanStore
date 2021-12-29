package com.rashanstore.rashanstore;

import android.graphics.Bitmap;

public class BestOffersModel {
    public Bitmap img;
    public String id,name,companName,description;

    BestOffersModel(Bitmap img,String id,String name,String companName,String description) {
        this.name = name;
        this.companName=companName;
        this.description=description;
        this.img = img;
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
