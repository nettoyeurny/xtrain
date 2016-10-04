package pbj.math.graph.train;

import gnu.getopt.Getopt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import pbj.io.GenericPrint;
import pbj.math.geometry.Arc;
import pbj.math.geometry.Geodesic;
import pbj.math.geometry.HypPic;
import pbj.math.geometry.HypPoint;
import pbj.math.geometry.Isometry;
import pbj.math.graph.Word;

/**
An extension of HypPic that draws pictures of train tracks embedded in
surfaces.
@author Peter Brinkmann
@see HypPic
 */
public class TrainPic extends HypPic {

	private TrainTrack g;

	private static final int DDD=5;

	/**
Constructs a new TrainPic that draws on a graphics object.
@param Parameters are the same as the parameter of the HypPic constructor, plus a TrainTrack.
@see pbj.math.geometry.HypPic
	 */
	public TrainPic(Graphics gr,boolean model,boolean inf,
			TrainTrack h,Dimension di) {
		super(gr,model,inf,di);
		g=new TrainTrack(h);
		draw();
	}

	/**
Constructs a new TrainPic that draws a PostScript picture.
@param Parameters are the same as the parameter of the HypPic constructor, plus a TrainTrack.
@see pbj.math.geometry.HypPic
	 */
	public TrainPic(Object o,boolean model,boolean inf,
			TrainTrack h,String lab,double scale) {
		super(o,model,inf,lab,scale);
		g=new TrainTrack(h);
		draw();
	}

	/**
Constructs a new TrainPic that draws a PostScript picture.
@param Parameters are the same as the parameter of the HypPic constructor, plus a TrainTrack.
@see pbj.math.geometry.HypPic
	 */
	public TrainPic(boolean model,boolean inf,TrainTrack h,String lab,
			double scale) {
		super(model,inf,lab,scale);
		g=new TrainTrack(h);
		draw();
	}

	private void findArcs(double r[],Arc ar[][]) {
		/* given the radii of the circle packing, this function embeds the triangles of
   the triangulation into the hyperbolic plane such that they form a Dirichlet
   domain centered at the puncture at (0,1). */
		double phi,dphi;
		HypPoint ll,p1,p2;
		int i,v1,v2,e,h;
		phi=0;
		for(i=0;i<g.getFix().length();i++) {
			e=Word.charToIndex(g.getFix().charAt(i));
			v1=g.getStart(e);
			v2=g.getEnd(e);
			dphi=CirclePack.angleA(r[g.getVertices()],r[v1],r[v2]);
			if (Word.isInverse(g.getFix().charAt(i))) { h=v1; v1=v2; v2=h; }
			ll=new HypPoint(0,Math.exp(r[g.getVertices()]+r[v1]));
			ll.changeModel();
			p1=new HypPoint(-ll.y*Math.sin(phi),-ll.y*Math.cos(phi));
			p1.changeModel();
			ll=new HypPoint(0,Math.exp(r[g.getVertices()]+r[v2]));
			ll.changeModel();
			p2=new HypPoint(-ll.y*Math.sin(phi+dphi),
					-ll.y*Math.cos(phi+dphi));
			p2.changeModel();
			phi+=dphi;
			if (Word.isInverse(g.getFix().charAt(i)))
				ar[e][1]=new Arc(p2,p1);
			else
				ar[e][0]=new Arc(p1,p2);
		}
	}

	private void findGens(Isometry f[][],Arc ar[][]) {
		/* given the Dirichlet domain, this function computes the side pairing maps
   generating the fundamental group. */
		int i;
		for(i=0;i<g.getEdges();i++) {
			f[i][0]=Isometry.identifyGeods(ar[i][1].g,ar[i][0].g);
			/* remark: identifygeods is good enough in this case because
		   of our special choice of the Dirichlet domain. */
			f[i][1]=new Isometry();
			f[i][1].copyIsom(f[i][0]);
			f[i][1].invert();
		}
	}

	/* finds a spanning tree of approximately minimal diameter */
	private void computeSpanningTree(boolean spt[],double r[]) {
		double len[]=new double[g.getEdges()];
		int i;

		for(i=0;i<g.getEdges();i++)
			len[i]=r[g.getStart(i)]+r[g.getEnd(i)];

		g.spanningTree(spt,null,len);
	}

