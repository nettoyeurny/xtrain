package pbj.math.graph.train;

import pbj.math.graph.*;
import pbj.math.numerical.*;
import gnu.getopt.*;
import java.util.*;

/**
An extension of GraphMap --- the heart and soul of the package. It notifies
observers of changes in the train track by sending them objects of type
Integer indicating the state of the train track, or an object of type String
with an error message in the unlikely case of an exception.
@author Peter Brinkmann
@see GraphMap
 */
public class TrainTrack extends GraphMap implements Observer, Runnable{
	
	private static final long serialVersionUID = 1L;

	private transient IntMatrix m;		// transition matrix
	private boolean STEP=false;
	private boolean TDEBUG=false;

	/**
Integer value indicating the beginning of a new computation.
	 */
	public static final int NEW_COMP=1;

	/**
Integer value indicating a reduction of the PF-eigenvalue.
	 */
	public static final int PROGRESS=2;

	/**
Integer value indicating successful completion of the train track algorithm.
	 */
	public static final int SUCCESS=3;

	/**
Integer value indicating failure of the train track algorithm, due to
reducible input.
	 */
	public static final int FAILURE=4;

	/**
Integer value indicating that the thread was stopped.
	 */
	public static final int STOPPED=5;

	/**
Integer value indicating that the TrainTrack has changed.
	 */
	public static final int CHANGE=6;

	public TrainTrack() {
		super();
	};

	/* a little helper class for growing char arrays, pretty much
   self-explanatory. */
	private class GrowingCharArray {
		char v[];
		int cnt;

		GrowingCharArray(int n) {
			if (n<1)
				throw new RuntimeException(
						"number of entries too small: "+n);

			v=new char[n];
			cnt=0;
		}

		void setValue(int i,char c) {
			int j;
			char newv[];

			if (i>=v.length) {
				newv=new char[2*i];
				for(j=0;j<v.length;j++)
					newv[j]=v[j];
				v=newv;
			}

			if (i>=cnt)
				cnt=i+1;

			v[i]=c;
		}

		char getValue(int i) {
			if (i>=cnt)
				throw new RuntimeException("GrowingCharArray."+
						"getValue: index too large: "+i);

			return v[i];
		}

		int size() {
			return cnt;
		}
	}

	/* just a little helper class for growing boolean arrays, pretty much
   self-explanatory. Reimplementation was necessary because behavior
   slightly differs from GrowingCharArray. */
	private class GrowingBoolArray {
		boolean v[];
		int cnt;

		GrowingBoolArray(int n) {
			int i;
			if (n<1)
				throw new RuntimeException(
						"number of entries too small: "+n);

			v=new boolean[n];
			for(i=0;i<v.length;i++) v[i]=false;
			cnt=0;
		}

		void setValue(int i,boolean c) {
			int j;
			boolean newv[];

			if (i>=v.length) {
				newv=new boolean[2*i];
				for(j=0;j<newv.length;j++) newv[j]=false;

				for(j=0;j<v.length;j++)
					newv[j]=v[j];
				v=newv;
			}

			if (i>=cnt)
				cnt=i+1;

			v[i]=c;
		}

		boolean getValue(int i) {
			if (i>=cnt) 	// no error message here:
				return false; // out of bounds means false...

			return v[i];
		}

		int size() {
			return cnt;
		}
	}


	/**
Constructs a new TrainTrack object (almost) identical to g.
@param GraphMap g: GraphMap to be copied.
	 */
	public TrainTrack(GraphMap g) {
		super(g);
	}

	private void updateTransitionMatrix() {
		/* returns the transition matrix of the given topological representative */
		int k,j;

		if ((m==null) || ((m!=null) && (m.a.length<getEdges())))
			m=new IntMatrix(getEdges());

		m.n=getEdges();

		for(j=0;j<m.n;j++)
			for(k=0;k<m.n;k++)
				m.a[j][k]=0;

		for(j=0;j<getEdges();j++)
			for(k=0;k<getIm(j).length();k++)
				m.a[Word.charToIndex(getIm(j).charAt(k))][j]++;
	}

	/**
@return transition matrix of this.
	 */
	public IntMatrix transitionMatrix() {
		updateTransitionMatrix();
		return new IntMatrix(m);
	}

	/**
@return true iff the transition matrix is irreducible.
	 */
	public boolean isIrreducible() {
		updateTransitionMatrix();
		return m.isIrreducible();
	}

