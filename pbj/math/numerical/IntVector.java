package pbj.math.numerical;


/**
A class that handles integral vectors.
@author Peter Brinkmann
@see IntMatrix
 */
public class IntVector {

	/** vector entries */
	public int v[];
	/** number of entries in use */
	public int n;

	/**
Constructs an IntVector.
@param int sz: number of entries
	 */
	public IntVector(int sz) {
		int i;

		v=new int[sz];
		n=sz;		// some redundancy here, but never mind...
		for(i=0;i<n;i++)
			v[i]=0;
	}

	/**
Constructs an identical copy of b.
@param IntVector b: original vector to be copied
	 */
	public IntVector(IntVector b) {
		int i;

		n=b.n;
		v=new int[b.n];

		for(i=0;i<b.n;i++)
			v[i]=b.v[i];
	}

	public String toString() {
		int i;
		String res=n+"\n";
		for(i=0;i<n;i++)
			res=res+v[i]+" ";
		res=res+"\n";
		return res;
	}

	/**
Writes 'this' to a String suitable for Maple.
	 */
	public String toMaple() {
		int i;
		String res="linalg[vector]("+n+",[";
		for(i=0;i<n;i++) {
			res=res+v[i];
			if (i<n-1)
				res=res+",";
		}
		res=res+"])\n";
		return res;
	}

	/**
Writes 'this' to a String suitable for Mathematica.
	 */
	public String toMathematica() {
		int i;
		String res="{";
		for(i=0;i<n;i++) {
			res=res+v[i];
			if (i<n-1)
				res=res+",";
		}
		res=res+"}\n";
		return res;
	}

	/**
Writes 'this' to a String suitable for MATLAB.
	 */
	public String toMATLAB() {
		int i;
		String res="[";
		for(i=0;i<n;i++) {
			res=res+v[i];
			if (i<n-1)
				res=res+" ";
		}
		res=res+"]\n";
		return res;
	}

	/**
Returns m\cdot v.
@param IntMatrix m: first factor
@param IntVector v: second vector
@return product of m and v
	 */
	public static IntVector product(IntMatrix m, IntVector v) {
		IntVector prod;
		int i,j;

		if (m.n!=v.n)
			throw new RuntimeException("different dimensions");

		prod=new IntVector(v.n);

		for(i=0;i<m.n;i++)
			for(j=0;j<m.n;j++)
				prod.v[i]+=m.a[i][j]*v.v[j];

		return prod;
	}

	/**
Returns the sum of a and b.
@param IntVector a,b: summands
@return sum of a and b
	 */
	public static IntVector sum(IntVector a, IntVector b) {
		IntVector s;
		int i;

		if (a.n!=b.n)
			throw new RuntimeException("different dimensions");

		s=new IntVector(b.n);

		for(i=0;i<a.n;i++)
			s.v[i]=a.v[i]+b.v[i];

		return s;
	}

	/**
@return number of nonzero entries.
	 */
	public int countVectorEntries() {
		int i,cnt;
		cnt=0;
		for(i=0;i<n;i++)
			if (v[i]!=0) cnt++;

		return cnt;
	}

	/**
@return sum of the entries of v.
	 */
	public int sumOfEntries() {
		int i,s;
		s=0;
		for(i=0;i<n;i++)
			s+=v[i];

		return s;
	}

}
