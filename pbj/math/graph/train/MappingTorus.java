package pbj.math.graph.train;

import gnu.getopt.Getopt;

import java.util.Enumeration;
import java.util.Vector;

import pbj.io.GenericPrint;
import pbj.math.graph.Word;
import pbj.math.manifold.ThreeComplex;

/**
The class takes a train track map representing an homeomorphism of a
punctured surface and computes a triangulation of the corresponding
mapping torus. The output format is suitable for the class ThreeComplex.
@author Peter Brinkmann
@see TrainTrack
@see pbj.math.manifold.ThreeComplex
 */
public class MappingTorus {

	private Vector<Layer> layers;		// collects the layers obtained by subdividing
	// and folding
	private String label;
	private GenericPrint gp;	// output device
	private ThreeComplex cx;	// alternative output device
	private boolean VERBOSE;	// verbose flag
	int splt;			// marker attached to edges arising from
	// subdivision
	// Note: This variable is supposed to be
	// private, but somehow netscape and
	// appletviewer choke on it...

	private String inverse(String s) {
		return Word.inverse(s.charAt(0))+s.substring(1);
	}

	private String toLabel(String s) {
		return Word.charToLabel(s.charAt(0))+s.substring(1);
	}

	// some convenient routines for computations mod n
	private int cyclPrev(int i,int n) {
		return ((i-1+n) % n);
	}

	private int cyclNext(int i,int n) {
		return ((i+1) % n);
	}

	private boolean cyclLeq(int i,int j,int n,int i0) {
		if (i<i0)
			i+=n;
		if (j<i0)
			j+=n;
		return (i<=j);
	}

	// a class that manages layers
	private class Layer {

		final int BASE=0;	// a layer can have three types: base type,
		final int FOLD=1;	// obtained by folding,
		final int SUBD=2;	// or obtained by subdivision

		int n;			// the number of the layer
		int type;		// its type
		int f0,F0;		// index of the folded edge and its inverse
		boolean subd0,subd1;	// index of the subdivided edge(s)
		Vector<String> edges;		// collects labels of edges
		Vector<String> images;		// collects images of edges
		Vector<String> vertices;	// collects labels of initial vertices of edges

		// construct empty layer
		Layer(int nn) {
			n=nn;
			edges=new Vector<String>();
			images=new Vector<String>();
			vertices=new Vector<String>();
			subd0=false;
			subd1=false;
		}

		// construct base layers
		Layer(TrainTrack t) {
			this(0);

			int i,e;
			char c;

			if (t.getFix().equals(""))
				throw new RuntimeException("empty fixed word");

			type=BASE;

			for(i=0;i<t.getFix().length();i++) { // add an edge for each letter of
				// the fixed word
				c=t.getFix().charAt(i);
				e=Word.charToIndex(c);
				if (!Word.isInverse(c)) {
					edges.addElement(""+c);
					images.addElement(t.getIm(e));
				}
				else {
					edges.addElement(""+c);
					images.addElement(Word.reverseWord(t.getIm(e)));
				}
			}

			// assign standard labels for vertices
			stdLabels();

			n=0;
		}

		// computes standard labels for vertices, of the form v.n.m.k, where n is
		// the number of the layers, m is the number of the vertex after gluing
		// and k distinguishes vertices that differ before gluing.
		private void stdLabels() {
			int i,j,cnt,cnt1;
			String lab;

			cnt=0;	// counter for labels

			vertices.removeAllElements();
			for(i=0;i<edges.size();i++)
				vertices.addElement(""); // start with dummy labels

			for(i=0;i<edges.size();i++)
				// has the initial vertex of edge i been taken care of?
				if (((String) vertices.elementAt(i)).equals("")) { // no
					lab="v."+n+"."+(cnt++);	// assign label
					cnt1=0;
					j=i;
					do {	// find all other vertices that get glued to
						// the current one
						j=(edges.indexOf(
								inverse((String) edges.elementAt(j)))+1)
								% edges.size();
						vertices.setElementAt(lab+"-"+(cnt1++),j);
					} while (i!=j);
					// note: i gets changed inside the do/while loop, but
					// now it's back to its original value!
				}
		}

