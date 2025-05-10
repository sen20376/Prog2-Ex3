package at.ac.fhcampuswien.fhmdb.exceptions;

public class MovieApiException extends RuntimeException {
    public MovieApiException(String message, Throwable cause) {
        super(message, cause);
    }
}