package pbj.math.graph;

import java.io.*;
import java.util.*;

/**
A class that implements Dehn twists.
@author Peter Brinkmann
@see GraphMap
 */
public abstract class DehnTwist {

	/** open bracket */
	public static final char BRA='[';

	/** close bracket */
	public static final char KET=']';

	/** inverse symbol */
	public static final char INV='~';

	private static final char DOT='.';

	/* takes a turn, given by the chars start and end, and finds out which
   part of the boundary string it spans.
	 */
	private static String findintersections(String bd,char start,char end,
			int res[]) {
		int i,j,len;
		String inter;

		len=bd.length();
		// locate start in bd
		for(i=0;bd.charAt(i)!=start;i++)
			;	// do nothing

		// locate end in bd
		for(j=0;bd.charAt(j)!=end;j++)
			;	// do nothing

		// find out which way to go to find a shortest path from start to end
		if (((j-i+len)%len)<=((i-j+len)%len)) {
			res[0]=(j-i+len)%len+1;
			res[1]=1;
			if (!Word.isInverse(start)) i=(i+1)%len;
			if (!Word.isInverse(end)) j=(j+1)%len;
		}
		else {
			res[0]=(i-j+len)%len+1;
			res[1]=-1;
			if (Word.isInverse(start)) i=(i-1+len)%len;
			if (Word.isInverse(end)) j=(j-1+len)%len;
		}

		// compute shortest path
		inter="";
		for(;i!=j;i=(i+res[1]+len)%len) inter=inter+bd.charAt(i)
		;	// do nothing

		return inter;
	}

	private static boolean xor(boolean a,boolean b) {
		if (a && b) return false;
		if (a || b) return true;
		return false;
	}

	/* checks whether a twist is admissible (i.e., doesn't contain the same turn
   twice), throws an exception if it is not admissible */
	private static void checkTwist(int n,String tw) {
		int i,j,k,l;				// just some indices
		char t1,t2;				// storage for one turn
		boolean flag[][]=new boolean[2*n][2*n];	// flags for twists

		// init flags
		for(i=0;i<2*n;i++)
			for(j=0;j<2*n;j++)
				flag[i][j]=false;

		// checks twists
		for(i=0;i<tw.length();i++) {
			t1=Word.inverse(tw.charAt(i));	// first edge of turn
			t2=tw.charAt((i+1) % tw.length());// second edge of turn

			k=Word.charToIndex(t1);		// compute index of first edge
			if (Word.isInverse(t1))
				k+=n;

			l=Word.charToIndex(t2);		// compute index of second edge
			if (Word.isInverse(t2))
				l+=n;

			if (flag[k][l])			// did this turn occur before?
				throw new RuntimeException(Word.pathToString(t1+""+t2));

			flag[k][l]=true;		// mark turn as visited
			flag[l][k]=true;
		}
	}

	/* writes the effect of a Dehn twist to g */
	private static void twist(GraphMap g,String bd,String twist,boolean inv) {
		String inter[]=new String[twist.length()];	// boundary arcs given
		// by turns of twist
		String ctw[]=new String[twist.length()];	// cyclic conjugates of
		// twisting curves
		String ctwi[]=new String[twist.length()];	// cyclic conjugates of
		// inverses of curves
		int l[]=new int[twist.length()];		// length of bd arc
		int di[]=new int[twist.length()];		// direction of bd arc
		int ind[]=new int[twist.length()];		// for sorting...
		int res[]=new int[2];				// for temporary storage
		int i,j,tlen,h,e;				// some counters & stuff

		// make sure the twist is admissible
		checkTwist(g.getEdges(),twist);

		// generate boundary arcs
		tlen=twist.length();
		for(i=0;i<tlen;i++) {
			for(ctw[i]="",j=0;j<tlen;j++)
				ctw[i]=ctw[i]+twist.charAt((i+j)%tlen);
			ctwi[i]=Word.reverseWord(ctw[i]);
			inter[i]=findintersections(bd,ctwi[i].charAt(0),
					ctw[i].charAt(0),res);
			l[i]=res[0];
			di[i]=res[1];
			ind[i]=i;
		}

		// sort boundary arcs according to length
		for(i=0;i<tlen-1;i++)
			for(j=i+1;j<tlen;j++)
				if (l[ind[i]]>l[ind[j]]) {
					h=ind[i];
					ind[i]=ind[j];
					ind[j]=h;
				}

		// compute images
		for(i=0;i<tlen;i++)
			for(j=0;j<inter[ind[i]].length();j++) {
				e=Word.charToIndex(inter[ind[i]].charAt(j));
				if (Word.isInverse(inter[ind[i]].charAt(j)))
					if (xor(inv,di[ind[i]]>0))
						g.setImage(e,g.getIm(e)+ctw[ind[i]]);
					else
						g.setImage(e,g.getIm(e)+ctwi[ind[i]]);
				else {
					if (xor(inv,di[ind[i]]>0))
						g.setImage(e,ctwi[ind[i]]+g.getIm(e));
					else
						g.setImage(e,ctw[ind[i]]+g.getIm(e));
				}
			}
		g.tighten();
	}

