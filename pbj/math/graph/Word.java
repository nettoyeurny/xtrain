package pbj.math.graph;

import java.util.ArrayList;
import java.util.List;

/**
A class for some basic String manipulations in graphs. This class defines the
internal representation of edge paths in graphs, as well as the input/output
format for edges. The first 26 edges are labelled by a,...,z (with capital
letters for inverse edges); the remaining edges (if any) are denoted by
z&lt;number&gt; (Z&lt;number&gt; for inverse edges), where &lt;number&gt;
is a nonnegative integer (for example, there might be edge labelled z0,
Z1, z15, Z223, etc.).
@author Peter Brinkmann
 */
public abstract class Word {

	/**
The maximum number of edges in a graph, dictated by the encoding of edges.
It should be large enough for most practical purposes.
	 */
	public static final int MAXINDEX=32767;

	private static final int INVMASK=32768;
	private static final int LOWMASK=32767;
	private static final int PLAINLIM=26;

	/**
@return true iff c is inverse.
	 */
	public static boolean isInverse(char c) {
		return ((((int) c) & INVMASK)>0);
	}

	/**
Takes a character, returns the corresponding index.
@param char c: argument
@return index of edge represented by c
	 */
	public static int charToIndex(char c) {
		return (((int) c) & LOWMASK);
	}

	/**
Inverts a character.
@param char c: characted to be inverted
@return opposite character
	 */
	public static char inverse(char c) {
		return (char) (((int) c) ^ INVMASK);
	}

	/**
Turns indices into characters.
@param int ind: index
@param boolean inv: indicates wether an inverse char is desired
@return noninverse character for the index ind if inv==false, the inverse char otherwise.
	 */
	public static char indexToChar(int ind,boolean inv) {
		return (char) (inv ? ind+INVMASK : ind);
	}

	/**
Turns indices into labels.
@param int ind: index
@param boolean inv: indicates wether an inverse char is desired
@return the lower case label for the index ind if inv==false,
the upper case label otherwise.
	 */
	public static String indexToLabel(int ind,boolean inv) {
		String res="";
		if (!inv)
			if (ind<PLAINLIM)
				res=((char) (((int) 'a')+ind))+"";
			else
				res='z'+""+(ind-PLAINLIM);
		else
			if (ind<PLAINLIM)
				res=((char) (((int) 'A')+ind))+"";
			else
				res='Z'+""+(ind-PLAINLIM);

		return res;
	}

	/**
Computes LaTeX labels of edges.
@param int ind: index
@param boolean inv: indicates wether an inverse char is desired
@return the lower case label for the index ind if inv==false,
the upper case label otherwise.
	 */
	public static String indexToLaTeXLabel(int ind,boolean inv) {
		String res="";
		if (!inv)
			if (ind<PLAINLIM)
				res=((char) (((int) 'a')+ind))+"";
			else
				res="z_{"+(ind-PLAINLIM)+"}";
		else
			if (ind<PLAINLIM)
				res="{\\bar "+((char) (((int) 'a')+ind))+"}";
			else
				res="{\\bar z}_{"+(ind-PLAINLIM)+"}";

		return res;
	}

	/**
Computes the LaTeX label of a char.
@param char c: represents an (oriented) edge
@return LaTeX label of c
	 */
	public static String charToLaTeXLabel(char c) {
		return indexToLaTeXLabel(charToIndex(c),isInverse(c));
	}

	/**
Turns labels into characters.
@param String lab: label
@return character represented by the label lab.
	 */
	public static char labelToChar(String lab) {
		char c=lab.charAt(0);
		int ind;
		boolean inv;

		inv=(c<'a');
		ind=((int) c-1) & 31;
		if ((ind>=PLAINLIM-1) && (lab.length()>1))
			ind+=Integer.parseInt(lab.substring(1))+1;

		return indexToChar(ind,inv);
	}

	/**
Turns characters into labels.
@param char c: argument
@return label representing the character c.
	 */
	public static String charToLabel(char c) {
		return indexToLabel(charToIndex(c),isInverse(c));
	}

