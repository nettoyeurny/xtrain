package pbj.math.manifold;

import java.util.*;
import java.io.*;
import pbj.io.*;

/**
<p>
This class handles simplicial 3-complexes.
At this point, it only reads triangulations of 3-manifolds and prints them in
a form that's suitable for
<a href="http://thames.northnet.org/weeks/">SnapPea,</a> but it has been
designed in such a way that it can easily be extended to classes that perform
more sophisticated operations on complexes.
The input format consists of a sequence of tetrahedra and gluings of the
following form: </p>
<p>T a b c d </p>
<p>where a, b, c, and d a are strings denoting the vertices of the tetrahedron.
Two tetrahedra are glued if they have a face in common (i.e., three of their
four vertex labels coincide) or if the input contains a line of the form</p>
<p>G a1 a2 a3 b1 b2 b3</p>
<p>where a1, a2, a3 are strings denoting the vertices of one triangle, and
b1, b2, b3 are strings denoting the vertices of another triangle. In this
case, the vertex a1 gets glued to b1, a2 to b2, etc.
For example, the figure-eight knot complement triangulated with two ideal
tetrahedra (see Thurston's notes, Chapter 1) can be expressed as follows. </p>

<pre>
T a b c d
T b c d e
G b e d a c d
G c b e a b d
G c e d a c b
</pre>

<p>Finally, input files can contain one-line comments beginning with '//'. </p>

<p>Alternatively, a Java program can build a triangulation by calling the
constructor ThreeComplex and the methods readFromFile, readTriangulation,
addTetrahedron, addGluing and toSnapPea.</p>
@author Peter Brinkmann
 */
public class ThreeComplex {

	/** just the String "T", for tetrahedron */
	public static final String TETRAHEDRON="T";

	/** just the String "S", for gluing */
	public static final String GLUING="G";

	Vector<Tetrahedron> tetrahedra;
	Hashtable<Triangle, GluingData> gluings;
	String name = null;

	// a class for easy handling of triangles
	class Triangle {
		final String v[];	// the labels of the vertices
		final int hc;		// hash code
		final String norm;	// normed representative of the triangle,
		// just the sum of the labels in alpha order

		Triangle(String v1,String v2,String v3) {
			v=new String[]{v1,v2,v3};

			// sort the labels
			if ((v1.compareTo(v2)<0) && (v2.compareTo(v3)<0))
				norm=v1+v2+v3;
			else if ((v1.compareTo(v3)<0) && (v3.compareTo(v2)<0))
				norm=v1+v3+v2;
			else if ((v2.compareTo(v1)<0) && (v1.compareTo(v3)<0))
				norm=v2+v1+v3;
			else if ((v2.compareTo(v3)<0) && (v3.compareTo(v1)<0))
				norm=v2+v3+v1;
			else if ((v3.compareTo(v1)<0) && (v1.compareTo(v2)<0))
				norm=v3+v1+v2;
			else if ((v3.compareTo(v2)<0) && (v2.compareTo(v1)<0))
				norm=v3+v2+v1;
			else
				norm="";

			hc=norm.hashCode(); // fix the hash code
		}

		public int hashCode() {
			return hc;
		}

		public boolean equals(Object o) {
			if (o instanceof Triangle)
				return norm.equals(((Triangle) o).norm);
			else
				return false;
		}

		public String toString() {
			String res="// triangle ";
			int i;
			for(i=0;i<3;i++)
				res=res+v[i]+" ";
			return res;

		}
	}

	// a class for easy handling of gluings
	class GluingData {
		int tet1,face1;		// glue face face1 of tetrahedron tet1 to...
		int tet2,face2;		// face face2 of tetrahedron tet2.
		Triangle tr1;		// face1 of tet1
		Triangle tr2;		// face2 of tet2

		// construct GluingData, given one face of a tetrahedron involved
		GluingData(int tet,int face) {
			tet1=tet;
			face1=face;
			tet2=-1;
			tr1=null;
			tr2=null;
		}

		// construct GluingData, given two triangles involved
		GluingData(Triangle t1,Triangle t2) {
			tet1=-1;
			tet2=-1;
			tr1=t1;
			tr2=t2;
		}

		public String toString() {
			return "// "+tet1+" "+face1+" "+tet2+" "+face2+" "+tr1+" "+tr2;
		}
	}

	// a class that handles tetrahedra
	class Tetrahedron {
		String v[];		// labels of the vertices
		Triangle faces[];	// the four faces

