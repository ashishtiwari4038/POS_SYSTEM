

package main.ui.components;

import main.database.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class SalesReport extends JPanel {
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    private JButton deleteBtn;

    public SalesReport() {
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        // Table setup
        String[] columns = {"ID", "Date", "Customer", "Product", "MRP", "Discount", "GST", "Total", "Cashier"};
        tableModel = new DefaultTableModel(columns, 0);
        reportTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        add(scrollPane, BorderLayout.CENTER);

        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        filterCombo = new JComboBox<>(new String[]{"All", "Today", "Weekly", "Monthly"});
        filterCombo.addActionListener(e -> loadSalesData());
        filterPanel.add(new JLabel("Filter: "));
        filterPanel.add(filterCombo);

        deleteBtn = new JButton("Delete Sale");
        deleteBtn.addActionListener(e -> deleteSelectedSale());
        filterPanel.add(deleteBtn);

        add(filterPanel, BorderLayout.NORTH);

        loadSalesData();
    }

    private void loadSalesData() {
        tableModel.setRowCount(0);
        String filter = (String) filterCombo.getSelectedItem();
        String sql = "";

        switch (filter) {
            case "Today":
                sql = "SELECT s.*, u.username FROM sales s LEFT JOIN users u ON s.cashier_id=u.id " +
                        "WHERE DATE(s.sale_date) = CURDATE() ORDER BY s.sale_date DESC";
                break;

            case "Weekly":
                sql = "SELECT s.*, u.username FROM sales s LEFT JOIN users u ON s.cashier_id=u.id " +
                        "WHERE s.sale_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) ORDER BY s.sale_date DESC";
                break;

            case "Monthly":
                sql = "SELECT s.*, u.username FROM sales s LEFT JOIN users u ON s.cashier_id=u.id " +
                        "WHERE MONTH(s.sale_date) = MONTH(CURDATE()) AND YEAR(s.sale_date) = YEAR(CURDATE()) " +
                        "ORDER BY s.sale_date DESC";
                break;

            default: 
                sql = "SELECT s.*, u.username FROM sales s LEFT JOIN users u ON s.cashier_id=u.id " +
                        "ORDER BY s.sale_date DESC";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getTimestamp("sale_date").toString(),
                        rs.getString("customer_name"),
                        rs.getString("product_company") + " " + rs.getString("product_model"),
                        String.format("₹%.2f", rs.getDouble("mrp")),
                        String.format("%.1f%%", rs.getDouble("discount")),
                        String.format("%.1f%%", rs.getDouble("gst")),
                        String.format("₹%.2f", rs.getDouble("total_price")),
                        rs.getString("username")
                };
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteSelectedSale() {
        int selectedRow = reportTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a sale record to delete.");
            return;
        }

        int id = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Sale ID: " + id + "?",
                "Delete Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM sales WHERE id=?");
                stmt.setInt(1, id);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Sale deleted successfully!");
                loadSalesData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error while deleting: " + e.getMessage());
            }
        }
    }

    public void refreshData() {
        loadSalesData();
    }
}