	private String dualBoundary(boolean spt[],String bw) {
		/* given the fixed word, this functions computes the dual boundary word wrt
   a given spanning tree */
		int i,i0,j,bl;
		String dbw="";

		if (!Word.isBoundary(bw))
			throw new RuntimeException("bad boundary word: "+bw);

		bl=bw.length();
		for(i0=0;spt[Word.charToIndex(bw.charAt(i0))];i0++);
		i=i0;
		do {
			dbw=dbw+bw.charAt(i);
			for(j=0;bw.charAt(j)!=Word.inverse(bw.charAt(i));j++);
			i=j;
			do {
				i=(i+1) % bl;
			} while (spt[Word.charToIndex(bw.charAt(i))]);
		}
		while (i!=i0);
		return dbw;
	}

	private void dualDomain(Isometry f[][],Arc ar[][],
			Isometry df[][],Arc dar[][],
			String dbw,HypPoint ppp) {
		/* given a Dirichlet fundamental domain with its side pairing maps, this
   function computes a fundamental 4g-gon which is dual wrt the given
   spanning tree. */

		HypPoint pp;
		HypPoint pt=new HypPoint(ppp);
		Isometry h,hh;
		Isometry p[][]=new Isometry[g.getEdges()][2];
		int i,j,e,bl;

		bl=dbw.length();
		pp=new HypPoint(pt);
		h=new Isometry(1,0,0,1);
		hh=new Isometry();

		for(i=0;i<dbw.length();i++) {
			e=Word.charToIndex(dbw.charAt(i));
			hh.copyIsom(h);
			hh.invert();
			if (Word.isInverse(dbw.charAt(i))) {
				p[e][1]=new Isometry();
				p[e][1].copyIsom(h);
				p[e][1].comp(f[e][1]);
				h.comp(f[e][1]);
				p[e][1].comp(hh);
				pp=p[e][1].apply(pp);
				dar[e][1]=new Arc(pt,pp);
			}
			else {
				p[e][0]=new Isometry();
				p[e][0].copyIsom(h);
				p[e][0].comp(f[e][0]);
				h.comp(f[e][0]);
				p[e][0].comp(hh);
				pp=p[e][0].apply(pp);
				dar[e][0]=new Arc(pp,pt);
			}
			pt.copyHypPoint(pp);
		}
		for(i=0;i<dbw.length();i++)
			if (!Word.isInverse(dbw.charAt(i))) {
				e=Word.charToIndex(dbw.charAt(i));
				df[e][0]=new Isometry(1,0,0,1);
				for(j=(i-1+bl) % bl;
				dbw.charAt(j)!=Word.inverse(dbw.charAt(i));
				j=(j-1+bl) % bl)
					if (Word.isInverse(dbw.charAt(j)))
						df[e][0].comp(
								p[Word.charToIndex(dbw.charAt(j))][1]);
					else
						df[e][0].comp(
								p[Word.charToIndex(dbw.charAt(j))][0]);
				df[e][1]=new Isometry();
				df[e][1].copyIsom(df[e][0]);
				df[e][1].invert();
			}
	}

