package main.ui.dialogs;

import main.models.Sale;
import main.database.DatabaseConnection;
import main.pdf.PDFInvoiceGenerator;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddSaleDialog extends JDialog {
    private JTextField customerNameField, customerNumberField, companyField, modelField;
    private JTextField mrpField, discountField, gstField;
    private JLabel totalPriceLabel;
    private int cashierId;
    private String cashierName;
    
    private boolean saleCompleted = false;
    
    public AddSaleDialog(Frame parent, int cashierId, String cashierName) {
        super(parent, "Add New Sale", true);
        this.cashierId = cashierId;
        this.cashierName = cashierName;
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initializeUI();
    }
    
    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        
        formPanel.add(new JLabel("Customer Name:"));
        customerNameField = new JTextField();
        formPanel.add(customerNameField);
        
        formPanel.add(new JLabel("Customer Number:"));
        customerNumberField = new JTextField();
        formPanel.add(customerNumberField);
        
        
        formPanel.add(new JLabel("Product Company:"));
        companyField = new JTextField();
        formPanel.add(companyField);
        
        formPanel.add(new JLabel("Product Model:"));
        modelField = new JTextField();
        formPanel.add(modelField);
        
        formPanel.add(new JLabel("MRP:"));
        mrpField = new JTextField();
        formPanel.add(mrpField);
        
        formPanel.add(new JLabel("Discount (%):"));
        discountField = new JTextField("0");
        formPanel.add(discountField);
        
        formPanel.add(new JLabel("GST (%):"));
        gstField = new JTextField("18");
        formPanel.add(gstField);
        
        formPanel.add(new JLabel("Total Price:"));
        totalPriceLabel = new JLabel("₹0.00");
        totalPriceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(totalPriceLabel);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton calculateButton = new JButton("Calculate Total");
        JButton saveButton = new JButton("Save & Generate Invoice");
        JButton cancelButton = new JButton("Cancel");
        
        buttonPanel.add(calculateButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        
        calculateButton.addActionListener(e -> calculateTotal());
        saveButton.addActionListener(e -> saveSale());
        cancelButton.addActionListener(e -> dispose());
        
        add(mainPanel);
    }
    
    private void calculateTotal() {
        try {
            double mrp = Double.parseDouble(mrpField.getText());
            double discount = Double.parseDouble(discountField.getText());
            double gst = Double.parseDouble(gstField.getText());
            
            double discountedPrice = mrp - (mrp * discount / 100);
            double total = discountedPrice + (discountedPrice * gst / 100);
            
            totalPriceLabel.setText(String.format("₹%.2f", total));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for MRP, Discount, and GST");
        }
    }
    
    private void saveSale() {
        try {
            String customerName = customerNameField.getText();
            String customerNumber = customerNumberField.getText();
            String company = companyField.getText();
            String model = modelField.getText();
            double mrp = Double.parseDouble(mrpField.getText());
            double discount = Double.parseDouble(discountField.getText());
            double gst = Double.parseDouble(gstField.getText());
            
            if (customerName.isEmpty() || company.isEmpty() || model.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields");
                return;
            }
            
            
            double discountedPrice = mrp - (mrp * discount / 100);
            double total = discountedPrice + (discountedPrice * gst / 100);
            
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO sales (customer_name, customer_number, product_company, " +
                           "product_model, mrp, discount, gst, total_price, cashier_id) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, customerName);
                stmt.setString(2, customerNumber);
                stmt.setString(3, company);
                stmt.setString(4, model);
                stmt.setDouble(5, mrp);
                stmt.setDouble(6, discount);
                stmt.setDouble(7, gst);
                stmt.setDouble(8, total);
                stmt.setInt(9, cashierId);
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        int saleId = rs.getInt(1);
                        
                        
                        Sale sale = new Sale(customerName, customerNumber, company, model, mrp, discount, gst, cashierId);
                        sale.setId(saleId);
                        
                     
                        String pdfPath = PDFInvoiceGenerator.generateInvoice(sale, cashierName);
                        
                        if (pdfPath != null) {
                            JOptionPane.showMessageDialog(this, 
                                "Sale saved successfully!\nInvoice generated: " + pdfPath);
                        } else {
                            JOptionPane.showMessageDialog(this, 
                                "Sale saved but PDF generation failed!");
                        }
                        
                        saleCompleted = true;
                        dispose();
                    }
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
    
    public boolean isSaleCompleted() {
        return saleCompleted;
    }
}