	/**
@return Perron-Frobenius eigenvalue of the transition matrix.
	 */
	public double growthRate() {
		updateTransitionMatrix();
		return m.eigenValue();
	}

	private IntVector countValences() {
		/* returns the valence of each vertex in the vector v */
		int i;
		IntVector v=new IntVector(getVertices());

		for(i=0;i<getEdges();i++) {
			v.v[getStart(i)]++;
			v.v[getEnd(i)]++;
		}

		return v;
	}

	/**
Removes a vertex of valence one if there is one.
@return true iff a vertex was removed
	 */
	public boolean v1Homotopy() {
		IntVector val;
		int i;

		val=countValences();

		for(i=0;i<getEdges();i++)
			if ((val.v[getStart(i)]==1) || (val.v[getEnd(i)]==1)) {
				collapseEdge(i);
				return true;
			}
		return false;
	}

	private boolean isBigger(int e1,int e2) {
		double v1[];

		updateTransitionMatrix();
		v1=new double[m.n];
		m.eigenPair(v1);

		return (v1[e1]>v1[e2]);
	}

	/**
Performs a valence 2 homotopy if possible
@return true iff a valence 2 homotopy was performed
	 */
	public boolean v2Homotopy() {
		/* performs a valence 2 homotopy if possible. */
		IntVector val;
		int i,j,vt,tmp;
		boolean flag;

		val=countValences();
		flag=false;

		for(i=0;(i<getEdges()) && (!flag);)
			if (((val.v[getStart(i)]==2) || (val.v[getEnd(i)]==2))
					&& (getStart(i)!=getEnd(i)))
				flag=true;		/* checks and valences... */
			else i++;
		if (!flag) return false;   /* No vertices of valence 2: return 0 */

		if (val.v[getStart(i)]==2) vt=getStart(i);
		else vt=getEnd(i);	/* the index of a vertex of valence 2 is in vt*/

		j=i+1;
		while ((getStart(j)!=vt) && (getEnd(j)!=vt)) j++;
		/* find the adjacent edge */
		if (isBigger(i,j))
		{	tmp=i;
		i=j;
		j=tmp;
		}
		joinEdges(i,j);	/* and join the edges */
		return true;
	}

	private void iterate(boolean subgr[],int ed) {
		/* recursive slave for genInvSub() */
		int i;

		if (!subgr[ed]) {	/* has e been checked yet? */
			subgr[ed]=true;/* If not, add e to the subgraph and go through
				   the image of e recursively */
			for(i=0;i<getIm(ed).length();i++)
				iterate(subgr,Word.charToIndex(getIm(ed).charAt(i)));
		}
	}

	private void genInvSub(boolean subgr[],int ed) {
		/* generates an invariant subgraph, starting with the edge with index ed. */
		int i;

		for(i=0;i<getEdges();i++) subgr[i]=false;

		iterate(subgr,ed);
	}

	private static void explore(IntVector val,
			IntMatrix ad,boolean subvt[],int vt) {
		/* recursive slave for findConnectedComponent() */
		int i;

		if (val.v[vt]==0)
			for(i=0;i<val.n;i++)
				if (subvt[i] && (ad.a[i][vt]!=0)) {
					val.v[vt]+=ad.a[i][vt];
					explore(val,ad,subvt,i);
				}
	}

	private void findConnectedComp(IntVector val,IntMatrix ad,boolean subvt[]) {
		/* finds a connected component of the subgraph and computes the valences of the
   vertices in this subgraph at the same time. */
		int i;

		for(i=0;(!subvt[i]);i++);
		explore(val,ad,subvt,i);
	}

	private static boolean notEmpty(boolean v[]) {
		int i;

		for(i=0;i<v.length;i++) if (v[i]) return true;
		return false;
	}

