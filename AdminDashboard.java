import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

public class AdminDashboard {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/cinebook";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Joshna@2007";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void showDashboard(
            JFrame loginFrame,
            int adminId,
            String fullName) {

        JFrame frame = new JFrame("Admin Dashboard");
        frame.setSize(700, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(25, 25, 45));

        JLabel title = new JLabel(
                "ADMIN DASHBOARD",
                SwingConstants.CENTER);
        title.setBounds(100, 25, 500, 40);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.CYAN);
        panel.add(title);

        JLabel welcome = new JLabel(
                "Welcome, " + fullName,
                SwingConstants.CENTER);
        welcome.setBounds(100, 70, 500, 30);
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(welcome);

        JButton bookButton =
                new JButton("BOOK TICKET");
        bookButton.setBounds(80, 130, 240, 50);
        panel.add(bookButton);

        JButton moviesButton =
                new JButton("VIEW MOVIES");
        moviesButton.setBounds(380, 130, 240, 50);
        panel.add(moviesButton);

        JButton addMovieButton =
                new JButton("ADD MOVIE");
        addMovieButton.setBounds(80, 200, 240, 50);
        panel.add(addMovieButton);
        
        JButton removeMovieButton =
        new JButton("REMOVE MOVIE");

        removeMovieButton.setBounds(380, 340, 240, 50);

        panel.add(removeMovieButton); 

        JButton bookingsButton =
                new JButton("ALL BOOKINGS");
        bookingsButton.setBounds(380, 200, 240, 50);
        panel.add(bookingsButton);

        JButton usersButton =
                new JButton("ALL USERS");
        usersButton.setBounds(80, 270, 240, 50);
        panel.add(usersButton);

        JButton revenueButton =
                new JButton("TOTAL REVENUE");
        revenueButton.setBounds(380, 270, 240, 50);
        panel.add(revenueButton);

        JButton seatsButton =
                new JButton("AVAILABLE SEATS");
        seatsButton.setBounds(80, 340, 240, 50);
        panel.add(seatsButton);

        JButton logoutButton =
                new JButton("LOGOUT");
        logoutButton.setBounds(170, 420, 360, 80);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(logoutButton);

        bookButton.addActionListener(e ->
                bookTicket(frame, adminId));

        moviesButton.addActionListener(e ->
                viewMovies(frame));

        addMovieButton.addActionListener(e ->
                addMovie(frame));
        removeMovieButton.addActionListener(e ->
                removeMovie(frame));

        bookingsButton.addActionListener(e ->
                viewAllBookings(frame));

        usersButton.addActionListener(e ->
                viewAllUsers(frame));

        revenueButton.addActionListener(e ->
                showRevenue(frame));

        seatsButton.addActionListener(e ->
                showAvailableSeats(frame));

        logoutButton.addActionListener(e -> {
            frame.dispose();
            loginFrame.setVisible(true);
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void bookTicket(
            JFrame parent,
            int adminId) {

        JTextField customerName =
                new JTextField();

        JTextField mobile =
                new JTextField();

        JComboBox<String> movieBox =
                new JComboBox<>();

        JTextField showDate =
                new JTextField(
                        new SimpleDateFormat(
                                "yyyy-MM-dd")
                                .format(new Date()));

        JComboBox<String> showTime =
                new JComboBox<>(
                        new String[]{
                                "10:00 AM",
                                "1:00 PM",
                                "4:00 PM",
                                "7:00 PM",
                                "10:00 PM"
                        });

        loadMovies(movieBox);

        JPanel panel =
                new JPanel(new GridLayout(5, 2, 8, 8));

        panel.add(new JLabel("Customer Name:"));
        panel.add(customerName);

        panel.add(new JLabel("Mobile Number:"));
        panel.add(mobile);

        panel.add(new JLabel("Movie:"));
        panel.add(movieBox);

        panel.add(new JLabel("Show Date:"));
        panel.add(showDate);

        panel.add(new JLabel("Show Time:"));
        panel.add(showTime);

        int result =
                JOptionPane.showConfirmDialog(
                        parent,
                        panel,
                        "Counter Booking",
                        JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (customerName.getText()
                .trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    parent,
                    "Please enter customer name.");
            return;
        }

        String movie =
                (String) movieBox.getSelectedItem();

        String date =
                showDate.getText().trim();

        String time =
                (String) showTime.getSelectedItem();

        if (!isValidDate(date)) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Date must be yyyy-MM-dd.");
            return;
        }

        selectSeats(
                parent,
                adminId,
                customerName.getText().trim(),
                mobile.getText().trim(),
                movie,
                date,
                time);
    }

    private static void loadMovies(
            JComboBox<String> movieBox) {

        String sql =
                "SELECT movie_name FROM movies " +
                "ORDER BY movie_name";

        try (Connection con = connect();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movieBox.addItem(
                        rs.getString("movie_name"));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Database Error: "
                            + ex.getMessage());
        }
    }