		// goes through the fixed word looking for cancellation in the image
		int findFold() {
			int i;
			String s;

			for(i=0;i<edges.size();i++) {
				s=((String) images.elementAt(i))+
				((String) images.elementAt(cyclNext(i,edges.size())));
				if (Word.tightenWord(s).length()<s.length())
					return i;	// cancellation occurs, so we can
				// subdivide and fold edges i, i+1
			}

			return -1;	// no cancellation
		}

		// creates the next layer, either by subdivision or folding
		Layer nextLayer() {
			int it,d,n0;
			String f1,f2,s,s1,s2;
			Layer l;
			int i;
			boolean flag;		// flag indicating folding layer

			l=new Layer(n+1);	// construct new layer
			n0=edges.size();	// number of edges
			flag=true;		// init flag

			// did 'this' arise from subdivision?
			if (type!=SUBD) {	// no
				it=findFold();	// find two edges to fold
				l.F0=it;	// mark the edge to be folded

				if (it<0)	// folding impossible? that's trouble...
					throw new RuntimeException("no next layer");

				// find the images of the edges to be folded...
				s1=(String) images.elementAt(it);
				s2=(String) images.elementAt((it+1) % edges.size());
				// ... and concatenate them.
				s=s1+s2;

				// does s cancel completely?
				if (Word.tightenWord(s).length()>0) { // no; have to subdivide
					flag=false;	// hence, this is no folding layer
					l.type=SUBD;	// it's a subdivision layer

					// find the number of letters cancelled
					d=(s.length()-Word.tightenWord(s).length())/2;

					for(i=0;i<n0;i++) {
						if (i==it) {
							// do we need to subdivide edge it?
							if (s1.length()==d) {	// no
								l.f0=l.edges.size();
								l.edges.addElement(
										(String) edges.elementAt(i));
								l.images.addElement(
										(String) images.elementAt(i));
							}
							else { // yes
								l.edges.addElement(
										(String) edges.elementAt(i));
								l.f0=l.edges.size();
								l.edges.addElement(
										((String) edges.elementAt(i))
										+"."+splt);
								l.images.addElement(
										s1.substring(0,s1.length()-d));
								l.images.addElement(
										s1.substring(s1.length()-d));
								l.subd0=true;
							}
						}
						else if (i==cyclNext(it,n0)) {
							// do we need to subdivide edge it+1?
							if (s2.length()==d) { // no
								l.edges.addElement(
										(String) edges.elementAt(i));
								l.images.addElement(
										(String) images.elementAt(i));
							}
							else { // yes
								l.edges.addElement(
										((String) edges.elementAt(i))
										+"."+splt);
								l.edges.addElement(
										(String) edges.elementAt(i));
								l.images.addElement(
										s2.substring(0,d));
								l.images.addElement(
										s2.substring(d));
								l.subd1=true;
							}
						}
						// same thing for the inverses of edges
						// it and it+1
						else if (i==edges.indexOf(inverse((String)
								edges.elementAt(it)))) {
							// subdivision needed?
							if (s1.length()==d) { // no
								l.edges.addElement(
										(String) edges.elementAt(i));
								l.images.addElement(
										(String) images.elementAt(i));
							}
							else { // yes
								l.edges.addElement(
										((String) edges.elementAt(i))
										+"."+splt);
								l.edges.addElement(
										(String) edges.elementAt(i));
								l.images.addElement(
										Word.reverseWord(s1).
										substring(0,d));
								l.images.addElement(
										Word.reverseWord(s1).
										substring(d));
								l.subd0=true;
							}
						}
						else if (i==edges.indexOf(inverse((String)
								edges.elementAt(
										cyclNext(it,n0))))) {
							// subdivision needed?
							if (s2.length()==d) { // no
								l.edges.addElement(
										(String) edges.elementAt(i));
								l.images.addElement(
										(String) images.elementAt(i));
							}
							else { // yes
								l.edges.addElement(
										(String) edges.elementAt(i));
								l.edges.addElement(
										((String) edges.elementAt(i))
										+"."+splt);
								l.images.addElement(
										Word.reverseWord(s2).
										substring(0,s2.length()-d));
								l.images.addElement(
										Word.reverseWord(s2).
										substring(s2.length()-d));
								l.subd1=true;
							}

						}
						else {	// edge i is nothing special,
							// just copy it
							l.edges.addElement(
									(String) edges.elementAt(i));
							l.images.addElement(
									(String) images.elementAt(i));
						}
					}
					splt++;		// update marker for subdivided edges
				}
				else {
					f0=it;		// mark edge to be folded
				}
			}

			if (flag) {	// flag==true means folding
				l.type=FOLD;	// record the type of the operation
				f1=inverse((String) edges.elementAt(f0));	// and fold...
				f2=(String) edges.elementAt(cyclNext(f0,n0));
				for(i=cyclNext(f0+1,n0);i!=f0;i=cyclNext(i,n0)) {
					s=(String) edges.elementAt(i);
					if (s.equals(f1))
						l.edges.addElement(f2);
					else if (s.equals(inverse(f1)))
						l.edges.addElement(inverse(f2));
					else
						l.edges.addElement(s);
					l.images.addElement((String) images.elementAt(i));
				}
			}

			// assign standard labels to vertices
			l.stdLabels();

			return l;
		}

