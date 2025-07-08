package com.example.appc2c.models;

public class Offer {
    private String id;
    private String productId;
    private String buyerId;
    private int proposedPrice;
    private String note;
    private String status;
    private String sellerId;

    // Constructor rỗng cho Firebase
    public Offer() {
    }

    //  Constructor đầy đủ có gán giá trị
    public Offer(String buyerId, String id, String note,  String productId, int proposedPrice, String status, String sellerId) {

        this.buyerId = buyerId;
        this.id = id;
        this.productId = productId;
        this.proposedPrice = proposedPrice;
        this.note = note;
        this.status = status;
        this.sellerId= sellerId;
    }

    // Getter
    public String getId() {
        return id;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getProductId() {
        return productId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public int getProposedPrice() {
        return proposedPrice;
    }

    public String getNote() {
        return note;
    }

    public String getStatus() {
        return status;
    }

    // Setter
    public void setId(String id) {
        this.id = id;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public void setProposedPrice(int proposedPrice) {
        this.proposedPrice = proposedPrice;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSellerId(String sellerId) {
    this.sellerId = sellerId;
    }
}
