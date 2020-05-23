package pl.polsl.solutions;

public class InvalidAssumptionsError extends RuntimeException{
    public InvalidAssumptionsError(String error) {
        super(error);
    }
}