    private static void selectSeats(
            JFrame parent,
            int adminId,
            String customerName,
            String mobile,
            String movie,
            String date,
            String time) {

        JDialog dialog =
                new JDialog(
                        parent,
                        "Select Seats",
                        true);

        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(parent);

        JPanel panel =
                new JPanel(
                        new GridLayout(5, 8, 8, 8));

        ArrayList<JButton> selectedButtons =
                new ArrayList<>();

        String sql =
                "SELECT seat_number " +
                "FROM seat_bookings " +
                "WHERE movie_name = ? " +
                "AND show_time = ? " +
                "AND show_date = ?";

        ArrayList<String> bookedSeats =
                new ArrayList<>();

        try (Connection con = connect();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, movie);
            ps.setString(2, time);
            ps.setDate(3, java.sql.Date.valueOf(date));

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {
                    bookedSeats.add(
                            rs.getString("seat_number"));
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    dialog,
                    "Database Error: "
                            + ex.getMessage());
            return;
        }

        for (char row = 'A'; row <= 'E'; row++) {

            for (int col = 1; col <= 8; col++) {

                String seat =
                        row + String.valueOf(col);

                JButton button =
                        new JButton(seat);

                if (bookedSeats.contains(seat)) {

                    button.setBackground(Color.RED);
                    button.setEnabled(false);

                } else {

                    button.setBackground(Color.GREEN);

                    button.addActionListener(e -> {

                        if (selectedButtons
                                .contains(button)) {

                            button.setBackground(
                                    Color.GREEN);

                            selectedButtons.remove(
                                    button);

                        } else {

                            button.setBackground(
                                    Color.YELLOW);

                            selectedButtons.add(
                                    button);
                        }
                    });
                }

                panel.add(button);
            }
        }

        JButton confirm =
                new JButton("CONFIRM BOOKING");

        confirm.addActionListener(e -> {

            if (selectedButtons.isEmpty()) {

                JOptionPane.showMessageDialog(
                        dialog,
                        "Please select at least one seat.");
                return;
            }

            ArrayList<String> seats =
                    new ArrayList<>();

            for (JButton button :
                    selectedButtons) {

                seats.add(button.getText());
            }

            String[] paymentOptions = {
    "Cash",
    "Online (Demo)"
};

int paymentChoice = JOptionPane.showOptionDialog(
        dialog,
        "Choose Payment Method:",
        "Admin Payment",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        paymentOptions,
        paymentOptions[0]);

if (paymentChoice < 0) {
    return;
}

String paymentMethod = paymentOptions[paymentChoice];

if ("Online (Demo)".equals(paymentMethod)) {
    int paid = JOptionPane.showConfirmDialog(
            dialog,
            "Demo payment only. No real money will be charged.\nConfirm payment?",
            "Confirm Payment",
            JOptionPane.YES_NO_OPTION);

    if (paid != JOptionPane.YES_OPTION) {
        return;
    }
}

saveBooking(
        dialog,
        adminId,
        customerName,
        mobile,
        movie,
        date,
        time,
        seats,
        paymentMethod);
        });

        JPanel mainPanel =
                new JPanel(new BorderLayout());

