
public class Movie {
    private int movieId;
    private String movieName;
    private double price;

    public Movie(int movieId, String movieName, double price) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.price = price;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public double getPrice() {
        return price;
    }
}