/* Mesquite source code.  Copyright 2002-2003 D. Swofford & W. Maddison and D. Maddison. All Rights reserved.
Version 1.0, September 2003. */
package mesquite.lib;
import java.util.Random;
/* ======================================================================== */
/* translated april 2002 from C translation by D. Swofford of Algol version by Brent */
public class Optimizer {
	static final int MAX_ITER = 50;
	static final double CGOLD = 0.3819660113;		/* = (3 - sqrt(5))/2 */
	//static long			praxisSeed;
	public  int			nl;				/* counter for number of line minimizations performed */
	 double		dmin; //most of these changed to instance variables from statics  WPM Nov '09
	 double		ldt;
	 double		qf1;
	 double		qd0, qd1;
	 double		m2, m4;
	 double		esmall;			/* DLS 03feb2001: renamed 'small' to 'esmall' to avoid conflict with Win32
										   "#define small char" */
	 double		toler, htol;	/* global versions of Praxis arguments */
	 int			n;
	 double		gfx;
//	static double[]		xnew;
	 double		machEps;
	 double		DBL_EPSILON = 2.2204460492503131e-16;
	 boolean FOREVER = true;
	 double[][]	v;
	Random rng;
	double[] d, q0, q1, xnew, y, z;
	Evaluator evaluator;
	double t = 1e-10; //for LocalMin
	double eps = DBL_EPSILON; //for LocalMin 16-13
	double tol = 1e-4; //original e-4
	static final double h = 1.0;
	int dimension;
	Abortable abortable;
	double acceptableValue = MesquiteDouble.unassigned;
	
	double[] lowerBoundsSearched, upperBoundsSearched;//added WPM April 2002
	
	public Optimizer(Evaluator evaluator){
		rng = new Random(1);
		this.evaluator = evaluator;
		/*if (dimension!=1){
			v = new double[dimension][dimension];
			d = new double[dimension];
			q0 = new double[dimension];
			q1 = new double[dimension];
			xnew = new double[dimension];
			y = new double[dimension];
			z = new double[dimension];
		}*/
		this.dimension = 0;
	}
	public void setDimension(int n){//added WPM April 2002
		if (dimension!=n){
			dimension = n;
			v = new double[dimension][dimension];
			d = new double[dimension];
			q0 = new double[dimension];
			q1 = new double[dimension];
			xnew = new double[dimension];
			y = new double[dimension];
			z = new double[dimension];
			lowerBoundsSearched = new double[dimension];
			upperBoundsSearched = new double[dimension];
		}
		dimension = n;
	}
	public int getDimension(){//added WPM April 2002
		return dimension;
	}
	void checkBoundsDimension(double[] x){//added WPM April 2002
		if (x==null)
			return;
		if (lowerBoundsSearched == null) {
			lowerBoundsSearched = new double[x.length];
			upperBoundsSearched = new double[x.length];
			for (int i=0; i<x.length; i++){
				lowerBoundsSearched[i]=x[i];
				upperBoundsSearched[i]=x[i];
			}
		}
		else if (x.length>lowerBoundsSearched.length) {
			double[] lbs = new double[x.length];
			double[] ubs = new double[x.length];
			for (int i=0; i<x.length; i++){
				if (i>=lowerBoundsSearched.length) {
					lbs[i]=x[i];
					ubs[i]=x[i];
				}
				else {
					lbs[i]=lowerBoundsSearched[i];
					ubs[i]=upperBoundsSearched[i];
				}
			}
		}
	}
	void checkBoundsSearched(double[] x){//added WPM April 2002
		if (x==null)
			return;
		checkBoundsDimension(x);
		for (int i=0; i<x.length; i++){
			double xi = x[i];
			if (xi<lowerBoundsSearched[i]) 
				lowerBoundsSearched[i] = xi;
			if (xi>upperBoundsSearched[i]) 
				upperBoundsSearched[i] = xi;
		}
	}

	public double[] getLowerBoundsSearched(){//added WPM April 2002
		return lowerBoundsSearched;
	}
	public double[] getUpperBoundsSearched(){ //added WPM April 2002
		return upperBoundsSearched;
	}
	
	public double[][] grid(double min1, double max1, double min2, double max2, int divisions, Object extraInfo){
		double[][] g = new double[divisions][divisions];
		double[] x = new double[2];
		for (int i = 0; i<divisions; i++){
			x[0] =i* (max1-min1)/divisions + min1;
			for (int j = 0; j<divisions; j++){
				x[1] =j* (max2-min2)/divisions + min2;
				double fcn = evaluator.evaluate(x, extraInfo);
				g[i][j] = fcn;
				//System.out.println(x[0] + "\t" + x[1] + "\t" + fcn);
			}
		}
		return g;
	}
	
	
	public boolean valueAcceptable(double fx){  //DRM
		if (MesquiteDouble.isCombinable(acceptableValue) && fx<=acceptableValue) {
			return true;
		}
		return false;
	}
	public boolean getAbort(){  //DRM
		if (abortable!=null && abortable.isAborted()) {
			return true;
		}
		return false;
	}

	public double optimize(double[] point, Object extraInfo){ //multi-dimensional; p should contain initial guess
		return optimize(point,MesquiteDouble.unassigned, extraInfo,null);
	}

	public double optimize(double[] point, double acceptableValue, Object extraInfo, Abortable abortable){ //multi-dimensional; p should contain initial guess
		this.acceptableValue = acceptableValue;
		this.abortable = abortable;
		if (point.length != dimension)
			setDimension(point.length);
		return Praxis(tol, h, point.length,point, evaluator, extraInfo);
	}
	
	public double optimize(MesquiteDouble p, double lower, double upper, Object extraInfo){ //one dimensional; p should contain initial guess
		if (!p.isCombinable())
			p.setValue(0.1);
		return LocalMin(lower, upper,  eps,	t, evaluator, extraInfo, p)	; //t and eps need to be defined
	}
	
