package pbj.math.graph.train;

import pbj.math.numerical.Newton;
import pbj.math.numerical.VectorFunction;

/**
A class that implements Thurston's circle packing in a simple case.
@author Peter Brinkmann
@see VectorFunction
 */
public class CirclePack implements VectorFunction {

	private TrainTrack g;

	/**
Constructor.
@param TrainTrack gg: the graph for which a circle packing will be computed
	 */
	public CirclePack(TrainTrack gg) {
		g=new TrainTrack(gg);
	}

	private static double cosh(double x) {
		return (Math.exp(x)+Math.exp(-x))/2;
	}

	private static double sinh(double x) {
		return (Math.exp(x)-Math.exp(-x))/2;
	}

	/**
This method has to have package access so the numerical routines can access it.
	 */
	static double angleA(double ra,double rb,double rc) {
		/* computes the angle A corresponding to the vertex with associated
   radius r_a */

		double a,b,c,ca,sb,cb,sc,cc;
		a=rb+rc; b=ra+rc; c=ra+rb;
		sb=sinh(b);
		sc=sinh(c); ca=cosh(a);
		cb=cosh(b); cc=cosh(c);

		return Math.acos((cb*cc-ca)/(sb*sc));
	}

	private static double dAdra(double ra,double rb,double rc) {
		/* computes the derivative dA/dr_a */

		double a,b,c,ca,sb,cb,sc,cc;
		a=rb+rc; b=ra+rc; c=ra+rb;
		sb=sinh(b);
		sc=sinh(c); ca=cosh(a);
		cb=cosh(b); cc=cosh(c);

		return -(sb*cc+cb*sc)*(sb*sc-cb*cc+ca)/(sb*sb*sc*sc)
		/Math.sin(angleA(ra,rb,rc));
	}

	private static double dAdrb(double ra,double rb,double rc) {
		/* computes the derivative dA/dr_b */

		double a,b,c,sa,ca,sb,cb,sc,cc;
		a=rb+rc; b=ra+rc; c=ra+rb;
		sa=sinh(a); sb=sinh(b);
		sc=sinh(c); ca=cosh(a);
		cb=cosh(b); cc=cosh(c);

		return -(ca*cc-sa*sc-cb)/(sb*sc*sc)/Math.sin(angleA(ra,rb,rc));
	}

	/**
Computes the angle at a vertex.
@param int n: number of vertices (1+number of vertices of the graph)
@param double r[]: array of radii of circles at vertices
@param int v: index of a vertex
@return angle at vertex v
	 */
	public double f(int n,double r[],int v) {
		int i;
		double phi;
		phi=0;

		for(i=0;i<g.getVertices()+1;i++)
			if (r[i]<0)	r[i]=1;	/* no negative radii, please */

		if (g.getVertices()==v)
			for(i=0;i<g.getEdges();i++)
				phi+=angleA(r[v],r[g.getStart(i)],r[g.getEnd(i)]);
		else
			for(i=0;i<g.getEdges();i++) {
				if (g.getStart(i)==v)
					phi+=angleA(r[v],r[g.getVertices()],
							r[g.getEnd(i)]);
				if (g.getEnd(i)==v)
					phi+=angleA(r[v],r[g.getVertices()],
							r[g.getStart(i)]);
			}
		return 2*phi;
	}

	/**
Given a list of radii at vertices, df computes the partial
derivative of angles with respect to radii.
@param int n: number of vertices
@param double r[]: radii at vertices
@param int i: index of vertex
@param int j: index of radius
@return partial derivative dA_i/dr_j.
	 */
	public double df(int n,double r[],int i,int j) {
		int k;
		double dphi;

		for(k=0;k<g.getVertices()+1;k++)
			if (r[k]<0)	r[k]=1;

		dphi=0;
		if (i==j)
			if (i==g.getVertices())
				for(k=0;k<g.getEdges();k++)
					dphi+=dAdra(r[i],r[g.getStart(k)],
							r[g.getEnd(k)]);
			else
				for(k=0;k<g.getEdges();k++) {
					if (g.getStart(k)==i)
						dphi+=dAdra(r[i],r[g.getVertices()],
								r[g.getEnd(k)]);
					if (g.getEnd(k)==i)
						dphi+=dAdra(r[i],r[g.getVertices()],
								r[g.getStart(k)]);
				}
		else
			if (i==g.getVertices())
				for(k=0;k<g.getEdges();k++) {
					if (g.getStart(k)==j)
						dphi+=dAdrb(r[i],r[j],
								r[g.getEnd(k)]);
					if (g.getEnd(k)==j)
						dphi+=dAdrb(r[i],r[j],
								r[g.getStart(k)]);
				}
			else if (j==g.getVertices())
				for(k=0;k<g.getEdges();k++) {
					if (g.getStart(k)==i)
						dphi+=dAdrb(r[i],r[j],
								r[g.getEnd(k)]);
					if (g.getEnd(k)==i)
						dphi+=dAdrb(r[i],r[j],
								r[g.getStart(k)]);
				}
			else
				for(k=0;k<g.getEdges();k++) {
					if ((g.getStart(k)==i) && (g.getEnd(k)==j))
						dphi+=dAdrb(r[i],r[j],r[g.getVertices()]);
					if ((g.getStart(k)==j) && (g.getEnd(k)==i))
						dphi+=dAdrb(r[i],r[j],r[g.getVertices()]);
				}
		return 2*dphi;
	}

	/**
Computes the radii of a circle packing.
@param double r[]: array of size #vertices+1 for storing radii
	 */
	public void pack(double r[])
	{	int i;
	double y[]=new double [r.length];
	for(i=0;i<g.getVertices()+1;i++) {
		r[i]=1;		/* 1 is a sensible radius for starters */
		y[i]=2*Math.PI;	/* we want a total angle of 2pi around */
	}			/* each vertex */
	if (Newton.newton(this,g.getVertices()+1,r,y))
		throw new RuntimeException("circle packing didn't succeed");
	}

}
