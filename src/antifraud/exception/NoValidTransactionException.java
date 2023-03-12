package antifraud.exception;

public class NoValidTransactionException extends RuntimeException{
    public NoValidTransactionException(String message){
        super(message);
    }
}
