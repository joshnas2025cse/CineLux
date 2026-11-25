import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class CinemaGUI {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/cinebook";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Joshna@2007";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CinemaGUI::showWindow);
    }

    private static void showWindow() {
        JFrame frame = new JFrame("CineLux");
        frame.setSize(700, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(25, 25, 45));

        JLabel title = new JLabel("CineLux", SwingConstants.CENTER);
        title.setBounds(100, 20, 500, 45);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        panel.add(title);

        JButton viewMovies = new JButton("View All Movies");
        JButton bookMovie = new JButton("Book a Movie");
        JButton viewBookings = new JButton("View All Bookings");
        JButton exit = new JButton("Exit");

        JButton[] buttons = {viewMovies, bookMovie, viewBookings, exit};

        int y = 145;
        for (JButton button : buttons) {
            button.setBounds(220, y, 260, 45);
            button.setFont(new Font("Arial", Font.BOLD, 16));
            panel.add(button);
            y += 65;
        }

        viewMovies.addActionListener(e -> showMovies(frame));
        bookMovie.addActionListener(e -> startBooking(frame, 0));
        viewBookings.addActionListener(e -> showBookings(frame, 0));
        exit.addActionListener(e -> frame.dispose());

        frame.add(panel);
        frame.setVisible(true);
    }

    public static void showMovies(JFrame frame) {
        JFrame movieFrame = new JFrame("Available Movies");
        movieFrame.setSize(650, 500);
        movieFrame.setLocationRelativeTo(frame);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(25, 25, 45));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("NOW SHOWING", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.CYAN);
        panel.add(title, BorderLayout.NORTH);

        JPanel moviesPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        moviesPanel.setBackground(new Color(25, 25, 45));

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT movie_name, price FROM movies")) {

            while (rs.next()) {
                String movie = rs.getString("movie_name");
                double price = rs.getDouble("price");

                JPanel card = new JPanel(new GridLayout(3, 1, 5, 5));
                card.setBackground(new Color(45, 45, 75));
                card.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

                JLabel icon = new JLabel("MOVIE", SwingConstants.CENTER);
                icon.setForeground(Color.CYAN);
                icon.setFont(new Font("Arial", Font.BOLD, 22));

                JLabel name = new JLabel(movie, SwingConstants.CENTER);
                name.setForeground(Color.WHITE);
                name.setFont(new Font("Arial", Font.BOLD, 17));

                JLabel priceLabel = new JLabel("Ticket Price: Rs. " + price, SwingConstants.CENTER);
                priceLabel.setForeground(Color.GREEN);

                card.add(icon);
                card.add(name);
                card.add(priceLabel);

                moviesPanel.add(card);
            }

        } catch (SQLException ex) {
            showError(movieFrame, ex);
            movieFrame.dispose();
            return;
        }

        panel.add(new JScrollPane(moviesPanel), BorderLayout.CENTER);

        JButton close = new JButton("CLOSE");
        close.addActionListener(e -> movieFrame.dispose());

        JPanel bottom = new JPanel();
        bottom.add(close);
        panel.add(bottom, BorderLayout.SOUTH);

        movieFrame.add(panel);
        movieFrame.setVisible(true);
    }

    public static void startBooking(JFrame frame, int userId) {
        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT movie_name FROM movies ORDER BY movie_name")) {

            java.util.List<String> movies = new ArrayList<>();

            while (rs.next()) {
                movies.add(rs.getString("movie_name"));
            }

            if (movies.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No movies available.");
                return;
            }

            JComboBox<String> movieBox = new JComboBox<>(movies.toArray(new String[0]));

            String[] times = {"10:00 AM", "2:00 PM", "6:00 PM", "9:00 PM"};
            JComboBox<String> timeBox = new JComboBox<>(times);

            SpinnerDateModel dateModel = new SpinnerDateModel(
                    new java.util.Date(), null, null, Calendar.DAY_OF_MONTH);

            JSpinner dateSpinner = new JSpinner(dateModel);
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy"));

            JPanel choices = new JPanel(new GridLayout(3, 2, 10, 10));
            choices.add(new JLabel("Choose Movie:"));
            choices.add(movieBox);
            choices.add(new JLabel("Choose Booking Date:"));
            choices.add(dateSpinner);
            choices.add(new JLabel("Choose Show Time:"));
            choices.add(timeBox);

            int choice = JOptionPane.showConfirmDialog(
                    frame, choices, "Movie Booking",
                    JOptionPane.OK_CANCEL_OPTION);

            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            String movie = (String) movieBox.getSelectedItem();
            String time = (String) timeBox.getSelectedItem();

            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            java.sql.Date showDate = new java.sql.Date(selectedDate.getTime());

            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

            if (showDate.before(today)) {
                JOptionPane.showMessageDialog(frame, "Please select today or a future date.");
                return;
            }

            double price;

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT price FROM movies WHERE movie_name = ?")) {
                ps.setString(1, movie);

                try (ResultSet priceRs = ps.executeQuery()) {
                    if (!priceRs.next()) {
                        JOptionPane.showMessageDialog(frame, "Movie price not found.");
                        return;
                    }
                    price = priceRs.getDouble("price");
                }
            }

            showSeatMap(frame, movie, time, showDate, userId, price);

        } catch (SQLException ex) {
            showError(frame, ex);
        }
    }

    private static void showSeatMap(
            JFrame parent, String movie, String time,
            java.sql.Date showDate, int userId, double price) {

        JFrame seatFrame = new JFrame("Choose Your Seats");
        seatFrame.setSize(650, 530);
        seatFrame.setLocationRelativeTo(parent);
        seatFrame.setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                movie + " | " + showDate + " | " + time,
                SwingConstants.CENTER);

        heading.setFont(new Font("Arial", Font.BOLD, 18));
        heading.setForeground(Color.WHITE);
        heading.setOpaque(true);
        heading.setBackground(new Color(25, 25, 45));

        JLabel screen = new JLabel("MOVIE SCREEN", SwingConstants.CENTER);
        screen.setFont(new Font("Arial", Font.BOLD, 22));
        screen.setForeground(Color.CYAN);

        JPanel top = new JPanel(new GridLayout(2, 1, 5, 5));
        top.setBackground(new Color(25, 25, 45));
        top.add(heading);
        top.add(screen);

        seatFrame.add(top, BorderLayout.NORTH);

        JPanel seatsPanel = new JPanel(new GridLayout(5, 8, 8, 8));
        seatsPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        Set<String> selectedSeats = new LinkedHashSet<>();
        Set<String> bookedSeats = new HashSet<>();

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT seat_number FROM seat_bookings WHERE movie_name = ? AND show_time = ? AND show_date = ?")) {

            ps.setString(1, movie);
            ps.setString(2, time);
            ps.setDate(3, showDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookedSeats.add(rs.getString("seat_number"));
                }
            }

        } catch (SQLException ex) {
            showError(parent, ex);
            seatFrame.dispose();
            return;
        }

        for (int row = 0; row < 5; row++) {
            char letter = (char) ('A' + row);

            for (int col = 1; col <= 8; col++) {
                String seat = letter + String.valueOf(col);
                JButton button = new JButton(seat);

                if (bookedSeats.contains(seat)) {
                    button.setBackground(Color.RED);
                    button.setForeground(Color.WHITE);
                    button.setEnabled(false);
                } else {
                    button.setBackground(Color.GREEN);

                    button.addActionListener(e -> {
                        if (selectedSeats.contains(seat)) {
                            selectedSeats.remove(seat);
                            button.setBackground(Color.GREEN);
                        } else {
                            selectedSeats.add(seat);
                            button.setBackground(Color.YELLOW);
                        }
                    });
                }

                seatsPanel.add(button);
            }
        }

        seatFrame.add(seatsPanel, BorderLayout.CENTER);

        JLabel legend = new JLabel(
                "Green: Available     Yellow: Selected     Red: Booked",
                SwingConstants.CENTER);

        JButton confirm = new JButton("Proceed to Payment");

        confirm.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(seatFrame, "Please select seats.");
                return;
            }

            double total = price * selectedSeats.size();

            showPayment(
                    seatFrame, movie, time, showDate,
                    userId, price, total, new ArrayList<>(selectedSeats));
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(legend, BorderLayout.CENTER);
        bottom.add(confirm, BorderLayout.SOUTH);

        seatFrame.add(bottom, BorderLayout.SOUTH);
        seatFrame.setVisible(true);
    }

    private static void showPayment(
            JFrame seatFrame, String movie, String time,
            java.sql.Date showDate, int userId, double price,
            double total, java.util.List<String> seats) {

        JPanel paymentPanel = new JPanel(new GridLayout(0, 1, 8, 8));

        JLabel amount = new JLabel("Total Amount: Rs. " + total);
        amount.setFont(new Font("Arial", Font.BOLD, 18));

        JRadioButton gpay = new JRadioButton("Google Pay");
        JRadioButton phonepe = new JRadioButton("PhonePe");
        JRadioButton paytm = new JRadioButton("Paytm");
        JRadioButton upi = new JRadioButton("UPI");

        ButtonGroup group = new ButtonGroup();
        group.add(gpay);
        group.add(phonepe);
        group.add(paytm);
        group.add(upi);

        gpay.setSelected(true);

        paymentPanel.add(amount);
        paymentPanel.add(new JLabel("Choose Payment Method:"));
        paymentPanel.add(gpay);
        paymentPanel.add(phonepe);
        paymentPanel.add(paytm);
        paymentPanel.add(upi);
        paymentPanel.add(new JLabel("Demo Payment Only - No Real Money Will Be Charged"));

        int choice = JOptionPane.showConfirmDialog(
                seatFrame, paymentPanel,
                "Online Payment",
                JOptionPane.OK_CANCEL_OPTION);

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String method = gpay.isSelected() ? "Google Pay"
                : phonepe.isSelected() ? "PhonePe"
                : paytm.isSelected() ? "Paytm"
                : upi.isSelected() ? "UPI" : null;

        if (method == null) {
            JOptionPane.showMessageDialog(seatFrame, "Please select a payment method.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                seatFrame,
                "Pay Rs. " + total + " using " + method + "?\nThis is a demo payment.",
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        saveBooking(
                seatFrame, movie, time, showDate,
                userId, price, total, seats, method);
    }

    private static void saveBooking(
            JFrame seatFrame, String movie, String time,
            java.sql.Date showDate, int userId, double price,
            double total, java.util.List<String> seats, String method) {

        try (Connection con = connect()) {
            con.setAutoCommit(false);

            try {
                
String customerName = "Guest";
                if (userId != 0) {
                    try (PreparedStatement userPs = con.prepareStatement(
                            "SELECT full_name FROM users WHERE user_id = ?")) {
                        userPs.setInt(1, userId);

                        try (ResultSet userRs = userPs.executeQuery()) {
                            if (userRs.next()) {
                                customerName = userRs.getString("full_name");
                            }
                        }
                    }
                }

                int bookingId;

                String bookingSql = "INSERT INTO bookings "
                        + "(movie_name, tickets, total_amount, user_id, customer_name, show_date, show_time, booking_status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = con.prepareStatement(
                        bookingSql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, movie);
                    ps.setInt(2, seats.size());
                    ps.setDouble(3, total);

                    if (userId == 0) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setInt(4, userId);
                    }

                    ps.setString(5, customerName);
                    ps.setDate(6, showDate);
                    ps.setString(7, time);
                    ps.setString(8, "CONFIRMED");

                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Booking ID could not be generated.");
                        }
                        bookingId = keys.getInt(1);
                    }
                }

                String seatSql = "INSERT INTO seat_bookings "
                        + "(movie_name, show_time, seat_number, booking_id, show_date) "
                        + "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement ps = con.prepareStatement(seatSql)) {
                    for (String seat : seats) {
                        ps.setString(1, movie);
                        ps.setString(2, time);
                        ps.setString(3, seat);
                        ps.setInt(4, bookingId);
                        ps.setDate(5, showDate);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();

                JOptionPane.showMessageDialog(
                        seatFrame,
                        "PAYMENT SUCCESSFUL!\n\n"
                                + "Payment Method: " + method + "\n"
                                + "Booking ID: " + bookingId + "\n"
                                + "Movie: " + movie + "\n"
                                + "Date: " + showDate + "\n"
                                + "Show Time: " + time + "\n"
                                + "Seats: " + String.join(", ", seats) + "\n"
                                + "Total: Rs. " + total,
                        "Payment Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                            
                seatFrame.dispose();

            } catch (SQLException ex) {
                con.rollback();
                showError(seatFrame, ex);
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            showError(seatFrame, ex);
        }
    }

    public static void showBookings(JFrame frame, int userId) {

        JFrame bookingFrame = new JFrame("My Bookings");
        bookingFrame.setSize(700, 550);
        bookingFrame.setLocationRelativeTo(frame);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(25, 25, 45));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("BOOKING HISTORY", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Color.CYAN);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(new Color(25, 25, 45));

        boolean hasBookings = false;

        String sql;

        if (userId == 0) {
            sql = "SELECT * FROM bookings ORDER BY booking_id DESC";
        } else {
            sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY booking_id DESC";
        }

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (userId != 0) {
                ps.setInt(1, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    hasBookings = true;

                    int id = rs.getInt("booking_id");
                    String movie = rs.getString("movie_name");
                    int tickets = rs.getInt("tickets");
                    double total = rs.getDouble("total_amount");
                    String date = rs.getString("show_date");
                    String time = rs.getString("show_time");
                    String status = rs.getString("booking_status");

                    JPanel card = new JPanel(new BorderLayout(10, 10));
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
                    card.setBackground(new Color(45, 45, 75));
                    card.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

                    JPanel details = new JPanel(new GridLayout(0, 1, 5, 5));
                    details.setBackground(new Color(45, 45, 75));

                    JLabel idLabel = new JLabel("Booking ID: " + id);
                    idLabel.setForeground(Color.CYAN);
                    JLabel movieLabel = new JLabel("Movie: " + movie);
movieLabel.setForeground(Color.WHITE);

JLabel ticketsLabel = new JLabel("Tickets: " + tickets);
ticketsLabel.setForeground(Color.WHITE);

JLabel dateLabel = new JLabel("Date: " + date + " | Show: " + time);
dateLabel.setForeground(Color.WHITE);

JLabel amountLabel = new JLabel("Total Amount: Rs. " + total);
amountLabel.setForeground(Color.GREEN);

JLabel statusLabel = new JLabel("Status: " + status);
statusLabel.setForeground(
        "CANCELLED".equalsIgnoreCase(status) ? Color.RED : Color.GREEN);

details.add(idLabel);
details.add(movieLabel);
details.add(ticketsLabel);
details.add(dateLabel);
details.add(amountLabel);
details.add(statusLabel);

card.add(details, BorderLayout.CENTER);

if (!"CANCELLED".equalsIgnoreCase(status)) {
    JButton cancelButton = new JButton("CANCEL BOOKING");

    cancelButton.addActionListener(e -> {
        int choice = JOptionPane.showConfirmDialog(
                bookingFrame,
                "Are you sure you want to cancel Booking ID " + id + "?",
                "Cancel Booking",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            cancelBooking(bookingFrame, id, userId);
            bookingFrame.dispose();
            showBookings(frame, userId);
        }
    });

    card.add(cancelButton, BorderLayout.SOUTH);
}
if (!"CANCELLED".equalsIgnoreCase(status)) {

    if (isCancellationAllowed(date, time)) {

        JButton cancelButton =
                new JButton("CANCEL BOOKING");

        cancelButton.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                    bookingFrame,
                    "Are you sure you want to cancel Booking ID " + id + "?",
                    "Cancel Booking",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {

                cancelBooking(bookingFrame, id, userId);

                bookingFrame.dispose();

                showBookings(frame, userId);
            }
        });

        card.add(cancelButton, BorderLayout.SOUTH);

    } else {

        JLabel closedLabel = new JLabel(
                "Cancellation Closed - Less than 1 hour");

        closedLabel.setForeground(Color.ORANGE);

        card.add(closedLabel, BorderLayout.SOUTH);
    }
}
cardsPanel.add(card);
cardsPanel.add(Box.createVerticalStrut(12));
}
}