	private void moveToDomain(boolean spt[],Isometry df[][],
			Arc ar[][], Arc dar[][],Arc far[][],
			HypPoint pt[]) {
		/* given a 4g-gon that is a fundamental domain, this function computes the
   translates of the edges of g lying in the new fundamental domain. */
		boolean vtflag[]=new boolean[g.getVertices()];
		HypPoint pp=new HypPoint();
		Isometry mv,h;
		Arc a;
		int i,e,inv,ind;

		mv=new Isometry(1,0,0,1);

		pt[g.getVertices()]=new HypPoint(0,1);
		for(i=0;i<g.getFix().length();i++) {
			e=Word.charToIndex(g.getFix().charAt(i));
			if (Word.isInverse(g.getFix().charAt(i))) inv=1;
			else inv=0;
			if (inv==0) {	// check this if necessary
				a=mv.apply(ar[e][0]);
				if (spt[e]) 
					far[e][0]=new Arc(a.p1,a.p2);
				else {
					dar[e][0].intersect(a,pp);
					// far[e][0]=new Arc(a.p1,a.p2);
					far[e][0]=new Arc(a.p1,pp); // deviation from c
					a=new Arc(pp,a.p2);
					a=df[e][1].apply(a);
					far[e][1]=new Arc(a.p1,a.p2);
				}
			}
			if (!spt[e]) {
				h=new Isometry();
				h.copyIsom(df[e][1-inv]);
				h.comp(mv);
				mv.copyIsom(h);
			}
		}

		for(i=0;i<g.getVertices();i++)
			vtflag[i]=false;

		for(i=0;i<g.getEdges();i++) {
			if (!vtflag[g.getStart(i)]) {
				vtflag[g.getStart(i)]=true;
				pt[g.getStart(i)]=new HypPoint(far[i][0].p1);
			}
			if (!vtflag[g.getEnd(i)]) {
				vtflag[g.getEnd(i)]=true;
				if (!spt[i])	ind=1;
				else		ind=0;
				pt[g.getEnd(i)]=new HypPoint(far[i][ind].p2);
			}
		}
	}

	private void newCenter(boolean spt[],Isometry df[][],Arc dar[][],
			Arc far[][],HypPoint pt[],HypPoint cp) {
		/* this function moves the dual fundamental domain by conjugation in order to
   make it look nicer. */
		Isometry gg,gi,f1;
		int i;

		f1=new Isometry(1,-cp.x,0,1);
		gg=new Isometry(1/cp.y,0,0,1);
		gg.comp(f1);
		gi=new Isometry();
		gi.copyIsom(gg);
		gi.invert();
		for(i=0;i<g.getEdges();i++) {
			far[i][0]=gg.apply(far[i][0]);
			if (!spt[i]) {
				far[i][1]=gg.apply(far[i][1]);
				dar[i][0]=gg.apply(dar[i][0]);
				dar[i][1]=gg.apply(dar[i][1]);
				df[i][0].comp(gi);
				f1.copyIsom(gg);
				f1.comp(df[i][0]);
				df[i][0].copyIsom(f1);
				df[i][1].comp(gi);
				f1.copyIsom(gg);
				f1.comp(df[i][1]);
				df[i][1].copyIsom(f1);
			}
		}
		for(i=0;i<g.getVertices()+1;i++)
			pt[i]=gg.apply(pt[i]);
	}

	private void adjustArcs(boolean spt[],Arc far[][],HypPoint pt[],
			double r[], double divi) {
		/* technical stuff: this function changes the embedding of edges of g such
   that they end at circles around the vertices. */
		int i,j,ind;
		Isometry f;
		Geodesic gg;

		gg=new Geodesic(0,0);
		for(i=0;i<g.getVertices();i++)
			for(j=0;j<g.getEdges();j++) {
				if (g.getStart(j)==i) {
					f=Isometry.identifyGeods(far[j][0].g,gg);
					far[j][0]=f.apply(far[j][0]);
					if (far[j][0].p1.y>far[j][0].p2.y)
						far[j][0].p1.y=far[j][0].p1.y/
						Math.exp(r[i]/divi);
					else
						far[j][0].p1.y=far[j][0].p1.y*
						Math.exp(r[i]/divi);
					far[j][0]=new Arc(far[j][0].p1,far[j][0].p2);
					f.invert();
					far[j][0]=f.apply(far[j][0]);
				}
				if (g.getEnd(j)==i) {
					if (!spt[j])	ind=1;
					else		ind=0;
					f=Isometry.identifyGeods(far[j][ind].g,gg);
					far[j][ind]=f.apply(far[j][ind]);
					if (far[j][ind].p2.y>far[j][ind].p1.y)
						far[j][ind].p2.y=far[j][ind].p2.y/
						Math.exp(r[i]/divi);
					else
						far[j][ind].p2.y=far[j][ind].p2.y*
						Math.exp(r[i]/divi);
					far[j][ind]=new Arc(far[j][ind].p1,
							far[j][ind].p2);
					f.invert();
					far[j][ind]=f.apply(far[j][ind]);
				}
			}
	}

