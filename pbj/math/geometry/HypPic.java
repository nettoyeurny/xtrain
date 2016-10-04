package pbj.math.geometry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.text.DecimalFormat;

import pbj.io.GenericPrint;

/**
A class that draws objects in the hyperbolic plane. Pictures can be drawn
using both the upper halfspace model and the Poincare disc model.
@author Peter Brinkmann
@see HypPoint
@see Geodesic
@see Arc
 */
public class HypPic {

	/** parameter for constructor, chooses the disc model */
	public static final boolean DISC=true;

	/** parameter for constructor, chooses the halfspace model */
	public static final boolean HALFSPACE=false;

	/** parameter for constructor, specifies no circle at infinity */
	public static final boolean NOINF=false;

	/** parameter for constructor, specifies circle at infinity */
	public static final boolean INF=true;

	private static final double TOL=0.000000000001;
	// tolerance for numerical computations

	private static final double MRAD=1000.0;
	// circles of radius > MRAD are treated as lines

	private static final double BIG=1000.0;
	// cutoff for drawing vertical geodesics

	private static double XOFF;	// horizontal offset
	private static double YOFF;	// vertical offset
	private static double UNIT;	// number of points for one unit in H^2
	private static double XTRANS;	// horizontal translation distance
	private static double YTRANS;	// vertical translation distance

	/** standard width */
	public static final int XNORM=600;

	/** standard height */
	public static final int YNORM=600;

	private int xSize=XNORM;		// current width
	private int ySize=YNORM;		// current height

	private GenericPrint pw;		// output class for PostScript
	private boolean model;			// model chosen
	private boolean ps;			// flag for PostScript output
	// (rather than Graphics)
	private Graphics gr;			// target for drawing on screen
	private Color lineColor;		// colors for drawing on screen
	private Color discColor;
	private Color textColor;
	private static final DecimalFormat df=new DecimalFormat("#.000");
	// rounding for PostScript output

	private double x0Bound=0.0;		// keeps track of BoundingBox
	private double y0Bound=0.0;
	private double x1Bound=0.0;
	private double y1Bound=0.0;

	// adjusts x to current size
	private int adjustX(double x) {
		return (int) ((x+XOFF)*xSize/XNORM);
	}

	// adjusts y to current size
	private int adjustY(double y) {
		return (int) ((-y+YOFF)*ySize/YNORM);
	}

	// updates BoundingBox
	private void adjustBounds(double x,double y) {
		x0Bound=Math.min(x0Bound,x);
		y0Bound=Math.min(y0Bound,y);
		x1Bound=Math.max(x1Bound,x);
		y1Bound=Math.max(y1Bound,y);
	}

	// draws a straight line between two points
	private void drawLine(double x1, double y1, double x2, double y2) {
		// catch floating point exceptions
		if (Double.isNaN(x1) || Double.isNaN(y1)
				|| Double.isNaN(x2) || Double.isNaN(y2))
			throw new RuntimeException("floating point exception");

		if (!ps) { // Graphics case
			gr.setColor(lineColor);
			gr.drawLine(adjustX(x1),adjustY(y1),adjustX(x2),adjustY(y2));
		}
		else {	// PostScript case
			pw.println(df.format(x1)+" "+df.format(y1)
					+" moveto "+df.format(x2)+" "+df.format(y2)
					+" lineto stroke");
			adjustBounds(x1,y1);
			adjustBounds(x2,y2);
		}
	}

	// draws a filled (euclidean) circle
	private void drawCircle(double mx, double my, double r) {
		// catch floating point exceptions
		if (Double.isNaN(mx) || Double.isNaN(my)
				|| Double.isNaN(r))
			throw new RuntimeException("floating point exception");

		if (!ps) {	// Graphics case
			gr.setColor(discColor);
			gr.fillOval(adjustX(mx-r),adjustY(my+r),
					(int) ((2*r)*xSize/XNORM),
					(int) ((2*r)*ySize/YNORM));
		}
		else {		// PostScript case
			pw.println("gsave 0.9 setgray "+df.format(mx)+" "
					+df.format(my)+" "+df.format(r)
					+" 0 360 arc fill grestore");
			adjustBounds(mx+r,my+r);
			adjustBounds(mx-r,my-r);
		}
	}

