package pbj.math.graph;


public class LorenzKnot {

	private int[] r, s;
	private int[][] edges;
	private int[] fixedLoop;
	private int edgeCount = 0;

	public LorenzKnot(int[] r, int[] s) {
		if (r.length!=s.length) {
			throw new IllegalArgumentException("array lengths differ");
		}
		if (r.length==0) {
			throw new IllegalArgumentException("empty arrays");
		}
		int prev = 0;
		for(int i=0; i<r.length; i++) {
			if (r[i]<=prev || s[i]<=0) {
				throw new IllegalArgumentException("bad index or exponent");
			}
			prev = r[i];
		}
		this.r = r;
		this.s = s;
		initEdges();
		initBoundary();
	}
	
	private void initEdges() {
		int j = r.length-1;
		int i = r[j]-1;
		int n = s[j--];
		edges = new int[i--][];
		for(; i>=0; i--) {
			if (j>=0 && i+2==r[j]) {
				n += s[j];
				j -= 1;
			}
			edges[i] = new int[n];
			for(int k=0; k<n; k++) {
				edges[i][k] = ++edgeCount;
			}
		}
	}
	
	private void initBoundary() {
		int k = 0;
		int i = 0, j = 0;
		int di = 1, dj = 0;
		fixedLoop = new int[2*edgeCount];
		do {
			i += di;
			j += dj;
			if (i<0) {
				di = 1;
				dj = 0;
			} else if (i>=edges.length || j>=edges[i].length) {
				di = -1;
				dj = -1;
			} else {
				if (j<0) {
					j += edges[i].length;
				}
				fixedLoop[k++] = di*edges[i][j];
			}
		} while (i!=0 || j!=0 || di!=1);
		if (k<fixedLoop.length) {
			throw new IllegalArgumentException("more than one boundary component");
		}
	}
	
	public String getFixedWord() {
		StringBuilder fixedWord = new StringBuilder();
		for(int i = 0; i<fixedLoop.length; i++) {
			fixedWord.append(getLabel(fixedLoop[i]));
		}
		return fixedWord.toString();
	}
	
	public String getTwists() {
		StringBuilder  twists = new StringBuilder();
		for(int i = 0; i<edges.length; i++) {
			for(int j = edges[i].length-2; j>=0; j--) {
				twists.append(DehnTwist.BRA);
				twists.append(getLabel(edges[i][j]));
				twists.append(getLabel(-edges[i][j+1]));
				twists.append(DehnTwist.KET);
			}
		}
		return twists.toString();
	}
	
	private String getLabel(int e) {
		return (e>0) ? Word.indexToLabel(e-1, false) : Word.indexToLabel(-e-1, true);
	}
	
	public GraphMap getMonodromy() {
		return DehnTwist.twistWithFixedWord(getFixedWord(), getTwists(), toString());
	}

	public String toString() {
		StringBuilder label = new StringBuilder("LorenzKnot_");
		for(int i = 0; i<r.length; i++) {
			label.append(r[i]);
			label.append("^");
			label.append(s[i]);
			if (i<r.length-1) {
				label.append(".");
			}
		}
		return label.toString();
	}
}
