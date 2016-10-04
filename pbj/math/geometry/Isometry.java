package pbj.math.geometry;

/**
A class that represents isometries of H^2 in terms of linear fractional
transformations of the form (az+b)/(cz+d).
@author Peter Brinkmann
@see HypPoint
*/
public class Isometry {

	public double a;
	public double b;
	public double c;
	public double d;

	private static final double TOL=0.0000000000000000000000000001;
		// tolerance for numerical computations

public Isometry() {
}

public Isometry(double aa,double bb,double cc, double dd) {
	this();

	// catch floating point exceptions
	if (Double.isNaN(aa) || Double.isNaN(bb)
	    || Double.isNaN(cc) || Double.isNaN(dd))
		throw new RuntimeException("floating point exception");

	a=aa;
	b=bb;
	c=cc;
	d=dd;
}

/**
makes sure the determinant of f is 1.
*/
public void normalize() {
	double det;
	det=a*d-b*c;
	if (det<0)
		throw new RuntimeException("negative determinant");
	det=Math.sqrt(det);
	a=a/det;
	b=b/det;
	c=c/det;
	d=d/det;

	// catch floating point exceptions
	if (Double.isNaN(a) || Double.isNaN(b)
	    || Double.isNaN(c) || Double.isNaN(d))
		throw new RuntimeException("floating point exception");
}

/**
Returns the image of p under 'this'.
@param HypPoint p: argument
@return image of p
*/
public HypPoint apply(HypPoint p) {
	double hn,fx,fy;

	hn=(p.x*c+d)*(p.x*c+d)+p.y*(p.y)*c*c;
	fx=(p.x*a+b)*(p.x*c+d)+p.y*(p.y)*a*c;
	fy=p.y*((p.x*c+d)*a-(p.x*a+b)*c);

	return new HypPoint(fx/hn,fy/hn);
}

/**
Returns the image of g under 'this'.
@param Geodesic g: argument
@return image of g
*/
public Geodesic apply(Geodesic g) {
	Arc a;
	double q=Math.sqrt(0.5);

	if (g.r<=0)
		a=apply(new Arc(new HypPoint(g.c,1),new HypPoint(g.c,100)));
	else
		a=apply(new Arc(new HypPoint(g.c-g.r*q,g.r*q),
				new HypPoint(g.c+g.r*q,g.r*q)));
	return a.g;
}

/**
Returns the image of a under 'this'.
@param Arc a: argument
@return image of a
*/
public Arc apply(Arc a) {
	return new Arc(apply(a.p1),apply(a.p2));
}

/**
Composes 'this' with f2, writes result to 'this'.
@param Isometry f2: second map in composition
*/
public void comp(Isometry f2) {
	double aa,bb,cc,dd;
	aa=a;
	bb=b;
	cc=c;
	dd=d;
	a=aa*f2.a+bb*f2.c;
	b=aa*f2.b+bb*f2.d;
	c=cc*f2.a+dd*f2.c;
	d=cc*f2.b+dd*f2.d;
	normalize();
}

/**
Inverts 'this'.
*/
public void invert() {
	double tmp;
	tmp=a;
	a=d;
	d=tmp;
	c=-c;
	b=-b;
}

/**
Finds an isometry that takes g1 to g2. If g1 and g2 are disjoint, then the
result is a translation along the common perpendicular. If g1 and g2 intersect
in the interior of H^2, then the result fixes the intersection. If g1 and g2
are asymptotic, then the result could be any isometry taking g1 to g2.
@param Geodesic g1,g2: geodesics that are supposed to get mapped to each other
@return isometry that maps g1 to g2.
*/
public static Isometry identifyGeods(Geodesic g1,Geodesic g2) {
	Isometry h,f;
	Geodesic gg=new Geodesic();
	Geodesic p=new Geodesic();
	HypPoint p1=new HypPoint();
	HypPoint p2=new HypPoint();

	// do g1 and g2 have a common perpendicular?
        if (!g1.commonPerp(p,g2)) {	// no: find a geodesic gg disjoint from
					// g1 and g2, then take g1 to gg to g2
                gg.c=Math.abs(g1.c)+Math.abs(g1.r)+
				Math.abs(g2.c)+Math.abs(g1.r)+2;
                gg.r=1;
                h=Isometry.identifyGeods(g1,gg);
                f=Isometry.identifyGeods(gg,g2);
                f.comp(h);
		g1.intersect(g2,p1);
		if (p1.y<TOL)
			return f;
		p2=f.apply(p1);
		h=Isometry.translation(g2,p2,p1);
		h.comp(f);
                return h;
        }

	g1.intersect(p,p1);
	g2.intersect(p,p2);
	return Isometry.translation(p,p1,p2);
}

/**
Returns an isometry that maps the geodesic g to the vertical line at x=0.
@param Geodesic g: Geodesic to be mapped to Geodesic(0,0)
@param The Isometry that takes g to Geodesic(0,0).
*/
public static Isometry mapTo00(Geodesic g) {
	double r,s;

	if (g.r<TOL)
		return new Isometry(1,-g.c,0,1);

	r=g.c-g.r;
	s=g.c+g.r;
	if ((r-s)>=TOL)
		return new Isometry(1,-r,1,-s);
	else
		return new Isometry(1,-r,-1,s);
}

/**
<p>Returns the translation by dist along the geodesic g.</p>
<p><em>Convention:</em> If dist>0 and g is a vertical line,
then the direction  is upward. If dist>0 and g is a half circle,
then the translation is from left to right.</p>
@param Geodesic g: The axis of the translation
@param double dist: The (signed) translation length
@return An Isometry that is the translation along g by dist
*/
public static Isometry translation(Geodesic g,double dist) {
	Isometry f,h;

	f=Isometry.mapTo00(g);
	h=new Isometry(Math.exp(dist),0,0,1);
	h.comp(f);
	f.invert();
	f.comp(h);
	return f;
}

/**
<p>Returns the translation along the geodesic g that takes the projection of
p1 (onto g) to the projection of p2.</p>
@param Geodesic g: The axis of the translation
@param HypPoint p1,p2: Two points.
@return An Isometry that is the appropriate translation along g.
*/
public static Isometry translation(Geodesic g,HypPoint p1,HypPoint p2) {
	HypPoint h1=g.project(p1);
	HypPoint h2=g.project(p2);
	Isometry f;

	f=Isometry.mapTo00(g);
	h1=f.apply(h1);
	h2=f.apply(h2);
	if ((h1.y>TOL) && (h2.y>TOL))
		return translation(g,Math.log(h2.y/h1.y));
	else
		return new Isometry(1,0,0,1);
}

/**
Reflects a point p in a geodesic g.
@param g: The axis of the reflection
@param p: The point to be reflected
@return Image of p under reflection with respect to g.
*/
public static HypPoint reflect(Geodesic g,HypPoint p) {
	Isometry f=Isometry.mapTo00(g);
	HypPoint p1=f.apply(p);

	p1.x=-p1.x;
	f.invert();
	return f.apply(p1);
}

/**
Reflects an arc a in a geodesic g.
@param g: The axis of the reflection
@param a: The arc to be reflected
@return Image of a under reflection with respect to g.
*/
public static Arc reflect(Geodesic g,Arc a) {
	return new Arc(Isometry.reflect(g,a.p1),Isometry.reflect(g,a.p2));
}

/**
Reflects geodesic gg in a geodesic g.
@param g: The axis of the reflection
@param gg: The geodesic to be reflected
@return Image of gg under reflection with respect to g.
*/
public static Geodesic reflect(Geodesic g,Geodesic gg) {
	Isometry f=Isometry.mapTo00(g);
	Geodesic g1=f.apply(gg);

	g1.c=-g1.c;
	f.invert();
	return f.apply(g1);
}

/**
Copies f2 to 'this'.
@param Isometry f2: original
*/
public void copyIsom(Isometry f2) {
	a=f2.a;
	b=f2.b;
	c=f2.c;
	d=f2.d;

	if (Double.isNaN(a) || Double.isNaN(b)
	    || Double.isNaN(c) || Double.isNaN(d))
		throw new RuntimeException("floating point exception");
}

public String toString() {
	return "Isometry: a="+a+", b="+b+", c="+c+", d="+d;
}

}