	private void findInfEdges(boolean spt[],Gates gat,
			Arc infed[][],int nred[],
			Arc far[][],double r[],double divi,
			HypPoint pv[]) {
		/* this function identifies ends of edges belonging to the same gates and
   computes the embedding of the infinitesimal edges. */
		int i,j,k,e,cnt,ind;
		HypPoint p[]=new HypPoint[2*g.getEdges()];
		HypPoint gp[]=new HypPoint[2*g.getEdges()];
		Arc a;
		Geodesic gg;
		Isometry f;
		char ch;

		gg=new Geodesic(0,0);
		for(i=0;i<g.getVertices();i++) {
			for(j=0;j<gat.nogates[i];j++) {
				cnt=0;
				for(k=0;k<gat.noad[i];k++)
					if (gat.ga[i][k]==j) {
						ch=gat.adj[i][k];
						e=Word.charToIndex(ch);
						if (Word.isInverse(ch)) {
							if (!spt[e])	ind=1;
							else		ind=0;
							p[cnt]=new
							HypPoint(far[e][ind].p2);
						}
						else {
							p[cnt]=new
							HypPoint(far[e][0].p1);
						}
						cnt++;
					}
				gp[j]=HypPoint.findCenter(cnt,p);
				a=new Arc(gp[j],pv[i]);
				f=Isometry.identifyGeods(a.g,gg);
				a=f.apply(a);
				if (a.p1.y<a.p2.y)
					a.p1.y=a.p2.y/Math.exp(r[i]/divi);
				else
					a.p1.y=a.p2.y*Math.exp(r[i]/divi);
				f.invert();
				a=f.apply(a);
				gp[j]=new HypPoint(a.p1);
			}
			for(k=0;k<gat.noad[i];k++) {
				ch=gat.adj[i][k];
				e=Word.charToIndex(ch);
				if (Word.isInverse(ch)) {
					if (!spt[e]) ind=1;
					else            ind=0;
					far[e][ind]=new Arc(far[e][ind].p1,
							gp[gat.ga[i][k]]);
				}
				else
					far[e][0]=new Arc(gp[gat.ga[i][k]],
							far[e][0].p2);
			}
			cnt=0;
			for(j=0;j<gat.nogates[i]-1;j++)
				for(k=j+1;k<gat.nogates[i];k++)
					if (gat.infini[i][j][k]) {
						infed[i][cnt]=new Arc(gp[j],gp[k]);
						cnt++;
					}
			nred[i]=cnt;
		}
	}

	/**
Draws a picture of a TrainTrack embedded in a surface.
	 */
	private void draw() {
		double r[]=new double[g.getVertices()+1];
		Arc ar[][]=new Arc[g.getEdges()][2];
		Arc dar[][]=new Arc[g.getEdges()][2];
		Arc far[][]=new Arc[g.getEdges()][2];
		Arc infed[][]=new Arc[g.getEdges()][2*g.getEdges()];
		Isometry f[][]=new Isometry[g.getEdges()][2];
		Isometry df[][]=new Isometry[g.getEdges()][2];
		Gates gat=new Gates(g);
		HypPoint p[]=new HypPoint[g.getEdges()];
		HypPoint cp;
		boolean spt[]=new boolean[g.getEdges()];
		int nred[]=new int[g.getEdges()];
		String dbw;
		int i,j;
		CirclePack cc=new CirclePack(g);

		if (g.getRank()<4)
			throw new RuntimeException("genus must be at least 2.");

		cc.pack(r);

		findArcs(r,ar);
		findGens(f,ar);
		computeSpanningTree(spt,r);
		dbw=dualBoundary(spt,g.getFix());
		dualDomain(f,ar,df,dar,dbw,new HypPoint(0,1));
		moveToDomain(spt,df,ar,dar,far,p);
		cp=HypPoint.findCenter(g.getVertices(),p);
		newCenter(spt,df,dar,far,p,cp);
		adjustArcs(spt,far,p,r,DDD);
		findInfEdges(spt,gat,infed,nred,far,r,DDD,p);

		setLineColor(new Color(255,255,100));
		setTextColor(Color.black);
		setDiscColor(new Color(0,200,155));

		thinLines();
		for(i=0;i<g.getVertices();i++) {
			drawHypCirc(p[i],r[i]/DDD);
			for(j=0;j<nred[i];j++)
				drawArc(infed[i][j]);
			markHypPoint(p[i],Word.vertexToLabel(i));
		}
		for(i=0;i<g.getEdges();i++) {
			medLines();
			drawLArc(far[i][0],
					Word.indexToLabel(i,false));
			if (!spt[i]) {
				drawLArc(far[i][1],
						Word.indexToLabel(i,true));
				thickLines();
				drawArc(dar[i][0]);
				drawArc(dar[i][1]);
			}
		}
		endPic();
	}