	// draws an empy (euclidean) circle
	private void drawEmptyCircle(double mx, double my, double r) {
		// catch floating point exceptions
		if (Double.isNaN(mx) || Double.isNaN(my)
				|| Double.isNaN(r))
			throw new RuntimeException("floating point exception");

		if (!ps) {	// Graphics case
			gr.setColor(lineColor);
			gr.drawOval(adjustX(mx-r),adjustY(my+r),
					(int) ((2*r)*xSize/XNORM),
					(int) ((2*r)*ySize/YNORM));
		}
		else {		// PostScript case
			pw.println(df.format(mx)+" "+df.format(my)+" "+df.format(r)
					+" 0 360 arc stroke");
			adjustBounds(mx+r,my+r);
			adjustBounds(mx-r,my-r);
		}
	}

	// draws a String at the given location
	private void drawString(String s,double x, double y) {
		// catch floating point exceptions
		if (Double.isNaN(x) || Double.isNaN(y))
			throw new RuntimeException("floating point exception");

		if (s.length()>0) { // make sure the String is not ""
			if (!ps) {// Graphics case
				gr.setColor(textColor);
				gr.drawString(s,adjustX(x),adjustY(y));
			}
			else {	// PostScript case
				pw.println(df.format(x-3)+" "+df.format(y-3)
						+" moveto ("+s+") show stroke");
				adjustBounds(x,y);
			}
		}
	}

	/*
I had to reimplement 'drawArc' because the Java command drawArc only
knows integral parameters, which is not sufficient for this purpose.
	 */
	private void drawArc(double x,double y,double r,double phi0,double dphi) {
		final int N=20;
		int i;
		int xi[]=new int[N+1];
		int yi[]=new int[N+1];

		// catch floating point exceptions
		if (Double.isNaN(x) || Double.isNaN(y)
				|| Double.isNaN(r) || Double.isNaN(phi0) || Double.isNaN(dphi))
			throw new RuntimeException("floating point exception");

		if (!ps) { // Graphics case
			gr.setColor(lineColor);

			for(i=0;i<N+1;i++) { // compute points on arc
				xi[i]=adjustX(
						x+r*Math.cos((phi0+dphi*i/N)*Math.PI/180));
				yi[i]=adjustY(
						y+r*Math.sin((phi0+dphi*i/N)*Math.PI/180));
			}

			for(i=0;i<N;i++)
				gr.drawLine(xi[i],yi[i],xi[i+1],yi[i+1]);
		}
		else {
			// is the radius small enough?
			if (r<MRAD) {	// yes: draw circle segment
				if (dphi>=0)	// counterclockwise
					pw.println(df.format(x)+" "+df.format(y)
							+" "+df.format(r)+" "
							+df.format(phi0)+" "
							+df.format(phi0+dphi)+" arc stroke");
				else	// clockwise
					pw.println(df.format(x)+" "+df.format(y)
							+" "+df.format(r)+" "
							+df.format(phi0+dphi)
							+" "+df.format(phi0)+" arc stroke");
			}
			else	// no: draw straight line instead of arc
				pw.println(df.format(x+r*Math.cos(phi0*Math.PI/180))
						+" "+df.format(y+r*Math.sin(phi0*Math.PI/180))
						+" moveto "
						+df.format(x+r*Math.cos((phi0+dphi)*Math.PI/180))
						+" "
						+df.format(y+r*Math.sin((phi0+dphi)*Math.PI/180))
						+" lineto stroke");

			adjustBounds(x+r*Math.cos(phi0*Math.PI/180),
					y+r*Math.sin(phi0*Math.PI/180));
			adjustBounds(x+r*Math.cos((phi0+dphi)*Math.PI/180),
					y+r*Math.sin((phi0+dphi)*Math.PI/180));
		}
	}

	// draws a String, location given in polar coordinates
	private void drawStringRad(String s,double x,double y,double r,double phi) {
		// catch floating point exceptions
		if (Double.isNaN(x) || Double.isNaN(y)
				|| Double.isNaN(r) || Double.isNaN(phi))
			throw new RuntimeException("floating point exception");

		if (!ps) {	// Graphics case
			gr.setColor(textColor);
			gr.drawString(s,adjustX(x+r*Math.cos(phi*Math.PI/180)),
					adjustY(y+r*Math.sin(phi*Math.PI/180)));
		}
		else {		// PostScript case
			pw.println(df.format(x+r*Math.cos(phi*Math.PI/180))+" "
					+df.format(y+r*Math.sin(phi*Math.PI/180))
					+" moveto ("+s+") show stroke");
			adjustBounds(x+r*Math.cos(phi*Math.PI/180),
					y+r*Math.sin(phi*Math.PI/180));
		}
	}