	private boolean isForest(boolean s[]) {
		/* checks whether a given subgraph is a forest */

		IntMatrix adj=new IntMatrix(getVertices());
		IntVector conval=new IntVector(getVertices());
		boolean subvt[]=new boolean[getVertices()];
		int i;

		for(i=0;i<getVertices();i++) subvt[i]=false;
		for(i=0;i<getEdges();i++) 
			if (s[i]) {
				subvt[getStart(i)]=true;
				subvt[getEnd(i)]=true;
			}
		/* now subvt.v[i] is nonzero iff the vertex i belong to the subgraph.*/

		for(i=0;i<getEdges();i++)
			if (s[i]) {
				adj.a[getStart(i)][getEnd(i)]++;
				adj.a[getEnd(i)][getStart(i)]++;
			}
		/* adj is the adjacency matrix of the subgraph. */

		while (notEmpty(subvt)) {
			/* this loop checks connected components of the subgraph for cycles.*/
			findConnectedComp(conval,adj,subvt);
			for(i=0;i<subvt.length;i++)
				if (conval.v[i]!=0) subvt[i]=false;
			/* conval contains the valences of a connected component. */
			if (conval.sumOfEntries()>=conval.countVectorEntries()*2)
				/* A little bit of topology: A connected graph has a cycle iff
		   the sum of all valences is greater or equal the number of
		   vertices times two. */
				return false;
		}
		return true;
	}

	private boolean collapseInvForest() {
		/* collapses an invariant forest if necessary. */
		boolean invsub[]=new boolean[getEdges()];
		boolean flag;
		int i,j;

		i=0;
		do	/* generate invariant subgraphs, starting with one edge, the
		   check if this subgraph is a forest. */
		{	genInvSub(invsub,i);
		if (TDEBUG) {
			System.err.println(
			"---------------\ninvariant subgraph:");
			System.err.println(toString());
			for(j=0;j<getEdges();j++)
				if (invsub[j])
					System.err.print(
							Word.indexToChar(j,false)+" ");
		}

		i++;
		flag=isForest(invsub);
		if (TDEBUG)
			if (flag)
				System.err.println("This is a forest!");
		}
		while ((!flag) && (i<getEdges()));

		if (flag) 	/* We have found an invariant forest? */
			for(i=getEdges()-1;i>=0;i--)	/* Great, remove it! */
				if (invsub[i])
					collapseEdge(i);

		return flag;
	}

	int charToCoord(char c) {
		if (Word.isInverse(c))
			return getEdges()+Word.charToIndex(c);
		else
			return Word.charToIndex(c);
	}

	private boolean tightenPlus() {
		boolean flag;

		flag=false;
		while (tightenVertex()) flag=true;

		return (flag || tighten());
	}

	private int isIllegalRec(boolean legal[][],char a,char b,int depth) {
		/* iterates the map T (cf. "Train tracks...") recursively and marks all turns
   legal until it reaches an illegal turn or a turn that has been checked
   before. One of these possibilities will occur eventually, so the algorithm
   terminates. By the way, illegal terms are marked legal as well, but that
   doesn't matter because the matrix is thrown away as soon an illegal turn
   has been identified. */
		if (a==b) return depth;
		else
			try {
				if (legal[charToCoord(a)][charToCoord(b)])
					return 0;
				else {
					legal[charToCoord(a)][charToCoord(b)]=true;
					legal[charToCoord(b)][charToCoord(a)]=true;
					return isIllegalRec(
							legal,mapd(a),mapd(b),depth+1);
				}
			}
		catch (Throwable e) {
			return 0;
		}
	}

	/**
Checks whether a turn is illegal.
@param char a: first edge in turn
@param char b: second edge in turn
@return number of iterations it takes to find identical initial segments of the image.
	 */
	public int isIllegal(char a,char b) {
		boolean legal[][]=new boolean[2*getEdges()][2*getEdges()];
		int i,j;

		for(i=0;i<2*getEdges();i++)
			for(j=0;j<2*getEdges();j++)
				legal[i][j]=false;

		return isIllegalRec(legal,a,b,1);
	}

	/**
Looks for an illegal turn in the images of edges. If there is
a choice, this function will pick an illegal turn such that the number of
iterates before cancellation is minimal.
@param int res[]: array of size 2 for results. If an illegal turn is found, res[0] will be the index of the edge whose image contains the illegal turn, and res[1] will be the the position of the illegal turn in the image of the edge.
@return true iff an illegal turn was found
	 */
	public boolean findIllegalTurn(int res[]) {
		int i,j,d1,dd;
		boolean flag=false;

		dd=8*getEdges()*getEdges();
		for(i=0;i<getEdges();i++)
			for(j=0;j<(getIm(i).length()-1);j++) {
				d1=isIllegal(Word.inverse(getIm(i).charAt(j)),
						getIm(i).charAt(j+1));
				if ((d1>0) && (d1<dd)) {
					res[0]=i;	
					res[1]=j;
					dd=d1;
					flag=true;
				}
			}
		return flag;
	}