		// constructs a tetrahedron, given the labels of its vertices
		Tetrahedron(String v1,String v2,String v3,String v4) {
			v=new String[]{v1,v2,v3,v4};
			faces=new Triangle[4];

			// init faces; note that faces[i] leaves out v_i
			faces[0]=new Triangle(v2,v3,v4);
			faces[1]=new Triangle(v1,v3,v4);
			faces[2]=new Triangle(v1,v2,v4);
			faces[3]=new Triangle(v1,v2,v3);
		}

		public String toString() {
			String res="// tetrahedron ";
			int i;
			for(i=0;i<4;i++)
				res=res+v[i]+" ";
			return res;
		}
	}

	/**
Constructs an object of type ThreeComplex.
	 */
	public ThreeComplex() {
		tetrahedra=new Vector<Tetrahedron>();
		gluings=new Hashtable<Triangle, GluingData>(2000);
		name="no_name";
	}

	/**
Allows the user to set the name of the manifold (default is "NoName").
@param String s: new name
	 */
	public void setLabel(String s) {
		name=s;
	}

	/**
Returns the current label
@return current label
	 */
	public String getLabel() {
		return name;
	}

	/**
Reads triangulation data from the file fn. If fn equals "", then the method
reads from stdin.
@String fn: file name
	 */
	public void readFromFile(String fn) throws FileNotFoundException {
		if (!fn.equals("")) {
			name=fn;
			readTriangulation(new FileReader(fn));
		}
		else
			readTriangulation(new InputStreamReader(System.in));
	}

	/* adds a new triangle to the list maintained in the HashTable "gluings"
   parameters are a tetrahedron, its index in the list of tetrahedra, and
   the face to be added to the list */
	private void addTriangle(Tetrahedron tet,int i0,int face) {
		GluingData gl;

		// is tet.faces[face] already in the HashTable?
		if (gluings.containsKey(tet.faces[face])) { // yes
			gl=(GluingData) gluings.get(tet.faces[face]);

			// record where this triangle can be found
			if (gl.tet1<0) { // first occurence
				gl.tet1=i0;
				gl.face1=face;
			}
			else {		// second occurence
				gl.tet2=i0;
				gl.face2=face;
			}
		}
		else	// add tet.faces[face] to the list of faces to be glued
			gluings.put(tet.faces[face],new GluingData(i0,face));
	}

	/**
Adds a tetrahedron to the triangulation. The four vertices have to be distinct.
@param String s1,s2,s3,s4: vertices of the tetrahedron.
	 */
	public void addTetrahedron(String s1,String s2,String s3,String s4) {
		Tetrahedron tet=new Tetrahedron(s1,s2,s3,s4);
		int face,i0;

		i0=tetrahedra.size();
		tetrahedra.addElement(tet);

		for(face=0;face<4;face++)
			addTriangle(tet,i0,face);
	}

	// adds a new gluing to the list maintained in the HashTable "gluings"
	void addGluing(Triangle t1,Triangle t2) {
		GluingData gl;

		// is t1 already in the HashTable?
		if (gluings.containsKey(t1)) { // yes
			gl=gluings.get(t1);
			gl.tr1=t1;	// t1 and t2 are glued
			gl.tr2=t2;
		}
		else // create a new gluing for t1 and t2
			gluings.put(t1,new GluingData(t1,t2));

		// symmetric thing for t2
		if (gluings.containsKey(t2)) {
			gl=gluings.get(t2);
			gl.tr1=t2;
			gl.tr2=t1;
		}
		else
			gluings.put(t2,new GluingData(t2,t1));
	}

	/**
Glues the two triangles with vertices a1,a2,a3 and b1,b2,b3 together; a1 gets
glued to b1, a2 to b2, etc.
@param String a1,a2,a3: vertices of first triangle
@param String b1,b2,b3: vertices of second triangle
	 */
	public void addGluing(String a1,String a2,String a3,
			String b1,String b2,String b3) {
		Triangle t1=new Triangle(a1,a2,a3);
		Triangle t2=new Triangle(b1,b2,b3);

		addGluing(t1,t2);
	}

	/**
Reads triangulation data.
@param Reader rr: source of triangulation data
	 */
	public void readTriangulation(Reader rr) {
		String s;
		EnhancedTokenizer st=new EnhancedTokenizer(rr);

		try{
			while ((s=st.nextString())!=null) {
				if (s.equals(TETRAHEDRON))
					addTetrahedron(st.nextString(),
							st.nextString(),st.nextString(),st.nextString());
				else if (s.equals(GLUING)) {
					addGluing(new Triangle(
							st.nextString(),st.nextString(),st.nextString()),
							new Triangle(
									st.nextString(),st.nextString(),st.nextString()));
				}
				else
					throw new RuntimeException(s+": wrong key word");
			}
		}catch(IOException e) { throw new RuntimeException(e.toString()); }
	}

