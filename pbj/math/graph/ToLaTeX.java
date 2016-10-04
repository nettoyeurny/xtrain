package pbj.math.graph;


/**
An abstract class the reads a GraphMap and prints it in LaTeX format.
@author Peter Brinkmann
@version $Id: ToLaTeX.java,v 1.1 2009/04/27 00:41:17 brinkman Exp $
 */
public abstract class ToLaTeX {

	public static void main (String s[]) {
		GraphMap g=new GraphMap();

		try{
			if (s.length>0)
				g.readFromFile(s[0]);
			else
				g.readFromFile("");

			if (!g.isGoodMap())
				throw new RuntimeException("bad map");

		} catch(Exception e) {System.err.println(e.toString());
		return; }

		System.out.println(g.toLaTeX());
	}

}