	/**
Converts internal representation into something legible.
@param String path: path in internal representation
@return A String consisting of the labels of the edges in a path.
	 */
	public static String pathToString(String path) {
		int i;
		StringBuilder res=new StringBuilder();

		for(i=0;i<path.length();i++)
			res.append(charToLabel(path.charAt(i)));

		return res.toString();
	}

	/**
Converts internal representation into a LaTeX expression.
@param String path: path in internal representation
@return A String consisting of the LaTeX labels of the edges in a path.
	 */
	public static String pathToLaTeX(String path) {
		int i;
		StringBuilder res=new StringBuilder();

		for(i=0;i<path.length();i++)
			res.append(charToLaTeXLabel(path.charAt(i)));

		return res.toString();
	}

	/**
Turns Strings into paths.
@param String s: argument (input/output format)
@return A path represented as a String (internal representation)
	 */
	public static String stringToPath(String s) {
		StringBuilder res=new StringBuilder();
		String lab;
		int i,j;

		for(i=0;i<s.length();) {
			for(j=i+1;(j<s.length()) && (s.charAt(j)<='9');j++);
			lab=s.substring(i,j);
			res.append(labelToChar(lab));
			i=j;
		}

		return res.toString();
	}

	/**
Computes labels for vertices.
@param int v: index of vertex
@return label of a vertex.
	 */
	public static String vertexToLabel(int v) {
		return "v"+v;
	}

	/**
Computes LaTeX labels for vertices.
@param int v: index of vertex
@return LaTeX label of  a vertex.
	 */
	public static String vertexToLaTeXLabel(int v) {
		return "v_{"+v+"}";
	}

	/**
Turns labels into indices.
@param String l: label
@return index of the vertex defined by l
	 */
	public static int labelToVertex(String l) {
		return Integer.parseInt(l.substring(1));
	}

	/**
Performs elementary cancellations in a word.
@param String s: original word
@return tightened word
	 */
	public static String tightenWord(String s) {
		StringBuilder sb = new StringBuilder(s);
		for(int j=0; j+1<sb.length();) {
			if (inverse(sb.charAt(j))==sb.charAt(j+1)) {
				sb.delete(j, j+2);
				if (j>0) j--;
			}
			else j++;
		}
		return sb.toString();
	}

	/**
Finds a shortest representative in a class of conjugated words.
@param String s: original word
@return cyclically tightened word
	 */
	public static String tightenCycl(String s) {
		StringBuilder sb = new StringBuilder(tightenWord(s));

		if (sb.length()>0)
			while (inverse(sb.charAt(0))==sb.charAt(sb.length()-1)) {
				sb.deleteCharAt(0);
				sb.deleteCharAt(sb.length()-1);
			}

		return sb.toString();
	}

	/**
Inverts words.
@param String s: original word
@return inverse of s.
	 */
	public static String reverseWord(String s) {
		int i;
		StringBuilder rev=new StringBuilder();

		for(i=s.length()-1;i>=0;i--)
			rev.append(inverse(s.charAt(i)));

		return rev.toString();
	}

	/**
Reverses all occurrences of an edge in a word.
@param String s: word
@param int e: index of edge
@return word with occurrences of e reversed
	 */
	public static String reverseChar(String s,int e) {
		StringBuilder sb = new StringBuilder(s);
		int j;
		for(j=0;j<sb.length();j++)
			if (Word.charToIndex(sb.charAt(j))==e)
				sb.setCharAt(j, Word.inverse(sb.charAt(j)));
		return sb.toString();
	}

	/**
Deletes all occurrences of the edge e from a word.
@param String w: word
@param int e: edge to be removed
@return word without e
	 */
	public static String removeEdge(String w, int e) {
		StringBuilder sb = new StringBuilder(w);
		for(int i=0; i<sb.length();) {
			if (charToIndex(sb.charAt(i))==e)
				sb.deleteCharAt(i);
			else
				i++;
		}
		return sb.toString();
	}

