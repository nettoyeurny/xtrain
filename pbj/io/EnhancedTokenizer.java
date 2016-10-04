package pbj.io;

import java.io.*;

/**
An extension of StreamTokenizer that scans for Strings and integers.
@author Peter Brinkmann
 */
public class EnhancedTokenizer extends StreamTokenizer {

	private static char dummy='_'; // represents the empty string.

	/**
Constructs an EnhancedTokenizer that reads from r.
@param Reader r: data source
	 */
	public EnhancedTokenizer(Reader r) {
		super(r);
		slashSlashComments(true);
		wordChars(65, 127);
	}

	/**
@return the char that stands for the empty string.
	 */
	public static char dummy() {
		return dummy;
	}

	/**
Sets the char that stands for the empty string.
@param char c: new dummy character
	 */
	public static void setDummy(char c) {
		dummy=c;
	}

	/**
Scans for the next String in the reader. All other input is
ignored, except for the "dummy" character, which causes nextString to return
"". The return value is null at EOF.
@return next String in the reader or null at EOF
	 */
	public String nextString() throws IOException{
		int t;
		do 
			t=nextToken();
		while ((t!=(int) dummy) && (t!=TT_EOF) && (t!=TT_WORD));

		if (t==TT_EOF)
			return null;
		else if (t==(int) dummy)
			return "";
		else
			return sval;
	}

	/**
Scans for the next int in the reader. All other input is ignored,
except for EOF, which results in the return value -1.
@return next integer in the reader or -1 at EOF
	 */
	public int nextInt() throws IOException{
		int t;
		do
			t=nextToken();
		while ((t!=TT_EOF) && (t!=TT_NUMBER));

		if (t==TT_EOF)
			return -1;
		else
			return (int) nval;
	}

	/**
Scans for the next double in the reader. All other input is ignored,
except for EOF, which results in the return value NaN.
@return next integer in the reader or NaN at EOF
	 */
	public double nextDouble() throws IOException{
		int t;
		do
			t=nextToken();
		while ((t!=TT_EOF) && (t!=TT_NUMBER));

		if (t==TT_EOF)
			return Double.NaN;
		else
			return nval;
	}

	/**
main routine, for testing purposes only.
	 */
	public static void main(String s[]) {
		EnhancedTokenizer et=new EnhancedTokenizer(
				new InputStreamReader(System.in));

		try{
			System.out.println("Waiting for integer...");
			System.out.println("Integer read: "+et.nextInt());
			System.out.println("Waiting for String...");
			System.out.println("String read: "+et.nextString());
		} catch (IOException e) {}
	}

}
