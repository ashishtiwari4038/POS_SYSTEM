package main.ui;

import main.models.User;
import main.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CashierDashboard extends JFrame {

    private User currentUser;

    private JTextField customerNameField, customerNumberField;
    private JTextField companyField, modelField, mrpField, discountField, gstField;
    private JLabel totalPriceLabel;
    private JTable salesTable;
    private DefaultTableModel tableModel;

    public CashierDashboard(User user) {
        this.currentUser = user;

        setTitle("POS System - Cashier : " + user.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadSales();
    }

    // Initialize all UI
    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        root.add(createHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("New Sale", createNewSalePanel());
        tabs.addTab("Sales History", createSalesHistoryPanel());

        root.add(tabs, BorderLayout.CENTER);
        add(root);
    }

    // Top header with logout
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(5, 10, 5, 10));
        header.setBackground(Color.LIGHT_GRAY);

        JLabel title = new JLabel("Cashier Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel user = new JLabel("Welcome, " + currentUser.getUsername());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(user);
        right.add(logoutBtn);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // New sale tab
    private JPanel createNewSalePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel left = new JPanel(new GridLayout(2, 1, 10, 10));
        left.add(createTitledPanel("Customer Details", createCustomerPanel()));
        left.add(createTitledPanel("Product Details", createProductPanel()));

        panel.add(left, BorderLayout.CENTER);
        panel.add(createRightPanel(), BorderLayout.EAST);

        return panel;
    }

    // Customer form
    private JPanel createCustomerPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
        p.add(new JLabel("Customer Name"));
        customerNameField = new JTextField();
        p.add(customerNameField);

        p.add(new JLabel("Customer Phone"));
        customerNumberField = new JTextField();
        p.add(customerNumberField);
        return p;
    }

    // Product form
    private JPanel createProductPanel() {
        JPanel p = new JPanel(new GridLayout(5, 2, 5, 5));

        p.add(new JLabel("Company"));
        companyField = new JTextField();
        p.add(companyField);

        p.add(new JLabel("Model"));
        modelField = new JTextField();
        p.add(modelField);

        p.add(new JLabel("MRP"));
        mrpField = new JTextField();
        p.add(mrpField);

        p.add(new JLabel("Discount %"));
        discountField = new JTextField("0");
        p.add(discountField);

        p.add(new JLabel("GST %"));
        gstField = new JTextField("18");
        p.add(gstField);

        return p;
    }

    // Right panel with total and buttons
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        totalPriceLabel = new JLabel("₹0.00", SwingConstants.CENTER);
        totalPriceLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        totalPriceLabel.setBorder(BorderFactory.createTitledBorder("Final Amount"));

        JPanel btns = new JPanel(new GridLayout(3, 1, 5, 5));
        JButton calc = new JButton("Calculate");
        JButton save = new JButton("Save Sale");
        JButton clear = new JButton("Clear");

        calc.addActionListener(e -> calculateTotal());
        save.addActionListener(e -> saveSale());
        clear.addActionListener(e -> clearForm());

        btns.add(calc);
        btns.add(save);
        btns.add(clear);

        panel.add(totalPriceLabel, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);

        return panel;
    }

    // Sales history
    private JPanel createSalesHistoryPanel() {
        String[] cols = {"ID", "Customer", "Phone", "Product", "MRP", "Discount", "GST", "Total", "Date"};
        tableModel = new DefaultTableModel(cols, 0);
        salesTable = new JTable(tableModel);
        return new JPanel(new BorderLayout()) {{
            add(new JScrollPane(salesTable));
        }};
    }

    // Reusable titled panel
    private JPanel createTitledPanel(String title, JPanel content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12)
        ));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // Calculate bill
    private void calculateTotal() {
        try {
            double mrp = Double.parseDouble(mrpField.getText());
            double disc = Double.parseDouble(discountField.getText());
            double gst = Double.parseDouble(gstField.getText());

            double discounted = mrp - (mrp * disc / 100);
            double total = discounted + (discounted * gst / 100);

            totalPriceLabel.setText("₹" + String.format("%.2f", total));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }

    // Save to DB
    private void saveSale() {
        calculateTotal();
        try (Connection con = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO sales(customer_name, customer_number, product_company, product_model, mrp, discount, gst, total_price, cashier_id) VALUES(?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, customerNameField.getText());
            ps.setString(2, customerNumberField.getText());
            ps.setString(3, companyField.getText());
            ps.setString(4, modelField.getText());
            ps.setDouble(5, Double.parseDouble(mrpField.getText()));
            ps.setDouble(6, Double.parseDouble(discountField.getText()));
            ps.setDouble(7, Double.parseDouble(gstField.getText()));
            ps.setDouble(8, Double.parseDouble(totalPriceLabel.getText().replace("₹", "")));
            ps.setInt(9, currentUser.getId());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Sale Saved");
            loadSales();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load sales into table
    private void loadSales() {
        tableModel.setRowCount(0);
        try (Connection con = DatabaseConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM sales");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_number"),
                        rs.getString("product_company") + " " + rs.getString("product_model"),
                        rs.getDouble("mrp"),
                        rs.getDouble("discount"),
                        rs.getDouble("gst"),
                        rs.getDouble("total_price"),
                        rs.getTimestamp("sale_date")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        customerNameField.setText("");
        customerNumberField.setText("");
        companyField.setText("");
        modelField.setText("");
        mrpField.setText("");
        discountField.setText("0");
        gstField.setText("18");
        totalPriceLabel.setText("₹0.00");
    }

    // Modern logout (no reflection)
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
