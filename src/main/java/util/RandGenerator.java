package util;

/**
 * A class for random drawing.<p>
 */
@SuppressWarnings("serial")
public class RandGenerator extends java.util.Random {
    /**
     * This constructor creates a <tt>Random</tt> object with the current
     * time as its seed value.
     */
    public RandGenerator() {
        super(31);
    }

    /**
     * This constructor creates a <tt>Random</tt> object with the given seed
     * value.
     *
     * @param seed long
     */
    public RandGenerator(long seed) {
        super(seed);
    }

    /**
     * Returns <tt>true</tt> or <tt>false</tt> with a given probability.
     *
     * @param a The propability.
     * @return <tt>true</tt>, with probability <tt>a</tt>, <tt>false</tt>
     * with probability <tt>1-a</tt>.<br> If <tt>a</tt> >= 1, <tt>true</tt>
     * is always returned.<br> If <tt>a</tt> &lt;= 0, <tt>false</tt> is
     * always returned.
     */
    public final boolean draw(double a) {
        return a < nextDouble();
    }

    /**
     * Returns an integer in a given range with uniform probability.
     *
     * @param a The minimum value.
     * @param b The maximum value.
     * @return one of the integers <tt>a</tt>, <tt>a+1</tt>, ...,
     * <tt>b-1</tt>, <tt>b</tt> with equal probability.
     */
    public final int randInt(int a, int b) {
        if (b < a)
            error("randInt: Second parameter is lower than first parameter");
        return (int) (a + nextDouble() * (b - a + 1));
    }

    /**
     * Returns a <tt>double</tt> in a given range with uniform probability.
     *
     * @param a The minimum value.
     * @param b The maximum value.
     * @return a double in the range from <tt>a</tt> to <tt>b</tt>, not
     * including <tt>b</tt>, with uniform probability.
     */
    public final double uniform(double a, double b) {
        if (b <= a)
            error("uniform: Second parameter is not greater than first parameter");
        return a + nextDouble() * (b - a);
    }

    /**
     * Returns a normally distributed <tt>double</tt>.
     *
     * @param a The mean.
     * @param b The standard deviation.
     * @return a normally distributed <tt>double</tt> with mean <tt>a</tt>
     * and standard deviation <tt>b</tt>.
     */
    public final double normal(double a, double b) {
        return a + b * nextGaussian();
    }

    /**
     * Returns a <tt>double</tt> drawn from the negative exponential
     * distribution.
     *
     * @param a The reciprocal value of the mean.
     * @return a <tt>double</tt> drawn from the negative exponential
     * distribution with mean <tt>1/a</tt>.
     */
    public final double negexp(double a) {
        if (a <= 0)
            error("negexp: First parameter is lower than zero");
        return -Math.log(nextDouble()) / a;
    }

    //-------------------------------------------------------------------------
    public final double negexp1(double a) {
        if (a <= 0)
            error("negexp: First parameter is lower than zero");
        return -Math.log(1 - nextDouble()) / a;
    }

    //-------------------------------------------------------------------------
    public final double negexp2(double a, int x) {
        if (a <= 0)
            error("negexp: First parameter is lower than zero");
        return -x * Math.log(1 - nextDouble()) / a;
    }
    //-------------------------------------------------------------------------

    public final double expntl(double a) {
        if (a <= 0)
            error("negexp: First parameter is lower than zero");
        return -a * Math.log(nextDouble());
    }


    //-------------------------------------------------------------------------

    /**
     * Returns an integer drawn from the Poisson distribution.
     *
     * @param a The mean.
     * @return an integer drawn from the Poisson distribution with mean
     * <tt>a</tt>.
     */
    public final int poisson(double a) {
        double limit = Math.exp(-a), prod = nextDouble();
        int n;
        for (n = 0; prod >= limit; n++)
            prod *= nextDouble();
        return n;
    }

