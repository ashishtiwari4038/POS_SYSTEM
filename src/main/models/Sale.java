package main.models;

import java.time.LocalDateTime;

public class Sale {
    private int id;
    private String customerName;
    private String customerNumber;
    private String productCompany;
    private String productModel;
    private LocalDateTime saleDate;
    private double mrp;
    private double discount;
    private double gst;
    private double totalPrice;
    private int cashierId;
    
    public Sale(String customerName, String customerNumber, String productCompany, 
                String productModel, double mrp, double discount, double gst, int cashierId) {
        this.customerName = customerName;
        this.customerNumber = customerNumber;
        this.productCompany = productCompany;
        this.productModel = productModel;
        this.mrp = mrp;
        this.discount = discount;
        this.gst = gst;
        this.totalPrice = calculateTotalPrice();
        this.cashierId = cashierId;
    }
    
    private double calculateTotalPrice() {
        double discountedPrice = mrp - (mrp * discount / 100);
        return discountedPrice + (discountedPrice * gst / 100);
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerNumber() { return customerNumber; }
    public String getProductCompany() { return productCompany; }
    public String getProductModel() { return productModel; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    public double getMrp() { return mrp; }
    public double getDiscount() { return discount; }
    public double getGst() { return gst; }
    public double getTotalPrice() { return totalPrice; }
    public int getCashierId() { return cashierId; }
}