		public String toString() {
			int i;
			String res="// "+type+" "+f0+" "+F0+" "+subd0+" "+" "+subd1+"\n";

			for(i=0;i<edges.size();i++)
				res=res+"// "+toLabel((String) edges.elementAt(i))+" "+
				Word.pathToString((String) images.elementAt(i))+" "+
				((String) vertices.elementAt(i))+"\n";

			res=res+"// size: "+size();

			return res+"\n";
		}

		// computes the sum of the lengths of the images of edges
		int size() {
			int s;
			int i;

			for(i=0,s=0;i<edges.size();i++)
				s+=((String) images.elementAt(i)).length();

			return s;
		}

	}

	/**
Constructor
@param TrainTrack t: train track whose mapping torus will be computed
	 */
	public MappingTorus(TrainTrack t) {
		Layer l;

		layers=new Vector<Layer>();

		l=new Layer(t);		// construct the base layer
		setLabel(t.getLabel());
		VERBOSE=false;
		splt=0;

		layers.addElement(l);

		// fold until we reach an immersion
		while (l.findFold()>=0) {
			l=l.nextLayer();
			layers.addElement(l);
		}

		if (layers.size()<2)
			throw new RuntimeException("need at least two layers");
	}

	/**
sets the label of this
@param String lab: new label
	 */
	public void setLabel(String lab) {
		label=lab;
	}

	/**
returns the label of this
@return the current label
	 */
	public String getLabel() {
		return label;
	}

	/**
Determines whether the output is supposed to contains comments explaining
the triangulation. Default is false.
@param boolean vb: indicates whether verbose mode is desired
	 */
	public void setVerbose(boolean vb) {
		VERBOSE=vb;
	}

	// adds two tetrahedra glued along on common face
	private void addTuple(Object s1,Object s2,Object s3,Object s4,Object s5,
			Object s6) {
		if (gp!=null) {
			gp.println(ThreeComplex.TETRAHEDRON+" p "+((String) s1)
					+" "+((String) s2)+" "+((String) s3));
			gp.println(ThreeComplex.TETRAHEDRON+" p "+((String) s4)
					+" "+((String) s5)+" "+((String) s6));
			gp.println(ThreeComplex.GLUING+" "+((String) s1)
					+" "+((String) s2)
					+" "+((String) s3)+" "+((String) s4)
					+" "+((String) s5)+" "+((String) s6));
		}
		else if (cx!=null) {
			cx.addTetrahedron("p",((String) s1),((String) s2),
					((String) s3));
			cx.addTetrahedron("p",((String) s4),((String) s5),
					((String) s6));
			cx.addGluing(((String) s1),((String) s2),
					((String) s3),((String) s4),
					((String) s5),((String) s6));

		}
	}

	// adds a gluing to the list
	private void addGluing(Object s1,Object s2,Object s3,Object s4) {
		if (gp!=null)
			gp.println(ThreeComplex.GLUING+" p "+((String) s1)
					+" "+((String) s2)+
					" p "+((String) s3) +" "+((String) s4));
		else if (cx!=null)
			cx.addGluing("p",((String) s1),((String) s2),
					"p",((String) s3),((String) s4));

	}

	/**
Prints a triangulation in a format that's suitable for ThreeComplex.
The output goes to stdout.
@see ThreeComplex
	 */
	public void triangulate() {
		triangulate(System.out);
	}

