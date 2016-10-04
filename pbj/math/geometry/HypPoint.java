package pbj.math.geometry;

/**
A class that represents points in the hyperbolic plane, using the upper
halfspace model.
@author Peter Brinkmann
@see Arc
@see Geodesic
@see Isometry
 */
public class HypPoint {

	public double x;
	public double y;

	public HypPoint() {
	}

	/**
Constructs a point.
@param double xx,yy: coordinates of point (upper halfspace model)
	 */
	public HypPoint(double xx,double yy) {
		if (Double.isNaN(xx) || Double.isNaN(yy))
			throw new RuntimeException("floating point exception");

		x=xx;
		y=yy;
	}

	/**
Constructs a point identical to p.
@param HypPoint p: original point
	 */
	public HypPoint(HypPoint p) {
		this(p.x,p.y);
	}

	/**
Copies p to 'this'.
@param HypPoint p: original point
	 */
	public void copyHypPoint(HypPoint p) {
		if (Double.isNaN(p.x) || Double.isNaN(p.y))
			throw new RuntimeException("floating point exception");

		x=p.x;
		y=p.y;
	}

	/**
Transforms 'this' to the Poincare disc model and back.
	 */
	public void changeModel() {
		double l;
		l=x*x+(y+1)*(y+1);
		x=x*2/l;
		y=(y*2+2)/l-1;

		if (Double.isNaN(x) || Double.isNaN(y))
			throw new RuntimeException("floating point exception");

	}

	/**
Finds the center of a collection of points.
@param int n: number of points
@param HypPoint x[]: list of points
@return center of the given collection of points
	 */
	public static HypPoint findCenter(int n,HypPoint x[]) {
		double ld,sx,sy,sz;
		int i;
		HypPoint p=new HypPoint();;

		sx=sy=sz=0;
		for(i=0;i<n;i++) {
			p.x=x[i].x; p.y=x[i].y;
			p.changeModel();
			ld=2/(1-p.x*p.x-p.y*p.y);
			sx+=ld*p.x;
			sy+=ld*p.y;
			sz+=ld-1;
		}
		ld=Math.sqrt(sz*sz-sx*sx-sy*sy);
		sx=sx/ld;
		sy=sy/ld;
		sz=sz/ld;
		p.x=sx/(1+sz);
		p.y=sy/(1+sz);
		p.changeModel();

		// catch floating point exceptions
		if (Double.isNaN(p.x) || Double.isNaN(p.y))
			throw new RuntimeException("floating point exception");

		return p;
	}

	/**
Computes the distance between a point and 'this'.
@param HypPoint p: Some point.
@return distance between 'this' and p.
	 */
	public double distance(HypPoint p) {
		Arc a=new Arc(this,p);
		Isometry f=Isometry.mapTo00(a.g);
		HypPoint h1=f.apply(p);
		HypPoint h2=f.apply(this);
		return Math.abs(Math.log(h1.y/h2.y));
	}

	/**
Computes the distance between a geodesic and 'this'.
@param Geodesic g: Some geodesic.
@return distance between 'this' and g.
	 */
	public double distance(Geodesic g) {
		return g.distance(this);
	}

	public String toString() {
		return "HypPoint: x="+x+", y="+y;
	}

}
