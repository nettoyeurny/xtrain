package pbj.math.graph.train;

import pbj.math.graph.*;

/**
A class that implements the notion of gates at vertices, along with the
infinitesimal edges connecting them. (nothing to do with Bill...)
@author Peter Brinkmann
@see TrainTrack
 */
public class Gates{			/* data structure which contains */
	char adj[][];	/* the structure of the infinitesimal */
	int noad[];		/* edges of a train track map */
	int ga[][];
	int nogates[];
	boolean infini[][][];
	TrainTrack g;

	/**
Computes the gates of a train track.
@param TrainTrack gg: train track whose gates and infinitesimal edges will be computed
	 */
	public Gates(TrainTrack gg) {
		int i,j,k;
		g=new TrainTrack(gg);
		adj=new char[g.getVertices()][2*g.getEdges()];
		noad=new int[g.getVertices()];
		ga=new int[g.getVertices()][2*g.getEdges()];
		nogates=new int[g.getVertices()];
		infini=new boolean[g.getVertices()][2*g.getEdges()][2*g.getEdges()];
		for(i=0;i<g.getVertices();i++) {
			noad[i]=nogates[i]=0;
			for(j=0;j<2*g.getEdges();j++) {
				adj[i][j]=(char) 0;
				ga[i][j]=0;
				for(k=0;k<2*g.getEdges();k++)
					infini[i][j][k]=false;
			}
		}
		infinitesimalEdges();
	}

	private void findAdj() {
		/* writes the edges adjacent to the individual vertices to the respective
   adjacency lists. */
		int i,j,cnt;
		for(i=0;i<g.getVertices();i++) {
			for(cnt=j=0;j<g.getEdges();j++) {
				if (g.getStart(j)==i) {
					adj[i][cnt]=Word.indexToChar(j,false);
					cnt++;
				}
				if (g.getEnd(j)==i) {
					adj[i][cnt]=Word.indexToChar(j,true);
					cnt++;
				}
			}
			noad[i]=cnt;
		}
	}

	private void findGates() {
		/* groups edges together in gates. */
		int i,j,k,cnt;
		boolean flag;

		for(i=0;i<g.getVertices();i++) {
			cnt=0; 				// number of current gate
			for(j=0;j<noad[i]-1;j++) {
				flag=false;
				if (ga[i][j]>=cnt)
					for(k=j+1;k<noad[i];k++)
						if ((ga[i][k]>=cnt) &&
								(0==g.isIllegal(adj[i][j],
										adj[i][k]))) {
							ga[i][k]=cnt+1;
							flag=true;
						}

				if (flag) cnt++;
			}
			nogates[i]=cnt+1;
		}
	}

	private void infedgesrec(boolean flags[][],char a,char b) {
		int vt,i,x,y;
		x=g.charToCoord(a);
		y=g.charToCoord(b);
		if (!flags[x][y]) {
			flags[y][x]=flags[x][y]=true; // deviates from C code
			vt=g.firstVertex(b);
			for(i=0;adj[vt][i]!=a;i++);
			x=ga[vt][i];
			for(i=0;adj[vt][i]!=b;i++);
			y=ga[vt][i];
			infini[vt][x][y]=infini[vt][y][x]=true;
			infedgesrec(flags,g.mapd(a),g.mapd(b));
		}
	}

	private void findInfEdges() {
		/* finds out where infinitesimal edges have to be drawn. */
		int i,j;
		boolean flags[][]=new boolean[2*g.getEdges()][2*g.getEdges()];

		for(i=0;i<2*g.getEdges();i++)
			for(j=0;j<2*g.getEdges();j++)
				flags[i][j]=false;

		for(i=0;i<g.getEdges();i++)
			for(j=0;j<g.getIm(i).length()-1;j++)
				infedgesrec(flags,
						Word.inverse(g.getIm(i).charAt(j)),
						g.getIm(i).charAt(j+1));
	}

	private void infinitesimalEdges() {
		/* finds gates and connects them with infinitesimal edges. */
		findAdj();
		findGates();
		findInfEdges();
	}

	private void exchange(int vt,int g1,int g2) {
		int i;
		boolean tmp;

		for(i=0;i<noad[vt];i++)
			if (ga[vt][i]==g1) ga[vt][i]=g2;
			else if (ga[vt][i]==g2) ga[vt][i]=g1;
		for(i=0;i<nogates[vt];i++) {
			tmp=infini[vt][i][g1];
			infini[vt][i][g1]=infini[vt][i][g2];
			infini[vt][i][g2]=tmp;
		}
		for(i=0;i<nogates[vt];i++) {
			tmp=infini[vt][g1][i];
			infini[vt][g1][i]=infini[vt][g2][i];
			infini[vt][g2][i]=tmp;
		}
	}

	/**
Checks whether a given map is pseudo-Anosov.
@return true iff map is pseudo-Anosov
	 */
	public boolean isPseudoAnosov() {
		int i,j,k,cnt,flag,pos;
		for(i=0;i<g.getVertices();i++) {
			pos=flag=0;
			for(j=0;j<nogates[i];j++) {
				cnt=0;
				for(k=0;k<nogates[i];k++)
					if (infini[i][j][k])
						cnt++;
				if ((cnt>2) || (cnt==0)) return false;
				if (cnt==1)
					if (flag>1)
						return false;
					else
					{	pos=j;
					flag++;
					}
			}

			for(j=0;((j<nogates[i]) && (pos<nogates[i]));j++) {
				exchange(i,j,pos);
				for(pos=j+1;((pos<nogates[i]) && 
						(!infini[i][j][pos]));pos++);
			}

			if (j<nogates[i]) return false;
		}
		return g.isIrreducible();
	}

	public String toString() {
		int i,j,k;
		String res="";

		for(i=0;i<g.getVertices();i++) {
			res=res+"vertex "+i+"\n";
			for(j=0;j<noad[i];j++)
				res=res+Word.charToLabel(adj[i][j])
				+" "+ga[i][j]+"\n";
			res=res+"\n";
			for(j=0;j<nogates[i];j++) {
				for(k=0;k<nogates[i];k++)
					if (infini[i][j][k])
						res=res+"1 ";
					else
						res=res+"0 ";
				res=res+"\n";
			}
			res=res+"\n";
		}

		return res;
	}

}
