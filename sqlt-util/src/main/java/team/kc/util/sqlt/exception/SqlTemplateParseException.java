package team.kc.util.sqlt.exception;

public class SqlTemplateParseException extends Exception{

	private static final long serialVersionUID = 1L;

	public SqlTemplateParseException() {
		super();
	}

	public SqlTemplateParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlTemplateParseException(String message) {
		super(message);
	}

	public SqlTemplateParseException(Throwable cause) {
		super(cause);
	}


}