	private double Praxis_FLin(int j, double lambda, double[] x, double[] q0, double[] q1, double[][] v,
                      Evaluator f, Object dataPtr)
	{
	int		i;
	double	qa, qb, qc;

	if (j >= 0)
		{
		/* Try point in linear search */
		for (i = 0; i < n; i++)
			xnew[i] = x[i] + lambda*v[i][j];
		}
	else
		{
		/* Search along a parabolic space curve */
		qa = lambda*(lambda - qd1)/(qd0*(qd0 + qd1));
		qb = (lambda + qd0)*(qd1 - lambda)/(qd0*qd1);
		qc = lambda*(lambda + qd0)/(qd1*(qd0 + qd1));
		
		/* Previous three points were stored as follows: x' in q0, x'' in x, and x''' in q1 */
		for (i = 0; i < n; i++)
			xnew[i] = qa*q0[i] + qb*x[i] + qc*q1[i];
		}

	checkBoundsSearched(xnew); //added WPM April 2002
	return f.evaluate(xnew, dataPtr);
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	LocalMin
|
|	Finds a private minimum of a function of one variable using Brent's (1973) method.
|
|	This function is reentrant.
*/


public double LocalMin(
  double		a,			/* on input, (a,b) must bracket a private minimum */
  double		b,
  double		eps,		/* t and eps define tol = eps|x| + t, f is never evaluated at two points */
  double		t,			/*   closer together than tol;  eps should be > sqrt(DBL_EPSILON)        */
  Evaluator	f,			/* function to minimize */
  Object 	dataPtr,	/* pointer to data to be passed to 'f' */
  MesquiteDouble		px)		/* value of x when f(x) is minimal */
	{
	double		e, m, p, q, r, x, tol, t2, u, v, w, fu, fv, fw, fx,
				d = 0.0;	/* shuts up bogus lint warning */
	int			iter;
	MesquiteDouble xA = new MesquiteDouble();
	MesquiteDouble uA = new MesquiteDouble();

	v = w = x = a + CGOLD*(b - a);
	e = 0.0;
	xA.setValue(x);
	fv = fw = fx =f.evaluate(xA, dataPtr);
	x = xA.getValue();
	if (getAbort() || valueAcceptable(fx))   //DRM April 2005
		return fx;
	//RETURN_IF_ABORTED 
	

	/* Main loop */

	for (iter = 0; iter < MAX_ITER; iter++)
		{
		m = 0.5*(a + b);
		tol = eps*Math.abs(x) + t;
		t2 = 2.0*tol;

		/* Check stopping criterion */
		if (Math.abs(x - m) <= t2 - 0.5*(b - a))
			break;

		p = q = r = 0.0;
		if (Math.abs(e) > tol)
			{
			/* Fit parabola (trial) */
			r = (x - w)*(fx - fv);
			q = (x - v)*(fx - fw);
			p = (x - v)*q - (x - w)*r;
			q = 2.0*(q - r);
			if (q > 0.0)
				p = -p;
			else
				q = -q;
			r = e;
			e = d;	/* lint complains about possible use of 'd' before being set, but it's not a problem because e=0
			           first time through, so "Math.abs(e) > tol" test fails */
			}
			
		/* Take parabolic-interpolation or golden-section step (note that Brent's Algol procedure had an error,
		   p > q*(a-x) is correct) */
		if ((Math.abs(p) < Math.abs(0.5*q*r)) && (p > q*(a - x)) && (p < q*(b - x)))
			{
			/* Parabolic interpolation step */
			d = p/q;
			u = x + d;
			/* Don't evaluate f too close to a or b */
			if ((u - a < t2) || (b - u < t2))
				d = (x < m) ? tol : -tol;
			}
		else
			{
			/* "Golden section" step */
			e = ((x < m) ? b : a) - x;
			d = CGOLD*e;
			}

		/* Don't evaluate f too close to x */
		if (Math.abs(d) >= tol)
			u = x + d;
		else if (d > 0.0)
			u = x + tol;
		else 
			u = x - tol;

		uA.setValue(u);
		fu = f.evaluate(uA, dataPtr);
		u = uA.getValue();
		if (getAbort() || valueAcceptable(fx))   //DRM April 2005
			return fx;
		//RETURN_IF_ABORTED 
		
		/* Update a, b, v, w, and x */
		if (fu <= fx)
			{
			if (fu <= fx)
				{
				if (u < x)
					b = x;
				else
					a = x;
				v = w;
				fv = fw;
				w = x;
				fw = fx;
				x = u;
				fx = fu;
				}
			}
		else
			{
			if (u < x)
				a = u;
			else
				b = u;
			if ((fu <= fw) || (w == x))
				{
				v = w;
				fv = fw;
				w = u;
				fw = fu;
				}
			else if ((fu <= fv) || (v == x) || (v == w))
				{
				v = u;
				fv = fu;
				}
			}
		}
	px.setValue(x);
	return fx;
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	TransformUnboundedToLU
|
|	Maps y in R to x in [g,h] using the transformation x = g + (h - g)sin^2 y, where g and h are lower and upper
|	bounds, respectively, on x.
|
|	This transformation is used to allow unconstrained minimization of a function with simple linear bounds on the
|	variables; see: Box, M. J.  1966.  A comparison of several current optimization methods, and the use of
|	transformations in constrained problems.  Computer Journal 9:67-77.
*/
	
public double TransformUnboundedToLU(MesquiteDouble py, double g, double h, boolean oldSystem)
	{
	if (oldSystem)
		{
		/* Brute force adjustment of out-of-bounds parameter values */
		if (py.getValue() < g)
			py.setValue(g);
		else if (py.getValue() > h)
			py.setValue(h);
		return py.getValue();
		}
	else
		{
		double sin_y = Math.sin(py.getValue());
		return g + (h - g)*sin_y*sin_y;
		}
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	TransformLUtoUnbounded
|
|	Maps x in [g,h] to y in R using the inverse of the transformation x = g + (h - g)sin^2 y, where g and h are lower
|	and upper bounds, respectively, on x.
*/

public double TransformLUtoUnbounded(double x, double g, double h, boolean oldSystem)
	{
	if (oldSystem)
		{
		return x;				/* No transformation was actually done */
		}
	else
		{
		double		y, z;

		y = (g - x)/(g - h);	/* (Caller guarantees that h > g) */

		if (y < 0.0)			/* Protect against roundoff error */				
			y = 0.0;

		z = Math.sqrt(y);	
		if (z > 1.0)			/* Should never happen, but prevent death in asin just in case */
			z = 1.0;

		return Math.asin(z);
		}
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	TransformUnboundedToLower
|
|	Maps y in R to x in [g,infinity] using the transformation x = exp(y) + g, where g is a lower bound on x.
|
|	This transformation is used to allow unconstrained minimization of a function with simple linear bounds on the
|	variables; see: Box, M. J.  1966.  A comparison of several current optimization methods, and the use of
|	transformations in constrained problems.  Computer Journal 9:67-77.
|
|	TO DO: If/when we abandon the 'oldSystem' option for brute-force adjustment of out-of-bounds parameter values, we
|	       can pass y directly (rather than py).
*/
	
public double TransformUnboundedToLower(MesquiteDouble py, double g, boolean oldSystem)
	{
	if (oldSystem)
		{
		if (py.getValue() < g)
			py.setValue(g);
		return py.getValue();
		}
	else
		{
		double y = py.getValue();
		if (y < -300.0)				/* avoid underflow potential */
			y = -300.0;

		return Math.exp(y) + g;
		}
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	TransformLowerToUnbounded
|
|	Maps x in [g,infinity] to y in R using the inverse of the transformation x = exp(y) + g where g is a lower bound
|	on x.
*/

public double TransformLowerToUnbounded(double x, double g, boolean oldSystem)
	{
	if (oldSystem)
		{
		return x;					/* No transformation was actually done */
		}
	else
		{
		double	z = x - g;

		if (z < DBL_EPSILON)		/* Avoid possible numerical problems */
			z = DBL_EPSILON;
		
		return Math.log(z);
		}
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	Praxis_Min
|
|	Minimizes the objective function from point x in the direction v(*,j) (if j >= 0), or performs a quadratic search
|	in the plane defined by q0, q1, and x (if j < 0).
*/

private void Praxis_Min(
  int			j,			/* column of direction matrix (or <0 flag for quadratic search) */
  int			nits,		/* number of times an attempt is made to halve the interval */
  double[]		x,			/* current point */
  double[]		q0,		/* previous point */
  double[]		q1,		/* second previous point */
  double[][]	v,		/* direction-set matrix */
  MesquiteDouble	pd2,		/* approximation to half f'' (or zero) */
  MesquiteDouble	px1,		/* x1 = estimate of distance to minimum, returned as actual distance found */
  double		f1,			/* if fk=true, FLin(x1), otherwise ignored */
  boolean			fk,			/* flag (see above) */
  Evaluator	f,			/* function to minimize */
  Object	dataPtr)	/* pointer to data to be passed to 'f' */
	{
	boolean	dz, success;
	int			i, k;
	double		x1, x2, xm, f0, f2, fm, d1, d2, t2, s, sf1, sx1;
	
	/* Copy args passed by reference to locals (will pass back at end) */
	d2 = pd2.getValue();
	x1 = px1.getValue();
	
	sf1 = f1;
	sx1 = x1;
	k = 0;
	xm = 0.0;
	f0 = fm = gfx;
	dz = (d2 < machEps);	/* if true, we need f''(0) */
	
	/* Find step size */
	s = 0.0;
	for (i = 0; i < n; i++)
		{
//#		if 0
		/* DLS 22jun00: Use of the temporary variable here seems to be important for numerical accuracy (not sure why,
		   but different code is generated in both PPC Metrowerks and Dec Alpha compilers). */
//		s += x[i]*x[i];
//#		else	/* use this for release version */
		double xi = x[i];
		s += xi*xi;
//#		endif
		}
	s = Math.sqrt(s);

	t2 = m4*Math.sqrt(Math.abs(gfx)/(dz ? dmin : d2) + s*ldt) + m2*ldt;
	s = m4*s + toler;
	if (dz && (t2 > s))
		t2 = s;
	if (t2 < esmall)
		t2 = esmall;
	if (t2 > 0.01*htol)
		t2 = 0.01*htol;

	if (fk && (f1 <= fm))
		{
		xm = x1;
		fm = f1;
		}

	if (!fk || (Math.abs(x1) < t2))
		{
		x1 = (x1 >= 0.0) ? t2 : -t2;

		f1 = Praxis_FLin(j, x1, x, q0, q1, v, f, dataPtr);
		if (getAbort())  //DRM Apr 2005
			return; //VOID_RETURN_IF_ABORTED 
		}
	if (f1 <= fm)
		{
		xm = x1;
		fm = f1;
		}
	
	/* Find a distance x2 ("lambda*") that approximately minimizes f in the chosen direction */
	do	{
		if (dz)
			{
			/* Evaluate FLin at another point and estimate the second derivative */
			x2 = (f0 < f1) ? -x1 : 2.0*x1;
			f2 = Praxis_FLin(j, x2, x, q0, q1, v, f, dataPtr);
			if (getAbort())  //DRM Apr 2005
				return; //VOID_RETURN_IF_ABORTED 
			if (f2 <= fm)
				{
				xm = x2;
				fm = f2;
				}
			d2 = (x2*(f1 - f0) - x1*(f2 - f0))/(x1*x2*(x1 - x2));
			dz = false;		/* DLS 11jun00: see explanation below */
			}
	
		/* Estimate first derivative at 0 */	
		d1 = (f1 - f0)/x1 - x1*d2;

		/* DLS 11jun00: I think the statement below on Brent's page 160 is erroneous.  If dz remains true, and this
		                loop executes again, then x2 will be reset to -x1 at the top of the loop, but x1 is never
		                changed within the loop. Since neither x1 nor x2 changes, additional iterations of the loop
		                are just wasted calculation requiring pointless function evaluations.  I think what Brent
		                *meant* to do was to set dz to false, since we now have an estimate of the second derivative
		                and don't need to redo this again if the loop continues to iterate.
		dz = true;
		*/
			//dls 10 apr: but why would dz = true below then be necessary?????????

//#		if defined(PAUP)
			/* This is necessary to reproduce B4a results with aml.usingOldBoundSys=true */
//			if (aml.usingOldBoundSys)
//				dz = true;
//#		endif

		/* Predict minimum */
		if (d2 <= esmall)
			x2  = (d1 < 0.0) ? htol : -htol;
		else
			{
			x2 = -0.5*d1/d2;
			if (Math.abs(x2) > htol)
				x2 = (x2 > 0.0) ? htol : -htol;
			}

		/* Evaluate f at predicted minimum */
		do	{
			f2 = Praxis_FLin(j, x2, x, q0, q1, v, f, dataPtr);
			if (getAbort())  //DRM Apr 2005
				return; //VOID_RETURN_IF_ABORTED 
			success = true;
			if ((k < nits) && (f2 > f0))
				{
				/* No success so halve interval and try again */
				success = false;
				k++;
				if ((f0 < f1) && (x1*x2 > 0.0))
					break;
				x2 *= 0.5;
				}
			}
			while (!success);
		}		
		while (!success);

	nl++;	/* increment counter for number of line searches */

	if (f2 > fm)
		x2 = xm;
	else
		fm = f2;
		
	/* Get new estimate of second derivative */
	if (Math.abs(x2*(x2 - x1)) > esmall)
		d2 = (x2*(f1 - f0) - x1*(fm - f0))/(x1*x2*(x1 - x2));
	else if (k > 0)
		d2 = 0.0;
	if (d2 < esmall)
		d2 = esmall;

	x1 = x2;
	gfx = fm;
	if (sf1 < gfx)
		{
		gfx = sf1;
		x1 = sx1;
		}
		
	/* Update x for linear search but not for parabolic search */
	if (j >= 0)
		{
		for (i = 0; i < n; i++)
			x[i] += x1*v[i][j];
		}

	px1.setValue(x1);
	pd2.setValue(d2);
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	Praxis_Quad
|
|	Looks for the function minimum along a curve defined by q0, q1, and x.
|
|	DLS 15jun00: I modified Brent's code so that q0 and q1 are passed as arguments to Praxis_MIN rather than being
|	             global within Praxis.  This made it possible to avoid some unnecessary vector swapping.
*/
MesquiteDouble sA = new MesquiteDouble(); //WPM: this might cause reentrancy problems; pass into PraxisQuad?
MesquiteDouble lambdaA = new MesquiteDouble();

private void Praxis_Quad(double[] x, double[] q0, double[] q1, double[][] v, Evaluator f, Object dataPtr)
	{
	int		i;
	double	lambda, s;
	double	qa, qb, qc;

	/* q0 and q1 contain previous two points */

	s = gfx;
	gfx = qf1;
	qf1 = s;
	
	qd1 = 0.0;
	for (i = 0; i < n; i++)
		{
		/* DLS: Brent had this (see above) ...
		s = x[i];
		x[i] = q1[i];
		q1[i] = s;
		qd1 += (q1[i] - x[i])*(q1[i] - x[i]);
		
		... but we avoid having to swap q1 and x vectors ... */
		s = q1[i] - x[i];
		qd1 += s*s;
		}
	qd1 = Math.sqrt(qd1);
	if ((qd0 > 0.0) && (qd1 > 0.0) && (nl >= 3*n*n))
		{
		s = 0.0;
		lambda = qd1;

		/* DLS: note position of q1 and x args (see comments above) */
		sA.setValue(s);
		lambdaA.setValue(lambda);
		Praxis_Min(-1, 2, q1, q0, x, v, sA, lambdaA, qf1, true, f, dataPtr);
		s = sA.getValue();
		lambda = lambdaA.getValue();
		if (getAbort())  //DRM Apr 2005
			return; //VOID_RETURN_IF_ABORTED 
		qa = lambda*(lambda - qd1)/(qd0*(qd0 + qd1));
		qb = (lambda + qd0)*(qd1 - lambda)/(qd0*qd1);
		qc = lambda*(lambda + qd0)/(qd1*(qd0 + qd1));
		}
	else
		{
		gfx = qf1;
		qa = qb = 0.0;
		qc = 1.0;
		}
	qd0 = qd1;
	for (i = 0; i < n; i++)
		{
		s = q0[i];

		/* DLS: Brent had this ...
		q0[i] = x[i];
		x[i] = qa*s + qb*x[i] + qc*q1[i];

		... but must be modified as below since we didn't swap x and q1 above ... */
		q0[i] = q1[i];
		q1[i] = x[i];
		x[i] = qa*s + qb*q0[i] + qc*x[i];
		}
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	Praxis_MinFit
|
|	Gets singular value decomposition using a modified version of Golub and Reinsch's (1969) routine, restricted to
|	m=n.  The singular values of the array 'ab' are returned in 'q', and 'ab' is overwritten with the orthogonal
|	matrix V such that U.diag(Q) = AB.V, where U is another orthogonal matrix.
*/

private void Praxis_MinFit(double eps, double tol, double[][] ab, double[] q,
                         double[] e)	/* work vector of size n */
	{
	int			i, j, k, l, l2, kt;
	double		c, f, g, h, s, x, y, z;
	l = 0; //here merely so java compiler doesn't complain that l might not have been initialized
	
	/* Householder's reduction to bidiagonal form */
	g = x = 0.0;
	for (i = 0; i < n; i++)
		{
		e[i] = g;
		s = 0.0;
		l = i + 1;
		for (j = i; j < n; j++)
			s += ab[j][i]*ab[j][i];
		if (s < tol)
			g = 0.0;
		else
			{
			f = ab[i][i];
			g = (f < 0.0) ? Math.sqrt(s) : -Math.sqrt(s);
			h = f*g - s;
			ab[i][i] = f - g;
			for (j = l; j < n; j++)
				{
				f = 0.0;
				for (k = i; k < n; k++)
					f += ab[k][i]*ab[k][j];
				f /= h;
				for (k = i; k < n; k++)
					ab[k][j] += f*ab[k][i];
				}
			}
		q[i] = g;
		s = 0.0;
		for (j = l; j < n; j++)
			s += ab[i][j]*ab[i][j];
		
		if (s < tol)
			g = 0.0;
		else
			{
			f = ab[i][i + 1];		/* (DLS note: when i=n-1, s is always 0) */
			g = (f < 0.0) ? Math.sqrt(s) : -Math.sqrt(s);
			h = f*g - s;
			ab[i][i + 1] = f - g;
			for (j = l; j < n; j++)
				e[j] = ab[i][j]/h;
			for (j = l; j < n; j++)
				{
				s = 0.0;
				for (k = l; k < n; k++)
					s += ab[j][k]*ab[i][k];
				for (k = l; k < n; k++)
					ab[j][k] += s*e[k];
				}
			}
		y = Math.abs(q[i]) + Math.abs(e[i]);
		if (y > x)
			x = y;
		}
	
	/* Accumulation of right-hand transformations */
	for (i = n - 1; i >= 0; i--)
		{
		if (g != 0.0)
			{
			h = ab[i][i + 1]*g;
			for (j = l; j < n; j++)
				ab[j][i] = ab[i][j]/h;
			for (j = l; j < n; j++)
				{
				s = 0.0;
				for (k = l; k < n; k++)
					s += ab[i][k]*ab[k][j];
				for (k = l; k < n; k++)
					ab[k][j] += s*ab[k][i];
				}
			}
		for (j = l; j < n; j++)
			ab[i][j] = ab[j][i] = 0.0;
		ab[i][i] = 1.0;
		g = e[i];
		l = i;
		}
	boolean testSplitting = false;
	/* Diagonalization of the bidiagonal form */
	eps *= x;
	kt = 0;

	for (k = n - 1; k >= 0 || testSplitting; k--)
		{

		if (testSplitting)
			k++;
		else
			kt = 0;
		testSplitting = false;
		boolean gotoConvergence = false;
		if (++kt > 30)
			{
			e[k] = 0.0;
			}
		for (l2 = k; l2 >= 0; l2--)
			{
			l = l2;
			if (Math.abs(e[l]) <= eps)
				{
				gotoConvergence = true;
				break;
				}
			if (Math.abs(q[l - 1]) <= eps)
				break;
			}
		if (!gotoConvergence)
			{
			/* Cancellation of e[l] if l > 1 */
			c = 0.0;
			s = 1.0;
			for (i = l; i <= k; i++)
				{
				f = s*e[i];
				e[i] *= c;
				if (Math.abs(f) <= eps)
					break;
				g = q[i];
				if (Math.abs(f) < Math.abs(g))						/* (implies g > eps) */
					h = Math.abs(g)*Math.sqrt(1.0 + (f/g)*(f/g));
				else if (f != 0.0)
					h = Math.abs(f)*Math.sqrt(1.0 + (g/f)*(g/f));
				else
					h = 0.0;
				q[i] = h;
				if (h == 0.0)
					g = h = 1.0;	/* Note: this replaces q[i]=h=sqrt(g*g+f*f) which may give incorrect results if the
									         squares underflow or if f=g=0 */
				c = g/h;
				s = -f/h;
				}
			}
			
		z = q[k];
		
		testSplitting = false;
		if (l != k)
			{			
			/* Shift from bottom 2x2 minor */
			x = q[l];
			y = q[k - 1];
			g = e[k - 1];
			h = e[k];
			f = ((y - z)*(y + z) + (g - h)*(g + h))/(2.0*h*y);
			g = Math.sqrt(f*f + 1.0);
			s = (f < 0.0) ? f - g : f + g;
			f = ((x - z)*(x + z) + h*(y/s - h))/x;
				
			/* Next QR transformation */
			c = s = 1.0;
			for (i = l + 1; i <= k; i++)
				{
				g = e[i];
				y = q[i];
				h = s*g;
				g *= c;
				/* DLS note: Golob and Reinsch had "z = sqrt(f*f + h*h))" below */
				if (Math.abs(f) < Math.abs(h))
					z = Math.abs(h)*Math.sqrt(1.0 + (f/h)*(f/h));
				else if (f != 0.0)
					z = Math.abs(f)*Math.sqrt(1.0 + (h/f)*(h/f));
				else
					z = 0.0;
				e[i - 1] = z;
				if (z == 0.0)
					z = f = 1.0;
				c = f/z;
				s = h/z;
				f = x*c + g*s;
				g = -x*s + g*c;
				h = y*s;
				y *= c;
				for (j = 0; j < n; j++)
					{
					x = ab[j][i - 1];
					z = ab[j][i];
					ab[j][i - 1] = x*c + z*s;
					ab[j][i] = -x*s + z*c;
					}
				/* DLS note: Golob and Reinsch had "z = sqrt(f*f + h*h))" below */
				if (Math.abs(f) < Math.abs(h))
					z = Math.abs(h)*Math.sqrt(1.0 + (f/h)*(f/h));
				else if (f != 0.0)
					z = Math.abs(f)*Math.sqrt(1.0 + (h/f)*(h/f));
				else
					z = 0.0;
				q[i - 1] = z;
				if (z == 0.0)
					z = f = 1.0;
				c = f/z;
				s = h/z;
				f = c*g + s*y;
				x = -s*g + c*y;
				}
			e[l] = 0.0;
			e[k] = f;
			q[k] = x;
			testSplitting = true;
			}
		
		if (!testSplitting && z < 0.0)
			{
			/* q[k] is made non-negative */
			q[k] = -z;
			for (j = 0; j < n; j++)
				ab[j][k] = -ab[j][k];
			}
		}
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	Praxis_SortDV
|
|	Sorts the elements of 'd' and corresponding elements of 'v' into descending order.
*/

private void Praxis_SortDV(double[] d, double[][] v)
	{
	int		i, j, k;
	double	s;

	for (i = 0; i < n - 1; i++)
		{
		k = i;
		s = d[i];
		for (j = i + 1; j < n; j++)
			{
			if (d[j] > s)
				{
				k = j;
				s = d[j];
				}
			}
		if (k > i)
			{
			d[k] = d[i];
			d[i] = s;
			for (j = 0; j < n; j++)
				{
				s = v[j][i];
				v[j][i] = v[j][k];
				v[j][k] = s;
				}
			}
		}
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	Praxis
|
|	Minimizes a function f of n variables using Brent's (1973) principal axis method,which is a modification of
|	Powell's (1964) method.  Translated by David Swofford from the Algol program in Brent's book.
|
|	Note: this function is NOT reentrant, due to the need for the globals below.
*/

public double Praxis(
  double		tol,		/* tolerance used for convergence criterion */
  double		h,			/* maximum step size */
  int			nn,			/* number of variables */
  double[]		x,			/* on input, must contain initial guess for optimal point */
  Evaluator	f,			/* the function to be minimized */
  Object		dataPtr	/* pointer to data to be passed to 'f' */
)		
	{
	int			i, j, k, k2, kl, kt, ktm;
	boolean illc;
	double		vsmall, large, vlarge, scbd, ldfac, sf, df, f1, lds, t2, sl, dn, s, sz;

	machEps = DBL_EPSILON;

	/* Copy args to global equivalents */
	toler = tol;
	htol = h;
	n = nn;
	/* Partition work space provided by caller into arrays used here --WPM: in translation to Java, done on instantiation 
	d = work;
	q0 = d + nn;
	q1 = q0 + nn;
	xnew = q1 + nn;
	y = xnew + nn;
	z = y + nn;
	*/
	
	/* Machine-dependent initializations */
	esmall = machEps*machEps;
	vsmall = esmall*esmall;
	large = 1.0/esmall;
	vlarge = 1.0/vsmall;
	m2 = Math.sqrt(machEps);
	m4 = Math.sqrt(m2);
	
	//praxisSeed = 1;		/* starting seed for random number generator */
	
	/* Heuristic numbers:
	   - if axes may be badly scaled (which should be avoided if possible), set scbd=10, otherwise 1
	   - if the problem is known to be ill-conditioned, set illc=true, otherwise false
	   - ktm+1 is the number of iterations without improvement before the algorithm terminates (see section 7.6 of
	     Brent's book).  ktm=4 is very cautious; usually ktm=1 is satisfactory.
	*/
	scbd = 1.0;
	illc = false;
	ktm = 1;

	ldfac = illc ? 0.1 : 0.01;
	kt = nl = 0;
	checkBoundsSearched(x); //added WPM April 2002
	qf1 = gfx = f.evaluate(x, dataPtr);
	if (getAbort() || valueAcceptable(gfx))  //DRM Apr 2005
		return gfx; 
	//RETURN_IF_ABORTED 
	toler = t2 = esmall + Math.abs(toler);
	dmin = esmall;
	if (htol < 100.0*toler)
		htol = 100.0*toler;
	ldt = htol;
	Double2DArray.setToIdentityMatrix(v);

	d[0] = qd0 = 0.0;
	for (i = 0; i < n; i++)
		{
		q0[i] = 0.0;	/* DLS: this wasn't included in Brent's code, but it's important */
		q1[i] = x[i];
		}

	checkBoundsSearched(x); //added WPM April 2002
	
	/* ------ main loop ------ */
	MesquiteDouble dEA = new MesquiteDouble();
	MesquiteDouble sA = new MesquiteDouble();

	while (FOREVER)
		{
		checkBoundsSearched(x); //added WPM April 2002
		sf = d[0];
		d[0] = s = 0.0;
		
		/* Minimize along first direction */
		dEA.setValue(d[0]);
		sA.setValue(s);
		Praxis_Min(0, 2, x, q0, q1, v,dEA, sA, gfx, false, f, dataPtr);
		d[0] = dEA.getValue();
		s = sA.getValue();
		if (getAbort() || valueAcceptable(gfx))  //DRM Apr 2005
			return gfx; 
		//RETURN_IF_ABORTED 
		if (s < 0.0)
			{
			for (i = 0; i < n; i++)
				v[i][0] = -v[i][0];
			}
		if ((sf <= 0.9*d[0]) || (0.9*sf >= d[0]))
			{
			for (i = 1; i < n; i++)
				d[i] = 0.0;
			}
		kl = 0; //so compiler doesn't complain not initialized
		for (k = 1; k < n; k++)
			{
			for (i = 0; i < n; i++)
				y[i] = x[i];
			sf = gfx;
			illc = illc || (kt > 0);
	
			while (FOREVER)
				{
				kl = k;
				df = 0.0;
				if (illc)
					{
					/* Random step to get off resolution valley */
					for (i = 0; i < n; i++)
						{
						s = z[i] = (0.1*ldt + t2*Math.pow(10.0, (double)kt))*(rng.nextDouble() - 0.5); //rnd 0 to 1
						for (j = 0; j < n; j++)
							x[j] += s*v[j][i];
						}
					checkBoundsSearched(x); ////added WPM April 2002
					gfx = f.evaluate(x, dataPtr);
					if (getAbort() || valueAcceptable(gfx)) //DRM Apr 2005
						return gfx; 
					//RETURN_IF_ABORTED 
					}
				for (k2 = k; k2 < n; k2++)
					{
					sl = gfx;
					s = 0.0;
					/* Minimize along "non-conjugate" directions */
					dEA.setValue(d[k2]);
					sA.setValue(s);
					Praxis_Min(k2, 2, x, q0, q1, v, dEA, sA, gfx, false, f, dataPtr);
					d[k2] = dEA.getValue();
					s = sA.getValue();
					if (getAbort() || valueAcceptable(gfx)) //DRM Apr 2005
						return gfx; 
					//RETURN_IF_ABORTED 
					if (illc)
						{
						sz = s + z[k2];
						s = d[k2]*sz*sz;
						}
					else
						s = sl - gfx;
					if (df < s)
						{
						df = s;
						kl = k2;
						}
					}
				if (!illc && (df < Math.abs(100.0*machEps*gfx)))
					illc = true;	/* no success with illc=false so try once with illc=true */
				else
					break;
				}
			
			for (k2 = 0; k2 < k; k2++)
				{
				/* Minimize along "conjugate" directions */
				s = 0.0;
				dEA.setValue(d[k2]);
				sA.setValue(s);
				Praxis_Min(k2, 2, x, q0, q1, v, dEA, sA, gfx, false, f, dataPtr);
				d[k2] = dEA.getValue();
				s = sA.getValue();
				if (getAbort() || valueAcceptable(gfx)) //DRM Apr 2005
					return gfx; 
				//RETURN_IF_ABORTED 
				}
	
			f1 = gfx;
			gfx = sf;
			lds = 0.0;
			for (i = 0; i < n; i++)
				{
				sl = x[i];
				x[i] = y[i];
				y[i] = (sl -= y[i]);
				lds += sl*sl;
				}
			lds = Math.sqrt(lds);
			if (lds > esmall)
				{
				/* Throw away direction kl */
				for (i = kl - 1; i >= k; i--)
					{
					for (j = 0; j < n; j++)
						v[j][i + 1] = v[j][i];
					d[i + 1] = d[i];
					}

				/* Set new "conjugate" direction ... */
				d[k] = 0.0;
				for (i = 0; i < n; i++)
					v[i][k] = y[i]/lds;
								
				/* ... and minimize along it */
				dEA.setValue(d[k]);
				sA.setValue(lds);
				Praxis_Min(k, 4, x, q0, q1, v, dEA, sA, f1, true, f, dataPtr);
				d[k] = dEA.getValue();
				lds = sA.getValue();
				if (getAbort() || valueAcceptable(gfx))  //DRM Apr 2005
					return gfx; 
				//RETURN_IF_ABORTED 
				if (lds <= 0.0)
					{
					lds = -lds;
					for (i = 0; i < n; i++)
						v[i][k] = -v[i][k];
					}
				}
			ldt *= ldfac;
			if (ldt < lds)
				ldt = lds;
			t2 = 0.0;
			for (i = 0; i < n; i++)
				t2 += x[i]*x[i];
			t2 = m2*Math.sqrt(t2) + toler;
			
			/* See if step length exceeds half the tolerance (stopping criterion) */
			kt = (ldt > 0.5*t2) ? 0 : kt + 1;
			if (kt > ktm)
				{
				return gfx;
				}
			}
		
		/* Try quadratic extrapolation in case we are stuck in a curved valley */
		Praxis_Quad(x, q0, q1, v, f, dataPtr);
		if (getAbort() || valueAcceptable(gfx))  //DRM Apr 2005
			return gfx; 
		//RETURN_IF_ABORTED 
	
		/* Calculate V = U.(D^(-1/2))  (note: 'v' currently contains U) */
		dn = 0.0;
		for (i = 0; i < n; i++)
			{
			d[i] = 1.0/Math.sqrt(d[i]);
			if (d[i] > dn)
				dn = d[i];
			}
		for (j = 0; j < n; j++)
			{
			s = d[j]/dn;
			for (i = 0; i < n; i++)
				v[i][j] *= s;
			}

		if (scbd > 1.0)
			{
			/* Scale axes in attempt to reduce condition number */
			s = vlarge;
			for (i = 0; i < n; i++)
				{
				sl = 0.0;
				for (j = 0; j < n; j++)
					sl += v[i][j]*v[i][j];
				z[i] = Math.sqrt(sl);
				if (z[i] < m4)
					z[i] = m4;
				if (s > z[i])
					s = z[i];
				}
			for (i = 0; i < n; i++)
				{
				sl = s/z[i];
				z[i] = 1.0/sl;
				if (z[i] > scbd)
					{
//#					if 0	/* DLS 6jan97: Borland C says this is a do-nothing statement,
 //                                          and they're right */
//					sl = 1.0/scbd;
//#					endif
					z[i] = scbd;
					}
				}
			}

		/* Transpose v for MinFit */
		for (i = 1; i < n; i++)
			{
			for (j = 0; j < i; j++)
				{
				s = v[i][j];
				v[i][j] = v[j][i];
				v[j][i] = s;
				}
			}

		/* Find the singular value decomposition of v.  This gives the eigenvalues and principal axes of the
		   approximating quadratic form without squaring the condition number. */
		Praxis_MinFit(machEps, vsmall, v, d, y);	/* ('y' is just a scratch vector) */

		if (scbd > 1.0)
			{
			/* Unscaling */
			for (i = 0; i < n; i++)
				{
				s = z[i];
				for (j = 0; j < n; j++)
					v[i][j] *= s;
				}
			for (i = 0; i < n; i++)
				{
				s = 0.0;
				for (j = 0; j < n; j++)
					s += v[j][i]*v[j][i];
				s = Math.sqrt(s);
				d[i] *= s;
				s = 1.0/s;
				for (j = 0; j < n; j++)
					v[j][i] *= s;
				}
			}

		for (i = 0; i < n; i++)
			{
			s = dn*d[i];
			if (s > large)
				d[i] = vsmall;
			else if (s < esmall)
				d[i] = vlarge;
			else
				d[i] = 1.0/(s*s);
			}
			
		/* Sort new eigenvalues and eigenvectors */
		Praxis_SortDV(d, v);

		dmin = d[n - 1];
		if (dmin < esmall)
			dmin = esmall;
		
		illc = (m2*d[0] > dmin);
		}
		return 0;
	}

/*----------------------------------------------------------------------------------------------------------------------
|
|	BracketMinimum
|
|	Brackets a function minimum using method of Press et al.
|
|	This function is reentrant.
*/
private double sign(double a,double b){
	return ((b) > 0.0 ? Math.abs(a) : -Math.abs(a));
}
static final double GOLD = 1.618034;
static final double TINY = 1e-20;
//#if 0
//#	define GLIMIT	100.0	/* DLS 07mar98: this was the original setting (see below) */
//#else
	/* DLS 07mar98: the original GLIMIT setting of 100.0 was sometimes allowing too great an extrapolation, causing
	   problems with gamma shape parameter estimation in PAUP*.  Setting to a smaller value shouldn't hurt anything;
	   in the worst case it will just take a little longer to find the bracket */
//#	define GLIMIT	10.0
//#endif
static final double GLIMIT = 10.0;

public void BracketMinimum(MesquiteDouble pA, MesquiteDouble pB, Evaluator f, Object dataPtr)
	{
	double		a, b, c, x, fa, fb, fc, fx, xlim, r, q, temp;
	MesquiteDouble cA = new MesquiteDouble();
	MesquiteDouble xA = new MesquiteDouble();
	
	fa = f.evaluate(pA, dataPtr);
	if (getAbort())  //DRM Apr 2005
		return; 
	//VOID_RETURN_IF_ABORTED 
	a = pA.getValue();
	fb = f.evaluate(pB, dataPtr);
	if (getAbort())  //DRM Apr 2005
		return; 
	//VOID_RETURN_IF_ABORTED 
	b = pB.getValue();
	if (fb > fa)
		{
		/* Swap a and b so we can go downhill in direction of a to b */
		temp = a;
		a = b;
		b = temp;
		temp = fa;
		fa = fb;
		fb = temp;
		}
	c = b + GOLD*(b - a);		/* first guess for c */
	cA.setValue(c);
	fc = f.evaluate(cA, dataPtr);
	c = cA.getValue();
	if (getAbort())  //DRM Apr 2005
		return; 
	//VOID_RETURN_IF_ABORTED 


	while (fb > fc)
		{
		/* Use inverse parabolic interpolation to compute a new trial point x (= the abcissa which is the minimum of
		   a parabola through f(a), f(b), and f(c))  */
		r = (b - a)*(fb - fc);
		q = (b - c)*(fb - fa);
		x = b - ((b - c)*q - (b - a)*r) / (2.0*sign(Math.max(Math.abs(q - r), TINY), q - r));
		xlim = b + GLIMIT*(c - b);
		if ((b - x)*(x - c) > 0.0)
			{
			/* x is between b and c */
			xA.setValue(x);
			fx = f.evaluate(xA, dataPtr);
			x = xA.getValue();
			if (getAbort())  //DRM Apr 2005
				return; 
			//VOID_RETURN_IF_ABORTED 
			if (fx < fc)
				{
				/* There's a minimum between b and c */
				a = b;
//#				if 0	/* lines below are apparently do-nothing statements */
//				fa = fb;
//				b = x;
//				fb = fx;
//#				endif
				break;
				}
			else if (fx > fb)
				{
				/* There's a minimum between a and x */
				c = x;
//#				if 0	/* line below is apparently a do-nothing statement */
//				fc = fx;
//#				endif
				break;
				}
			/* Parabolic fit failed; "magnify" using default magnification */
			x = c + GOLD*(c - b);
			xA.setValue(x);
			fx = f.evaluate(xA, dataPtr);
			x = xA.getValue();
			if (getAbort())  //DRM Apr 2005
				return; 
			//VOID_RETURN_IF_ABORTED 
			}
		else if ((c - x)*(x - xlim) > 0.0)
			{
			/* x from parabolic fit is between c and its allowed limit */
			xA.setValue(x);
			fx = f.evaluate(xA, dataPtr);
			x = xA.getValue();
			//VOID_RETURN_IF_ABORTED 
			if (getAbort())  //DRM Apr 2005
				return; 
			if (fx < fc)
				{
				b = c; fb = fc;
				c = x; fc = fx;
				x = c + GOLD*(c - b);
				xA.setValue(x);
				fx = f.evaluate(xA, dataPtr);
				x = xA.getValue();
				if (getAbort())  //DRM Apr 2005
					return; 
				//VOID_RETURN_IF_ABORTED 
				}
			}
		else if ((x - xlim)*(xlim - c) > 0.0)
			{
			/* Limit parabolic fit to its maximum allowed value */
			x = xlim;
			xA.setValue(x);
			fx = f.evaluate(xA, dataPtr);
			x = xA.getValue();
			if (getAbort())  //DRM Apr 2005
				return; 
			//VOID_RETURN_IF_ABORTED 
			}
		else
			{
			/* Reject parabolic x; use default magnification */
			x = c + GOLD*(c - b);
			xA.setValue(x);
			fx = f.evaluate(xA, dataPtr);
			x = xA.getValue();
			if (getAbort())  //DRM Apr 2005
				return; 
			//VOID_RETURN_IF_ABORTED 
			}
		
		/* New bracket is (b,c,x) */
		a = b; fa = fb;
		b = c; fb = fc;
		c = x; fc = fx;		
		}
	if (a < c)
		{
		pA.setValue(a);
		pB.setValue(c);
		}
	else
		{
		pA.setValue(c);
		pB.setValue(a);
		}
	}
}