	/**
Prints a triangulation in a format that's suitable for ThreeComplex.
@param Object pp: object of Writer class or of type ThreeComplex. Output goes to pp
@see ThreeComplex
	 */
	public void triangulate(Object pp) {
		Enumeration<Layer> elts;
		Layer base,l0,l1;
		int i,i0,k0,i1,k1,r0,r1,R0,R1,n0,n1;
		char c;

		if (pp instanceof ThreeComplex) {
			cx=(ThreeComplex) pp;
			cx.setLabel(getLabel());
			gp=null;
		}
		else {
			cx=null;
			gp=new GenericPrint(pp);
		}

		elts=layers.elements();
		base=(Layer) elts.nextElement();
		l1=base;
		
		if ((VERBOSE) && (gp!=null))
			gp.println("\n"+l1.toString());

		// triangulate between successive layers
		while (elts.hasMoreElements()) {
			l0=l1;
			l1=(Layer) elts.nextElement();
			n0=l0.edges.size();
			n1=l1.edges.size();

			if (l1.type==l1.FOLD) {		// handle folding layers
				i0=l0.f0;
				i1=0;
				k0=cyclNext(l0.edges.indexOf(inverse((String)
						l0.edges.elementAt(i0))),n0);

				k1=cyclNext(l1.edges.indexOf((String)
						l0.edges.elementAt(cyclNext(i0,n0))),n1);

				if ((VERBOSE) && (gp!=null))
					gp.println("// collapse edge at "+i0);

				addTuple(l0.vertices.elementAt(i0),
						l0.vertices.elementAt(cyclNext(i0,n0)),
						l1.vertices.elementAt(i1),
						l0.vertices.elementAt(k0),
						l0.vertices.elementAt(cyclPrev(k0,n0)),
						l1.vertices.elementAt(k1));
				if ((VERBOSE) && (gp!=null))
					gp.println("// done collapsing edge at "+i0);

				r0=cyclNext(i0,n0);
				R0=cyclNext(l0.edges.indexOf(inverse((String)
						l0.edges.elementAt(r0))),n0);
				R1=cyclNext(l1.edges.indexOf(inverse((String)
						l0.edges.elementAt(r0))),n1);

				if ((VERBOSE) && (gp!=null))
					gp.println("// fold edge at "+(i0+1));

				addTuple(l0.vertices.elementAt(r0),
						l0.vertices.elementAt(cyclNext(r0,n0)),
						l1.vertices.elementAt(i1),
						l0.vertices.elementAt(R0),
						l0.vertices.elementAt(cyclPrev(R0,n0)),
						l1.vertices.elementAt(cyclPrev(R1,n1)));

				addTuple(l0.vertices.elementAt(cyclPrev(k0,n0)),
						l1.vertices.elementAt(k1),
						l1.vertices.elementAt(cyclPrev(k1,n1)),
						l0.vertices.elementAt(R0),
						l1.vertices.elementAt(cyclPrev(R1,n1)),
						l1.vertices.elementAt(R1));
				if ((VERBOSE) && (gp!=null))
					gp.println("// done folding edge at "+(i0+1));

				if ((VERBOSE) && (gp!=null))
					gp.println("// fill in remaining edges");

				for(r0=cyclNext(r0,n0);r0!=i0;r0=cyclNext(r0,n0)) {
					r1=l1.edges.indexOf(
							(String) l0.edges.elementAt(r0));
					R0=cyclNext(l0.edges.indexOf(inverse((String)
							l0.edges.elementAt(r0))),n0);
					R1=cyclNext(l1.edges.indexOf(inverse((String)
							l0.edges.elementAt(r0))),n1);

					if (cyclLeq(r0,cyclPrev(R0,n0),n0,i0)) {
						addTuple(l0.vertices.elementAt(r0),
								l1.vertices.elementAt(r1),
								l1.vertices.elementAt(
										cyclNext(r1,n1)),
										l0.vertices.elementAt(R0),
										l1.vertices.elementAt(R1),
										l1.vertices.elementAt(
												cyclPrev(R1,n1)));

						addTuple(l0.vertices.elementAt(r0),
								l0.vertices.elementAt(
										cyclNext(r0,n0)),
										l1.vertices.elementAt(
												cyclNext(r1,n1)),
												l0.vertices.elementAt(R0),
												l0.vertices.elementAt(
														cyclPrev(R0,n0)),
														l1.vertices.elementAt(
																cyclPrev(R1,n1)));
					}
				}
				if ((VERBOSE) && (gp!=null))
					gp.println(
					"// done filling in remaining edges");
			}
			else if (l1.type==l1.SUBD) {	// handle subdivision layers
				R0=cyclNext(l0.edges.indexOf(inverse((String)
						l0.edges.elementAt(l1.F0))),n0);
				R1=cyclNext(l1.edges.indexOf(inverse((String)
						l1.edges.elementAt(l1.f0))),n1);

				if ((VERBOSE) && (gp!=null))
					gp.println("// fill in edge "+l1.F0);

				addTuple(l0.vertices.elementAt(l1.F0),
						l0.vertices.elementAt(cyclNext(l1.F0,n0)),
						l1.vertices.elementAt(l1.f0),
						l0.vertices.elementAt(R0),
						l0.vertices.elementAt(cyclPrev(R0,n0)),
						l1.vertices.elementAt(R1));

				addTuple(l0.vertices.elementAt(cyclNext(l1.F0,n0)),
						l1.vertices.elementAt(l1.f0),
						l1.vertices.elementAt(cyclNext(l1.f0,n1)),
						l0.vertices.elementAt(cyclPrev(R0,n0)),
						l1.vertices.elementAt(R1),
						l1.vertices.elementAt(cyclPrev(R1,n1)));
				if ((VERBOSE) && (gp!=null))
					gp.println("// done filling in edge "+l1.F0);

				if (l1.subd0) {
					if ((VERBOSE) && (gp!=null))
						gp.println(
								"// fill in remainder of "+l1.F0);

					addTuple(l0.vertices.elementAt(l1.F0),
							l1.vertices.elementAt(
									cyclPrev(l1.f0,n1)),
									l1.vertices.elementAt(l1.f0),
									l0.vertices.elementAt(R0),
									l1.vertices.elementAt(cyclNext(R1,n1)),
									l1.vertices.elementAt(R1));
					if ((VERBOSE) && (gp!=null))
						gp.println("// done filling "+
								"in remainder of "+l1.F0);
				}

				r0=cyclNext(l1.F0,n0);
				R0=cyclNext(l0.edges.indexOf(inverse((String)
						l0.edges.elementAt(r0))),n0);
				r1=cyclNext(l1.f0,n1);
				R1=cyclNext(l1.edges.indexOf(inverse((String)
						l1.edges.elementAt(r1))),n1);

				if ((VERBOSE) && (gp!=null))
					gp.println("// fill in edge "+r0);

				addTuple(l0.vertices.elementAt(r0),
						l0.vertices.elementAt(cyclNext(r0,n0)),
						l1.vertices.elementAt(cyclNext(r1,n1)),
						l0.vertices.elementAt(R0),
						l0.vertices.elementAt(cyclPrev(R0,n0)),
						l1.vertices.elementAt(cyclPrev(R1,n1)));

				addTuple(l0.vertices.elementAt(r0),
						l1.vertices.elementAt(r1),
						l1.vertices.elementAt(cyclNext(r1,n1)),
						l0.vertices.elementAt(R0),
						l1.vertices.elementAt(R1),
						l1.vertices.elementAt(cyclPrev(R1,n1)));
				if ((VERBOSE) && (gp!=null))
					gp.println("// done filling in edge "+r0);

				if (l1.subd1) {
					if ((VERBOSE) && (gp!=null))
						gp.println("// fill in remainder"
								+" of "+r0);
					addTuple(l0.vertices.elementAt(
							cyclNext(r0,n0)),
							l1.vertices.elementAt(
									cyclNext(r1,n1)),
									l1.vertices.elementAt(
											cyclNext(r1+1,n1)),
											l0.vertices.elementAt(
													cyclPrev(R0,n0)),
													l1.vertices.elementAt(
															cyclPrev(R1,n1)),
															l1.vertices.elementAt(
																	cyclPrev(R1-1,n1)));
					if ((VERBOSE) && (gp!=null))
						gp.println("// done filling in "+
								"remainder of "+
								+r0);
				}

				if ((VERBOSE) && (gp!=null))
					gp.println("// fill in remaining edges");

				for(i=cyclNext(l1.F0+1,n0);i!=l1.F0;i=cyclNext(i,n0)) {
					r0=i;
					R0=cyclNext(l0.edges.indexOf(inverse((String)
							l0.edges.elementAt(r0))),n0);
					r1=l1.edges.indexOf((String)
							l0.edges.elementAt(r0));
					R1=cyclNext(l1.edges.indexOf(inverse((String)
							l1.edges.elementAt(r1))),n1);

					if (cyclLeq(r0,cyclPrev(R0,n0),n0,l1.F0)) {
						addTuple(l0.vertices.elementAt(r0),
								l0.vertices.elementAt(cyclNext(r0,n0)),
								l1.vertices.elementAt(r1),
								l0.vertices.elementAt(R0),
								l0.vertices.elementAt(cyclPrev(R0,n0)),
								l1.vertices.elementAt(R1));

						addTuple(l0.vertices.elementAt(
								cyclNext(r0,n0)),
								l1.vertices.elementAt(r1),
								l1.vertices.elementAt(
										cyclNext(r1,n1)),
										l0.vertices.elementAt(
												cyclPrev(R0,n0)),
												l1.vertices.elementAt(R1),
												l1.vertices.elementAt(
														cyclPrev(R1,n1)));
					}
				}
				if ((VERBOSE) && (gp!=null))
					gp.println(
					"// done filling in remaining edges");
			}

			if ((VERBOSE) && (gp!=null))
				gp.println("\n"+l1.toString());
		}

		if ((VERBOSE) && (gp!=null))
			gp.println("// final gluing");
		n0=base.edges.size();
		n1=l1.edges.size();
		for(i=0;l1.images.indexOf(""+Word.indexToChar(i,false))>=0;i++) {
			c=Word.indexToChar(i,false);
			r0=base.edges.indexOf(c+"");
			R0=cyclNext(base.edges.indexOf(Word.inverse(c)+""),n0);
			r1=l1.images.indexOf(c+"");
			R1=cyclNext(l1.images.indexOf(Word.inverse(c)+""),n1);

			addGluing(l1.vertices.elementAt(r1),
					l1.vertices.elementAt(cyclNext(r1,n1)),
					base.vertices.elementAt(r0),
					base.vertices.elementAt(cyclNext(r0,n0)));

			addGluing(l1.vertices.elementAt(R1),
					l1.vertices.elementAt(cyclPrev(R1,n1)),
					base.vertices.elementAt(R0),
					base.vertices.elementAt(cyclPrev(R0,n0)));
		}
		if ((VERBOSE) && (gp!=null)) {
			gp.println("// done with final gluing");
			gp.println("\n"+base.toString());
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Layer layer: layers) {
			sb.append(layer.toString());
		}
		return sb.toString();
	}