	/*
This functions finds the neighbor of a face of a tetrahedron. The array
perm[] is a permutation of {0,1,2,3} that indicates how the two neighbors
are glued, following SnapPea's gluing convention.
	 */
	int findNeighbor(int tet,int face,int perm[]) {
		Tetrahedron t1=(Tetrahedron) tetrahedra.elementAt(tet);
		// retrieve tetrahedron tet
		Tetrahedron t2;
		GluingData gl=(GluingData) gluings.get(t1.faces[face]);
		// retrieve gluing data for the face
		GluingData gl2;
		int i;
		int r,s;
		int nb,nf;

		if (gl==null)		// no gluing data?
			return -1;	// input is bad

		// does the gluing data already contain the right neighbor of tet?
		if (gl.tet2>=0) { // yes
			if ((gl.tet2==tet) && (gl.face2==face)) {
				nb=gl.tet1;
				nf=gl.face1;
			}
			else {
				nb=gl.tet2;
				nf=gl.face2;
			}
			t2=(Tetrahedron) tetrahedra.elementAt(nb);
			// retrieve the neighbor

			perm[face]=nf;	 // now compute the permutation for SnapPea
			for(r=0;r<4;r++) // such that the labels match up
				if (r!=face)
					for(s=0;s<4;s++)
						if ((s!=nf) &&
								(t1.v[r].equals(t2.v[s])))
							perm[r]=s;

		}
		else { // neighboring tetrahedron is not yet known
			if (gl.tr2==null) // no neighboring triangle?
				throw new RuntimeException(t1+" has no neighbor");

			gl2=(GluingData) gluings.get(gl.tr2);
			// get gluing data for neighboring triangle

			nb=gl2.tet1;
			nf=gl2.face1;
			t2=(Tetrahedron) tetrahedra.elementAt(nb);
			// now we've found the neighboring tetrahedron

			perm[face]=nf; // as above, make the labels match
			for(i=0;i<3;i++) {
				for(r=0;!t1.v[r].equals(gl.tr1.v[i]);r++);
				for(s=0;!t2.v[s].equals(gl.tr2.v[i]);s++);
				perm[r]=s;
			}
		}

		return nb;
	}

	/**
Writes input for SnapPea to System.out.
	 */
	public void toSnapPea() {
		toSnapPea(System.out);
	}

	/**
Writes input for SnapPea to the object o, which has to be suitable for
GenericPrint.
@param: Object o: object suitable for GenericPrint
@see pbj.io.GenericPrint
	 */
	public void toSnapPea(Object o) {
		int i,j;
		int gl[]=new int[4];
		int ti;
		String tet,perm;
		GenericPrint gp=new GenericPrint(o);

		gp.println("% Triangulation\n");	// required first line
		gp.println((name!=null && name.length()>0) ? name : "no_name");

		gp.println("no_solution 0.0\nunknown_orientability\n"+
				"CS_unknown\n\n0 0\n\n"+tetrahedra.size()+"\n");
		// dummy entries, SnapPea will figure it out...

		// now, print tetrahedra in SnapPea's format
		for(i=0;i<tetrahedra.size();i++) {
			tet="";
			perm="";
			for(j=0;j<4;j++) {
				if ((ti=findNeighbor(i,j,gl))<0)
					throw new RuntimeException("tetrahedron "+i+
							", face "+j+" has no neighbor");

				tet=tet+ti+" ";
				perm=perm+gl[0]+gl[1]+gl[2]+gl[3]+" ";
			}

			gp.println(tet+"\n"+perm+"\n"+"-1 -1 -1 -1\n"+
					"0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"+
					"0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"+
					"0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"+
					"0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"+
			"0.0 0.0\n");
		}
	}

	/**
If no parameter is given, main reads from System.in. If the program is called
with one parameter, this parameter is assumed to be a file name, and main will
try to read its input from this file.
The output goes to System.out in both cases.
	 */
	public static void main(String s[]) {
		ThreeComplex c=new ThreeComplex();

		try{
			if (s.length>0) {
				c.readFromFile(s[0]);
				if (s[0].length()>0)
					c.setLabel(s[0]);
			}
			else
				c.readFromFile("");
		} catch (FileNotFoundException e) {System.err.println(e.toString());
		return; }

		c.toSnapPea();
	}

}
