package pbj.math.numerical;

/**
A class that solves systems of linear equations.
@author Peter Brinkmann
 */
public class LinEq {

	private static final double TOL=0.0000000001;

	// uses LR decomposition, straight from some textbook
	private static double lrDecomp(int n,double a[][],int p[]) {
		double det,max,s,q,h;
		int i,j,k;
		det=1;
		for(k=0;k<n-1;k++) {
			max=0;
			p[k]=0;
			for(i=k;i<n;i++) {
				s=0;
				for(j=k;j<n;j++) s+=Math.abs(a[i][j]);
				if (s<TOL) return 0;
				q=Math.abs(a[i][k])/s;
				if (q>max) {
					max=q;
					p[k]=i;
				}
			}
			if (max<TOL) return 0;
			if (p[k]!=k) {
				det=-det;
				for(j=0;j<n;j++) {
					h=a[k][j];
					a[k][j]=a[p[k]][j];
					a[p[k]][j]=h;
				}
			}
			det=det*a[k][k];
			for(i=k+1;i<n;i++) {
				a[i][k]=a[i][k]/a[k][k];
				for(j=k+1;j<n;j++) a[i][j]=a[i][j]-a[i][k]*a[k][j];
			}
		}
		det=det*a[n-1][n-1];
		return det;
	}

	private static void forwardSubst(int n,double a[][],double c[],double b[],
			int p[]) {
		double h;	
		int i,j,k;
		for(k=0;k<n-1;k++)
			if (p[k]!=k) {
				h=b[k];
				b[k]=b[p[k]];
				b[p[k]]=h;
			}
		for(i=0;i<n;i++) {
			c[i]=b[i];
			for(j=0;j<i;j++)
				c[i]=c[i]-a[i][j]*c[j];
		}
	}

	private static void backwardSubst(int n,double a[][],double x[],double c[]) {
		int i,k;
		double s;
		for(i=n-1;i>=0;i--) {
			s=c[i];
			for(k=i+1;k<n;k++)
				s+=a[i][k]*x[k];
			x[i]=-s/a[i][i];
		}
	}

	/**
Solves a system of linear equations (ax=b).
@param int n: number of equations (=number of unknown variables)
@param double a[][]: coefficient matrix
@param int x[]: unknown vector for solution
@param int b[]: inhomogeneity
@return determinant of a[][]
	 */
	public static double solve(int n,double a[][],double x[],double b[]) {
		int p[]=new int[n];
		double c[]=new double[n];
		double det;
		if (Math.abs(det=lrDecomp(n,a,p))<TOL) return 0;
		forwardSubst(n,a,c,b,p);
		backwardSubst(n,a,x,c);
		return det;
	}

}
