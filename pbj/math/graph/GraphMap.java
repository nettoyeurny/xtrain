package pbj.math.graph;

import java.io.*;
import java.util.*;
import pbj.io.*;
import pbj.math.numerical.*;

/**
A class the implements self maps of graphs, the heart and soul of the
train track package.
@author Peter Brinkmann
@see pbj.math.graph.train.TrainTrack
 */
public class GraphMap extends Observable implements Serializable {

	private static final long serialVersionUID = 1L;

	private int edges;		// pretty much self-explanatory
	private int vertices;
	private String im[];
	private int start[];
	private int end[];
	private int nmarks;
	private String marklabel[];
	private String mark[];
	private boolean marked;
	private String fix;
	private String label;
	private boolean strict=true;

	private final static String LABELTAG="label";
	private final static String MARKINGTAG="marking";

	private static final boolean DEBUG = false;
	// set DEBUG to true for extra runtime information
	private String msg="";	// for error messages

	public GraphMap() {};

	/**
Constructs a new GraphMap identical to g2.
@param GraphMap g2: original
@return A clone of g2
	 */
	public GraphMap(GraphMap g2) {
		copyGraph(g2);
	}

	/**
Initializes a GraphMap with a given maximal number of edges.
@param int n: max number of edges
	 */
	public void init(int n) {
		int i;

		if (n>Word.MAXINDEX)
			throw new RuntimeException("too many edges");

		im=new String[n];
		start=new int[n];
		end=new int[n];

		edges=0;
		vertices=0;
		for(i=0;i<n;i++) {
			im[i]="";
			start[i]=0;
			end[i]=0;
		}
		nmarks=0;
		marked=false;
		fix="";
		label="";
	}

	/**
Input method for GraphMap.
@param Reader rr: some Reader
	 */
	public void readGraph(Reader rr) {
		int i,ed;
		String s;
		EnhancedTokenizer st=new EnhancedTokenizer(rr);

		try{
			ed=st.nextInt();
			if (ed<0 || ed>Word.MAXINDEX)
				throw new RuntimeException("no of edges to small or too large");

			init(4*ed);
			edges=ed;
			vertices=st.nextInt();
			if (vertices<0 || vertices>edges*2)
				throw new RuntimeException(
				"no of vertices to small or too large");

			for(i=0;i<edges;i++) {
				s=st.nextString();
				if (s==null)
					throw new RuntimeException("unexpected end of input");
				if (!s.equals(Word.indexToLabel(i,false)+""))
					throw new RuntimeException("bad edge label: "+s);

				s=st.nextString();
				if (s==null)
					throw new RuntimeException("unexpected end of input");
				start[i]=Word.labelToVertex(s);
				if (start[i]<0 || start[i]>=vertices)
					throw new RuntimeException(
							"bad vertex number "+start[i]);

				s=st.nextString();
				if (s==null)
					throw new RuntimeException("unexpected end of input");
				end[i]=Word.labelToVertex(s);
				if (end[i]<0 || end[i]>=vertices)
					throw new RuntimeException(
							"bad vertex number: "+end[i]);

				s=st.nextString();
				if (s==null)
					throw new RuntimeException("unexpected end of input");

				im[i]=Word.stringToPath(s);
			}
			s=st.nextString();
			if ((s!=null) && (!s.equals(LABELTAG)) && (!s.equals(MARKINGTAG))) {
				fix=Word.stringToPath(s);
				s=st.nextString();
			}

			for(;s!=null;s=st.nextString()) {
				if (s.equals(LABELTAG)) {
					s=st.nextString();
					if (s==null)
						throw new RuntimeException(
						"unexpected end of input");
					label=s;
				}
				else if (s.equals(MARKINGTAG)) {
					marked=true;
					nmarks=st.nextInt();
					if (nmarks<0)
						throw new RuntimeException(
						"unexpected end of input");
					mark=new String[nmarks];
					marklabel=new String[nmarks];

					for(i=0;i<nmarks;i++) {
						marklabel[i]=st.nextString();
						if (marklabel[i]==null)
							throw new RuntimeException(
							"unexpected end of input");
						s=st.nextString();
						if (s==null)
							throw new RuntimeException(
							"unexpected end of input");

						mark[i]=Word.stringToPath(s);
					}
				}
			}
		} catch(IOException e) { throw new RuntimeException(e.getMessage()); }

		if (strict && (!isGoodMap()))
			throw new RuntimeException(msg);
	}

	/**
Reads a GraphMap from a file.
@param String fn: file name
	 */
	public void readFromFile (String fn) throws FileNotFoundException {
		if (!fn.equals(""))
			readGraph(new FileReader(fn));
		else
			readGraph(new InputStreamReader(System.in));
	}

	/**
Makes a copy of g2.
@param: GraphMap g2: original
	 */
	public void copyGraph(GraphMap g2) {
		int i;

		init(g2.getCapacity());
		edges=g2.edges;
		vertices=g2.vertices;

		for(i=0;i<g2.edges;i++) {
			start[i]=g2.start[i];
			end[i]=g2.end[i];
			im[i]=g2.im[i];
		}

		marked=g2.marked;
		if (marked) {
			nmarks=g2.nmarks;
			mark=new String[nmarks];
			marklabel=new String[nmarks];
			for(i=0;i<nmarks;i++) {
				marklabel[i]=g2.marklabel[i];
				mark[i]=g2.mark[i];
			}
		}

		fix=g2.fix;
		label=g2.label;
		strict=g2.strict;
	}

	/**
Enables/disables strict checking of maps.
@param boolean s: indicates whether strict checking is desired
	 */
	public void setStrict(boolean s) {
		strict=s;
	}

	/**
@return true iff strict checking is enabled.
	 */
	public boolean isStrict() {
		return strict;
	}

	/**
Sets a marking for this graph.
@param int nm: number of loops
@param String ml[]: array of labels of loops
@param String ma[]: array of loops (internal format)
	 */
	public void setMarking(int nm,String ml[],String ma[]) {
		int i;

		nmarks=nm;
		mark=new String[nmarks];
		marklabel=new String[nmarks];
		for(i=0;i<nm;i++) {
			marklabel[i]=ml[i];
			mark[i]=ma[i];
		}
		marked=true;
	}

	/**
Disables the marking feature.
	 */
	public void unmark() {
		marked=false;
	}

	/**
@return number of edges of 'this'.
	 */
	public int getEdges() {
		return edges;
	}

	/**
@return maximum number of edges in this GraphMap.
	 */
	public int getCapacity() {
		return im.length;
	}

