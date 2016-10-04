package pbj.math.graph.train;

import pbj.math.graph.GraphMap;
import pbj.math.graph.Word;
import pbj.math.numerical.IntMatrix;

/**
A little utility class that reads a TrainTrack (or GraphMap), computes its
transition matrix, and the induced map in homology, as well as a rose and its
induced map in homology.
@author Peter Brinkmann
@see TrainTrack
@see GraphMap
 */
public class Matrices {

	public static void main (String s[]) {
		TrainTrack g=new TrainTrack();
		IntMatrix a;
		int i;

		System.out.println("// $Id: Matrices.java,v 1.1 2009/04/27 00:41:17 brinkman Exp $");
		System.out.println("");

		try{
			if (s.length>0)
				g.readFromFile(s[0]);
			else
				g.readFromFile("");
		} catch(Exception e) {System.err.println(e.toString());
		return; }

		System.out.println("// --------- original graph ---------");
		System.out.println(g.toString());
		System.out.println("// ----------- stars of vertices -----------");
		for(i=0;i<g.getVertices();i++)
			System.out.println("star of vertex "+i+": "
					+Word.pathToString(g.starOfVertex(i)));


		System.out.println("// ----- transition matrix -----");
		a=g.transitionMatrix();
		System.out.println(a.toString());
		System.out.println("// Maple");
		System.out.println(a.toMaple());
		System.out.println("// Mathematica");
		System.out.println(a.toMathematica());
		System.out.println("// MATLAB");
		System.out.println(a.toMATLAB());

		System.out.println("// ----- homology -----");
		a=g.abelianized();
		System.out.println(a.toString());
		System.out.println("// Maple");
		System.out.println(a.toMaple());
		System.out.println("// Mathematica");
		System.out.println(a.toMathematica());
		System.out.println("// MATLAB");
		System.out.println(a.toMATLAB());

		System.out.println("// --------- rose ---------");
		g.rose();
		System.out.println(g.toString());

		System.out.println("// ----- homology -----");
		a=g.abelianized();
		System.out.println(a.toString());
		System.out.println("// Maple");
		System.out.println(a.toMaple());
		System.out.println("// Mathematica");
		System.out.println(a.toMathematica());
		System.out.println("// MATLAB");
		System.out.println(a.toMATLAB());
	}

}
