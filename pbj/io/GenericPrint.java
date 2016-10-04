package pbj.io;

import java.io.*;

/**
A class that allows to print to PrintWriter, PrintStream, OutputStreamWriter,
FileWriter, and StringWriter in a uniform fashion.
@author Peter Brinkmann
@see java.io.FileWriter
@see java.io.PrintWriter
@see java.io.PrintStream
@see java.io.StringWriter
@see java.io.OutputStreamWriter
 */
public class GenericPrint {

	private Object pr;

	/**
	By default, GenericPrint prints to System.out.
	 */
	public GenericPrint() {
		this(System.out);
	}

	/**
	This constructor returns an object of type GenericPrint that writes
	to the object o.
	@param Object o: output device
	 */
	public GenericPrint(Object o) {
		if ((!(o instanceof PrintWriter)) &&
				(!(o instanceof StringWriter)) &&
				(!(o instanceof FileWriter)) &&
				(!(o instanceof OutputStreamWriter)) &&
				(!(o instanceof PrintStream)))
			throw new RuntimeException(
					"wrong argument in constructor");
		pr=o;
	}

	/**
	The usual println method.
	@Object o: object to be printed
	 */
	public void println(Object o) {
		if (pr instanceof PrintWriter)
			((PrintWriter) pr).println(o);
		else if (pr instanceof StringWriter)
			((StringWriter) pr).write(o+"\n");
		else if (pr instanceof FileWriter)
			try {
				((FileWriter) pr).write(o+"\n");
			} catch (IOException e) {
				throw new RuntimeException(e.toString());
			}
			else if (pr instanceof PrintStream)
				((PrintStream) pr).println(o);
			else if (pr instanceof OutputStreamWriter)
				try {
					((OutputStreamWriter) pr).write(o+"\n");
				} catch (IOException e) {
					throw new RuntimeException(e.toString());
				}
	}
}