	/**
Split the edge e into two edges e' and n.
@param String s: word in which e is to be split
@param int e: index of edge to be split
@param int n: index of second half of e
@return word after splitting
	 */
	public static String splitChar(String s,int e,int n) {
		StringBuilder sb = new StringBuilder(s);
		for(int j=0;j<sb.length();j++)
			if (Word.charToIndex(sb.charAt(j))==e) {
				if (Word.isInverse(sb.charAt(j)))
					sb.insert(j, Word.indexToChar(n, true));
				else
					sb.insert(j+1, Word.indexToChar(n, false));
				j++;
			}
		return sb.toString();
	}

	/**
replaces every occurrence of the char c in w by the String s
(including inverses).
@param String w: word
@param char c: char to be replaced
@param String s: replacement of c
@return word with replacements
	 */
	public static String replaceChar(String w, char c, String s) {
		int i;				// counter
		StringBuilder res=new StringBuilder(); // result
		String si=reverseWord(s);	// inverse of s

		// scan w for occurences of c
		for(i=0;i<w.length();i++)
			if (c==w.charAt(i))
				res.append(s);
			else if (Word.inverse(c)==w.charAt(i))
				res.append(si);
			else
				res.append(w.charAt(i));

		return tightenWord(res.toString());
	}

	/**
Checks boundary words.
@return true iff w is an admissible boundary word.
	 */
	public static boolean isBoundary(String w) {
		int i,j;

		if ((w.length()!=tightenCycl(w).length()) || ((w.length() % 2)!=0))
			return false;

		for(i=0;i<w.length();i++) {
			for(j=(i+1) % w.length(); ((w.charAt(i)!=Word.inverse(w.charAt(j))) && (j!=i)); j=(j+1) % w.length())
				;	// do nothing

			if (i==j)
				return false;
		}

		return true;
	}

	/**
Uses a Whitehead-type algorithm for dualizing Strings.
@param String w: Word to be dualized
@return dual word of w
	 */
	public static String dualize(String w) {
		int i,len;
		char ch;

		if (!isBoundary(w))
			throw new RuntimeException("bad boundary word: "
					+pathToString(w));

		i=0;
		len=w.length();
		StringBuilder dw=new StringBuilder();
		do {
			ch=w.charAt(i);
			dw.append(ch);
			while (w.charAt(i)!=Word.inverse(ch))
				i=(i+1) % len;
			i=(i+1) % len;
		} while (i!=0);

		return dw.toString();
	}

	/**
Extracts relations from a boundary word.
@param String bd: boundary word
@return array of relations derived from bd
	 */
	public static List<String> relations(String bd) {
		int i,j;					// just some counters
		char c;						// current char
		boolean visited[]=new boolean[bd.length()];	// visited flags
		List<String> rels=new ArrayList<String>();			// container for results
		StringBuilder rel;					// relation

		for(i=0;i<bd.length();i++)
			visited[i]=false;

		for(j=0;j<bd.length();j++)
			if (!visited[j]) {
				rel=new StringBuilder();

				for(i=j;!visited[i];i=(i+1) % bd.length()) {
					visited[i]=true;
					c=bd.charAt(i);
					rel.append(c);

					while (bd.charAt(i)!=Word.inverse(c))
						i=(i+1) % bd.length();
				}

				rels.add(rel.toString());
			}

		return rels;
	}

	/**
Checks whether two given words are cyclically conjugate.
@return boolean indicating whether the two are cyclically conjugate
	 */
	public static boolean isCyclicallyConjugate(String w1,String w2) {
		int i,j,len;

		w1=tightenCycl(w1);
		w2=tightenCycl(w2);

		if (w1.length()!=w2.length())
			return false;

		len=w1.length();
		if (len==0) return true;

		for(i=0;i<len;i++) {
			for(j=0;(j<len) && (w1.charAt(j)==w2.charAt((i+j)%len));j++)
				;	// do nothing

			if (j>=len)
				return true;
		}

		return false;
	}

	/**
Main routine, just for testing purposes.
	 */
	public static void main(String args[]) {
		System.out.println(Word.pathToString(
				Word.dualize(Word.stringToPath(args[0]))));
	}

}