	/**
Constructs an object of type HypPic that draws on a graphics object.
@param Graphics gg: object for drawing
@param boolean mm: indicates the desired model (HALFSPACE or DISC)
@param boolean inf: indicates whether the circle at infinity will be drawn
@param Dimension di: size of the area to draw on
	 */
	public HypPic(Graphics gg,boolean mm,boolean inf,Dimension di) {
		ps=false;
		model=mm;
		gr=gg;
		xSize=di.width;
		ySize=di.height;

		if (model==DISC) {
			UNIT=250.0;
			XOFF=300.0;
			YOFF=300.0;
		}
		else {
			UNIT=30.0;
			XOFF=300.0;
			YOFF=580.0;
		}

		if (inf) {
			thinLines();
			if (model==DISC)
				drawEmptyCircle(0,0,UNIT);
			else
				drawLine(-XOFF,0,XOFF,0);
		}
	}

	/**
Constructs an object of type HypPic that draws a PostScript picture.
@param Object o: object indicating what to write on. Must be suitable for GenericPrint
@param boolean mm: indicates the model used (HALFSPACE or DISC)
@param boolean inf: indicates whether the circle at infinity will be drawn
@param String lab: label of picture
@param double scale: scaling factor (should be 1.0 in most cases)
	 */
	public HypPic(Object o,boolean mm,boolean inf,String lab,double scale) {
		pw=new GenericPrint(o);
		initialize(mm,inf,lab,scale);
	}

	// print header of PostScript code
	private void initialize(boolean mm,boolean inf,String lab,double scale) {
		ps=true;
		model=mm;

		pw.println("%!PS-Adobe-3.0 EPSF-3.0");
		pw.println("%%Creator: XTrain");
		if (model==DISC) {
			UNIT=250.0*scale;
			XOFF=300.0*scale;
			YOFF=300.0*scale;
			XTRANS=300.0*scale;
			YTRANS=400.0*scale;
			pw.println("%%BoundingBox: "
					+((int) (XTRANS-XOFF))+" "
					+((int) (YTRANS-YOFF))+" "
					+((int) (XTRANS+XOFF))+" "
					+((int) (YTRANS+YOFF)));
		}
		else {
			UNIT=30.0*scale;
			XOFF=300.0*scale;
			YOFF=550.0*scale;
			XTRANS=300.0*scale;
			YTRANS=300.0*scale;
			pw.println("%%BoundingBox: (atend)");
		}

		pw.println(df.format(XTRANS)+" "+df.format(YTRANS)+" translate");

		pw.println("/Times-Roman findfont 15 scalefont setfont");
		if (model==DISC)
			pw.println(-UNIT+" "+UNIT
					+" moveto ("+lab+") show stroke");
		else
			pw.println("0.0 -10.0 moveto ("+lab+") show stroke");

		pw.println("/Times-Roman findfont 10 scalefont setfont");

		if (inf) {
			thinLines();
			if (model==DISC)
				pw.println("0 0 "+UNIT+" 0 360 arc stroke\n");
			else
				pw.println(-XOFF+" 0 moveto "
						+XOFF+" 0 lineto stroke\n");
		}
	}

	/**
Like HypPic(PrintWriter pp,boolean mm,boolean inf,String lab,double scale),
but writes to System.out.
	 */
	public HypPic(boolean mm,boolean inf,String lab,double scale) {
		pw=new GenericPrint(System.out);
		initialize(mm,inf,lab,scale);
	}

	/**
Finishes a picture. Only necessary for PostScript output.
	 */
	public void endPic() {
		if (ps) {
			pw.println("showpage");
			pw.println("%%BoundingBox: "
					+((int) (XTRANS+x0Bound)-1)+" "
					+((int) (YTRANS+y0Bound)-1)+" "
					+((int) (XTRANS+x1Bound)+1)+" "
					+((int) (YTRANS+y1Bound)+1));
		}
	}