	/*
Extracts relations from bd and minimizes the number of generators using
Tietze transformations.
	 */
	private static void eliminateGenerators(GraphMap g,String bd) {
		// extract relations from boundary word
		List<String> rels=Word.relations(bd);

		if (rels.size()>1) {	// more than one relation?
			int i;
			String rel[]=new String[rels.size()-1];

			for(i=1;i<rels.size();i++) //extract relations (except fixed wd)
				rel[i-1]=rels.get(i);

			// now eliminate generators using the relations
			g.tietzeTrafos(rel);
		}
	}

	private static GraphMap twist(String bd,String twists,String lab,boolean fix) {
		twists = normalize(twists);
		int i,j,k,l;
		boolean inv;
		GraphMap g,gg;
		String bd_int;			      // boundary word in internal rep
		String tw,raw_tw;		      // invidivual twist
		String ma[];			      // list of loops in marking
		String ml[];			      // labels of loops in marking
		int nm;
		boolean flag;

		// extra variables needed if fix==true
		String sp="";		// spanning tree as a list of letters
		boolean spt[]=null;	// spanning tree
		String path[][]=null;	// paths in spanning tree
		String bd_red="";	// reduced boundary (without edges in spt)
		String im_old,im_new;	// temp storage for images of edges

		// is bd a boundary word?
		if (!fix) {	// yes
			bd_int=Word.stringToPath(bd);
			g=GraphMap.identity(bd_int);
		}
		else {		// no, it is a fixed word
			// did the user specify a spanning tree?
			if (bd.indexOf(DOT)<0)	// no
				bd_int=Word.stringToPath(bd);
			else {	// yes
				// extract fixed word
				bd_int=Word.stringToPath(
						bd.substring(0,bd.indexOf(DOT)));
				// extract spanning tree
				sp=Word.stringToPath(
						bd.substring(bd.indexOf(DOT)+1));
			}

			g=GraphMap.identityFromFixed(bd_int);
			spt=new boolean[g.getEdges()];
			path=new String[g.getVertices()][g.getVertices()];

			// find spanning tree
			if (sp.length()==0)
				g.spanningTree(spt,path,null);
			else
				g.evaluateTree(spt,path,sp);

			// compute boundary word with respect to spanning tree
			bd_red=bd_int;
			for(i=0;i<g.getEdges();i++)
				if (spt[i]) {
					bd_red=Word.removeEdge(bd_red, i);
				}
			bd_red=Word.dualize(bd_red);
		}

		g.setLabel(lab);

		nm=0;
		ma=new String[twists.length()];
		ml=new String[twists.length()];

		for(i=0;i<twists.length();i++) {
			// determine sign of current twist
			for(inv=false;twists.charAt(i)==INV;i++,inv=!inv)
				;	// do nothing

			// extract current twist from twists
			if (twists.charAt(i)==BRA) {
				j=i+1;
				for(i++;twists.charAt(i)!=KET;i++)
					;	// do nothing

				tw=twists.substring(j,i);
			}
			else
				tw=twists.charAt(i)+"";

			// convert current twist into internal format and tighten
			tw=Word.stringToPath(tw);
			tw=Word.tightenWord(tw);
			tw=Word.tightenCycl(tw);

			// make sure the twist is OK
			if (!g.isGoodLoop(tw))
				throw new RuntimeException("bad loop: "
						+Word.pathToString(tw));

			// check whether this twist occurred before
			flag=false;
			for(k=0;k<nm;k++)
				flag=flag || (tw.equals(ma[k]));

			// if it didn't occur before, add it to the marking
			if (!flag) {
				ml[nm]="C"+nm;
				ma[nm]=tw;
				nm++;
			}

			// evaluate current twist and compose with intermediate result
			// is bd a boundary word?
			if (!fix) {	// yes
				gg=GraphMap.identity(bd_int);
				raw_tw=Word.pathToString(tw);

				try {
					twist(gg,bd_int,tw,inv);
				} catch(RuntimeException e) {
					throw new RuntimeException("bad twist: "
							+Word.pathToString(tw)+"\nturn "
							+e.getMessage()+" occurs twice");
				}
			}
			else {		// no, it is a fixed word
				gg=GraphMap.identityFromFixed(bd_int);

				// compute twist with respect to boundary word
				raw_tw=Word.pathToString(tw);
				for(k=0;k<g.getEdges();k++)
					if (spt[k]) {
						tw=Word.removeEdge(tw, k);
					}

				// now, evaluate twist, skipping edges in spanning tree
				try {
					twist(gg,bd_red,tw,inv);
				} catch(RuntimeException e) {
					throw new RuntimeException("bad twist: "
							+raw_tw+"\nturn "+e.getMessage()
							+" occurs twice");
				}

				// turn gg into a continuous map by filling in
				// paths in the spanning tree
				for(k=0;k<gg.getEdges();k++)
					if (!spt[k]) {
						im_old=gg.getIm(k);

						im_new=path[gg.getStart(k)]
						            [gg.firstVertex(
						            		im_old.charAt(0))]
						            		+im_old.charAt(0);

						for(l=1;l<im_old.length();l++)
							im_new+=path[
							             gg.lastVertex(
							            		 im_new.charAt(
							            				 im_new.length()-1))]
							            				 [gg.firstVertex(
							            						 im_old.charAt(l))]
							            						 +im_old.charAt(l);

						im_new+=path[gg.lastVertex(
								im_new.charAt(
										im_new.length()-1))]
										[gg.getEnd(k)];

						gg.setImage(k,im_new);
					}
				gg.tighten();
			}

			if (!gg.isGoodMap())
				throw new RuntimeException("bad twist: "+raw_tw);

			g.compose(gg);
		}

		g.setMarking(nm,ml,ma);

		if (!fix)
			// get rid of unnecessary generators if necessary
			eliminateGenerators(g,bd_int);

		if (!g.isGoodMap())
			throw new RuntimeException("exception. please save your input "
					+"and email it to brinkman@math.utah.edu");

		return g;
	}

