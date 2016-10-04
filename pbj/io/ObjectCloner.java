package pbj.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
Quick'n dirty implementation of deep clones, from 
<a href="http://www.javaworld.com/">JavaWorld:</a>
<a href="http://www.javaworld.com/javatips/jw-javatip76.html?080999txt">
Java Tip 76.</a>
 */
public abstract class ObjectCloner
{

	private ObjectCloner(){}

	// returns a deep copy of an object
	static public Object deepCopy(Object oldObj) throws IOException, ClassNotFoundException
	{

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		try
		{

			ByteArrayOutputStream bos = 
				new ByteArrayOutputStream(); // A
			oos = new ObjectOutputStream(bos); // B

			// serialize and pass the object
			oos.writeObject(oldObj); // C
			oos.flush(); // D

			ByteArrayInputStream bin = 
				new ByteArrayInputStream(bos.toByteArray()); // E
			ois = new ObjectInputStream(bin); // F

			// return the new object
			return ois.readObject(); // G

		} finally
		{
			oos.close();
			ois.close();
		}

	}

}
