import javax.swing.*;
import java.awt.*;

public class UserDashboard {

    public static void showDashboard(
            JFrame loginFrame,
            int userId,
            String fullName) {

        JFrame frame = new JFrame("User Dashboard");

        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(25, 25, 45));

        JLabel title = new JLabel(
                "USER DASHBOARD",
                SwingConstants.CENTER);

        title.setBounds(100, 30, 500, 50);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.CYAN);
        panel.add(title);

        JLabel welcome = new JLabel(
                "Welcome, " + fullName);

        welcome.setBounds(150, 110, 400, 35);
        welcome.setFont(new Font("Arial", Font.BOLD, 20));
        welcome.setForeground(Color.WHITE);
        panel.add(welcome);

        JButton bookButton = new JButton("BOOK TICKET");
        bookButton.setBounds(200, 180, 300, 45);
        panel.add(bookButton);

        JButton historyButton = new JButton("MY BOOKINGS");
        historyButton.setBounds(200, 245, 300, 45);
        panel.add(historyButton);

        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setBounds(200, 310, 300, 45);
        panel.add(logoutButton);

        bookButton.addActionListener(e ->
        CinemaGUI.startBooking(frame, userId));

        historyButton.addActionListener(e ->
        CinemaGUI.showBookings(frame, userId));

        logoutButton.addActionListener(e -> {
            frame.dispose();
            loginFrame.setVisible(true);
        });

        frame.add(panel);
        loginFrame.setVisible(false);
        frame.setVisible(true);
    }
}