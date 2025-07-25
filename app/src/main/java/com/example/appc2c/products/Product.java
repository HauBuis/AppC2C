package com.example.appc2c.products;

import java.util.ArrayList;

public class Product {
    private String id;
    private String name;
    private String price;
    private String imageUrl;
    private String description;
    private String category;
    private String condition;
    private String sellerId;
    private String status;
    private String location;
    private boolean selected;
    private boolean allowNegotiation;
    private String features;
    private String tags;
    private int views;
    private int interactions;
    private ArrayList<String> images;

    // Bổ sung cho tìm kiếm khoảng cách, sort liên quan (có thể null nếu Firestore không có)
    private Double lat;
    private Double lng;
    private int relevanceScore; // Chỉ dùng ở client để sort "Liên quan"

    public Product() {
    }

    // ID
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Name
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Price
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    // Image
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Description
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Category
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // Condition
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    // Seller
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    // Status
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Location
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    // Selected
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // Allow negotiation
    public boolean isAllowNegotiation() { return allowNegotiation; }
    public void setAllowNegotiation(boolean allowNegotiation) { this.allowNegotiation = allowNegotiation; }

    // Features
    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }

    // Tags
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    // Views
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    // Interactions
    public int getInteractions() { return interactions; }
    public void setInteractions(int interactions) { this.interactions = interactions; }

    // Images
    public ArrayList<String> getImages() { return images; }
    public void setImages(ArrayList<String> images) { this.images = images; }

    // Lat/Lng cho filter theo vị trí (nếu có dữ liệu)
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    // Điểm liên quan (relevance) cho sort custom client (không map lên server)
    public int getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(int relevanceScore) { this.relevanceScore = relevanceScore; }
}
