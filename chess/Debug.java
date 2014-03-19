package chess;

/*
*  Debug.java
*
*  Example of how to use this class:
*
*    Before any debug messages from a class are printed the class must register
*    with this class here are some examples...
*      Debug.setDebug( (this.getClass()).getName(), Debug.DEFAULT );
*
*   Next you may want to set an new debug level for class that use
*   "DEFAULT" when they register...
*      Debug.setDebugLevel(Debug.INFO);
*
*   Then your ready to send the debug messages passing in the current object and
*   the debug leve for this message.
*    Debug.debugMsg(this, Debug.INFO, "Server is initializing to port " + port);
*
*/

import java.util.*;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
*   This class contains misc. utility methods. All methods are static.
*/
public class Debug
{
	private static final String CLASS_MESSAGE_LEVEL = "_Message_Level";
	private static final String DEFAULT_MESSAGE_LEVEL = "DEFAULT_Message_Level";
	private static final String DEFAULT_CLASS = "$DEFAULT_CLASS$";

	public static final int
	/* debug level codes */
		DEFAULT = 0,
		ERROR   = 1,
		WARNING = 2,
		INFO    = 3,
		DEBUG   = 4,
		DEBUG_HI= 5;
		
	static String[] msgType = { "DEFAULT", "ERROR", "WARNING", "INFO", "DEBUG", "DEBUG_HI" };
	static boolean printingEnabled = true;
	static boolean printingAbbreviated = false;
	static Hashtable debugClasses = new Hashtable();
	static int debugLevel = DEBUG;
	
	static {
		// set up to be robust if for some reason get an invalid object
		// in debugMsg
		setDebug (DEFAULT_CLASS, DEFAULT);
	}

	static PrintStream printStream = System.out;
	/**
	* Check debug level and if debugging has been enabled for the
	* calling object then print line to System.out 
	* @param class - the calling object or string
	* @param msgLevel - ERROR, INFO, DEBUG
	* @param msg
	*/
	static public void debugMsg(Object obj, int msgLevel, Object msg)
	{
		Class callingClass;
		String className;

		if (null != obj)
		{
	        callingClass = obj.getClass();
	        className = callingClass.getName();
		} // if
		else
		{
			className = DEFAULT_CLASS;
		} // else

		debugMsg(className, msgLevel, msg);
	}
static public void debugMsg(String className, int msgLevel, Object msg) {
	int level;

	if (!debugClasses.containsKey(className) && debugLevel < DEBUG)
		return;
		
	if (printingEnabled) {
		if (!debugClasses.containsKey(className))
			level = DEBUG;
		else
			level = ((Integer) (debugClasses.get(className))).intValue();
			
		if (level == DEFAULT)
			level = debugLevel;

			if (msgLevel <= level) {
			if (printingAbbreviated)
				className = className.substring(className.lastIndexOf('.') + 1);

			StringBuffer debug_msg = new StringBuffer(120);
			debug_msg.append("(").append(className).append(" ");
			debug_msg.append(Thread.currentThread().getName());
			debug_msg.append(" ").append(msgType[msgLevel]).append(") ");
			debug_msg.append(msg.toString());
			outputString(debug_msg.toString());
			debug_msg = null;
		}
	}
}
	static public void extractDebugLevels (Properties props)
	{
		String levelStr;
		int suffixLength, level;
		
		suffixLength = CLASS_MESSAGE_LEVEL.length();

		levelStr = props.getProperty (DEFAULT_MESSAGE_LEVEL);
		try
		{
	        level = Integer.parseInt(levelStr);
		}
		catch(Exception ex)
		{
	        System.out.println("Setting default message level to ERROR");
	        level = Debug.ERROR;
		}

		setDebugLevel (level);
		
		Enumeration e = props.propertyNames();
		while ( e.hasMoreElements() )
		{
			String propertyName = (String)e.nextElement();
			if( propertyName.endsWith(CLASS_MESSAGE_LEVEL) )
			{
				String className = propertyName.
					substring (0, propertyName.length() - suffixLength);
				levelStr = props.getProperty (propertyName);
		        try
		        {
			        level = Integer.parseInt (levelStr);
		        }
		        catch(Exception ex)
		        {
			        System.out.println ("Setting default message level for class " +
			        	className);
			        level = Debug.ERROR;
		        }
   		        Debug.setDebug( className, level );
			}
		}
		
	}
/**
 * Insert the method's description here.
 * Creation date: (4/5/00 11:44:58 AM)
 * @return java.io.PrintStream
 */
public static java.io.PrintStream getPrintStream() {
	return printStream;
}
/**
 * Outputs message to the output stream.
 * Creation date: (4/5/00 11:23:54 AM)
 * @param message java.lang.String
 */
public static void outputString(String message) {
	// to do: create log file stream
	getPrintStream().println(message);
}
/**
 * Output the stack trace.
 * Creation date: (4/5/00 11:41:52 AM)
 * @param exception java.lang.Exception
 */
public static void printStackTrace(Exception e) {
	StringWriter s = new StringWriter();
	e.printStackTrace(new PrintWriter(s));
	outputString(s.toString());
}
	/**
	* Enable or disable printing of messages for specified class 
	* @param className
	*/
	static public void setDebug(Object obj, int level)
	{
		setDebug(obj.getClass().getName(), level);    
	}
	/**
	* Enable or disable printing of messages for specified class 
	* @param className
	*/
	static public synchronized void setDebug(String className, int level)
	{
		debugClasses.put(className, new Integer(level));    
	}
	/**
	* Set debug level
	* @param level
	*/
	static public void setDebugLevel(int level)
	{
		debugLevel = level;
	}
	/**
	* Enable or disable printing in the debugMsg method
	* @param enabled
	*/
	static public void setPrinting(boolean enabled)
	{
		setPrinting(enabled,false);
	}
	static public void setPrinting(boolean enabled, boolean abbreviated)
	{
		printingEnabled = enabled;
		printingAbbreviated = abbreviated;
	}
/**
 * Insert the method's description here.
 * Creation date: (4/5/00 11:44:58 AM)
 * @param newPrintStream java.io.PrintStream
 */
public static void setPrintStream(java.io.PrintStream newPrintStream) {
	printStream = newPrintStream;
}
	/* This method will output all instantiated threads that belong to
	the running thread's thread group that are running. Interestingly,
	the activeCount method returns the number of threads that have not yet
	run to completion including those that haven't been started yet. */
	
	// use debug methods instead of system.out
	
	static public void threadDump() {
		Thread[] threads=new Thread[Thread.activeCount()];
		System.out.println("thread dump: "+
			Thread.currentThread()+" active count is "+
			Thread.enumerate(threads));
		
		for (int i=0;i<threads.length;i++) {
			// enumerate() returned me a null thread once
			if (threads[i]==null) {
				System.out.println("*** NULL THREAD ***");
				continue;
			}
				
			System.out.print(threads[i]);
			System.out.println(
				threads[i].isAlive() ? "" : " ***not started yet***");
		}
	}
}
