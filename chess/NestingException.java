package chess;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Exception that nests another and serializes the stack trace for use with RMI
 * Creation date: (4/4/00 5:39:58 PM)
 * @author: Terren Suydam
 */
public class NestingException extends Exception {
	// the nested exception
	private Throwable nestedException;

	// String representation of stack trace - not transient!
	private String stackTraceString;

	// java.lang.Exception constructors
	public NestingException() {}
	public NestingException(String msg) {
		super(msg);
	}
	public NestingException(String msg, Throwable nestedException) {
		this(msg);
		this.nestedException = nestedException;
		stackTraceString = generateStackTraceString(nestedException);
	}
	// additional c'tors - nest the exceptions, storing the stack trace
	public NestingException(Throwable nestedException) {
		this.nestedException = nestedException;
		stackTraceString = generateStackTraceString(nestedException);
	}
	// convert a stack trace to a String so it can be serialized
	static public String generateStackTraceString(Throwable e) {
		StringWriter s = new StringWriter();
		e.printStackTrace(new PrintWriter(s));
		return s.toString();
	}
	// overrides Exception.getMessage()
	public String getMessage() {
		// superMsg will contain whatever String was passed into the
		// constructor, and null otherwise.
		String superMsg = super.getMessage();

		// if there's no nested exception, do like we would always do
		if (getNestedException() == null)
			return superMsg;

		StringBuffer theMsg = new StringBuffer();

		// get the nested exception's message
		String nestedMsg = getNestedException().getMessage();

		if (superMsg != null)
			theMsg.append(superMsg).append(": ").append(nestedMsg);
		else
			theMsg.append(nestedMsg);

		return theMsg.toString();
	}
	// methods
	public Throwable getNestedException() {return nestedException;}
	// descend through linked-list of nesting exceptions, & output trace
	// note that this displays the 'deepest' trace first
	public String getStackTraceString() {
		// if there's no nested exception, there's no stackTrace
		if (nestedException == null)
			return null;

		StringBuffer traceBuffer = new StringBuffer();

		if (nestedException instanceof NestingException) {
			traceBuffer.append(((NestingException)nestedException).getStackTraceString());
			traceBuffer.append("-------- nested by:\n");
		}

		traceBuffer.append(stackTraceString);
		return traceBuffer.toString();
	}
	// overrides Exception.toString()
	public String toString() {
		StringBuffer theMsg = new StringBuffer(super.toString());

		if (getNestedException() != null)
			theMsg.append("; \n\t---> nested ").append(getNestedException());

		return theMsg.toString();
	}
}
