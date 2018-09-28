package alchemystar.freedom.engine.net.exception;

/**
 * @author lizhuyang
 */
public class UnknownDataNodeException extends RuntimeException {
    private static final long serialVersionUID = -3752985849571697432L;

    public UnknownDataNodeException() {
        super();
    }

    public UnknownDataNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownDataNodeException(String message) {
        super(message);
    }

    public UnknownDataNodeException(Throwable cause) {
        super(cause);
    }

}
