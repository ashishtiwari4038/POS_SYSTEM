package main.pdf;

import main.models.Sale;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvoiceGenerator {
    
    public static String generateInvoice(Sale sale, String cashierName) {
        String fileName = "Invoice_" + sale.getId() + "_" + System.currentTimeMillis() + ".pdf";
        
        try {
            
            String receiptContent = generateReceiptText(sale, cashierName);
           
            System.out.println("=== INVOICE CONTENT ===");
            System.out.println(receiptContent);
            System.out.println("======================");
            
            String textFileName = fileName.replace(".pdf", ".txt");
            try (FileOutputStream fos = new FileOutputStream(textFileName)) {
                fos.write(receiptContent.getBytes());
            }
            
            System.out.println("Receipt saved as: " + textFileName);
            return textFileName;
            
        } catch (Exception e) {
            System.out.println("Error generating invoice: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static String generateReceiptText(Sale sale, String cashierName) {
        StringBuilder receipt = new StringBuilder();
        
        receipt.append("=========================================\n");
        receipt.append("              TAX INVOICE\n");
        receipt.append("=========================================\n");
        receipt.append("ELECTRONICS STORE\n");
        receipt.append("123 Business Street, City Center\n");
        receipt.append("Mumbai, Maharashtra - 400001\n");
        receipt.append("Phone: +91 9876543210\n");
        receipt.append("GSTIN: 27ABCDE1234F1Z5\n");
        receipt.append("=========================================\n");
        receipt.append("Invoice No: INV").append(sale.getId()).append("\n");
        receipt.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        receipt.append("Cashier: ").append(cashierName).append("\n");
        receipt.append("Customer: ").append(sale.getCustomerName()).append("\n");
        receipt.append("Phone: ").append(sale.getCustomerNumber()).append("\n");
        receipt.append("=========================================\n");
        receipt.append("PRODUCT DETAILS:\n");
        receipt.append("-----------------------------------------\n");
        receipt.append("Item: ").append(sale.getProductCompany()).append(" - ").append(sale.getProductModel()).append("\n");
        receipt.append("MRP: ₹").append(String.format("%.2f", sale.getMrp())).append("\n");
        receipt.append("Discount: ").append(sale.getDiscount()).append("%\n");
        receipt.append("GST: ").append(sale.getGst()).append("%\n");
        receipt.append("-----------------------------------------\n");
        
        double discountAmount = sale.getMrp() * sale.getDiscount() / 100;
        double baseAmount = sale.getMrp() - discountAmount;
        double gstAmount = baseAmount * sale.getGst() / 100;
        double totalAmount = sale.getTotalPrice();
        
        receipt.append("CALCULATIONS:\n");
        receipt.append("MRP: ₹").append(String.format("%.2f", sale.getMrp())).append("\n");
        receipt.append("Discount (").append(sale.getDiscount()).append("%): -₹").append(String.format("%.2f", discountAmount)).append("\n");
        receipt.append("Base Amount: ₹").append(String.format("%.2f", baseAmount)).append("\n");
        receipt.append("GST (").append(sale.getGst()).append("%): +₹").append(String.format("%.2f", gstAmount)).append("\n");
        receipt.append("-----------------------------------------\n");
        receipt.append("TOTAL AMOUNT: ₹").append(String.format("%.2f", totalAmount)).append("\n");
        receipt.append("=========================================\n");
        receipt.append("Thank you for your business!\n");
        receipt.append("We hope to see you again soon.\n");
        receipt.append("=========================================\n");
        
        return receipt.toString();
    }
}