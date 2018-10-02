package com.faisal.exception.path.json;

public class JsonException extends Exception {
	
	private static final long serialVersionUID = -2645485136116914758L;
	private Throwable cause;
	private String message;

	public JsonException() {

	}

	public JsonException(String message) {
		super(message);
		this.message = message;
	}

	public JsonException(Throwable cause) {
		super(cause);
		this.cause = cause;
	}

	public JsonException(String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
		this.message = message;
	}

	public JsonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.cause = cause;
		this.message = message;
	}

	@Override
	public Throwable getCause() {
		return this.cause;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return this.message;
	}

}