	/**
@return true iff this is a train track map
	 */
	public boolean isTrainTrack() {
		try {
			int res[]=new int[2];
			return !findIllegalTurn(res);
		}
		catch (Throwable e) {
			return false;
		}
	}

	private void updateInv(GrowingCharArray al,int old,int nw) {
		/* This function is called when an edge was split. The last part of the edge
   has got a new index, so all the inverse pointers to this edge must be
   updated. */
		int i;
		for(i=0;i<al.size();i++)
			if ((Word.charToIndex(al.getValue(i))==old)
					&& (Word.isInverse(al.getValue(i))))
				al.setValue(i,Word.indexToChar(nw,true));
	}

	private void updateAll(GrowingCharArray al,int old,int nw) {
		/* This function is called when a complete edge moved to a new position, all
   the pointers must be updated. */
		int i;
		for(i=0;i<al.size();i++)
			if (Word.charToIndex(al.getValue(i))==old)
				al.setValue(i,Word.indexToChar(nw,
						Word.isInverse(al.getValue(i))));
	}

	private void reverseList(GrowingCharArray al,int ed) {
		/* If an edge was inverted, all the indices pointing to it must be inverted
   as well. This function does it. */
		int i;
		for(i=0;i<al.size();i++)
			if (Word.charToIndex(al.getValue(i))==ed)
				al.setValue(i,Word.inverse(al.getValue(i)));
	}

	private void subSplitRec(char a,GrowingCharArray al,
			GrowingCharArray sl,int depth) {
		/* exception handling for the special case mentioned in the last paragraph
   on p. 7 of [HB2]. */
		int ed;

		sl.setValue(depth,a);
		ed=Word.charToIndex(a);

		if (getIm(ed).length()<=1)
			subSplitRec(mapd(a),al,sl,depth+1);

		a=sl.getValue(depth);
		ed=Word.charToIndex(a);
		updateInv(al,ed,getEdges());
		updateInv(sl,ed,getEdges());
		if (Word.isInverse(a))
			splitEdge(ed,1);
		else
			splitEdge(ed,getIm(ed).length()-1);
	}

	private int splitList(GrowingBoolArray fld,int len,GrowingCharArray al) {
		/* splits all the edges whose entry in the vector fld is nonzero such that
   the remaining length of the image is len. */
		int i;
		for(i=0;i<getEdges();i++)
			if ((fld.getValue(i)) && (getIm(i).length()>len)) {
				updateInv(al,i,getEdges());
				splitEdge(i,len);
				len=getIm(i).length();
			}
		return len;
	}