        mainPanel.add(panel, BorderLayout.CENTER);
        mainPanel.add(confirm,
                BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private static void saveBooking(
            JDialog dialog,
            int adminId,
            String customerName,
            String mobile,
            String movie,
            String date,
            String time,
            ArrayList<String> seats,
            String paymentMethod) {

        String priceSql =
                "SELECT price FROM movies " +
                "WHERE movie_name = ?";

        String bookingSql =
                "INSERT INTO bookings " +
                "(movie_name, tickets, total_amount, " +
                "user_id, customer_name, mobile_number, " +
                "booked_by_role, show_date, show_time, " +
                "booking_status, payment_method) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'ADMIN', ?, ?, 'CONFIRMED', ?)";

        String seatSql =
                "INSERT INTO seat_bookings " +
                "(movie_name, show_time, seat_number, " +
                "booking_id, show_date) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = connect()) {

            con.setAutoCommit(false);

            double price;

            try (PreparedStatement ps =
                         con.prepareStatement(priceSql)) {

                ps.setString(1, movie);

                try (ResultSet rs =
                             ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new SQLException(
                                "Movie price not found.");
                    }

                    price =
                            rs.getDouble("price");
                }
            }

            double total =
                    price * seats.size();

            int bookingId;

            try (PreparedStatement ps =
                         con.prepareStatement(
                                 bookingSql,
                                 Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, movie);
                ps.setInt(2, seats.size());
                ps.setDouble(3, total);
                ps.setInt(4, adminId);
                ps.setString(5, customerName);
                ps.setString(6, mobile);
                ps.setDate(7,
                        java.sql.Date.valueOf(date));
                ps.setString(8, time);
                ps.setString(9, paymentMethod);

                ps.executeUpdate();

                try (ResultSet rs =
                             ps.getGeneratedKeys()) {

                    if (!rs.next()) {
                        throw new SQLException(
                                "Booking ID not generated.");
                    }

                    bookingId =
                            rs.getInt(1);
                }
            }

            try (PreparedStatement ps =
                         con.prepareStatement(seatSql)) {

                for (String seat : seats) {

                    ps.setString(1, movie);
                    ps.setString(2, time);
                    ps.setString(3, seat);
                    ps.setInt(4, bookingId);
                    ps.setDate(5,
                            java.sql.Date.valueOf(date));

                    ps.executeUpdate();
                }
            }

            con.commit();

            JOptionPane.showMessageDialog(
                    dialog,
                    "Booking Successful!\n\n"
                            + "Booking ID: "
                            + bookingId
                            + "\nCustomer: "
                            + customerName
                            + "\nMovie: "
                            + movie
                            + "\nSeats: "
                            + String.join(", ", seats)
                            + "\nTotal: ₹"
                            + total);

            dialog.dispose();

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    dialog,
                    "Booking Failed: "
                            + ex.getMessage());
        }
    }

    private static void viewMovies(
            JFrame parent) {

        StringBuilder text =
                new StringBuilder();

        String sql =
                "SELECT movie_id, movie_name, price " +
                "FROM movies ORDER BY movie_id";

        try (Connection con = connect();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                text.append(
                        "ID: ")
                        .append(rs.getInt("movie_id"))
                        .append("\nMovie: ")
                        .append(rs.getString("movie_name"))
                        .append("\nPrice: ₹")
                        .append(rs.getDouble("price"))
                        .append("\n\n");
            }

            JTextArea area =
                    new JTextArea(text.toString());

            area.setEditable(false);

            JOptionPane.showMessageDialog(
                    parent,
                    new JScrollPane(area),
                    "Movies",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    parent,
                    "Database Error: "
                            + ex.getMessage());
        }
    }

    private static void addMovie(
            JFrame parent) {

        JTextField name =
                new JTextField();

        JTextField price =
                new JTextField();

        JPanel panel =
                new JPanel(new GridLayout(2, 2, 8, 8));

        panel.add(new JLabel("Movie Name:"));
        panel.add(name);

        panel.add(new JLabel("Price:"));
        panel.add(price);

        int result =
                JOptionPane.showConfirmDialog(
                        parent,
                        panel,
                        "Add Movie",
                        JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            double moviePrice =
                    Double.parseDouble(
                            price.getText().trim());

            String sql =
                    "INSERT INTO movies " +
                    "(movie_name, price) " +
                    "VALUES (?, ?)";

            try (Connection con = connect();
                 PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setString(
                        1,
                        name.getText().trim());

                ps.setDouble(
                        2,
                        moviePrice);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        parent,
                        "Movie Added Successfully.");
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    parent,
                    "Enter a valid price.");

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    parent,
                    "Database Error: "
                            + ex.getMessage());
        }
    }
    private static void removeMovie(JFrame parent) {

    JComboBox<String> movieBox = new JComboBox<>();

    String sql = "SELECT movie_name FROM movies ORDER BY movie_name";

    try (Connection con = connect();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            movieBox.addItem(rs.getString("movie_name"));
        }

        if (movieBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(
                    parent, "No movies available.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                parent,
                movieBox,
                "Select Movie to Remove",
                JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String movie = (String) movieBox.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(
                parent,
                "Do you want to remove " + movie + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String checkSql =
                "SELECT COUNT(*) FROM bookings WHERE movie_name = ?";

        try (PreparedStatement check =
                     con.prepareStatement(checkSql)) {

            check.setString(1, movie);

            try (ResultSet checkRs = check.executeQuery()) {
                checkRs.next();

                if (checkRs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(
                            parent,
                            "Cannot remove this movie. Bookings already exist.");
                    return;
                }
            }
        }

        String deleteSql =
                "DELETE FROM movies WHERE movie_name = ?";

        try (PreparedStatement delete =
                     con.prepareStatement(deleteSql)) {

            delete.setString(1, movie);
            delete.executeUpdate();

            JOptionPane.showMessageDialog(
                    parent,
                    "Movie Removed Successfully!");
        }

        } catch (SQLException ex) {

        JOptionPane.showMessageDialog(
                parent,
                "Database Error: " + ex.getMessage());
        }
      }

    private static void viewAllBookings(
            JFrame parent) {

        StringBuilder text =
                new StringBuilder();

        String sql =
                "SELECT booking_id, movie_name, " +
                "tickets, total_amount, " +
                "customer_name, mobile_number, " +
                "booked_by_role, show_date, " +
                "show_time, booking_status, payment_method " +
                "FROM bookings " +
                "ORDER BY booking_id DESC";

        try (Connection con = connect();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                text.append(
                        "Booking ID: ")
                        .append(rs.getInt("booking_id"))
                        .append("\nMovie: ")
                        .append(rs.getString("movie_name"))
                        .append("\nCustomer: ")
                        .append(rs.getString("customer_name"))
                        .append("\nMobile: ")
                        .append(rs.getString("mobile_number"))
                        .append("\nTickets: ")
                        .append(rs.getInt("tickets"))
                        .append("\nTotal: ₹")
                        .append(rs.getDouble("total_amount"))
                        .append("\nBooked By: ")
                        .append(rs.getString("booked_by_role"))
                        .append("\nDate: ")
                        .append(rs.getDate("show_date"))
                        .append("\nTime: ")
                        .append(rs.getString("show_time"))
                        .append("\nStatus: ")
                        .append(rs.getString("booking_status"))
                        .append("\n--------------------------\n");
            }

            JTextArea area =
                    new JTextArea(text.toString());

            area.setEditable(false);
            

JOptionPane.showMessageDialog(
        parent,
        new JScrollPane(area),
        "All Bookings",
        JOptionPane.INFORMATION_MESSAGE);

} catch (SQLException ex) {

    JOptionPane.showMessageDialog(
            parent,
            "Database Error: "
                    + ex.getMessage());
}
}
private static void viewAllUsers(
        JFrame parent) {

    StringBuilder text =
            new StringBuilder();

    String sql =
            "SELECT user_id, username, " +
            "full_name, mobile_number, role " +
            "FROM users ORDER BY user_id";

    try (Connection con = connect();
         PreparedStatement ps =
                 con.prepareStatement(sql);
         ResultSet rs =
                 ps.executeQuery()) {

        while (rs.next()) {

            text.append(
                    "ID: ")
                    .append(rs.getInt("user_id"))
                    .append("\nUsername: ")
                    .append(rs.getString("username"))
                    .append("\nName: ")
                    .append(rs.getString("full_name"))
                    .append("\nMobile: ")
                    .append(rs.getString("mobile_number"))
                    .append("\nRole: ")
                    .append(rs.getString("role"))
                    .append("\n--------------------------\n");
        }

        JTextArea area =
                new JTextArea(text.toString());

        area.setEditable(false);

        JOptionPane.showMessageDialog(
                parent,
                new JScrollPane(area),
                "All Users",
                JOptionPane.INFORMATION_MESSAGE);

    } catch (SQLException ex) {

        JOptionPane.showMessageDialog(
                parent,
                "Database Error: "
                        + ex.getMessage());
    }
}
private static void showAvailableSeats(
            JFrame parent) {

        JTextField date =
                new JTextField(
                        new SimpleDateFormat(
                                "yyyy-MM-dd")
                                .format(new Date()));

        JComboBox<String> movieBox =
                new JComboBox<>();

        JComboBox<String> timeBox =
                new JComboBox<>(
                        new String[]{
                                "10:00 AM",
                                "1:00 PM",
                                "4:00 PM",
                                "7:00 PM",
                                "10:00 PM"
                        });

        loadMovies(movieBox);

        JPanel panel =
                new JPanel(new GridLayout(3, 2, 8, 8));

        panel.add(new JLabel("Movie:"));
        panel.add(movieBox);

        panel.add(new JLabel("Date:"));
        panel.add(date);

        panel.add(new JLabel("Show Time:"));
        panel.add(timeBox);

        int result =
                JOptionPane.showConfirmDialog(
                        parent,
                        panel,
                        "Available Seats",
                        JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String sql =
                "SELECT seat_number " +
                "FROM seat_bookings " +
                "WHERE movie_name = ? " +
                "AND show_time = ? " +
                "AND show_date = ?";

        ArrayList<String> booked =
                new ArrayList<>();

        try (Connection con = connect();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(
                    1,
                    (String) movieBox.getSelectedItem());

            ps.setString(
                    2,
                    (String) timeBox.getSelectedItem());

            ps.setDate(
                    3,
                    java.sql.Date.valueOf(
                            date.getText().trim()));

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {
                    booked.add(
                            rs.getString("seat_number"));
                }
            }

            StringBuilder available =
                    new StringBuilder();

            for (char row = 'A';
                 row <= 'E';
                 row++) {

                for (int col = 1;
                     col <= 8;
                     col++) {

                    String seat =
                            row + String.valueOf(col);

                    if (!booked.contains(seat)) {
                        available.append(seat)
                                .append(" ");
                    }
                }

                available.append("\n");
            }

            JOptionPane.showMessageDialog(
                    parent,
                    "Available Seats:\n\n"
                            + available,
                    "Available Seats",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    parent,
                    "Error: "
                            + ex.getMessage());
        }
    }

    private static boolean isValidDate(
            String date) {

        try {

            java.sql.Date.valueOf(date);
            return true;

        } catch (Exception ex) {

            return false;
        }
    }
    private static void showRevenue(JFrame parent) {

        String sql =
                "SELECT COUNT(*), " +
                "COALESCE(SUM(total_amount), 0) " +
                "FROM bookings";

        try (Connection con = connect();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                int totalBookings = rs.getInt(1);
                double totalRevenue = rs.getDouble(2);

                JOptionPane.showMessageDialog(
                        parent,
                        "Total Bookings: " + totalBookings +
                        "\nTotal Revenue: Rs. " + totalRevenue,
                        "Revenue Report",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    parent,
                    "Error: " + ex.getMessage()
            );
        }
    }
}
