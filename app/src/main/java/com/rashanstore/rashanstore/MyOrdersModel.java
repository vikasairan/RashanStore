package com.rashanstore.rashanstore;


public class MyOrdersModel {
    public String orderid,date,total,payment,address;

    MyOrdersModel(String orderid,String date,String total, String payment,String address) {
        this.orderid = orderid;
        this.date=date;
        this.total=total;
        this.payment=payment;
        this.address=address;

    }

}
