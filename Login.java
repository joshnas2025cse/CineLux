import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Login {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/cinebook";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Joshna@2007";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::showLogin);
    }

    private static void showLogin() {

        JFrame frame = new JFrame("CineLux- Login");
        frame.setSize(500, 420);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(25, 25, 45));

        JLabel title = new JLabel(
                "CineLux",
                SwingConstants.CENTER);
        title.setBounds(50, 30, 400, 40);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(Color.CYAN);
        panel.add(title);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(80, 100, 120, 30);
        userLabel.setForeground(Color.WHITE);
        panel.add(userLabel);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(200, 100, 200, 30);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(80, 150, 120, 30);
        passwordLabel.setForeground(Color.WHITE);
        panel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(200, 150, 200, 30);
        panel.add(passwordField);

        JLabel roleLabel = new JLabel("Login As:");
        roleLabel.setBounds(80, 200, 120, 30);
        roleLabel.setForeground(Color.WHITE);
        panel.add(roleLabel);

        String[] roles = {"USER", "ADMIN"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setBounds(200, 200, 200, 30);
        panel.add(roleBox);

        JButton loginButton = new JButton("LOGIN");
        loginButton.setBounds(120, 270, 120, 40);
        panel.add(loginButton);

        JButton registerButton = new JButton("USER REGISTER");
        registerButton.setBounds(260, 270, 140, 40);
        panel.add(registerButton);

        loginButton.addActionListener(e -> {

            String username = usernameField.getText().trim();
            String password =
                    new String(passwordField.getPassword());
            String role =
                    (String) roleBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Please enter username and password.");
                return;
            }

            login(frame, username, password, role);
        });

        registerButton.addActionListener(e ->
                registerUser(frame));

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void login(
            JFrame frame,
            String username,
            String password,
            String role) {

        String sql =
                "SELECT user_id, full_name, role " +
                "FROM users " +
                "WHERE username = ? " +
                "AND password_hash = SHA2(?, 256) " +
                "AND role = ?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int userId = rs.getInt("user_id");
                    String fullName =
                            rs.getString("full_name");

                    JOptionPane.showMessageDialog(
                            frame,
                            "Login Successful!\nWelcome "
                                    + fullName);

                    if (role.equals("ADMIN")) {
                        showAdminDashboard(
                                frame, userId, fullName);
                    } else {
                        showUserDashboard(
                                frame, userId, fullName);
                    }

                } else {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Invalid username, password or role.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Database Error: " + ex.getMessage());
        }
    }

    private static void registerUser(JFrame parent) {

        JTextField username =
                new JTextField();

        JPasswordField password =
                new JPasswordField();

        JTextField name =
                new JTextField();

        JTextField mobile =
                new JTextField();

        JPanel panel =
                new JPanel(new GridLayout(4, 2, 8, 8));

        panel.add(new JLabel("Full Name:"));
        panel.add(name);

        panel.add(new JLabel("Username:"));
        panel.add(username);

        panel.add(new JLabel("Password:"));
        panel.add(password);

        panel.add(new JLabel("Mobile Number:"));
        panel.add(mobile);

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                "User Registration",
                JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (name.getText().trim().isEmpty()
                || username.getText().trim().isEmpty()
                || new String(password.getPassword()).isEmpty()) {

            JOptionPane.showMessageDialog(
                    parent,
                    "Please fill all required fields.");
            return;
        }

        String sql =
                "INSERT INTO users " +
                "(username, password_hash, full_name, " +
                "mobile_number, role) " +
                "VALUES (?, SHA2(?, 256), ?, ?, 'USER')";

        try (Connection con = connect();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, username.getText().trim());
            ps.setString(2,
                    new String(password.getPassword()));
            ps.setString(3, name.getText().trim());
            ps.setString(4, mobile.getText().trim());

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    parent,
                    "Registration Successful!");

        } catch (SQLException ex) {

            if (ex.getMessage()
                    .toLowerCase()
                    .contains("duplicate")) {

                JOptionPane.showMessageDialog(
                        parent,
                        "Username already exists.");

            } else {

                JOptionPane.showMessageDialog(
                        parent,
                        "Database Error: "
                                + ex.getMessage());
            }
        }
    }

    private static void showAdminDashboard(
            JFrame loginFrame,
            int userId,
            String fullName) {

        AdminDashboard.showDashboard(
                loginFrame,
                userId,
                fullName);
    }

    private static void showUserDashboard(
            JFrame loginFrame,
            int userId,
            String fullName) {

        UserDashboard.showDashboard(
                loginFrame,
                userId,
                fullName);
    }
}