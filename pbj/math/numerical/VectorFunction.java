package pbj.math.numerical;

/**
Interface for differentiable vector functions.
@author Peter Brinkmann
@see Newton
 */
public interface VectorFunction {

	/**
The i-th entry of the function.
	 */
	public double f(int n,double r[],int i);	// f_i(r)

	/**
The partial derivative of the j-th entry with respect to the i-th
variable.
@param int n: number of functions (=number of variables)
@param double r[]: evaluate at r[]
@param int i: index of entry in r[]
@param int j: index of entry in f
@return partial derivative (d/dr_i f)_j
	 */
	public double df(int n,double r[],int i,int j);// (d/dr_i f)_j

}
