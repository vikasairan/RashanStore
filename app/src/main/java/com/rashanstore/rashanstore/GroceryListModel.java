package com.rashanstore.rashanstore;

import android.graphics.Bitmap;

public class GroceryListModel {
    public Bitmap img;
    public String id;

    GroceryListModel(Bitmap img,String id) {
        this.img = img;
        this.id=id;
    }
}
