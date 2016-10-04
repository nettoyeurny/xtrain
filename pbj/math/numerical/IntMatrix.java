package pbj.math.numerical;

import java.text.DecimalFormat;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
A class that handles integral matrices.
@author Peter Brinkmann
@see IntVector
 */
public class IntMatrix {

	/** matrix entries */
	public double a[][];
	/** size of submatrix in use */
	public int n;
	
	/**
DecimalFormat that rounds eigenvalues to the last accurate digit.
	 */
	public static final DecimalFormat PFForm = new DecimalFormat("#.000000");
	// matches TOLERANCE

	public String toString() {
		int i,j;
		String res="";
		res=res+n+"\n";
		for(i=0;i<n;i++) {
			for(j=0;j<n;j++)
				res=res+a[i][j]+" ";
			res=res+"\n";
		}
		res=res+"\n";
		return res;
	}

	/**
Writes the matrix to a string suitable for Maple.
	 */
	public String toMaple() {
		int i,j;
		String res="linalg[matrix]("+n+","+n+",[";
		for(i=0;i<n;i++)
			for(j=0;j<n;j++) {
				res=res+a[i][j];
				if ((i<n-1) || (j<n-1))
					res=res+",";
			}
		res=res+"])\n";
		return res;
	}

	/**
Writes the matrix to a string suitable for Mathematica.
	 */
	public String toMathematica() {
		int i,j;
		String res="{";
		for(i=0;i<n;i++) {
			res=res+"{";
			for(j=0;j<n;j++) {
				res=res+a[i][j];
				if (j<n-1)
					res=res+",";
			}
			res=res+"}";
			if (i<n-1)
				res=res+",";
		}
		res=res+"}\n";
		return res;
	}

	/**
Writes the matrix to a string suitable for MATLAB.
	 */
	public String toMATLAB() {
		int i,j;
		String res="[";
		for(i=0;i<n;i++) {
			for(j=0;j<n;j++) {
				res=res+a[i][j];
				if (j<n-1)
					res=res+" ";
			}
			if (i<n-1)
				res=res+"; ";
		}
		res=res+"]\n";
		return res;
	}

	/**
Constructs a matrix with the given dimension.
@param int sz: dimension of matrix
	 */
	public IntMatrix(int sz) {
		int i,j;

		a=new double[sz][sz];
		n=sz;
		for(i=0;i<n;i++)
			for(j=0;j<n;j++)
				a[i][j]=0;
	}

	/**
Constructs a matrix identical to b.
@param IntMatrix b: original matrix
	 */
	public IntMatrix(IntMatrix b) {
		int i,j;

		n=b.n;
		a=new double[b.n][b.n];

		for(i=0;i<b.n;i++)
			for(j=0;j<b.n;j++)
				a[i][j]=b.a[i][j];
	}

	/**
Multiplies a and b and returns the result.
@param IntMatrix a,b: factors
@return product of a and b
	 */
	public static IntMatrix mult(IntMatrix a, IntMatrix b) {
		IntMatrix target;
		int i,j,k;
		if (a.n!=b.n)
			throw new RuntimeException("different dimensions");

		target=new IntMatrix(a.n);

		for(i=0;i<a.n;i++)
			for(j=0;j<a.n;j++)
				for(k=0;k<a.n;k++)
					target.a[i][j]+=a.a[i][k]*b.a[k][j];

		return target;
	}

	/**
Adds a and b and returns the result.
@param IntMatrix a,b: summands
@return sum of a and b
	 */
	public static IntMatrix add(IntMatrix a, IntMatrix b) {
		IntMatrix target;
		int i,j;
		if (a.n!=b.n)
			throw new RuntimeException("different dimensions");

		target=new IntMatrix(a.n);

		for(i=0;i<a.n;i++)
			for(j=0;j<a.n;j++)
				target.a[i][j]=a.a[i][j]+b.a[i][j];
		return target;
	}