	/**
Prints the necessary psfrag commands to replace PostScript labels by
LaTeX labels.
@param Object o: object suitable for GenericPrint. Output goes to o
@param TrainTrack tt: map whose labels are to be printed
	 */
	public static void psfragLabels(Object o,TrainTrack tt) {
		GenericPrint gp=new GenericPrint(o);
		int i;

		gp.println("% --------------------------------------------"+
		"----------------------------");

		gp.println("\\psfrag{"+tt.getLabel()+"}{"+tt.getLabel()+"}");
		
		for(i=0;i<tt.getVertices();i++)
			gp.println("\\psfrag{"+Word.vertexToLabel(i)+"}{$"
					+Word.vertexToLaTeXLabel(i)+"$}");

		for(i=0;i<tt.getEdges();i++) {
			gp.println("\\psfrag{"
					+Word.indexToLabel(i,false)+"}{$"
					+Word.indexToLaTeXLabel(i,false)+"$}");
			gp.println("\\psfrag{"
					+Word.indexToLabel(i,true)+"}{$"
					+Word.indexToLaTeXLabel(i,true)+"$}");
		}
		gp.println("% --------------------------------------------"+
		"----------------------------");
	}

	/**
Prints the necessary psfrag commands to replace PostScript labels by
LaTeX labels. The output goes to stdout.
@param TrainTrack tt: map whose labels are to be printed
	 */
	public static void psfragLabels(TrainTrack tt) {
		psfragLabels(System.out,tt);
	}

	/**
<p>The main routine. Recommended usage for Unix systems:</p>
<p>Add the line
<code>     alias jdraw "java pbj.math.graph.train.TrainPic" </code>
to your .cshrc (assuming you're using csh).
</p>

<p>Usage:
<kbd>	jdraw [-s<factor>][-n] [filename] </kbd>
or
<kbd>	jdraw [-p] [filename] </kbd>
</p>

<p>If a filename is specified, jdraw tries to read from the given file,
otherwise it reads from stdin. The input format is the same as the output
format.  The switch -n is no longer necessary; the software now computes
nice pictures by default. The switch -s followed by a number (no space
in between!) scales the picture. For example, jdraw -s2.0 will double
the size of the picture.  The switch -p prompts jdraw to print a list of
psfrag commands for replacing PostScript labels by LaTeX labels.</p>
	 */
	public static void main(String s[]) {
		TrainTrack tt=new TrainTrack();
		double scale=1.0;
		boolean psfrag=false;
		int i;

		Getopt opts=new Getopt("Trainpic.java",s,"pns:");
		int c;

		while ((c = opts.getopt()) != -1) {
			switch(c) {
			case 'p':
				psfrag=true;
				break;
			case 'n':
				System.err.println(
				"The option -n is no longer necessary.");
				break;
			case 's':
				scale=Double.valueOf(
						opts.getOptarg()).doubleValue();
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
			}
			else
				tt.readFromFile("");
		} catch (Exception e) {System.err.println(e.toString());
		return; }

		if ((tt.getRank()>=4) && (tt.isIrreducible()))
			try{
				if (!psfrag)
					new TrainPic(HypPic.DISC,false,tt,"",scale);
				else
					TrainPic.psfragLabels(tt);
			} catch(Throwable e) {
				System.err.println(e.toString());
			}
			else if (!tt.isIrreducible())
				System.err.println("map is reducible");
			else
				System.err.println("genus must be at least two.");
	}

}
