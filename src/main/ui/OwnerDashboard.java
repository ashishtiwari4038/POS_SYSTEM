package main.ui;

import main.models.User;
import main.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class OwnerDashboard extends JFrame {

    private final User currentUser;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> periodCombo;
    private JButton genBtn;

   
    private static final Font HEAD_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); 
    private static final Color ACCENT_COLOR = new Color(46, 204, 113); 
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241); 
    private static final Color TABLE_HEADER_COLOR = PRIMARY_COLOR.darker();
    // ----------------------------

    public OwnerDashboard(User user) {
       

        this.currentUser = user;
        setTitle("📊 POS System - Owner Dashboard: " + user.getUsername());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 700); 
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        initializeUI();
        loadReportData();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                
            }
        });
    }

    private void initializeUI() {
        setLayout(new BorderLayout(15, 15));
        
        // --- Header Panel (Top) ---
        JPanel topPanel = createHeaderPanel();
        add(topPanel, BorderLayout.NORTH);

        // --- Controls Panel (Center-Top) ---
        JPanel controls = createControlsPanel();
        add(controls, BorderLayout.CENTER);

        // --- Table Setup (Center-Bottom) ---
        JScrollPane sp = createReportTablePanel();
        add(sp, BorderLayout.SOUTH);

        // Initial setup for the period combo box
        updatePeriodOptions();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("💸 Sales Reporting & Overview");
        title.setFont(HEAD_FONT);
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        JButton logout = new JButton("🚪 Log out");
        styleButton(logout, new Color(231, 76, 60), Color.WHITE); // Red logout button
        logout.addActionListener(e -> {
            dispose();
          
            new LoginFrame().setVisible(true);
        });
        headerPanel.add(logout, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createControlsPanel() {
        
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBackground(BACKGROUND_COLOR);
        Border titleBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1), 
                "Report Configuration", TitledBorder.LEFT, TitledBorder.TOP, SUBTITLE_FONT, PRIMARY_COLOR);
        controls.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 15, 0, 15), titleBorder));
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.anchor = GridBagConstraints.WEST;
        
        // Report Type Label
        JLabel rptLabel = new JLabel("Report Type:");
        rptLabel.setFont(SUBTITLE_FONT);
        c.gridx = 0; c.gridy = 0;
        controls.add(rptLabel, c);

        // Report Type ComboBox
        reportTypeCombo = new JComboBox<>(new String[]{"Daily Report", "Weekly Report", "Monthly Report", "Yearly Report"});
        reportTypeCombo.setFont(UI_FONT);
        reportTypeCombo.addActionListener(e -> updatePeriodOptions());
        c.gridx = 1; c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5; 
        controls.add(reportTypeCombo, c);

        // Period Label
        JLabel perLabel = new JLabel("Period:");
        perLabel.setFont(SUBTITLE_FONT);
        c.gridx = 2; c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        controls.add(perLabel, c);

        // Period ComboBox
        periodCombo = new JComboBox<>();
        periodCombo.setFont(UI_FONT);
        c.gridx = 3; c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; // Give this the most room to show long date strings
        controls.add(periodCombo, c);

        // Generate Button
        genBtn = new JButton("📈 Generate Report");
        styleButton(genBtn, ACCENT_COLOR, Color.WHITE);
        genBtn.addActionListener(e -> loadReportData());
        c.gridx = 4; c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        controls.add(genBtn, c);

        // View All Sales Button
        JButton viewAllBtn = new JButton("📋 View All Sales");
        styleButton(viewAllBtn, PRIMARY_COLOR, Color.WHITE);
        viewAllBtn.addActionListener(e -> openSalesDialog());
        c.gridx = 5; c.gridy = 0;
        controls.add(viewAllBtn, c);
        
        // Push everything to the left
        c.gridx = 6; c.gridy = 0;
        c.weightx = 1.0;
        controls.add(Box.createHorizontalGlue(), c); 

        return controls;
    }
    
    private JScrollPane createReportTablePanel() {
        // Table setup (summary)
        String[] columns = {
                "Date / Period",
                "Total Sales (₹)",
                "Total Transactions",
                "Average Sale (₹)",
                "Highest Sale (₹)",
                "Lowest Sale (₹)"
        };
        tableModel = new DefaultTableModel(columns, 0);
        reportTable = new JTable(tableModel);
        
        // Table styling
        reportTable.setRowHeight(30);
        reportTable.setFont(UI_FONT);
        reportTable.setGridColor(Color.LIGHT_GRAY);
        reportTable.setSelectionBackground(PRIMARY_COLOR.brighter());

        // Header styling
        JTableHeader header = reportTable.getTableHeader();
        header.setFont(SUBTITLE_FONT.deriveFont(Font.BOLD, 15f));
        header.setBackground(TABLE_HEADER_COLOR);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        
        // Cell rendering
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // Align all numerical columns to the center
        for (int i = 1; i < reportTable.getColumnCount(); i++) {
            reportTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // First column (Period) alignment
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(new EmptyBorder(0, 10, 0, 0)); 
        reportTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        JScrollPane sp = new JScrollPane(reportTable);
        sp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 15, 15, 15), 
            BorderFactory.createLineBorder(PRIMARY_COLOR.darker())));
        
        sp.setPreferredSize(new Dimension(this.getWidth() - 30, 300));
        
        return sp;
    }
    
    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setFont(SUBTITLE_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(background.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)));
    }


    
    
    
    private void updatePeriodOptions() {
        periodCombo.removeAllItems();
        String rpt = (String) reportTypeCombo.getSelectedItem();
        if (rpt == null) return;

        if (rpt.equals("Daily Report")) {
            
            for (int i = 0; i < 7; i++) {
                LocalDate d = LocalDate.now().minusDays(i);
                periodCombo.addItem(d.toString()); 
            }
        } else if (rpt.equals("Weekly Report")) {
            
            periodCombo.addItem("Last 7 days (including today)");
            for (int i = 1; i <= 6; i++) {
                
                periodCombo.addItem("Last 7 days - " + i + " week(s) ago");
            }
        } else if (rpt.equals("Monthly Report")) {
            
            LocalDate now = LocalDate.now();
            for (int i = 0; i < 12; i++) {
                LocalDate d = now.minusMonths(i);
                Month m = d.getMonth();
                String label = m.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + d.getYear();
                periodCombo.addItem(label);
            }
        } else { 
            int currentYear = LocalDate.now().getYear();
            for (int i = 0; i < 6; i++) {
                periodCombo.addItem(String.valueOf(currentYear - i));
            }
        }
    }

    
    private void loadReportData() {
        tableModel.setRowCount(0);

        String reportType = (String) reportTypeCombo.getSelectedItem();
        String period = (String) periodCombo.getSelectedItem();
        if (reportType == null || period == null) return;

        String sql;
        PreparedStatement stmt = null;

        try (Connection conn = DatabaseConnection.getConnection()) {

            if (reportType.equals("Daily Report")) {
                
                LocalDate d = LocalDate.parse(period);
                sql = """
                        SELECT DATE(sale_date) AS period_label,
                               COALESCE(SUM(total_price),0) AS total_sales,
                               COALESCE(COUNT(*),0) AS tx_count,
                               COALESCE(AVG(total_price),0) AS avg_sale,
                               COALESCE(MAX(total_price),0) AS max_sale,
                               COALESCE(MIN(total_price),0) AS min_sale
                        FROM sales
                        WHERE DATE(sale_date) = ?
                        GROUP BY DATE(sale_date)
                        """;
                stmt = conn.prepareStatement(sql);
                stmt.setDate(1, Date.valueOf(d));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        addReportRow(rs.getString("period_label"),
                                rs.getDouble("total_sales"),
                                rs.getInt("tx_count"),
                                rs.getDouble("avg_sale"),
                                rs.getDouble("max_sale"),
                                rs.getDouble("min_sale"));
                    } else {
                        
                        addReportRow(d.toString(), 0, 0, 0, 0, 0);
                    }
                }
                return;
            }

            if (reportType.equals("Weekly Report")) {
                
                int shiftWeeks = 0;
                if (!period.startsWith("Last 7 days (including")) {
                  
                    String[] parts = period.split("-");
                    if (parts.length >= 2) {
                        String s = parts[1].trim().split(" ")[0];
                        try {
                            shiftWeeks = Integer.parseInt(s);
                        } catch (Exception ex) {
                            shiftWeeks = 0;
                        }
                    }
                }

               
                LocalDate end = LocalDate.now().minusWeeks(shiftWeeks);
                LocalDate start = end.minusDays(6);

                sql = """
                        SELECT CONCAT(DATE(?), ' to ', DATE(?)) AS period_label,
                               COALESCE(SUM(total_price),0) AS total_sales,
                               COALESCE(COUNT(*),0) AS tx_count,
                               COALESCE(AVG(total_price),0) AS avg_sale,
                               COALESCE(MAX(total_price),0) AS max_sale,
                               COALESCE(MIN(total_price),0) AS min_sale
                        FROM sales
                        WHERE sale_date >= ? AND sale_date < DATE_ADD(?, INTERVAL 1 DAY)
                        """;
                stmt = conn.prepareStatement(sql);
               
                stmt.setDate(1, Date.valueOf(start));
                stmt.setDate(2, Date.valueOf(end));
                stmt.setDate(3, Date.valueOf(start));
                stmt.setDate(4, Date.valueOf(end));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        addReportRow(rs.getString("period_label"),
                                rs.getDouble("total_sales"),
                                rs.getInt("tx_count"),
                                rs.getDouble("avg_sale"),
                                rs.getDouble("max_sale"),
                                rs.getDouble("min_sale"));
                    } else {
                        addReportRow(start + " to " + end, 0, 0, 0, 0, 0);
                    }
                }
                return;
            }

            if (reportType.equals("Monthly Report")) {
             
                String[] parts = period.split(" ");
                if (parts.length < 2) {
                   
                    LocalDate now = LocalDate.now();
                    parts = new String[]{ now.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), String.valueOf(now.getYear()) };
                }
                
                String monthName = parts[0];
                int year = Integer.parseInt(parts[1]);
                Month parsedMonth = null;
                
                for (Month m : Month.values()) {
                    if (m.getDisplayName(TextStyle.FULL, Locale.getDefault()).equalsIgnoreCase(monthName)
                            || m.name().equalsIgnoreCase(monthName)
                            || m.getDisplayName(TextStyle.SHORT, Locale.getDefault()).equalsIgnoreCase(monthName)) {
                        parsedMonth = m;
                        break;
                    }
                }
                if (parsedMonth == null) parsedMonth = LocalDate.now().getMonth();

                sql = """
                        SELECT CONCAT(?, ' ', ?) AS period_label,
                               COALESCE(SUM(total_price),0) AS total_sales,
                               COALESCE(COUNT(*),0) AS tx_count,
                               COALESCE(AVG(total_price),0) AS avg_sale,
                               COALESCE(MAX(total_price),0) AS max_sale,
                               COALESCE(MIN(total_price),0) AS min_sale
                        FROM sales
                        WHERE YEAR(sale_date) = ? AND MONTH(sale_date) = ?
                        """;
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, parsedMonth.getDisplayName(TextStyle.FULL, Locale.getDefault()));
                stmt.setInt(2, year);
                stmt.setInt(3, year);
                stmt.setInt(4, parsedMonth.getValue());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        addReportRow(rs.getString("period_label"),
                                rs.getDouble("total_sales"),
                                rs.getInt("tx_count"),
                                rs.getDouble("avg_sale"),
                                rs.getDouble("max_sale"),
                                rs.getDouble("min_sale"));
                    } else {
                        addReportRow(parsedMonth.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + year, 0, 0, 0, 0, 0);
                    }
                }
                return;
            }

            if (reportType.equals("Yearly Report")) {
                int year = Integer.parseInt(period); // period items are years
                sql = """
                        SELECT CAST(? AS CHAR) AS period_label,
                               COALESCE(SUM(total_price),0) AS total_sales,
                               COALESCE(COUNT(*),0) AS tx_count,
                               COALESCE(AVG(total_price),0) AS avg_sale,
                               COALESCE(MAX(total_price),0) AS max_sale,
                               COALESCE(MIN(total_price),0) AS min_sale
                        FROM sales
                        WHERE YEAR(sale_date) = ?
                        """;
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, year);
                stmt.setInt(2, year);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        addReportRow(rs.getString("period_label"),
                                rs.getDouble("total_sales"),
                                rs.getInt("tx_count"),
                                rs.getDouble("avg_sale"),
                                rs.getDouble("max_sale"),
                                rs.getDouble("min_sale"));
                    } else {
                        addReportRow(String.valueOf(year), 0, 0, 0, 0, 0);
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Application Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            
        }
    }

    private void addReportRow(String periodLabel, double totalSales, int txCount, double avg, double max, double min) {
        String totalFmt = String.format("₹%,.2f", totalSales); 
        String avgFmt = String.format("₹%,.2f", avg);
        String maxFmt = String.format("₹%,.2f", max);
        String minFmt = String.format("₹%,.2f", min);

        tableModel.addRow(new Object[]{
                periodLabel,
                totalFmt,
                txCount,
                avgFmt,
                maxFmt,
                minFmt
        });
    }

   
    private void openSalesDialog() {
        JDialog dialog = new JDialog(this, " All Sales (Detailed View)", true);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(BACKGROUND_COLOR.brighter());
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Date", "Customer", "Phone", "Product", "MRP", "Discount", "GST", "Total", "Cashier"};
        DefaultTableModel detailsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable detailsTable = new JTable(detailsModel);
        detailsTable.setRowHeight(28);
        detailsTable.setFont(UI_FONT);
        detailsTable.getTableHeader().setFont(SUBTITLE_FONT);
        detailsTable.setGridColor(Color.LIGHT_GRAY);
        
        JScrollPane scr = new JScrollPane(detailsTable);
        scr.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR.darker()));
        content.add(scr, BorderLayout.CENTER);

        // Buttons panel
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottom.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh Data");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton closeBtn = new JButton("Close");

        styleButton(refreshBtn, new Color(52, 152, 219), Color.WHITE);
        styleButton(deleteBtn, new Color(192, 57, 43), Color.WHITE); 
        styleButton(closeBtn, Color.GRAY.darker(), Color.WHITE);

        bottom.add(refreshBtn);
        bottom.add(deleteBtn);
        bottom.add(closeBtn);
        content.add(bottom, BorderLayout.SOUTH);

        Runnable loader = () -> {
            detailsModel.setRowCount(0);
            String sql = "SELECT s.*, u.username as cashier_name FROM sales s LEFT JOIN users u ON s.cashier_id = u.id ORDER BY s.sale_date DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    Object[] row = new Object[]{
                            rs.getInt("id"),
                            rs.getTimestamp("sale_date").toString().substring(0, 16), // Trim seconds/millis
                            rs.getString("customer_name"),
                            rs.getString("customer_number"),
                            rs.getString("product_company") + " " + rs.getString("product_model"),
                            String.format("₹%,.2f", rs.getDouble("mrp")),
                            String.format("%,.2f%%", rs.getDouble("discount")),
                            String.format("%,.2f%%", rs.getDouble("gst")),
                            String.format("₹%,.2f", rs.getDouble("total_price")),
                            rs.getString("cashier_name")
                    };
                    detailsModel.addRow(row);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "DB error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE));
            }
        };

        
        loader.run();

       
        refreshBtn.addActionListener(e -> loader.run());

        
        deleteBtn.addActionListener(e -> {
            int sel = detailsTable.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select a sale to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object idObj = detailsModel.getValueAt(sel, 0);
            if (idObj == null) return;
            int saleId = Integer.parseInt(idObj.toString());

            int conf = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to delete Sale ID: " + saleId + "?\nThis action cannot be undone.",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (conf == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pst = conn.prepareStatement("DELETE FROM sales WHERE id = ?")) {
                    pst.setInt(1, saleId);
                    int affected = pst.executeUpdate();
                    if (affected > 0) {
                        JOptionPane.showMessageDialog(dialog, "Sale deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                       
                        loader.run();
                        loadReportData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Delete failed (no rows affected).", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Error deleting sale: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        
        closeBtn.addActionListener(e -> dialog.dispose());

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }
}