package at.ac.fhcampuswien.fhmdb.exceptions;

public class MovieNotFoundException extends RuntimeException {
  public MovieNotFoundException(String message) {
    super(message);
  }
}