	/**
Computes Dehn twists with respect to a sequence of loops specified with respect
to the boundary word.
@param String bd: boundary word (input/output format)
@param String twists: sequence of loops (input/output format)
@param String lab: label
@return A GraphMap representing a homeomorphism of a surface given by bd and twists.
	 */
	public static GraphMap twist(String bd,String twists,String lab) {
		return twist(bd,twists,lab,false);
	}

	/**
Computes Dehn twists with respect to a sequence of loops specified with respect
to the fixed word (and possibly a given spanning tree).
@param String fx: fixed word (input/output format) plus possibly a spanning tree. Spanning trees are indicated by adding '.' and a list of labels representing the edges in the spanning tree. Example: abcABC specifies a fixed word, and abcABC.c specifies a fixed word and the spanning tree consisting of the edge c.
@param String twists: sequence of loops (input/output format)
@param String lab: label
@return A GraphMap representing a homeomorphism of a surface given by fx and twists.
	 */
	public static GraphMap twistWithFixedWord(String fx,String twists,String lab) {
		return twist(fx,twists,lab,true);
	}

	/**
Computes composition of standard Dehn twists.
@param int genus: genus of the surface
@param String twists: sequence of standard Dehn twists
@param String lab: Label
@return A GraphMap representing a homeomorphism of a surface given by
the genus and a sequence of standard Dehn twists.
	 */
	public static GraphMap stdGenerators(int genus,String twists,String lab) {
		GraphMap g;
		String tw[][]=new String[4][genus];		// loops for std twists
		boolean flag[][]=new boolean[4][genus];	// marks twists in use
		String tt="";
		String bd="";
		int i,i0,j,k;
		String ma[];			// loops in marking
		String ml[];			// labels of loops in marking
		int nm;				// number of loops in marking

		if (genus<1)
			throw new RuntimeException("genus too small");

		// init twisting loops and boundary word
		for(i=0;i<genus;i++) {
			tw[2][i]=""+Word.indexToChar(2*i+1,false);
			tw[1][i]=""+Word.indexToChar(2*i+1,false);
			tw[0][i]=""+Word.indexToChar(2*i+1,false);

			for(j=i+1;j<genus;j++)
				tw[1][i]=tw[1][i]+Word.indexToChar(2*j,false)+
				Word.indexToChar(2*j+1,true)+
				Word.indexToChar(2*j,true)+
				Word.indexToChar(2*j+1,false);
			if (i<genus-1)
				tw[2][i]=tw[2][i]+Word.indexToChar(2*i+2,false)+
				Word.indexToChar(2*i+3,true)+
				Word.indexToChar(2*i+2,true);

			tw[3][i]=""+Word.indexToChar(2*i,false);

			bd=bd+Word.indexToChar(2*i,false)+
			Word.indexToChar(2*i+1,false)+
			Word.indexToChar(2*i,true)+
			Word.indexToChar(2*i+1,true);

		}

		// init flags
		for(i=0;i<4;i++)
			for(j=0;j<genus;j++)
				flag[i][j]=false;

		// init marking
		ma=new String[twists.length()];
		ml=new String[twists.length()];
		nm=0;

		// create sequence of twists
		for(i=0;i<twists.length();) {
			if (twists.charAt(i)<'a')
				tt=tt+INV;
			tt=tt+BRA;
			j=(twists.charAt(i)-1) & 31;
			if (j>3)
				throw new RuntimeException("bad curve label");

			i++;
			for(i0=i;(i<twists.length()) && (twists.charAt(i)<='9');i++)
				;	// do nothing

			k=Integer.parseInt(twists.substring(i0,i));
			if (k>=genus)
				throw new RuntimeException("bad curve index");

			tt=tt+Word.pathToString(tw[j][k])+KET;

			if (!flag[j][k]) {
				ml[nm]=Word.charToLabel(Word.indexToChar(j,false))
				+(k+"");
				ma[nm]=tw[j][k];
				nm++;
				flag[j][k]=true;
			}
		}

		// evaluate twists (with respect to boundary word)
		g=twist(Word.pathToString(bd),tt,lab);
		g.setMarking(nm,ml,ma);

		if (lab.equals(""))
			g.setLabel(twists);

		return g;
	}