	private boolean splitAndFoldRecursively(char a,char b,GrowingCharArray al,
			int depth) {
		/* climbs down to the illegal turn, then it splits the preimage of the illegal
   turn and folds it until the resulting map is NOT locally injective (on the
   interior of edges), then the map is tightened. The return value 1 indicates
   that tightening has occurred, return value false means that splitting
   and folding must continue. */
		int i,j,e1,len;
		GrowingBoolArray fld=new GrowingBoolArray(4*getEdges());
		GrowingCharArray sl=new GrowingCharArray(4*getEdges());
		boolean fff;

		al.setValue(depth,a);
		/* Copy the current turn to the list of pointers. */

		if (a==b) {
			if (TDEBUG)
				for(i=0;i<=depth;i++)
					System.err.println(al.getValue(i));
			return false;	/* if a==b we have reached the illegal turn */
		}

		if (!splitAndFoldRecursively(mapd(a),mapd(b),al,depth+1)) {
			/* recursive call, split and fold if return value is 0 */
			a=al.getValue(depth);/* find out where a and b have gone... */
			if (TDEBUG)
				System.err.println("--"+a+"--");

			e1=Word.charToIndex(a);
			if (Word.isInverse(a)) {	/* reorient a if necessary */
				reverseList(al,e1);
				reverseEdge(e1);
				a=Word.inverse(a);
			}

			/* Now we determine all the edges which are to be folded and
		   find the length of the part of the image word that the
		   folding candidates have in common. */
			len=getIm(e1).length();
			for(i=0;i<getEdges();i++) {
				/* Must we fold the beginning of the edge no. i? */
				if ((getStart(i)==getStart(e1)) && (0<getIm(i).length())
						&& (getIm(i).charAt(0)==mapd(a))) {
					if (i<e1) {	/* We want e1 to be minimal */
						e1=i;
						a=Word.indexToChar(e1,false);
					}
					fld.setValue(i,true);

					if ((getEnd(i)==getStart(e1)) &&
							(mapd(Word.indexToChar(i,true))==mapd(a))){
						/* Must we also fold the end of no. i? */
						updateInv(al,i,getEdges());
						splitEdge(i,getIm(i).length()/2);
					}

					/* now figure out how many letters the images
				   have in common */
					for(j=0;((j<getIm(i).length()) &&
							(j<getIm(e1).length()) &&
							(getIm(i).charAt(j)==getIm(e1).charAt(j)));
					j++);
					if (j<len) len=j;
				}
				/* a very similar procedure in case the end of the
			   edge no. i must be folded */
				else if ((getEnd(i)==getStart(e1)) &&
						(mapd(Word.indexToChar(i,true))==mapd(a))) {
					if (i<e1) {
						e1=i;
						a=Word.indexToChar(e1,false);
					}
					fld.setValue(i,true);
					reverseEdge(i);		/* reverse edge i */
					reverseList(al,i);
					for(j=0;((j<getIm(i).length()) &&
							(j<getIm(e1).length()) &&
							(getIm(i).charAt(j)==getIm(e1).charAt(j)));
					j++);
					if (j<len) len=j;
				}
			}

			/* Make sure we don't increase the valence of the vertex 0, cf.
		   the last paragraph on p. 7 of [HB2]. */
			for(i=0;i<getEdges();i++)
				if ((fld.getValue(i)) && (0==getEnd(i)) &&
						(getIm(i).length()<=len))
					if (len>1) {
						len--;
						break;
					}
					else {
						subSplitRec(getIm(e1).charAt(0),al,sl,0);
						len=getIm(i).length()-1;
						break;
					}
			splitList(fld,len,al);	/* Now split the candidates */

			/* Fold 'em and take care of the bookkeeping */
			for(i=e1+1;i<getEdges();)
				if (fld.getValue(i)) {
					elementaryFold(e1,i);
					updateAll(al,i,e1);
					updateAll(al,getEdges(),i);
					fld.setValue(i,fld.getValue(getEdges()));
				}
				else i++;

			fff=tightenPlus();

			setChanged();
			notifyObservers(new Integer(CHANGE));

			return fff;
		}
		else	/* tightening has occured, nothing left to do here */
			return true;
	}

	private void splitAndFold(char a,char b) {
		/* does pretty much what the name suggests. */
		GrowingCharArray al=new GrowingCharArray(4*getEdges());
		/* The vector al keeps track of the edges of the graph. Folding
	   reduces the number of edges, and since other edges might be moved
	   around in this process, we need to know where to find an edge that
	   has left its original position. The vector al points to the
	   new location. */
		splitAndFoldRecursively(a,b,al,0);
	}

	/**
removes all vertices of valence one or two and all invariant forests that
might be left after a sequence of folding operations.
	 */
	public void cleanItUp() {
		while (v1Homotopy());
		do {
			tightenPlus();
			while (collapseInvForest()) tightenPlus();
		} while (v2Homotopy());
	}

	/**
Takes a map and transforms it into train track form if this is possible, 
otherwise the result is a map with reducible transition matrix.
@return true iff the resulting map is a train track map
	 */
	public boolean trainTrackMap() {
		int il[]=new int[2];
		int a,b;
		boolean flag;

		setChanged();
		notifyObservers(new Integer(CHANGE));

		cleanItUp();
		updateTransitionMatrix();
		setChanged();
		notifyObservers(new Integer(CHANGE));
		setChanged();
		notifyObservers(new Integer(NEW_COMP));

		try {
			while ((flag=findIllegalTurn(il)) && (m.isIrreducible())) {
				/* If there is an illegal turn, a will be the index of the edge whose
	   image contains the turn, b is the position of the illegal turn in
	   the image of the edge a. */
				a=il[0];
				b=il[1];
				splitEdge(a,b+1);	/* split at the illegal turn and */

				reverseEdge(a);
				/* turn edge a. Now the FIRST element in the image of a is one
		   half of the illegal turn. This little trick makes the rest
		   of the algorithm look symmetric. */

				swapVertices(getStart(a),0);
				/* The new vertex is labelled 0 now, so it won't be relabelled
		   in the folding process. Thus we can make sure that the
		   valence of the new vertex is not increased by folding. */

				splitAndFold(Word.indexToChar(a,false),
						Word.indexToChar(getEdges()-1,false));
				/* now split edges and fold them until tightening occurs. */

				cleanItUp();	// turn the map into a top representative
				updateTransitionMatrix();

				setChanged();
				notifyObservers(new Integer(PROGRESS));
				System.gc();
			}
		} catch (Throwable ex) {
			setChanged();
			notifyObservers(ex.toString());
			return false;
		}

		if (!isGoodMap()) {
			setChanged();
			notifyObservers("graph in inconsistent state.\n"
					+"please email your input to brinkman@math.utah.edu");
			return false;
		}

		setChanged();
		if (!flag)
			notifyObservers(new Integer(SUCCESS));
		else
			notifyObservers(new Integer(FAILURE));

		return (!flag);
	}