	/**
Sets the dimensions of the picture.
@param int xs: width
@param int ys: height
	 */
	public void setSize(int xs,int ys) {
		xSize=xs;
		ySize=ys;
	}

	/**
Sets the color of lines.
@param Color c: new line color
	 */
	public void setLineColor(Color c) {
		lineColor=c;
	}

	/**
Sets the color of text.
@param Color c: new text color
	 */
	public void setTextColor(Color c) {
		textColor=c;
	}

	/**
Sets the color of shaded discs.
@param Color c: new disc color
	 */
	public void setDiscColor(Color c) {
		discColor=c;
	}

	/**
Chooses thin lines (postscript only).
	 */
	public void thinLines() {
		if (ps)
			pw.println("0.1 setlinewidth");
	}

	/**
Chooses medium width lines (postscript only).
	 */
	public void medLines() {
		if (ps)
			pw.println("0.7 setlinewidth");
	}

	/**
Chooses thick lines (postscript only).
	 */
	public void thickLines() {
		if (ps)
			pw.println("1.0 setlinewidth");
	}

	/**
Draws a geodesic.
@param Geodesic g: geodesic to be drawn
	 */
	public void drawGeod(Geodesic g) {
		Arc ar=new Arc();
		if (g.r<=0) {	// handle vertical lines
			ar.p1.x=g.c;
			ar.p1.y=0;
			ar.p2.x=g.c;
			ar.p2.y=BIG;
		}
		else {		// handle half circles
			ar.p1.x=g.c-g.r;
			ar.p1.y=0;
			ar.p2.x=g.c+g.r;
			ar.p2.y=0;
		}
		ar.g.c=g.c;
		ar.g.r=g.r;
		drawArc(ar);
	}

	/**
Draws a String.
@param HypPoint p: location of String in picture
@param String c: String to be drawn
	 */
	public void markHypPoint(HypPoint p,String c) {
		HypPoint pp=new HypPoint(p);
		if (HALFSPACE==model)
			drawString(c,pp.x*UNIT-3,pp.y*UNIT-3);
		else {
			pp.changeModel();
			drawString(c,pp.x*UNIT-3,pp.y*UNIT-3);
		}
	}

	/**
Draws a (euclidean) straight line connecting two points.
@param HypPoint p1,p2: Points to be connected.
	 */
	public void drawLine(HypPoint p1,HypPoint p2) {
		HypPoint pp1=new HypPoint(p1);
		HypPoint pp2=new HypPoint(p2);

		if (HALFSPACE==model)
			drawLine(p1.x*UNIT,p1.y*UNIT,p2.x*UNIT,p2.y*UNIT);
		else {
			pp1.changeModel();
			pp2.changeModel();
			drawLine(pp1.x*UNIT,pp1.y*UNIT,pp2.x*UNIT,pp2.y*UNIT);
		}
	}

	/**
Draws a straight line with label in the middle.
@param HypPoint p1,p2: endpoints of line
@param String c: label of line
	 */
	public void drawLLine(HypPoint p1,HypPoint p2, String c) {
		HypPoint pp1=new HypPoint(p1);
		HypPoint pp2=new HypPoint(p2);

		drawLine(pp1,pp2);

		if (HALFSPACE==model)
			drawString(c,(pp1.x+pp2.x)/2-3,(pp1.y+pp2.y)/2-3);
		else {
			pp1.changeModel();
			pp2.changeModel();
			pp1=new HypPoint((pp1.x+pp2.x)/2,(pp1.y+pp2.y)/2);
			pp1.changeModel();
			markHypPoint(pp1,c);
		}
	}