	/**
@return number of nonzero entries of 'this'.
	 */
	public int countMatrixEntries() {
		int i,j,c;
		c=0;
		for(i=0;i<n;i++)
			for(j=0;j<n;j++)
				if (a[i][j]!=0) c++;

		return c;
	}

	/**
@return true iff 'this' is irreducible.
	 */
	public boolean isIrreducible() {
		IntMatrix iter=new IntMatrix(this); // for powers of this
		IntMatrix accu=new IntMatrix(this); // accumulates sum of powers of this
		int cnt,old; // counters of nonzero entries in accu
		old=cnt=0;
		while (old!=(cnt=accu.countMatrixEntries()))
			// has accu gained nonzero entries?
		{	old=cnt;
		iter=IntMatrix.mult(iter, this);
		accu=IntMatrix.add(accu, iter);
		}

		return (cnt==n*n); // return true iff all entries of accu are nonzero
	}


	/**
Computes the PF-eigenvalue and the PF-eigenvector of an irreducible matrix.
In order to keep the computation as simple as possible, exception handling
for reducible maps has been omitted.
@param double v[]: array of size n that contains a PF-eigenvector
@return PF-eigenvalue of this
	 */
	public double eigenPair(double v[]) {
		if (!isIrreducible())
			throw new RuntimeException("reducible matrix");

		reducedCharPoly();
		
		Matrix m = getMatrix();
		EigenvalueDecomposition e = m.eig();
		Matrix ed = e.getD();
		Matrix ev = e.getV();
		
		int i0 = 0;
		double q = 0;
		double sig = 1;
		
		for(int i = 0; i<n; i++) {
			if (ed.get(i, i)>q) {
				q = ed.get(i, i);
				i0 = i;
				sig = ev.get(i, i)>0 ? 1 : -1;
			}
		}
		
		for(int i = 0; i<n; i++) {
			v[i] = ev.get(i, i0)*sig;
		}
		return q;
	}

	private Matrix getMatrix() {
		double[][] b = new double[n][n];
		for(int i=0; i<n; i++)
			for(int j=0; j<n; j++)
				b[i][j] = a[i][j];
		
		Matrix m = new Matrix(b);
		return m;
	}
	
	/**
@return Perron-Frobenius eigenvalues of an irreducible matrix.
	 */
	public double eigenValue() {
		return eigenPair(new double[n]);
	}

	public long[] reducedCharPoly() {
		final double eps = 1e-8;
		EigenvalueDecomposition ed = new EigenvalueDecomposition(getMatrix());
		double[] re = ed.getRealEigenvalues(), im = ed.getImagEigenvalues();
		int m = 1;
		while (m<n+1) m *= 2;
		Complex[] p = new Complex[m];
		Complex[] q = new Complex[m];
		for(int i=0; i<m; i++) {
			p[i] = q[i] = new Complex(0, 0);
		}
		p[0] = new Complex(1, 0);
		q[1] = new Complex(1, 0);
		int deg = 1;
		for(int i=0; i<n; i++) {
			if (Math.abs(im[i])<eps && Math.abs(Math.abs(re[i])-1)<eps) continue;
			Complex e = new Complex(-re[i], -im[i]);
			if (e.abs()<eps) continue;
			q[0] = e;
			Complex[] r = FFT.convolve(p, q);
			for(int j=0; j<m; j++) {
				p[j] = r[j];
			}
			deg++;
		}
		long[] res = new long[deg];
		for(int k=0; k<deg; k++) {
			res[deg-k-1] = Math.round(p[k].re());
		}
		return res;
	}
	
	public static String polyString(long[] p) {
		String s = "";
		for(int i=0; i<p.length; i++) {
			long a = p[i];
			int d = p.length-i-1;
			if (a==0) continue;
			if (s.length()>0) {
				if (a>0)
					s += "+";
				else {
					s += "-";
					a = -a;
				}
			}
			if (d>0) {
				if (a==-1) s += "-";
				else if (a!=1) s += a;
				s += "x";
				if (d>1) s += "^"+d;
			} else {
				s += a;
			}
		}
		return s;
	}
}