	/**
update routine for interface "Observer", only called from the main routine.
	 */
	public void update(Observable ob,Object msg) {
		if (ob instanceof TrainTrack) {
			if (msg instanceof String)
				System.err.println("Exception: "+((String) msg));
			else if (msg instanceof Integer)
				switch (((Integer) msg).intValue()) {
				case NEW_COMP: {
					System.err.println(
					"\nNew computation...");
					break;
				}
				case PROGRESS: {
					System.err.println(
							"Current PF-eigenvalue: "
							+ IntMatrix.PFForm.format(
									((TrainTrack) ob).growthRate()));
					break;
				}
				case SUCCESS: {
					System.err.println("Done!\n");
					break;
				}
				case CHANGE: {
					if (STEP)
						System.err.println(
								(TrainTrack) ob+"\n");
					break;
				}
				case FAILURE: {
					System.err.println("Unable to compute "
							+"train track.");
					break;
				}
				}
		}
	}

	/**
Method for interface Runnable.
	 */
	public void run() {
		trainTrackMap();
	}

	/**
Tells observers that the computation has been stopped.
	 */
	public void stop() {
		setChanged();
		notifyObservers(new Integer(STOPPED));
	}

	/**
Sets the flag indicating whether intermediate results (steps) are desired.
@param boolean s: new value of STEP flag
	 */
	public void setStep(boolean s) {
		STEP=s;
	}

	/**
<p>The main routine. Recommended usage for Unix systems:</p>
<p>Add the lines
<code>	alias jtrain "java pbj.math.graph.train.TrainTrack" </code>
to your .cshrc (assuming you're using csh).</p>

<p>You can call the train track
routine by typing
<kbd>	jtrain [-m][-v][-q][-g] [filename] </kbd>
at the command line.</p>

<p>The switch -m enables the marking feature.
If a filename is given, jtrain tries to read a
GraphMap from the file, otherwise it reads from stdin.
The switch -v prompts the program in verbose mode; intermediate results are
being printed to stderr.
The switch -q runs the program in quiet mode, i.e. the current PF-eigenvalue
is not being printed to stderr (note that -q disables -v).
The switch -g prompts the program to print a list of the gates.
The input format is the same as the output format.</p>
	 */
	public static void main(String s[]) {
		TrainTrack tt=new TrainTrack();
		Gates gg;
		int i;
		boolean marked=false;
		boolean quiet=false;
		boolean gates=false;
		boolean step=false;

		Getopt opts=new Getopt("TrainTrack.class",s,"mqgv");
		int c;

		while ((c = opts.getopt()) != -1) {
			switch(c) {
			case 'm':
				marked=true;
				break;
			case 'q':
				quiet=true;
				break;
			case 'g':
				gates=true;
				break;
			case 'v':
				step=true;
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
				tt.readFromFile(s[i]);
				if (tt.getLabel().length()==0)
					tt.setLabel(s[i]);
			}
			else
				tt.readFromFile("");
			if (!marked)
				tt.unmark();

		} catch (Throwable e) {System.err.println(e.toString());
		System.exit(1); }

		if (!quiet)
			tt.addObserver(tt);

		tt.setStep(step);

		if (tt.trainTrackMap()) {
			System.err.println("");
			System.out.println(tt.toString());

			gg=new Gates(tt);
			if (gg.isPseudoAnosov()) {
				System.out.println("// pseudo-Anosov growth rate: "
						+IntMatrix.PFForm.format(tt.growthRate())
						+"\n");
			}
			else
				System.out.println("// non-pseudo-Anosov");

			if (gates) {
				System.out.println("// gates");
				System.out.println(gg.toString());
			}
		}
		else {
			System.out.println("\n"+tt.toString()
					+"\n// map is not irreducible");
		}
		System.out.println(tt.showVertexMap());

	}

}