	/**
@return number of vertices of 'this'.
	 */
	public int getVertices() {
		return vertices;
	}

	/**
@return rank of the fundamental group of 'this', assuming the
graph is connected.
	 */
	public int getRank() {
		return edges-vertices+1;
	}

	/**
@return image of the i-th edge.
	 */
	public String getIm(int i) {
		if (i>=edges)
			throw new RuntimeException("index too large");

		return im[i];
	}

	/**
@return initial vertex of the i-th edge.
	 */
	public int getStart(int i) {
		if (i>=edges)
			throw new RuntimeException("index too large");

		return start[i];
	}

	/**
@return terminal vertex of the i-th edge.
	 */
	public int getEnd(int i) {
		if (i>=edges)
			throw new RuntimeException("index too large");

		return end[i];
	}

	/**
@return label of the i-th loop of the marking.
	 */
	public String getMarkingLabel(int i) {
		if (!marked)
			throw new RuntimeException("no marking available");
		if (i>=nmarks)
			throw new RuntimeException("index too large");

		return marklabel[i];
	}

	/**
@return number of loops in the marking.
	 */
	public int getMarkingSize() {
		if (marked)
			return nmarks;
		else
			return 0;
	}

	/**
@return i-th loop of the marking.
	 */
	public String getMarkingLoop(int i) {
		if (!marked)
			throw new RuntimeException("no marking available");
		if (i>=nmarks)
			throw new RuntimeException("index too large");

		return mark[i];
	}

	/**
@return fixed word of the GraphMap, if any.
	 */
	public String getFix() {
		return fix;
	}

	/**
@return label of the GraphMap.
	 */
	public String getLabel() {
		return label;
	}

	/**
@return abelianization of the given topological representative.
	 */
	public IntMatrix abelianized() {
		int k,j;
		IntMatrix ab=new IntMatrix(getEdges());

		for(j=0;j<getEdges();j++)
			for(k=0;k<getIm(j).length();k++)
				if (Word.isInverse(getIm(j).charAt(k)))
					ab.a[Word.charToIndex(getIm(j).charAt(k))][j]--;
				else
					ab.a[Word.charToIndex(getIm(j).charAt(k))][j]++;
		return ab;
	}

	/**
Computes the star of a vertex.
@param int v: index of vertex
@return String containing the star of the vertex (with cyclic ordering (counter clockwise) when a fixed word is available)
	 */
	public String starOfVertex(int v) {
		int i,j;
		String res="";
		char c;

		if (fix.equals(""))
			for(i=0;i<getEdges();i++) {
				if (getStart(i)==v)
					res+=Word.indexToChar(v,false);

				if (getEnd(i)==v)
					res+=Word.indexToChar(v,true);
			}
		else {
			for(i=0;(firstVertex(fix.charAt(i))!=v);i++)
				;	// do nothing

			j=i;
			do {
				c=fix.charAt(j);
				res+=c;
				for(;(Word.inverse(fix.charAt(j))!=c);
				j=(j+1) % fix.length())
					;	// do nothing
				j=(j+1) % fix.length();
			} while (i!=j);
		}

		return res;
	}