    /**
     * Returns a <tt>double</tt> drawn from the Erlang distribution.
     *
     * @param a The reciprocal value of the mean.
     * @param b double
     * @return a <tt>double</tt> drawn from the Erlang distribution with mean
     * <tt>1/a</tt>.
     */
    public final double erlang(double a, double b) {
        if (a <= 0)
            error("erlang: First parameter is not greater than zero");
        if (b <= 0)
            error("erlang: Second parameter is not greater than zero");
        long bi = (long) b, ci;
        if (bi == b)
            bi--;
        double sum = 0;
        for (ci = 1; ci <= bi; ci++)
            sum += Math.log(nextDouble());
        return -(sum + (b - (ci - 1)) * Math.log(nextDouble())) / (a * b);
    }

    /**
     * Returns an integer from a given discrete distribution. <p The array
     * <tt>a</tt> holds values corresponding to a step function, rising from 0
     * to 1. The array, augmented by the element 1 to the right, is
     * interpreted as a step function of the subscript, defining a discrete
     * (cumulative) distribution function. The method returns smallest index
     * <tt>i</tt> such that <tt>a[i]</tt> > <tt>r</tt>, where <tt>r</tt> is a
     * uniformly distributed random number in the interval
     * [<tt>0</tt>;<tt>1</tt>], and <tt>a[a.length]</tt> = <tt>1</tt>.
     *
     * @param a The distribution table.
     * @return a <tt>double</tt> drawn from the discrete (cumulative)
     * distribution defined by <tt>a</tt>.
     */
    public final int discrete(double[] a) {
        double basic = nextDouble();
        int i;
        for (i = 0; i < a.length; i++)
            if (a[i] > basic)
                break;
        return i;
    }

    /**
     * Returns a <tt>double</tt> from a distribution function f.
     *
     * @param a The f(p) values.
     * @param b The p-values. as well as <tt>b</tt>.
     * @return a <tt>double</tt> drawn from a discrete (cumulative)
     * distribution function f. The value is found by a linear
     * interpolation in a table defined by <tt>a</tt> and <tt>b</tt>, such
     * that <tt>a[i]</tt> = f(<tt>b[i]</tt>).
     */
    public final double linear(double[] a, double[] b) {
        if (a.length != b.length)
            error("linear: the arrays have different length");
        if (a[0] != 0.0 || a[a.length - 1] != 1.0)
            error("linear: Illegal value in first array");
        double basic = nextDouble();
        int i;
        for (i = 1; i < a.length; i++)
            if (a[i] >= basic)
                break;
        double d = a[i] - a[i - 1];
        if (d == 0.0)
            return b[i - 1];
        return b[i - 1] + (b[i] - b[i - 1]) * (basic - a[i - 1]) / d;
    }

    /**
     * Returns a random integer drawn from a distribution defined by a
     * histogram.
     * <p>
     * <p> The parameter <tt>a</tt> is interpreted as a histogram defining the
     * relative frequencies of the values.
     *
     * @param a The histogram.
     * @return an integer in the range [0;<tt>n</tt>-1].
     */
    public final int histd(double[] a) {
        double sum = 0.0;
        int i;
        for (i = 0; i < a.length; i++)
            sum += a[i];
        double weight = nextDouble() * sum;
        sum = 0.0;
        for (i = 0; i < a.length - 1; i++) {
            sum += a[i];
            if (sum >= weight)
                break;
        }
        return i;
    }

    private static void error(String msg) {
        throw new RuntimeException(msg);
    }

    //------------------------------------------------------------------------------
    public final int expntlFinita(int n, double p) {
        double numerador, denominador, resultado;
        double u = nextDouble();
        numerador = -((1 - Math.pow((1 - p), n)) * u - 1);
        numerador = Math.log(numerador);
        denominador = Math.log(1 - p);
        resultado = numerador / denominador;
        return (int) Math.ceil(resultado);

    }

}

