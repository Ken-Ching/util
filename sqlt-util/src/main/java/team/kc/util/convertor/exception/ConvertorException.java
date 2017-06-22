package team.kc.util.convertor.exception;

public class ConvertorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ConvertorException() {
		super();
	}

	public ConvertorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConvertorException(String message) {
		super(message);
	}

	public ConvertorException(Throwable cause) {
		super(cause);
	}
}
