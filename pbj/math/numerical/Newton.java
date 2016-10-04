package pbj.math.numerical;

/**
A class that implements Newton's method for solving nonlinear systems of
equations.
@author Peter Brinkmann
@see VectorFunction
 */
public class Newton {

	private static double TOL=0.0000000000001; // tolerance for computations
	private static int MAXITER=20000;
	/* if we haven't reached a solution after
   MAXITER steps, we assume that there is none.*/

	// computes the euclidean norm of the vector b
	private static double norm(int n,double b[]) {
		int i;
		double s;
		s=0;
		for(i=0;i<n;i++)
			s+=b[i]*b[i];
		return Math.sqrt(s);
	}

	/**
Solves the equation c(x)=y using Newton's method.
@param VectorFunction: c is a function from R^n to R^n.
@param double x[]: unknown vector (for solution)
@param double y[]: inhomogeneity
@return true iff no solution was found (i.e., the derivative is singular)
	 */
	public static boolean newton(VectorFunction c,int n,double x[],double y[]) {
		double a[][]=new double[n][n];	// Jacobi matrix
		double dx[]=new double[n];	// increment
		double b[]=new double[n];	// b=c(x)-y (difference from 
		//	     the desired value)
		int i,j,cnt;

		for(i=0;i<n;i++) b[i]=c.f(n,x,i)-y[i];
		cnt=0;

		while ((norm(n,b)>=TOL) && (cnt<MAXITER)) {
			for(i=0;i<n;i++)	// init Jacobi matrix
				for(j=0;j<n;j++) a[i][j]=c.df(n,x,i,j);

			// solve a\cdot dx=b, return if result is good enough
			if (Math.abs(LinEq.solve(n,a,dx,b))<TOL) return true;

			for(i=0;i<n;i++) x[i]+=dx[i];		 // update x
			for(i=0;i<n;i++) b[i]=c.f(n,x,i)-y[i];	 // update b
			cnt++;
		}

		return (norm(n,b)>=TOL); // unsuccessful if b is still too large
	}

	/**
Sets the accuracy of the result (typically a small number, e.g., 10^-10).
@param double t: new numerical tolerance
	 */
	public static void setTolerance(double t) {
		TOL=t;
	}

	/**
Sets the max number of iterations (typically a few thousand).
@param int m: new limit for number of iterations
	 */
	public static void setMaxIter(int m) {
		MAXITER=m;
	}

}