	/**
<p> This function takes an object of type GraphMap or TrainTrack and computes
its mapping torus, suitable for pbj.math.manifold.ThreeComplex.</p>

<p>Usage:
<kbd>java pbj.math.graph.train.MappingTorus [-v] [file]</kbd>
</p>

<p>The option -v prompts the program to include some comments illustrating
how the mapping torus was computed. If a file name is given, it tries to
read its input from this file, otherwise it reads from stdin. The output
goes to stdout in both cases. In most cases, the output will be piped into
pbj.math.manifold.ThreeComplex, whose output in turn can be piped into a
command line version of SnapPea. Please send email to brinkman@math.utah.edu
if you're interested in the C-code that makes SnapPea accessible from the
command line.</p>
	 */
	public static void main(String s[]) {
		TrainTrack t=new TrainTrack();
		MappingTorus mt;
		int i=0;
		boolean vb=false;

		Getopt opts=new Getopt("MappingTorus.java",s,"v");
		int c;

		while ((c = opts.getopt()) != -1) {
			switch(c) {
			case 'v':
				vb=true;
				break;
			case '?':
				System.exit(1);
				break;
			}
		}

		i=opts.getOptind();
		try{
			if (s.length>i) {
				if (s.length>i+1) {
					System.err.println("Too many arguments.");
					System.exit(1);
				}
				t.readFromFile(s[i]);
				if (t.getLabel().length()==0)
					t.setLabel(s[i]);
			}
			else 
				t.readFromFile("");
		} catch(Exception e) {System.err.println(e.toString());
		return; }

		mt=new MappingTorus(t);
		mt.setVerbose(vb);

		mt.triangulate();
	}

}
