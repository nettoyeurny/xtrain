package pbj.io;

import java.io.*;

/**
An abstract class that implements basic FileIO.
@author Peter Brinkmann
 */
public abstract class FileIO {

	/**
Reads the contents of the file f to a String, reads from stdin if f=="".
Only recommended for small files!
@param String f: file name
	 */
	public static String fileToString(String f) {
		StringBuffer b=new StringBuffer();
		FileReader fr;
		int i=0;

		try {
			if (f.length()>0) {
				fr=new FileReader(f);

				while (i!=-1) {
					i=fr.read();

					if (i!=-1)
						b.append((char) i);
				}
			}
			else {
				while (i!=-1) {
					i=System.in.read();

					if (i!=-1)
						b.append((char) i);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("can't open file: "+f);
		}

		return b.toString();
	}

}