	/**
Draws an arc with a label.
@param Arc a: arc to be drawn
@param String c: label of arc
	 */
	public void drawLArc(Arc a,String c) {
		double phi1,phi2,dphi,t,det,rr;
		HypPoint p1,p2,pm=new HypPoint();

		// lots of formulas...
		if (HALFSPACE==model)
			if (a.g.r<=0) {
				drawLine(a.p1.x*UNIT,a.p1.y*UNIT,
						a.p2.x*UNIT,a.p2.y*UNIT);
				if (c.length()>0)
					drawString(c+"",(a.p1.x+a.p2.x)/2*UNIT-3,
							(a.p1.y+a.p2.y)/2*UNIT-3);
			}
			else {
				phi1=Math.atan2(a.p1.y,a.p1.x-a.g.c)*180/Math.PI;
				phi2=Math.atan2(a.p2.y,a.p2.x-a.g.c)*180/Math.PI;
				dphi=phi2-phi1;
				if (dphi<-180) dphi+=360;
				if (dphi>180) dphi-=360;
				if (phi1<0) phi1+=360;
				if (phi2<0) phi2+=360;
				drawArc(a.g.c*UNIT,0,a.g.r*UNIT,phi1,dphi);
				if (c.length()>0)
					drawStringRad(c+"",a.g.c*UNIT-3,-3,
							a.g.r*UNIT,phi1+dphi/2);
			}
		else {
			p1=new HypPoint(a.g.c-a.g.r,0);
			p1.changeModel();
			if (a.g.r<=0)
				p2=new HypPoint(0,-1);
			else {
				p2=new HypPoint(a.g.c+a.g.r,0);
				p2.changeModel();
			}
			rr=0;
			det=p1.y*p2.x-p1.x*p2.y;
			if (Math.abs(det)<TOL) {
				drawLLine(a.p1,a.p2,c);
				return;
			}
			t=(p2.x*(p1.x-p2.x)+p2.y*(p1.y-p2.y))/det;
			pm=new HypPoint(p1.x-p1.y*t,p1.y+p1.x*t);
			rr=Math.sqrt((p1.x-pm.x)*(p1.x-pm.x)
					+(p1.y-pm.y)*(p1.y-pm.y));

			p1.copyHypPoint(a.p1);
			p2.copyHypPoint(a.p2);
			p1.changeModel();
			p2.changeModel();

			phi1=Math.atan2(p1.y-pm.y,p1.x-pm.x)*180/Math.PI;
			phi2=Math.atan2(p2.y-pm.y,p2.x-pm.x)*180/Math.PI;
			dphi=phi2-phi1;
			if (dphi<-180) dphi+=360;
			if (dphi>180) dphi-=360;
			if (phi1<0) phi1+=360;
			if (phi2<0) phi2+=360;

			drawArc(pm.x*UNIT,pm.y*UNIT,
					rr*UNIT,phi1,dphi);
			if (c.length()>0)
				drawStringRad(c+"",pm.x*UNIT-3,
						pm.y*UNIT-3,rr*UNIT,phi1+dphi/2);
		}
	}

	/**
Draws an arc without label.
@param Arc a: arc to be drawn
	 */
	public void drawArc(Arc a) {
		drawLArc(a,"");
	}

	/**
Draws a circle in the hyperbolic plane. Note that it's a
circle in the hyperbolic plane, so mp is not the center of the euclidean
circle drawn in the plane.
@param HypPoint mp: center of circle
@param HypPoint hr: radius
	 */
	public void drawHypCirc(HypPoint mp,double hr) {
		double er,ll,ymin,ymax,mid,yy;
		HypPoint ep,tep,pmin,pmax;

		// even more formulas...
		if (model==DISC) {
			tep=new HypPoint(mp);
			tep.changeModel();
			ll=Math.sqrt(tep.x*tep.x+tep.y*tep.y);
			yy=2/(ll+1)-1;
			ymin=yy/Math.exp(hr);
			ymax=yy*Math.exp(hr);
			ymin=2/(ymin+1)-1;
			ymax=2/(ymax+1)-1;
			er=Math.abs(ymax-ymin)/2;
			mid=(ymax+ymin)/2;
			if (ll>TOL)
				ep=new HypPoint(mid*tep.x/ll,mid*tep.y/ll);
			else
				ep=new HypPoint(0,0);
		}
		else {
			pmin=new HypPoint(mp.x,mp.y/Math.exp(hr));
			pmax=new HypPoint(mp.x,mp.y*Math.exp(hr));
			ep=new HypPoint((pmin.x+pmax.x)/2,(pmin.y+pmax.y)/2);
			er=Math.sqrt((pmax.x-ep.x)*(pmax.x-ep.x)
					+(pmax.y-ep.y)*(pmax.y-ep.y));
		}
		drawCircle(ep.x*UNIT,ep.y*UNIT, er*UNIT);
	}

	/**
Disposes of the graphics object in order not to hog system resources.
	 */
	public void dispose() {
		gr.dispose();
	}

}

