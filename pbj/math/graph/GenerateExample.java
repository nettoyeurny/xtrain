package pbj.math.graph;


/**
A little utility class that prints a sequence of Dehn twists giving rise
to a pseudo-Anosov homeomorphism of a surface of genus g, where g given
by the command line parameter (default is g=2).
@author Peter Brinkmann
@version $Id: GenerateExample.java,v 1.1 2009/04/27 00:41:17 brinkman Exp $
@see DehnTwist
 */
public class GenerateExample {

	public static void main(String args[]) {
		int n,i;

		if (args.length>0)
			n=Integer.parseInt(args[0]);
		else 
			n=2;

		System.out.print(n+" ");
		for(i=0;i<n;i++)
			System.out.print("C"+i+"d"+i);

		System.out.println("");
	}

}
