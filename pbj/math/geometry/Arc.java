package pbj.math.geometry;

/**
A class that represents arcs in the hyperbolic plane. The public data
fields represent the two endpoints and the geodesic connecting them.
@author Peter Brinkmann
@see Geodesic
@see HypPic
@see Isometry
@see HypPoint
 */
public class Arc {

	/** initial endpoint of the arc */
	public HypPoint p1;

	/** terminal endpoint of the arc */
	public HypPoint p2;

	/** geodesic connecting p1 and p2 */
	public Geodesic g;

	private static final double TOL=0.00000000000000000001;
	// tolerance for numerical computations

	private static final double RMAX=10000.0;
	// treat g as a vertical line if the radius is > RMAX

	private static final double TOLINT=0;

	private static double sign(double w) {
		if (Math.abs(w)<TOL) return 0;
		else	return w/Math.abs(w);
	}

	public Arc() {
		p1=new HypPoint();
		p2=new HypPoint();
		g=new Geodesic();
	}

	/**
Constructs an arc with given endpoints.
@param HypPoint xp1,xp2: endpoints
	 */
	public Arc(HypPoint xp1,HypPoint xp2) {
		p1=new HypPoint(xp1);
		p2=new HypPoint(xp2);
		g=new Geodesic();

		// compute radius and center of the geodesic connecting p1 and p2
		g.c=.5*((p1.x+p2.x)+(p2.y-p1.y)*(p1.y+p2.y)/(p2.x-p1.x));
		g.r=Math.sqrt(p1.y*p1.y+(p1.x-g.c)*(p1.x-g.c));

		// radius too large?
		if (g.r>RMAX) { // yes: treat g as a vertical line
			g.c=p1.x;
			g.r=0;
		}

		if (Double.isNaN(g.c) || Double.isNaN(g.r))
			throw new RuntimeException("floating point exception");
	}

	/**
@return direction of the arc at the initial endpoint (HypPoint is being abused as a vector).
	 */
	public HypPoint direction1() {
		double l;
		HypPoint p=new HypPoint();

		if ((g.r<=0) || (g.r>RMAX)) {
			p.x=0;
			p.y=sign(p2.y-p1.y);
		}
		else if (p1.y<TOL) {
			p.x=0;
			p.y=1;
		}
		else {
			p.x=p1.y;
			p.y=-p1.x+g.c;
			l=Math.sqrt(p.x*(p.x)+p.y*(p.y));
			if (p2.x<p1.x) {
				p.x=-p.x;
				p.y=-p.y;
			}
			p.x=p.x/l;
			p.y=p.y/l;
		}

		if (Double.isNaN(p.x) || Double.isNaN(p.y))
			throw new RuntimeException("floating point exception");

		return p;
	}

	/**
@return direction of the arc at the terminal endpoint.
	 */
	public HypPoint direction2() {
		double l;
		HypPoint p=new HypPoint();

		if ((g.r<=0) || (g.r>RMAX)) {
			p.x=0;
			p.y=sign(p1.y-p2.y);
		}
		else if (p2.y<TOL) {
			p.x=0;
			p.y=1;
		}
		else {
			p.x=p2.y;
			p.y=-p2.x+g.c;
			l=Math.sqrt(p.x*(p.x)+p.y*(p.y));
			if (p1.x<p2.x) {
				p.x=-p.x;
				p.y=-p.y;
			}
			p.x=p.x/l;
			p.y=p.y/l;
		}

		if (Double.isNaN(p.x) || Double.isNaN(p.y))
			throw new RuntimeException("floating point exception");

		return p;
	}

	/**
Finds the intersection of two arcs, if any.
@param Arc a2: one of the arcs
@param HypPoint p: for point of intersection, if any
@return true iff the arc 'this' and a2 intersect, writes intersection
to p.
	 */
	public boolean intersect(Arc a2, HypPoint p) {
		// do the corresponding geodesics intersect?
		if (!g.intersect(a2.g,p))	return false;

		// is g a vertical line?
		if (g.r<=0) { // yes
			if (p.x<Math.min(a2.p1.x,a2.p2.x)-TOLINT) return false;
			if (p.x>Math.max(a2.p1.x,a2.p2.x)+TOLINT) return false;
		}
		// is a2.g a vertical line?
		else if (a2.g.r<=0) { //yes
			if (p.x<Math.min(p1.x,p2.x)-TOLINT) return false;
			if (p.x>Math.max(p1.x,p2.x)+TOLINT) return false;
		}
		else { // no vertical lines involved
			if (p.x<Math.min(p1.x,p2.x)-TOLINT) return false;
			if (p.x>Math.max(p1.x,p2.x)+TOLINT) return false;
			if (p.x<Math.min(a2.p1.x,a2.p2.x)-TOLINT) return false;
			if (p.x>Math.max(a2.p1.x,a2.p2.x)+TOLINT) return false;
		}
		return true;
	}

	/**
Checkes whether 'this' intersects a geodesic.
@param Geodesic gg: the geodesic
@param HypPoint pp: for point of intersection
@return true iff 'this' intersects gg, writes intersection to pp.
	 */
	public boolean intersect(Geodesic gg, HypPoint pp) {
		Arc ar=new Arc();
		ar.g.c=gg.c;
		ar.g.r=gg.r;

		// express gg as an arc with endpoints at infinity
		if (gg.r<=0) {
			ar.p1.x=ar.p2.x=gg.c;
			ar.p1.y=0;
			ar.p2.y=p1.y+p2.y+1;
		}
		else {
			ar.p1.x=gg.c-gg.r;
			ar.p2.x=gg.c+gg.r;
			ar.p1.y=ar.p2.y=0;
		}

		// now use the previous method
		return intersect(ar,pp);
	}

	/**
@return length of 'this'
	 */
	public double length() {
		return p1.distance(p2);
	}

	/**
copies a2 to 'this'.
@param Arc a2: original Arc
	 */
	public void copyArc(Arc a2) {
		p1.x=a2.p1.x;
		p2.x=a2.p2.x;
		p1.y=a2.p1.y;
		p2.y=a2.p2.y;
		g.copyGeod(a2.g);
	}

	public String toString() {
		return "Arc: p1="+p1+", p2="+p2+", g="+g;
	}

}