if (!hasBookings) {
    JLabel empty = new JLabel("No bookings found!", SwingConstants.CENTER);
    empty.setForeground(Color.WHITE);
    empty.setFont(new Font("Arial", Font.BOLD, 20));
    cardsPanel.add(empty);
}

} catch (SQLException ex) {
    showError(bookingFrame, ex);
    bookingFrame.dispose();
    return;
}

JScrollPane scrollPane = new JScrollPane(cardsPanel);
scrollPane.setBorder(null);
scrollPane.getViewport().setBackground(new Color(25, 25, 45));

mainPanel.add(scrollPane, BorderLayout.CENTER);

JButton close = new JButton("CLOSE");
close.addActionListener(e -> bookingFrame.dispose());

JPanel bottom = new JPanel();
bottom.add(close);
mainPanel.add(bottom, BorderLayout.SOUTH);

bookingFrame.add(mainPanel);
bookingFrame.setVisible(true);
}

private static void cancelBooking(JFrame frame, int bookingId, int userId) {

    try (Connection con = connect()) {
        con.setAutoCommit(false);

        try {
            String sql;

            if (userId == 0) {
                sql = "UPDATE bookings SET booking_status = 'CANCELLED' "
                        + "WHERE booking_id = ? AND booking_status <> 'CANCELLED'";
            } else {
                sql = "UPDATE bookings SET booking_status = 'CANCELLED' "
                        + "WHERE booking_id = ? AND user_id = ? AND booking_status <> 'CANCELLED'";
            }

            int updated;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, bookingId);

                if (userId != 0) {
                    ps.setInt(2, userId);
                }

                updated = ps.executeUpdate();
            }

            if (updated == 0) {
                con.rollback();
                JOptionPane.showMessageDialog(frame, "Booking could not be cancelled.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM seat_bookings WHERE booking_id = ?")) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            con.commit();

            JOptionPane.showMessageDialog(
                    frame, "Booking Cancelled Successfully!");

        } catch (SQLException ex) {
            con.rollback();
            showError(frame, ex);
        } finally {
            con.setAutoCommit(true);
        }

    } catch (SQLException ex) {
        showError(frame, ex);
    }
}
private static boolean isCancellationAllowed(
        String date, String time) {

    try {

        LocalDate showDate = LocalDate.parse(date);

        LocalTime showTime = LocalTime.parse(
                time,
                DateTimeFormatter.ofPattern(
                        "h:mm a", java.util.Locale.ENGLISH));

        LocalDateTime showDateTime =
                LocalDateTime.of(showDate, showTime);

        LocalDateTime deadline =
                showDateTime.minusHours(1);

        return !LocalDateTime.now().isAfter(deadline);

    } catch (Exception ex) {

        return false;
    }
}

private static void showError(JFrame frame, SQLException ex) {
    JOptionPane.showMessageDialog(
            frame,
            "Database Error: " + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
}
}