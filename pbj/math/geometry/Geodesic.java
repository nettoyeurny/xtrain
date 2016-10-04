package pbj.math.geometry;

/**
A class representing geodesics in the hyperbolic plane. Geodesics are
represented as semicircles in the upper half place model. c is the center
on the real line, and r is the radius. A geodesic with r=0 is a vertical line.
@author Peter Brinkmann
@see HypPoint
@see Arc
@see HypPic
 */
public class Geodesic {

	private static final double TOL=0.00000000000000000001;
	// tolerance for numerical computations

	/** center of circle (on the real line) */
	public double c;

	/** radius of circle (r=0 for vertical line) */
	public double r;

	public Geodesic() {
	}

	/**
Constructs a geodesic with given center and radius.
@param double cc: center (on real line)
@param double rr: radius (=0 for vertical lines)
	 */
	public Geodesic(double cc, double rr) {
		if (Double.isNaN(cc) || Double.isNaN(rr))
			throw new RuntimeException("floating point exception");

		c=cc;
		r=rr;
	}

	/**
Constructs a new geodesic identical to gg.
@param Geodesic gg: original geodesic
	 */
	public Geodesic(Geodesic gg) {
		if (Double.isNaN(gg.c) || Double.isNaN(gg.r))
			throw new RuntimeException("floating point exception");

		c=gg.c;
		r=gg.r;
	}

	/**
Computes the point of intersection of two geodesics, if any.
@param Geodesic g2: one of the geodesics
@param HypPoint p: for point of intersection, if any
@return true iff 'this' and g2 intersect, writes intersection to p.
	 */
	public boolean intersect(Geodesic g2,HypPoint p) {
		double disc;

		// just a bunch of formulas...
		if (r<=0)
			p.x=c;
		else if (g2.r<=0)
			p.x=g2.c;
		else {
			if (Math.abs(c-g2.c)<TOL) return false;
			p.x=(r*r-g2.r*g2.r+g2.c*g2.c-c*c)/
			(g2.c-c)/2;
		}

		if (r>0)
			disc=r*r-(p.x-c)*(p.x-c);
		else if (g2.r>0)
			disc=g2.r*g2.r-(p.x-g2.c)*(p.x-g2.c);
		else
			return false;

		if (disc>=0) {
			p.y=Math.sqrt(disc);

			if (Double.isNaN(p.x) || Double.isNaN(p.y))
				throw new RuntimeException("floating point exception");

			return true;
		}
		else
			return false;
	}

	/**
Finds the common perpendicular of two geodesics, if any.
@param Geodesic p: for common perpendicular, if any
@param Geodesic g2: the other geodesic
@return true iff 'this' and g2 have a common perpendicular, and writes it
to p.
	 */
	public boolean commonPerp(Geodesic p,Geodesic g2) {
		double d,disc,l;

		// more formulas...
		d=g2.c-c;
		if (r<=0) {
			if (g2.r<=0) 	return false;
			p.c=c;
			disc=d*d-g2.r*g2.r;
		}
		else if (g2.r<=0) {
			if (r<=0)	return false;
			p.c=g2.c;
			disc=d*d-r*r;
		}
		else if (Math.abs(d)<TOL) {
			p.c=c;
			disc=0;
		}
		else {
			l=(d*d-g2.r*g2.r+r*r)/d/2;
			p.c=c+l;
			disc=l*l-r*r;
		}

		if (disc>TOL) {
			p.r=Math.sqrt(disc);

			if (Double.isNaN(p.r) || Double.isNaN(p.c))
				throw new RuntimeException("floating point exception");

			return true;
		}
		else	return false;
	}

	/**
Computes the orthogonal projection of a point onto 'this'.
@param HypPoint p: The point to be projected onto 'this'
@return The HypPoint that is the projection of p.
	 */
	public HypPoint project(HypPoint p) {
		HypPoint h=new HypPoint(p);
		Isometry f;

		f=Isometry.mapTo00(this);
		h=f.apply(h);
		h=new HypPoint(0,Math.sqrt((h.x*h.x)+(h.y*h.y)));
		f.invert();
		return f.apply(h);
	}

	/**
Computes the distance between a point and 'this'.
@param HypPoint p: Some point.
@return distance between p and 'this'
	 */
	public double distance(HypPoint p) {
		HypPoint pp=project(p);
		return p.distance(pp);
	}

	/**
Computes the distance between a point and 'this'.
@param Geodesic g: Some geodesic
@return distance between g and 'this'
	 */
	public double distance(Geodesic g) {
		Geodesic p=new Geodesic();
		HypPoint pt=new HypPoint();

		if (commonPerp(p,g)) {
			intersect(p,pt);
			return g.distance(pt);
		}
		else
			return 0;
	}

	/**
Copies g2 to 'this'.
@param Geodesic g2: original geodesic
	 */
	public void copyGeod(Geodesic g2) {
		if (Double.isNaN(g2.r) || Double.isNaN(g2.c))
			throw new RuntimeException("floating point exception");

		c=g2.c;
		r=g2.r;
	}

	public String toString() {
		return "Geodesic: c="+c+", r="+r;
	}

}
