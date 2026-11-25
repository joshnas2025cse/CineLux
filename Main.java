import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/cinebook",
                "root",
                "Joshna@2007"
            );

            Scanner sc = new Scanner(System.in);

            while (true) {

                System.out.println("\n===== CineLux =====");
                System.out.println("1. View All Movies");
                System.out.println("2. Book a Movie");
                System.out.println("3. View All Bookings");
                System.out.println("4. Exit");
                System.out.print("Enter your choice: ");

                int choice = sc.nextInt();
                sc.nextLine();

                if (choice == 1) {

                    Statement st = con.createStatement();

                    ResultSet rs = st.executeQuery(
                        "SELECT * FROM movies"
                    );

                    System.out.println("\nAvailable Movies:");

                    while (rs.next()) {
                        System.out.println(
                            rs.getString("movie_name")
                            + " - Rs."
                            + rs.getDouble("price")
                        );
                    }

                    rs.close();
                    st.close();

                } else if (choice == 2) {

                    System.out.print("Enter movie name: ");
                    String name = sc.nextLine();

                    PreparedStatement ps = con.prepareStatement(
                        "SELECT price FROM movies WHERE movie_name = ?"
                    );

                    ps.setString(1, name);

                    ResultSet movie = ps.executeQuery();

                    if (movie.next()) {

                        double price = movie.getDouble("price");

                        System.out.print("Enter number of tickets: ");
                        int tickets = sc.nextInt();
                        sc.nextLine();

                        if (tickets > 0) {

                            double total = price * tickets;

                            PreparedStatement save = con.prepareStatement(
                                "INSERT INTO bookings "
                                + "(movie_name, tickets, total_amount) "
                                + "VALUES (?, ?, ?)"
                            );

                            save.setString(1, name);
                            save.setInt(2, tickets);
                            save.setDouble(3, total);

                            save.executeUpdate();

                            System.out.println("\nBooking saved successfully!");
                            System.out.println("Movie: " + name);
                            System.out.println("Ticket price: Rs." + price);
                            System.out.println("Tickets: " + tickets);
                            System.out.println("Total amount: Rs." + total);

                            save.close();

                        } else {
                            System.out.println("Invalid ticket count!");
                        }

                    } else {
                        System.out.println("Movie not found!");
                    }

                    movie.close();
                    ps.close();

                } else if (choice == 3) {

                    Statement st = con.createStatement();

                    ResultSet rs = st.executeQuery(
                        "SELECT * FROM bookings"
                    );

                    System.out.println("\n===== ALL BOOKINGS =====");

                    while (rs.next()) {
                        System.out.println(
                            "Booking ID: " + rs.getInt("booking_id")
                        );

                        System.out.println(
                            "Movie: " + rs.getString("movie_name")
                        );

                        System.out.println(
                            "Tickets: " + rs.getInt("tickets")
                        );

                        System.out.println(
                            "Total: Rs." + rs.getDouble("total_amount")
                        );

                        System.out.println("----------------------");
                    }

                    rs.close();
                    st.close();

                } else if (choice == 4) {

                    System.out.println("Thank you for using CineLux!");

                    break;

                } else {

                    System.out.println("Invalid choice! Please try again.");
                }
            }

            con.close();
            sc.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}