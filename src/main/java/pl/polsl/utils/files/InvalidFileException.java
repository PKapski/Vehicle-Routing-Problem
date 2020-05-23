package pl.polsl.utils.files;

public class InvalidFileException extends RuntimeException {
    InvalidFileException(String message) {
        super(message);
    }
}