	/**
<p>The main routine. Recommended usage for Unix systems:
Add the line</p>
<code> alias jtwist "java pbj.math.graph.DehnTwist" </code>
to your .cshrc (assuming you're using csh).</p>
<p>Usage: <kbd>jtwist [parameters]</kbd><br>
where <code>[parameters]</code> are the parameters of the old programs gen
and twist.  jtwist will determine from the input which of the two is needed.
Click <a href="doc.ps">here</a> for the documentation of the programs gen
and twist.</p>
	 */
	public static void main(String s[]) throws FileNotFoundException, IOException {
		GraphMap g;
		String a,lab,inp,tw;
		int genus;
		StreamTokenizer st;

		try {
			if (s.length==1) {
				st=new StreamTokenizer(new FileReader(s[0]));
				lab=s[0];
			}
			else if (s.length==0) {
				st=new StreamTokenizer(new InputStreamReader(System.in));
				lab="";
			}
			else {
				st=new StreamTokenizer(new StringReader(s[0]+" "+s[1]));
				lab="";
			}

			inp="// input: ";
			if (st.nextToken()==StreamTokenizer.TT_NUMBER) {
				genus=(int) st.nval;
				st.nextToken();
				tw=st.sval;
				if (tw==null)
					throw new RuntimeException("bad sequence of twists");

				st.nextToken();
				if (st.sval!=null)
					lab=st.sval;

				g=stdGenerators(genus,tw,lab);
				inp=inp+genus+" "+tw+" "+lab;
			}
			else {
				a=st.sval;
				if (a==null)
					throw new RuntimeException("bad boundary word");

				st.nextToken();
				tw=st.sval;
				if (tw==null)
					throw new RuntimeException("bad sequence of twists."+
							"\nDid you use double quotes "+
					"(e.g., \"\'-c(bD)aab\'\")?");

				st.nextToken();
				if (st.sval!=null)
					lab=st.sval;

				if (a.startsWith("fix.")) {
					g=twistWithFixedWord(a.substring(4),tw,lab);
				}
				else
					g=twist(a,tw,lab);

				inp=inp+a+" "+tw+" "+lab;
			}
		} catch (Exception e) {System.err.println(e.toString());
		return;}

		System.out.println(inp);
		System.out.println("");
		System.out.println(g.toString());
	}

	public static String normalize(String twists) {
		return twists.replace('(', BRA).replace(')', KET).replace('-', INV);
	}
}

