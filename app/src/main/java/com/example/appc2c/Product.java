package com.example.appc2c;

public class Product {
    private final String name;
    private final String price;
    private final String imageUrl;
    private String description;


    public Product(String name, String price, String imageUrl, String description) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public String getDescription() {
        return description;
    }

}
