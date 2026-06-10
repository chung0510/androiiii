package com.example.smartbox;

public class OptionModel {

    String title;
    String price;

    public OptionModel(String title, String price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }
}