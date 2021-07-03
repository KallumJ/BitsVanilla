package team.bits.vanilla.fabric.database.driver;

public class DatabaseDriverException extends RuntimeException {

    public DatabaseDriverException(String message) {
        super(message);
    }

    public DatabaseDriverException(String message, Throwable cause) {
        super(message, cause);
    }
}
