
package main.ui;

import main.utils.AuthHelper;
import main.models.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("POS System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
    }

    private void buildUI() {

        
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(new Color(240, 242, 245));

       
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(360, 430));
        card.setLayout(null); // absolute layout for clean spacing
        card.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        
        JLabel title = new JLabel("POS SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(30, 70, 130));
        title.setBounds(0, 20, 360, 40);
        card.add(title);

        JLabel subtitle = new JLabel("Login to continue", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setBounds(0, 55, 360, 25);
        card.add(subtitle);

        
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setBounds(40, 110, 200, 20);
        card.add(userLabel);

        
        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setBounds(40, 135, 280, 40);  // <-- thick field
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.add(usernameField);

        // Password Label
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLabel.setBounds(40, 190, 200, 20);
        card.add(passLabel);

        // Password Field
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setBounds(40, 215, 280, 40);  
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.add(passwordField);

        // Login Button
        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setBounds(40, 280, 280, 45);  
        loginBtn.setBackground(new Color(30, 70, 130));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(null);
        loginBtn.addActionListener(e -> login());
        card.add(loginBtn);

       

        bg.add(card);
        add(bg);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }

        User user = AuthHelper.authenticate(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password");
            return;
        }

        this.dispose();
        if ("OWNER".equals(user.getRole())) {
            new OwnerDashboard(user).setVisible(true);
        } else {
            new CashierDashboard(user).setVisible(true);
        }
    }
}