	/**
Checks whether a given path is continuous.
@param String p: path to be checked
@return boolean indicating whether the path is continuous
	 */
	public boolean isGoodPath(String p) {
		int i;

		try {
			for(i=0;i<p.length()-1;i++)
				if (lastVertex(p.charAt(i))!=
					firstVertex(p.charAt(i+1)))
					return false;
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	/**
Checks whether a given loop is continuous.
@param String p: loop to be checked
@return boolean indicating whether the loop is continous
	 */
	public boolean isGoodLoop(String p) {
		try {
			if (p.equals(""))
				return true;

			return ((isGoodPath(p)) &&
					(firstVertex(p.charAt(0))==
						lastVertex(p.charAt(p.length()-1))));
		} catch (Throwable e) {
			return false;
		}
	}

	/**
Checks whether this is a continous map and whether the fixed word is cyclically
fixed.
@return boolean indicating whether the map is ok
	 */
	public boolean isGoodMap() {
		int i;

		try {
			for(i=0;i<getEdges();i++)
				if (!isGoodPath(getIm(i))) {
					msg="bad image: "+Word.pathToString(getIm(i));
					return false;
				}

			if (marked)
				for(i=0;i<nmarks;i++)
					if (!isGoodPath(getMarkingLoop(i))) {
						msg="bad marking: "+
						Word.pathToString(
								getMarkingLoop(i));
						return false;
					}

			if (!isGoodLoop(fix)) {
				msg="bad fixed word: "+Word.pathToString(fix);
				return false;
			}

			if (Word.isCyclicallyConjugate(fix,mapWord(fix))
					|| Word.isCyclicallyConjugate(Word.reverseWord(fix),
							mapWord(fix))) {
				msg="";
				return true;
			}
			else {
				msg="fixed word not preserved";
				return false;
			}

		} catch (Throwable e) {
			msg="general error";
			return false;
		}
	}

	/* doubles the size of im, start, and end (if possible). */
	private void increaseCapacity() {
		int i;
		int n;
		String newim[];
		int newstart[];
		int newend[];

		if (getCapacity()>=Word.MAXINDEX)
			throw new RuntimeException("can't increase capacity");

		if (2*getCapacity()>Word.MAXINDEX)
			n=Word.MAXINDEX;
		else
			n=2*getCapacity();

		newim=new String[n];
		newstart=new int[n];
		newend=new int[n];

		for(i=0;i<getEdges();i++) {
			newim[i]=getIm(i);
			newstart[i]=getStart(i);
			newend[i]=getEnd(i);
		}

		for(i=getEdges();i<newim.length;i++) {
			newim[i]="";
			newstart[i]=0;
			newend[i]=0;
		}

		im=newim;
		start=newstart;
		end=newend;
	}

	/**
Adds an edges between the vertices v1 and v2, with given image.
@param int v1: initial vertex of new edge
@param int v2: terminal vertex of new edge
@param String image: image of new edge
	 */
	public void addEdge(int v1, int v2, String image) {
		if (edges>=getCapacity())
			increaseCapacity();

		im[edges]=image;
		start[edges]=v1;
		end[edges]=v2;
		if (v1>=vertices) vertices=v1+1;
		if (v2>=vertices) vertices=v2+1;
		edges++;
	}

	/**
Sets the image of an edge. Use with care.
@param int ed: index of edge
@param String s: new image of edge ed
	 */
	public void setImage(int ed,String s) {
		im[ed]=s;
	}

	/**
Sets the fixed word. Use with care.
@param String s: new fixed word
	 */
	public void setFix(String s) {
		fix=s;
	}

	/**
Sets the label.
@param String s: new label
	 */
	public void setLabel(String s) {
		label=s;
	}

	public String toString() {
		int i;
		String s="";

		s=edges+" // number of edges\n"
		+vertices+" // number of vertices\n"
		+ "// format: edge: (initial, terminal vertex) --> image\n";

		for(i=0;i<edges;i++)
			if (im[i].length()>0)
				s=s+Word.indexToLabel(i,false) + ": ("
				+ Word.vertexToLabel(start[i])+", "
				+ Word.vertexToLabel(end[i])
				+ ") --> "
				+ Word.pathToString(im[i]) +"\n";
			else
				s=s+Word.indexToLabel(i,false) + ": (v"
				+ start[i] + ", v" + end[i]
				                         + ") --> "
				                         + EnhancedTokenizer.dummy()
				                         + " // empty image\n";

		if (fix.length()>0)
			s=s+Word.pathToString(fix)+" // fixed word\n";

		if (label.length()>0)
			s=s+"\n"+LABELTAG+": "+label+"\n";

		s=s+"\n"+showMarking();

		return s;
	}

	/**
Converts the contents of 'this' into LaTeX format.
@return String in LaTeX format
	 */
	public String toLaTeX() {
		int i;
		String s;

		s="% --------------------------------------------"+
		"----------------------------\n";

		s+="\\begin{eqnarray*}\n";

		for(i=0;i<edges;i++)
			if (im[i].length()>0)
				s=s+Word.indexToLaTeXLabel(i,false) + ": ("
				+ Word.vertexToLaTeXLabel(start[i])+", "
				+ Word.vertexToLaTeXLabel(end[i])
				+ ") & \\rightarrow & "
				+ Word.pathToLaTeX(im[i]) +"\\\\\n";
			else
				s=s+Word.indexToLaTeXLabel(i,false) + ": ("
				+ Word.vertexToLaTeXLabel(start[i])+", "
				+ Word.vertexToLaTeXLabel(end[i])
				+ ") & \\rightarrow & "
				+ "(\\text{empty image})\\\\\n";

		if (fix.length()>0)
			s=s+"\\sigma & = & "+Word.pathToLaTeX(fix)+" \\\\\n";

		s+="\\end{eqnarray*}\n";
		s+="% --------------------------------------------"+
		"----------------------------\n\n";

		if (marked)
			s+=markingToLaTeX();

		return s;
	}

	/**
Writes the marking to a String.
@return A String containing the marking.
	 */
	public String showMarking() {
		int i;
		String s;

		if (!marked)
			return "// no marking\n";

		s=MARKINGTAG+":\n"+nmarks+" // number of loops\n"+
		"// format: label: loop\n";
		for(i=0;i<nmarks;i++)
			s=s+marklabel[i]+": "+ Word.pathToString(mark[i]) +"\n";

		return s;
	}

	/**
Prints the marking in LaTeX format.
@return String containing the marking in LaTeX format.
	 */
	public String markingToLaTeX() {
		int i;
		String s;

		s="% --------------------------------------------"+
		"----------------------------\n";

		if (!marked)
			return s+"\\text{No marking}\n";

		s+="\\begin{eqnarray*}\\\\\n";

		for(i=0;i<nmarks;i++)
			s=s+"& "+marklabel[i]+": & "+
			Word.pathToLaTeX(mark[i]) +"\\\\\n";

		s+="\\end{eqnarray*}\n";

		s+="% --------------------------------------------"+
		"----------------------------\n";

		return s;
	}

	/**
Compare two graphs.
@return true iff 'this' and g2 have the same underlying graph.
	 */
	public boolean sameGraph(GraphMap g2) {
		int i;
		if ((edges!=g2.edges) || (vertices!=g2.vertices))
			return false;
		for(i=0;i<edges;i++)
			if ((start[i]!=g2.start[i])
					|| (end[i]!=g2.end[i]))
				return false;
		return true;
	}


	/**
Compare two GraphMaps.
@return true iff 'this' and g2 are the same map (fixed words have to be cyclic conjugates)
	 */
	public boolean equals(GraphMap g2) {
		int i;

		if (!sameGraph(g2))
			return false;

		for(i=0;i<getEdges();i++)
			if (!getIm(i).equals(g2.getIm(i)))
				return false;

		return Word.isCyclicallyConjugate(getFix(),g2.getFix());
	}


	/**
Computes the images of words under the GraphMap.
@param String ar: argument
@param The image of ar under this GraphMap
	 */
	public String mapWord(String ar) {
		String tmp,image;
		int i;
		image="";
		for(i=0;i<ar.length();i++)
		{	tmp=im[Word.charToIndex(ar.charAt(i))];
		if (Word.isInverse(ar.charAt(i)))
			image=image + Word.reverseWord(tmp);
		else
			image=image + tmp;
		}
		return Word.tightenWord(image);
	}

	/**
Computes 'this' o g1, writes the result to 'this'.
@param GraphMap g1: second map in composition
	 */
	public void compose(GraphMap g1) {
		int i;
		GraphMap gog1=new GraphMap(this);

		if (!sameGraph(g1))
			throw new RuntimeException("different graphs");

		for(i=0;i<edges;i++)
			gog1.im[i]=mapWord(g1.im[i]);

		copyGraph(gog1);
	}

	/**
Splits a the image of the ed-th edge in two parts such that the part of its
image starting at position 'at' becomes the image of the second half.
@param int ed: edge to be split
@param int at: indicates where to split the image of ed
	 */
	public void splitEdge(int ed,int at) {
		int i,n;

		if (DEBUG)
			System.out.println("splitedge: "+
					Word.indexToChar(ed,false) + " " + at);

		if ((ed>=edges) || (im[ed].length()<=at))
			throw new RuntimeException("illegal argument");

		if (edges>=getCapacity())
			increaseCapacity();

		n=edges;			/* initialize a new edge */
		end[n]=end[ed];		/* adjust the vertices */
		start[n]=vertices;
		end[ed]=vertices;
		vertices++;
		edges++;
		im[n]=im[ed].substring(at);
		im[ed]=im[ed].substring(0,at);
		for(i=0;i<edges;i++)		/* update the images in the graph */
			im[i]=Word.splitChar(im[i],ed,n);
		fix=Word.splitChar(fix,ed,n);

		if (marked)
			for(i=0;i<nmarks;i++)
				mark[i]=Word.splitChar(mark[i],ed,n);

		if (DEBUG)
			System.out.println(toString()+showMarking());
	}

	/**
splits edges at fixed points so all fixed points (with the property that
the restriction of 'this' to the germ around them preserves orientation)
become vertices (still experimental!)
	 */
	public void splitAtFixedPoints() {
		int i,j;
		for(i=0;i<getEdges();i++)
			for(j=1;j<getIm(i).length()-1;j++)
				if ((i==Word.charToIndex(getIm(i).charAt(j))) &&
						(!Word.isInverse(getIm(i).charAt(j)))) {
					splitEdge(i,j);
					im[i]=im[i]+im[getEdges()-1].charAt(0);
					im[getEdges()-1]=im[getEdges()-1].substring(1,
							im[getEdges()-1].length());
				}
	}

	/**
computes indivisible Nielsen paths (still highly experimental, as well as
inefficient!); splits edges at fixed points.
	 */
	public String[] nielsenPaths() {
		List<String> iNps,pcs;
		String img,cnd,w1,w2;
		int i,j,k,m;
		char c;

		splitAtFixedPoints();

		pcs=new ArrayList<String>();
		for(i=0;i<getEdges();i++) 
			for(j=0;j<2;j++) {
				c=Word.indexToChar(i,j==0);
				if (mapd(c)==c) {
					img=c+"";
					while (img.length()<2*getEdges()) {
						img=mapWord(img);
					}
					pcs.add(img.substring(0,2*getEdges()));
				}
			}

		iNps=new ArrayList<String>();
		for(i=0;i<pcs.size()-1;i++) {
			w1=pcs.get(i);
			for(j=i+1;j<pcs.size();j++) {
				w2=Word.reverseWord(pcs.get(j));
				for(k=0;k<w1.length();k++)
					for(m=0;m<w2.length();m++)
						if ((lastVertex(w1.charAt(k))==firstVertex(w2.charAt(m))) &&
								(Word.inverse(w1.charAt(k))!=w2.charAt(m)) &&
								(mapd(Word.inverse(w1.charAt(k)))==mapd(w2.charAt(m))))
						{
							cnd=w1.substring(0,k+1)+w2.substring(m,w2.length());
							if (cnd.equals(mapWord(cnd)))
								iNps.add(cnd);
						}
			}
		}

		return (String[]) iNps.toArray();
	}

	/**
Reverses an edge.
@param int ed: index of edge to be reversed
	 */
	public void reverseEdge(int ed) {
		int i;
		if (DEBUG)
			System.out.println("reverseEdge: "+Word.indexToChar(ed,false));

		if (ed>=edges)
			throw new RuntimeException("nonexistent edge");

		im[ed]=Word.reverseWord(im[ed]);

		i=start[ed];		/* exchange vertices */
		start[ed]=end[ed];
		end[ed]=i;

		for(i=0;i<edges;i++)	/* adjust all the images in the graph */
			im[i]=Word.reverseChar(im[i],ed);
		fix=Word.reverseChar(fix,ed);
		/* Remark: Figuring out the orientations took forever, but I think
		   I've got it straight now. */

		if (marked)
			for(i=0;i<nmarks;i++)
				mark[i]=Word.reverseChar(mark[i],ed);

		if (DEBUG)
			System.out.println(toString()+showMarking());
	}

	private void adjustVertices(int v1,int v2) {
		/* just a book-keeping funtion. It adjusts numbers of the vertices if one
   vertex has been deleted. */
		int i;
		if (v1==v2) return;
		if (v1>v2) {
			i=v1;
			v1=v2;
			v2=i;
		}
		for(i=0;i<edges;i++) {
			if (start[i]==v2) start[i]=v1;
			else if (start[i]>v2) start[i]--;
			if (end[i]==v2) end[i]=v1;
			else if (end[i]>v2) end[i]--;
		}
		vertices--;
	}

	private void removeEntry(int ed) {
		/* This is just a subroutine that removes an edge WITHOUT book-keeping. It
   should only be called by functions inside this file. */
		int i;
		int last;
		edges--;
		last=edges;
		start[ed]=start[last];
		end[ed]=end[last];
		im[ed]=im[last];

		for(i=0;i<edges;i++) {
			im[i]=Word.removeEdge(im[i], ed);
			im[i]=im[i].replace(Word.indexToChar(last,false),
					Word.indexToChar(ed,false));
			im[i]=im[i].replace(Word.indexToChar(last,true),
					Word.indexToChar(ed,true));
		}
		fix=Word.removeEdge(fix, ed);
		fix=fix.replace(Word.indexToChar(last,false),
				Word.indexToChar(ed,false));
		fix=fix.replace(Word.indexToChar(last,true),
				Word.indexToChar(ed,true));

		if (marked)
			for(i=0;i<nmarks;i++) {
				mark[i]=Word.removeEdge(mark[i], ed);
				mark[i]=mark[i].replace(Word.indexToChar(last,false),
						Word.indexToChar(ed,false));
				mark[i]=mark[i].replace(Word.indexToChar(last,true),
						Word.indexToChar(ed,true));
			}
	}

	/**
Removes an edge and takes care of the book-keeping.
@param int ed: index of edge to be removed
	 */
	public void removeEdge(int ed) {
		int i,cnt1,cnt2;

		if (DEBUG)
			System.out.println("removeedge: "+Word.indexToChar(ed,false));

		if (ed>=edges)
			throw new RuntimeException("nonexistent edge");

		for(cnt1=cnt2=i=0;i<edges;i++) {
			if ((start[i]==start[ed])
					|| (end[i]==start[ed])) cnt1++;
			if ((start[i]==end[ed])
					|| (end[i]==end[ed])) cnt2++;
		}

		if ((cnt1<3) || (cnt2<3))
			adjustVertices(start[ed],end[ed]);

		removeEntry(ed);

		if (DEBUG)
			System.out.println(toString()+showMarking());
	}

	/**
collapses an edge (i.e. removes it and identifies the endpoints if they are
different) and takes care of the book-keeping.
@param int ed: index of edge to be collapsed
	 */
	public void collapseEdge(int ed) {
		if (DEBUG)
			System.out.println("collapseEdge: "
					+Word.indexToChar(ed,false));

		if (ed>=edges)
			throw new RuntimeException("nonexistent edge");

		adjustVertices(start[ed],end[ed]);

		removeEntry(ed);

		if (DEBUG)
			System.out.println(toString()+showMarking());
	}

	/**
contracts an edge to a point. The two endpoints have to be distinct, and the
induced outer automorphism of the fundamental group remains the same.
@param ind ed: index of edge to be contracted
	 */
	public void contractEdge(int ed) {
		int i;

		if (DEBUG)
			System.out.println("contractEdge: "
					+Word.indexToChar(ed,false));

		if (ed>=getEdges())
			throw new RuntimeException("index too large");

		if (start[ed]==end[ed])
			throw new RuntimeException("endpoints have to be distinct");

		for(i=0;i<edges;i++)
			if (i!=ed) {
				if (start[i]==end[ed])
					im[i]=im[ed]+im[i];
				if (end[i]==end[ed])
					im[i]=im[i]+Word.reverseWord(im[ed]);
			}

		im[ed]="";
		collapseEdge(ed);

		tighten();

		if (DEBUG)
			System.out.println(toString()+showMarking());
	}

	/**
turns the graph into a rose such that the induced outer automorphism of the
fundamental group remains the same.
	 */
	public void rose() {
		int i;

		for(i=edges-1;i>=0;i--)
			if (start[i]!=end[i])
				contractEdge(i);
	}

	/**
Performs a valence 2 homotopy with respect to e1 and e2. e2 will be deleted
in the process. This function is smart enough to figure out how to orient
the given edges.
@param int e1,e2: indices of edges to be joined 
	 */
	public void joinEdges(int e1,int e2) {
		int i;
		int valence;

		if (DEBUG)
			System.out.println("joinedges: "+Word.indexToChar(e1,false)
					+" "+Word.indexToChar(e2,false));

		if ((e1>=edges) || (e2>=edges))
			throw new RuntimeException("nonexistent edge");

		if (e1==e2) return;

		for(i=0,valence=0;i<edges;i++) {
			if (start[i]==end[e1]) valence++;
			if (end[i]==end[e1]) valence++;
		}
		if (valence!=2) reverseEdge(e1);
		if (start[e2]!=end[e1]) reverseEdge(e2);
		if (start[e2]!=end[e1]) reverseEdge(e1);
		if (start[e2]!=end[e1]) reverseEdge(e2);

		if (start[e2]!=end[e1])
			throw new RuntimeException("edges not adjacent");

		im[e1]=im[e1]+im[e2];
		collapseEdge(e2);

		if (DEBUG)
			System.out.println(toString()+showMarking());
	}

	/**
performs an elementary folding operation with e1 and e2, e2 is removed.
This function requires e1 and e2 to start at the same vertex, so orientation
is important.
@param int e1,e2: indices of edges to be folded
	 */
	public void elementaryFold(int e1,int e2) {
		int i;

		if (DEBUG)
			System.out.println("elementaryfold: "
					+Word.indexToChar(e1,false)
					+" "+Word.indexToChar(e2,false));

		if ((e1>=edges) || (e2>=edges))
			throw new RuntimeException("nonexistent edge");

		if (e1==e2) return;

		if ((!im[e1].equals(im[e2])) || (start[e1]!=start[e2]))
			throw new RuntimeException("different images");

		for(i=0;i<edges;i++) {
			im[i]=im[i].replace(Word.indexToChar(e2,false),
					Word.indexToChar(e1,false));
			im[i]=im[i].replace(Word.indexToChar(e2,true),
					Word.indexToChar(e1,true));
		}
		fix=fix.replace(Word.indexToChar(e2,false),Word.indexToChar(e1,false));
		fix=fix.replace(Word.indexToChar(e2,true),Word.indexToChar(e1,true));
		if (marked)
			for(i=0;i<nmarks;i++) {
				mark[i]=mark[i].replace(Word.indexToChar(e2,false),
						Word.indexToChar(e1,false));
				mark[i]=mark[i].replace(Word.indexToChar(e2,true),
						Word.indexToChar(e1,true));
			}

		adjustVertices(end[e1],end[e2]);
		removeEntry(e2);

		if (DEBUG)
			System.out.println(toString()+showMarking());
	}

	/**
identifies two vertices, adjusts labels of vertices in order to avoid
gaps.
@param int v1,v2: indices of vertices to be identified
	 */
	public void identifyVertices(int v1,int v2) {
		int i;
		int vmin,vmax;

		if ((v1<0) || (v2<0))
			throw new RuntimeException("nonexistent vertex");

		if ((v1>=vertices) || (v2>=vertices))
			throw new RuntimeException("nonexistent vertex");

		if (v1==v2) return;

		if (v1<v2) {
			vmin=v1;
			vmax=v2;
		}
		else {
			vmin=v2;
			vmax=v1;
		}

		for(i=0;i<edges;i++) {
			if (start[i]==vmax)
				start[i]=vmin;
			if (start[i]==vertices-1)
				start[i]=vmax;
			if (end[i]==vmax)
				end[i]=vmin;
			if (end[i]==vertices-1)
				end[i]=vmax;
		}
		vertices=vertices-1;
	}

	/**
Performs elementary reductions in the images of the edges and in the
wrapping strings.
@return true iff tightening has occurred.
	 */
	public boolean tighten() {
		int i;
		boolean flag;
		String tmp;

		if (DEBUG)
			System.out.println("tighten");

		flag=false;

		for(i=0;i<edges;i++) {
			tmp=im[i];
			im[i]=Word.tightenWord(im[i]);
			if (!tmp.equals(im[i])) flag=true;
		}

		fix=Word.tightenWord(fix);
		fix=Word.tightenCycl(fix);

		if (marked)
			for(i=0;i<nmarks;i++) {
				mark[i]=Word.tightenWord(mark[i]);
				mark[i]=Word.tightenCycl(mark[i]);
			}

		if (DEBUG && flag)
			System.out.println(toString()+showMarking());

		return flag;
	}

	/** makes sure the map D is not constant around any vertex */
	public boolean tightenVertex() {
		boolean fflag,flag;
		int i,j;
		int val;                // used to be signed char!
		char ed;
		fflag=false;
		for(i=0;i<vertices;i++) {
			val=-1;
			flag=true;
			for(j=0;(flag) && (j<edges);j++) {
				if (start[j]==i) {
					ed=Word.indexToChar(j,false);
					if (im[j].length()==0) flag=false;
					else {
						if (val<0) val=(int) mapd(ed);
						if (val!=mapd(ed)) flag=false;
					}
				}
				if (end[j]==i) {
					ed=Word.indexToChar(j,true);
					if (im[j].length()==0) flag=false;
					else {
						if (val<0) val=(int) mapd(ed);
						if (val!=mapd(ed)) flag=false;
					}
				}
			}
			if (flag) {             // used to be (flag && val)
				fflag=true;
				for(j=0;j<edges;j++)
				{       if (start[j]==i)
					im[j]=im[j].substring(1);
				if (end[j]==i)
					im[j]=im[j].substring(0,
							im[j].length()-1);
				}
			}
		}

		// recent addition!!!
		for(i=0;i<getEdges();)
			if (getIm(i).length()==0)
				collapseEdge(i);
			else
				i++;

		return fflag;
	}

	/**
realizes the map D defined in "Train tracks and... ". If c is not inverse,
it returns the first edge in the image of c, otherwise it returns the
inverse of the last edge in the image of c.
@param char c: argument
	 */
	public char mapd(char c) {
		int n;

		n=Word.charToIndex(c);

		if (n>=edges)
			return c;

		if (im[n].length()==0)
			throw new RuntimeException("mapd not defined for empty image");

		if (Word.isInverse(c))
			return Word.inverse(im[n].charAt(im[n].length()-1));
		else
			return im[n].charAt(0);
	}

	/**
Returns the terminal vertex of the edge with label c.
@param char c: char representing an edge
	 */
	public int lastVertex(char c) {
		if (Word.isInverse(c))
			return start[Word.charToIndex(c)];
		else
			return end[Word.charToIndex(c)];
	}

	/**
Returns the initial vertex of the edge with label c.
@param char c: char representing an edge
	 */
	public int firstVertex(char c) {
		return lastVertex(Word.inverse(c));
	}

	/**
Swaps the labels of the vertices v1 and v2.
@param int v1,v2: indices of vertices to be exchanged
	 */
	public void swapVertices(int v1,int v2) {
		int i;

		if (DEBUG)
			System.out.println("swapvertices "+v1+" "+v2);

		if ((v1>vertices) || (v2>vertices))
			throw new RuntimeException("nonexistent vertex");

		if (v1!=v2) {
			for(i=0;i<edges;i++) {
				if (start[i]==v1) start[i]=v2;
				else if (start[i]==v2) start[i]=v1;
				if (end[i]==v1) end[i]=v2;
				else if (end[i]==v2) end[i]=v1;
			}

			if (DEBUG)
				System.out.println(toString()+showMarking());
		}
	}

	/**
initializes g to be the identity on a rose. The fixed word is dual to the
boundary word.
@param String bd: boundary word
@return new GraphMap
	 */
	public static GraphMap identity(String bd) {
		int i;
		GraphMap g=new GraphMap();

		g.init(2*bd.length());
		for(i=0;i<bd.length()/2;i++)
			g.addEdge(0,0,""+Word.indexToChar(i,false));

		g.setFix(Word.dualize(bd));
		return g;
	}

	/**
initializes g to be the identity on a rose, with empty fixed word.
@param int n: number of petals
@return new GraphMap
	 */
	public static GraphMap identity(int n) {
		int i;
		GraphMap g=new GraphMap();

		g.init(2*n);
		for(i=0;i<n;i++)
			g.addEdge(0,0,""+Word.indexToChar(i,false));

		g.setFix("");
		return g;
	}

	/**
turns a GraphMap into the identity of the underlying graph.
	 */
	public void identity() {
		int i;

		for(i=0;i<getEdges();i++)
			setImage(i,Word.indexToChar(i,false)+"");
	}

	private void setFirstVertex(char c,int v) {
		if (Word.isInverse(c))
			end[Word.charToIndex(c)]=v;
		else
			start[Word.charToIndex(c)]=v;
	}

	/**
initializes g to be the identity on a graph determined by a fixed word.
@param String fw: fixed word
@return identity of the graph determined by fw
	 */
	public static GraphMap identityFromFixed(String fw) {
		int i,cnt;
		char c;
		GraphMap g=new GraphMap();

		if (!Word.isBoundary(fw))
			throw new RuntimeException("bad boundary word: "
					+Word.pathToString(fw));

		g.init(2*fw.length());
		for(i=0;i<fw.length()/2;i++)
			// start with dummy vertices
			g.addEdge(-1,-1,""+Word.indexToChar(i,false));

		cnt=0;
		for(i=0;i<fw.length();i++)
			if (g.firstVertex(fw.charAt(i))<0) {
				while (g.firstVertex(fw.charAt(i))<0) {
					c=fw.charAt(i);
					g.setFirstVertex(c,cnt);

					while (Word.inverse(fw.charAt(i))!=c)
						i=(i+1) % fw.length();
					i=(i+1) % fw.length();

				}
				cnt++;
			}

		g.vertices=cnt;

		g.setFix(fw);
		return g;
	}

	/**
Checks whether a marking is present.
@return boolean value indicating whether a marking is present
	 */
	public boolean isMarked() {
		return marked;
	}

	/**
Determines the images of the vertices under the given map.
@return IntVector containing the map
@see pbj.math.numerical.IntVector
	 */
	public IntVector vertexMap() {
		int i;
		IntVector vt=new IntVector(getVertices());

		for(i=0;i<getEdges();i++) {
			vt.v[getStart(i)]=firstVertex(mapd(Word.indexToChar(i,false)));
			vt.v[getEnd(i)]=firstVertex(mapd(Word.indexToChar(i,true)));
		}
		return vt;
	}

	/**
@return induced map of the vertex set in a String.
	 */
	public String showVertexMap() {
		int i;
		IntVector vm=vertexMap();
		String s="// induced map on vertices\n";

		for(i=0;i<vertices;i++)
			s=s+Word.vertexToLabel(i)+" --> "
			+Word.vertexToLabel(vm.v[i])+"\n";

		return s;
	}

	/**
Replaces every occurence of the edge c by the String s but doesn't remove the
edge c.  Also, if a list of relations is given, the same replacement will be
performed in the relations.
@param char c: char representing edge to be removed
@param String s: word replacing c
@param String rel[]: array of relations to be modified in the process (may be null)
	 */
	public void replaceEdge(char c,String s,String rel[]) {
		int i;

		for(i=0;i<edges;i++)
			im[i]=Word.replaceChar(im[i],c,s);

		fix=Word.replaceChar(fix,c,s);

		if (marked)
			for(i=0;i<nmarks;i++)
				mark[i]=Word.replaceChar(mark[i],c,s);

		if (rel!=null)
			for(i=0;i<rel.length;i++)
				rel[i]=Word.replaceChar(rel[i],c,s);

	}

	/**
Attempts to eliminate loops using Tietze transformations, given a list of
relations. Note that Tietze transformations also affect the relators.
@param String rel[]: array of relations
	 */
	public void tietzeTrafos(String rel[]) {
		int i,j,k,cnt,cntrep;	// some counters
		char c;			// current edge
		int gone[]=new int[rel.length];	// list of edges to be removed
		String s;				// replacement string

		// loop over relators
		cntrep=0;
		for(i=0;i<rel.length;i++)
			// scan current relation, look for letter that occurs only once
			for(j=0;j<rel[i].length();j++) {
				for(k=0,cnt=0;k<rel[i].length();k++)
					if (Word.charToIndex(rel[i].charAt(j))
							==Word.charToIndex(rel[i].charAt(k)))
						cnt++;

				if (cnt==1) {
					c=Word.inverse(rel[i].charAt(j));
					s=rel[i].substring(j+1)+rel[i].substring(0,j);
					replaceEdge(c,s,rel);
					gone[cntrep]=Word.charToIndex(c);
					cntrep++;
				}
			}

		for(i=0;i<cntrep;i++)
			removeEdge(gone[i]);

	}

	/**
Reads a spanning tree from a string and returns an array of booleans indicating
which edges are in the tree. Also computes paths in the tree.
@param boolean spt[]: array of size getEdges() then indicates which edges are in the spanning tree
@param String path[][]: square array of size getVertices() for paths in the spanning tree. path[i][j] will be the (unique) path connecting vertices i and j. Can be null is paths aren't needed
@param String tr: lists edges in the spanning tree
	 */
	public void evaluateTree(boolean spt[],String path[][],String tr) {
		int i,j,k,r;
		int cnt;

		for(i=0;i<getEdges();i++)
			spt[i]=false;

		cnt=0;
		for(i=0;i<tr.length();i++) {
			j=Word.charToIndex(tr.charAt(i));
			if (!spt[j])
				cnt++;
			spt[j]=true;
		}

		if (cnt!=getVertices()-1)
			throw new RuntimeException("wrong number of edges in "
					+"spanning tree: "+Word.pathToString(tr));

		if (path!=null) {
			for(i=0;i<getVertices();i++)
				for(j=0;j<getVertices();j++)
					path[i][j]="";

			for(i=0;i<getEdges();i++)
				if (spt[i]) {
					path[getStart(i)][getEnd(i)]=""+
					Word.indexToChar(i,false);
					path[getEnd(i)][getStart(i)]=""+
					Word.indexToChar(i,true);
				}

			for(r=2;r<getVertices();r++)
				for(i=0;i<getVertices()-1;i++)
					for(j=i+1;j<getVertices();j++)
						if (path[i][j].length()==0)
							for(k=0;k<getVertices();k++)
								if ((path[i][k].length()>0) &&
										(path[k][j].length()>0)) {
									path[i][j]=path[i][k]+
									path[k][j];
									path[j][i]=path[j][k]+
									path[k][i];
								}

			for(i=0;i<getVertices()-1;i++)
				for(j=i+1;j<getVertices();j++) {
					if (path[i][j].length()==0)
						throw new RuntimeException(
								"bad spanning tree: "
								+Word.pathToString(tr));

					path[i][j]=Word.tightenWord(path[i][j]);
					path[j][i]=Word.reverseWord(path[i][j]);
				}
		}
	}

	/**
Finds a spanning tree of approximately minimal diameter.
@param boolean spt[]: array of size getEdges() then indicates which edges are in the spanning tree
@param String path[][]: square array of size getVertices() for paths in the spanning tree. path[i][j] will be the (unique) path connecting vertices i and j. Can be null is paths aren't needed
@param double len[]: array of size getEdges() containing lengths of edges. Can be null if length doesn't matter.
	 */
	public void spanningTree(boolean spt[],String path[][],double len[]) {
		double dist[][]=new double[getVertices()][getVertices()];
		double maxdist[]=new double[getVertices()];
		boolean intree[]=new boolean[getVertices()];
		double min,mm,diam;
		int i,j,k,l0,l1;
		char c;

		if (len==null) {
			len=new double[getEdges()];
			for(i=0;i<getEdges();i++)
				len[i]=1.0;
		}

		for(i=0;i<getEdges();i++)
			spt[i]=false;

		for(i=0;i<getVertices();i++) {
			intree[i]=false;
			maxdist[i]=0.0;
			for(j=0;j<getVertices();j++) {
				dist[i][j]=0.0;
				if (path!=null)
					path[i][j]="";
			}
		}
		intree[0]=true;
		diam=0.0;

		k=0;
		for(i=0;i<getVertices()-1;i++) {
			min=-1.0;
			for(j=0;j<getEdges();j++)
				if ((!spt[j]) && 
						(intree[getStart(j)]!=intree[getEnd(j)])) {
					if (intree[getStart(j)])
						mm=Math.max(diam,
								maxdist[getStart(j)]+len[j]);
					else
						mm=Math.max(diam,
								maxdist[getEnd(j)]+len[j]);
					if ((min<0) || (mm<min)) {
						k=j;
						min=mm;
					}
				}

			if (intree[getStart(k)]) {
				l0=getStart(k);
				l1=getEnd(k);
				c=Word.indexToChar(k,false);
			}
			else {
				l0=getEnd(k);
				l1=getStart(k);
				c=Word.indexToChar(k,true);
			}

			diam=Math.max(diam,maxdist[l0]+len[k]);
			for(j=0;j<getVertices();j++)
				if (intree[j]) {	// note that !intree[l1]
					dist[j][l1]=dist[j][l0]+len[k];
					dist[l1][j]=dist[j][l1];
					if (dist[j][l1]>maxdist[j])
						maxdist[j]=dist[j][l1];

					if (path!=null) {
						path[j][l1]=path[j][l0]+c;
						path[l1][j]=Word.reverseWord(
								path[j][l1]);
					}
				}

			maxdist[l1]=maxdist[l0]+len[k];
			intree[l1]=true;
			spt[k]=true;
		}
	}

	/**
Inverts 'this'.
@return true iff inverting was successful
	 */
	public boolean invert() {
		GraphMap g=new GraphMap(this);
		int map[]=new int[getRank()];
		boolean spt[]=new boolean[getEdges()];
		String path[][]=new String[getVertices()][getVertices()];
		String tmp;
		int i,j;

		if (!isGoodMap())
			throw new RuntimeException(msg);

		spanningTree(spt,path,null);

		for(i=0;i<g.getEdges();i++) {
			if (i<getRank())
				map[i]=i;

			while ((i<g.getEdges()) && (spt[i])) {
				g.contractEdge(i);
				spt[i]=spt[g.getEdges()];
				if (i<getRank())
					map[i]=g.getEdges();
			}
		}

		if (!g.invertRose())
			return false;

		for(i=0;i<getEdges();i++)
			im[i]=Word.indexToChar(i,false)+"";

		for(i=0;i<getRank();i++) {
			im[map[i]]="";
			for(j=0;j<g.getIm(i).length();j++)
				im[map[i]]+=Word.indexToChar(
						map[Word.charToIndex(g.getIm(i).charAt(j))],
						Word.isInverse(g.getIm(i).charAt(j)));
		}

		for(i=0;i<getEdges();i++) {
			tmp=path[getStart(i)][firstVertex(getIm(i).charAt(0))];
			for(j=0;j<getIm(i).length()-1;j++)
				tmp+=getIm(i).charAt(j)+
				path[lastVertex(getIm(i).charAt(j))]
				     [firstVertex(getIm(i).charAt(j+1))];
			// j now equals getIm(i).length()-1
			tmp+=getIm(i).charAt(j)+
			path[lastVertex(getIm(i).charAt(j))][getEnd(i)];
			im[i]=Word.tightenWord(tmp);
		}

		if (!isGoodMap())
			throw new RuntimeException(msg);

		return true;
	}

	/**
computes the size, i.e., the sum of the lengths of images, of 'this'
	 */
	public int size() {
		int s;
		int i;
		s=0;
		for(i=0;i<getEdges();i++)
			s+=getIm(i).length();
		return s;
	}

	/* computes the left half of a string */
	private String leftHalf(String s) {
		return s.substring(0,(s.length()+1)/2);
	}

	/* implements the well-ordering defined in LS, p. 6, last paragraph */
	private boolean lessThan(String a,String b) {
		String la;	// left half of a
		String lA;	// left half of a^-1
		String lb;	// left half of b
		String lB;	// left half of b^-1
		String mina;
		String minb;
		String maxa;
		String maxb;

		// compare lengths first
		if (a.length()>b.length())
			return false;
		if (a.length()<b.length())
			return true;

		// lengths are the same, now use lexicographical ordering
		la=leftHalf(a);
		lA=leftHalf(Word.reverseWord(a));
		lb=leftHalf(b);
		lB=leftHalf(Word.reverseWord(b));

		if (la.compareTo(lA)<0) {
			mina=la;
			maxa=lA;
		}
		else {
			mina=lA;
			maxa=la;
		}
		if (lb.compareTo(lB)<0) {
			minb=lb;
			maxb=lB;
		}
		else {
			minb=lB;
			maxb=lb;
		}

		// compare mina,minb first
		if (mina.compareTo(minb)>0)
			return false;
		if (mina.compareTo(minb)<0)
			return true;

		// mina equals minb, now compare maxa,maxb
		return (maxa.compareTo(maxb)<0);
	}

	/*
inverts GraphMaps with one vertex,
basically implements Nielsen's method (see Lyndon-Schupp, ch. I.2)
	 */
	private boolean invertRose() {
		boolean progress;
		int i,j;
		GraphMap g;
		String tmp;

		g=new GraphMap(this);
		g.identity();

		// reduce lengths by moves of type T2 according to Lyndon-Schupp, p. 5
		do {
			progress=false;
			for(i=0;i<getEdges();i++)
				for(j=0;j<getEdges();j++) {
					if (i==j)
						continue;

					if (lessThan(Word.tightenWord(
							getIm(i)+getIm(j)),getIm(i))) {
						setImage(i,
								Word.tightenWord(getIm(i)+
										getIm(j)));
						g.setImage(i,Word.tightenWord(
								g.getIm(i)+g.getIm(j)));
						progress=true;
						continue;
					}

					if (lessThan(Word.tightenWord(getIm(i)+
							Word.reverseWord(getIm(j))),
							getIm(i))) {
						setImage(i,
								Word.tightenWord(getIm(i)+
										Word.reverseWord(getIm(j))));
						g.setImage(i,
								Word.tightenWord(g.getIm(i)+
										Word.reverseWord(g.getIm(j))));
						progress=true;
						continue;
					}

					if (lessThan(Word.tightenWord(
							Word.reverseWord(getIm(i))+
							Word.reverseWord(getIm(j))),
							getIm(i))) {
						setImage(i,
								Word.tightenWord(
										Word.reverseWord(getIm(i))+
										Word.reverseWord(getIm(j))));
						g.setImage(i,Word.tightenWord(
								Word.reverseWord(g.getIm(i))+
								Word.reverseWord(g.getIm(j))));
						progress=true;
						continue;
					}

					if (lessThan(Word.tightenWord(
							Word.reverseWord(getIm(i))+getIm(j)),
							getIm(i))) {
						setImage(i,
								Word.tightenWord(
										Word.reverseWord(
												getIm(i))+getIm(j)));
						g.setImage(i,Word.tightenWord(
								Word.reverseWord(g.getIm(i))
								+g.getIm(j)));
						progress=true;
						continue;
					}
				}
		} while (progress);

		// if some image has length different from one, then there's no inverse
		for(i=0;i<getEdges();i++)
			if (getIm(i).length()!=1)
				return false;	

		// permute images to make sure that each edge is mapped to itself
		for(i=0;i<getEdges()-1;i++)
			if (Word.charToIndex(getIm(i).charAt(0))!=i) {
				for(j=i+1;(j<getEdges()) &&
				(Word.charToIndex(getIm(j).charAt(0))!=i);j++)
					;	// do nothing

				if (j>=getEdges())
					// did we find the corresponding image?
					return false;	// no: there's no inverse

				tmp=getIm(i);
				setImage(i,getIm(j));
				setImage(j,tmp);

				tmp=g.getIm(i);
				g.setImage(i,g.getIm(j));
				g.setImage(j,tmp);
			}

		// invert edges to make sure that the composition of the two maps is id
		for(i=0;i<getEdges();i++)
			if (Word.isInverse(getIm(i).charAt(0))) {
				setImage(i,Word.reverseWord(getIm(i)));
				g.setImage(i,Word.reverseWord(g.getIm(i)));
			}

		im=g.im;

		return true;
	}


	/**
@return true iff 'this' is an automorphism.
	 */
	public boolean isAutomorphism() {
		GraphMap g=new GraphMap(this);

		return g.invert();
	}


	/**
A main routine, only for testing purposes.
	 */
	public static void main (String s[]) {
		GraphMap g=new GraphMap();

		try{
			if (s.length>0) {
				g.readFromFile(s[0]);
				if (g.getLabel().length()==0)
					g.setLabel(s[0]);
			}
			else
				g.readFromFile("");

		} catch(Exception e) {System.err.println(e.toString());
		return; }

		System.out.println(g.toString());
		System.out.println(g.showVertexMap());
		System.out.println("image of fixed word: "+Word.pathToString(
				Word.tightenCycl(g.mapWord(g.getFix()))));

		System.out.println(g.toLaTeX());
	}

}
