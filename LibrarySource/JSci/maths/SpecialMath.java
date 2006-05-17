package JSci.maths;

/**
* The special function math library.
* This class cannot be subclassed or instantiated because all methods are static.
* @version 1.0
* @author Mark Hale
*/
public final class SpecialMath extends AbstractMath implements NumericalConstants {
        private SpecialMath() {}

// Some IEEE machine constants
        /**
        * Relative machine precision.
        */
        private final static double EPS=2.22e-16;
        /**
        * The smallest positive floating-point number such that 1/xminin is machine representable.
        */
        private final static double XMININ=2.23e-308;


// CHEBYSHEV SERIES

// series for ai0        on the interval  1.25000d-01 to  3.33333d-01
//                                        with weighted error   7.87e-17
//                                         log weighted error  16.10
//                               significant figures required  14.69
//                                    decimal places required  16.76

        private final static double ai0cs[]={
                0.07575994494023796,
                0.00759138081082334,
                0.00041531313389237,
                0.00001070076463439,
                -0.00000790117997921,
                -0.00000078261435014,
                0.00000027838499429,
                0.00000000825247260,
                -0.00000001204463945,
                0.00000000155964859,
                0.00000000022925563,
                -0.00000000011916228,
                0.00000000001757854,
                0.00000000000112822,
                -0.00000000000114684,
                0.00000000000027155,
                -0.00000000000002415,
                -0.00000000000000608,
                0.00000000000000314,
                -0.00000000000000071,
                0.00000000000000007};

// series for ai02       on the interval  0.          to  1.25000d-01
//                                        with weighted error   3.79e-17
//                                         log weighted error  16.42
//                               significant figures required  14.86
//                                    decimal places required  17.09
        private final static double ai02cs[]={
                0.05449041101410882,
                0.00336911647825569,
                0.00006889758346918,
                0.00000289137052082,
                0.00000020489185893,
                0.00000002266668991,
                0.00000000339623203,
                0.00000000049406022,
                0.00000000001188914,
                -0.00000000003149915,
                -0.00000000001321580,
                -0.00000000000179419,
                0.00000000000071801,
                0.00000000000038529,
                0.00000000000001539,
                -0.00000000000004151,
                -0.00000000000000954,
                0.00000000000000382,
                0.00000000000000176,
                -0.00000000000000034,
                -0.00000000000000027,
                0.00000000000000003};

// series for ai1        on the interval  1.25000d-01 to  3.33333d-01
//                                        with weighted error   6.98e-17
//                                         log weighted error  16.16
//                               significant figures required  14.53
//                                    decimal places required  16.82

        private final static double ai1cs[]={
                -0.02846744181881479,
                -0.01922953231443221,
                -0.00061151858579437,
                -0.00002069971253350,
                0.00000858561914581,
                0.00000104949824671,
                -0.00000029183389184,
                -0.00000001559378146,
                0.00000001318012367,
                -0.00000000144842341,
                -0.00000000029085122,
                0.00000000012663889,
                -0.00000000001664947,
                -0.00000000000166665,
                0.00000000000124260,
                -0.00000000000027315,
                0.00000000000002023,
                0.00000000000000730,
                -0.00000000000000333,
                0.00000000000000071,
                -0.00000000000000006};

// series for ai12       on the interval  0.          to  1.25000d-01
//                                        with weighted error   3.55e-17
//                                         log weighted error  16.45
//                               significant figures required  14.69
//                                    decimal places required  17.12

        private final static double ai12cs[]={
                0.02857623501828014,
                -0.00976109749136147,
                -0.00011058893876263,
                -0.00000388256480887,
                -0.00000025122362377,
                -0.00000002631468847,
                -0.00000000383538039,
                -0.00000000055897433,
                -0.00000000001897495,
                0.00000000003252602,
                0.00000000001412580,
                0.00000000000203564,
                -0.00000000000071985,
                -0.00000000000040836,
                -0.00000000000002101,
                0.00000000000004273,
                0.00000000000001041,
                -0.00000000000000382,
                -0.00000000000000186,
                0.00000000000000033,
                0.00000000000000028,
                -0.00000000000000003};

// series for aif        on the interval -1.00000d+00 to  1.00000d+00
//                                        with weighted error   1.09e-19
//                                         log weighted error  18.96
//                               significant figures required  17.76
//                                    decimal places required  19.44

        private final static double aifcs[]={
                -0.03797135849666999750,
                0.05919188853726363857,
                0.00098629280577279975,
                0.00000684884381907656,
                0.00000002594202596219,
                0.00000000006176612774,
                0.00000000000010092454,
                0.00000000000000012014,
                0.00000000000000000010};

// series for aig        on the interval -1.00000d+00 to  1.00000d+00
//                                        with weighted error   1.51e-17
//                                         log weighted error  16.82
//                               significant figures required  15.19
//                                    decimal places required  17.27

        private final static double aigcs[]={
                0.01815236558116127,
                0.02157256316601076,
                0.00025678356987483,
                0.00000142652141197,
                0.00000000457211492,
                0.00000000000952517,
                0.00000000000001392,
                0.00000000000000001};

// series for aip        on the interval  0.          to  1.00000d+00
//                                        with weighted error   5.10e-17
//                                         log weighted error  16.29
//                               significant figures required  14.41
//                                    decimal places required  17.06

        private final static double aipcs[]={
                -0.0187519297793868,
                -0.0091443848250055,
                0.0009010457337825,
                -0.0001394184127221,
                0.0000273815815785,
                -0.0000062750421119,
                0.0000016064844184,
                -0.0000004476392158,
                0.0000001334635874,
                -0.0000000420735334,
                0.0000000139021990,
                -0.0000000047831848,
                0.0000000017047897,
                -0.0000000006268389,
                0.0000000002369824,
                -0.0000000000918641,
                0.0000000000364278,
                -0.0000000000147475,
                0.0000000000060851,
                -0.0000000000025552,
                0.0000000000010906,
                -0.0000000000004725,
                0.0000000000002076,
                -0.0000000000000924,
                0.0000000000000417,
                -0.0000000000000190,
                0.0000000000000087,
                -0.0000000000000040,
                0.0000000000000019,
                -0.0000000000000009,
                0.0000000000000004,
                -0.0000000000000002,
                0.0000000000000001,
                -0.0000000000000000};

// series for am21       on the interval -1.25000d-01 to  0.
//                                        with weighted error   2.89e-17
//                                         log weighted error  16.54
//                               significant figures required  14.15
//                                    decimal places required  17.34

        private final static double am21cs[]={
                0.0065809191761485,
                0.0023675984685722,
                0.0001324741670371,
                0.0000157600904043,
                0.0000027529702663,
                0.0000006102679017,
                0.0000001595088468,
                0.0000000471033947,
                0.0000000152933871,
                0.0000000053590722,
                0.0000000020000910,
                0.0000000007872292,
                0.0000000003243103,
                0.0000000001390106,
                0.0000000000617011,
                0.0000000000282491,
                0.0000000000132979,
                0.0000000000064188,
                0.0000000000031697,
                0.0000000000015981,
                0.0000000000008213,
                0.0000000000004296,
                0.0000000000002284,
                0.0000000000001232,
                0.0000000000000675,
                0.0000000000000374,
                0.0000000000000210,
                0.0000000000000119,
                0.0000000000000068,
                0.0000000000000039,
                0.0000000000000023,
                0.0000000000000013,
                0.0000000000000008,
                0.0000000000000005,
                0.0000000000000003,
                0.0000000000000001,
                0.0000000000000001,
                0.0000000000000000,
                0.0000000000000000,
                0.0000000000000000};

// series for ath1       on the interval -1.25000d-01 to  0.
//                                        with weighted error   2.53e-17
//                                         log weighted error  16.60
//                               significant figures required  15.15
//                                    decimal places required  17.38

        private final static double ath1cs[]={
                -0.07125837815669365,
                -0.00590471979831451,
                -0.00012114544069499,
                -0.00000988608542270,
                -0.00000138084097352,
                -0.00000026142640172,
                -0.00000006050432589,
                -0.00000001618436223,
                -0.00000000483464911,
                -0.00000000157655272,
                -0.00000000055231518,
                -0.00000000020545441,
                -0.00000000008043412,
                -0.00000000003291252,
                -0.00000000001399875,
                -0.00000000000616151,
                -0.00000000000279614,
                -0.00000000000130428,
                -0.00000000000062373,
                -0.00000000000030512,
                -0.00000000000015239,
                -0.00000000000007758,
                -0.00000000000004020,
                -0.00000000000002117,
                -0.00000000000001132,
                -0.00000000000000614,
                -0.00000000000000337,
                -0.00000000000000188,
                -0.00000000000000105,
                -0.00000000000000060,
                -0.00000000000000034,
                -0.00000000000000020,
                -0.00000000000000011,
                -0.00000000000000007,
                -0.00000000000000004,
                -0.00000000000000002};

// series for am22       on the interval -1.00000d+00 to -1.25000d-01
//                                        with weighted error   2.99e-17
//                                         log weighted error  16.52
//                               significant figures required  14.57
//                                    decimal places required  17.28

        private final static double am22cs[]={
                -0.01562844480625341,
                0.00778336445239681,
                0.00086705777047718,
                0.00015696627315611,
                0.00003563962571432,
                0.00000924598335425,
                0.00000262110161850,
                0.00000079188221651,
                0.00000025104152792,
                0.00000008265223206,
                0.00000002805711662,
                0.00000000976821090,
                0.00000000347407923,
                0.00000000125828132,
                0.00000000046298826,
                0.00000000017272825,
                0.00000000006523192,
                0.00000000002490471,
                0.00000000000960156,
                0.00000000000373448,
                0.00000000000146417,
                0.00000000000057826,
                0.00000000000022991,
                0.00000000000009197,
                0.00000000000003700,
                0.00000000000001496,
                0.00000000000000608,
                0.00000000000000248,
                0.00000000000000101,
                0.00000000000000041,
                0.00000000000000017,
                0.00000000000000007,
                0.00000000000000002};

// series for ath2       on the interval -1.00000d+00 to -1.25000d-01
//                                        with weighted error   2.57e-17
//                                         log weighted error  16.59
//                               significant figures required  15.07
//                                    decimal places required  17.34

        private final static double ath2cs[]={
                0.00440527345871877,
                -0.03042919452318455,
                -0.00138565328377179,
                -0.00018044439089549,
                -0.00003380847108327,
                -0.00000767818353522,
                -0.00000196783944371,
                -0.00000054837271158,
                -0.00000016254615505,
                -0.00000005053049981,
                -0.00000001631580701,
                -0.00000000543420411,
                -0.00000000185739855,
                -0.00000000064895120,
                -0.00000000023105948,
                -0.00000000008363282,
                -0.00000000003071196,
                -0.00000000001142367,
                -0.00000000000429811,
                -0.00000000000163389,
                -0.00000000000062693,
                -0.00000000000024260,
                -0.00000000000009461,
                -0.00000000000003716,
                -0.00000000000001469,
                -0.00000000000000584,
                -0.00000000000000233,
                -0.00000000000000093,
                -0.00000000000000037,
                -0.00000000000000015,
                -0.00000000000000006,
                -0.00000000000000002};

// series for bi0        on the interval  0.          to  9.00000d+00
//                                        with weighted error   2.46e-18
//                                         log weighted error  17.61
//                               significant figures required  17.90
//                                    decimal places required  18.15

        private final static double bi0cs[]={
                -0.07660547252839144951,
                1.927337953993808270,
                0.2282644586920301339,
                0.01304891466707290428,
                0.00043442709008164874,
                0.00000942265768600193,
                0.00000014340062895106,
                0.00000000161384906966,
                0.00000000001396650044,
                0.00000000000009579451,
                0.00000000000000053339,
                0.00000000000000000245};

// series for bj0        on the interval  0.          to  1.60000d+01
//                                        with weighted error   7.47e-18
//                                         log weighted error  17.13
//                               significant figures required  16.98
//                                    decimal places required  17.68

        private final static double bj0cs[]={
                0.100254161968939137,
                -0.665223007764405132,
                0.248983703498281314,
                -0.0332527231700357697,
                0.0023114179304694015,
                -0.0000991127741995080,
                0.0000028916708643998,
                -0.0000000612108586630,
                0.0000000009838650793,
                -0.0000000000124235515,
                0.0000000000001265433,
                -0.0000000000000010619,
                0.0000000000000000074};

// series for bm0        on the interval  0.          to  6.25000d-02
//                                        with weighted error   4.98e-17
//                                         log weighted error  16.30
//                               significant figures required  14.97
//                                    decimal places required  16.96

        private final static double bm0cs[]={
                0.09284961637381644,
                -0.00142987707403484,
                0.00002830579271257,
                -0.00000143300611424,
                0.00000012028628046,
                -0.00000001397113013,
                0.00000000204076188,
                -0.00000000035399669,
                0.00000000007024759,
                -0.00000000001554107,
                0.00000000000376226,
                -0.00000000000098282,
                0.00000000000027408,
                -0.00000000000008091,
                0.00000000000002511,
                -0.00000000000000814,
                0.00000000000000275,
                -0.00000000000000096,
                0.00000000000000034,
                -0.00000000000000012,
                0.00000000000000004};

// series for bth0       on the interval  0.          to  6.25000d-02
//                                        with weighted error   3.67e-17
//                                         log weighted error  16.44
//                               significant figures required  15.53
//                                    decimal places required  17.13

        private final static double bth0cs[]={
                -0.24639163774300119,
                0.001737098307508963,
                -0.000062183633402968,
                0.000004368050165742,
                -0.000000456093019869,
                0.000000062197400101,
                -0.000000010300442889,
                0.000000001979526776,
                -0.000000000428198396,
                0.000000000102035840,
                -0.000000000026363898,
                0.000000000007297935,
                -0.000000000002144188,
                0.000000000000663693,
                -0.000000000000215126,
                0.000000000000072659,
                -0.000000000000025465,
                0.000000000000009229,
                -0.000000000000003448,
                0.000000000000001325,
                -0.000000000000000522,
                0.000000000000000210,
                -0.000000000000000087,
                0.000000000000000036};

// series for by0        on the interval  0.          to  1.60000d+01
//                                        with weighted error   1.20e-17
//                                         log weighted error  16.92
//                               significant figures required  16.15
//                                    decimal places required  17.48

        private final static double by0cs[]={
                -0.011277839392865573,
                -0.12834523756042035,
                -0.10437884799794249,
                0.023662749183969695,
                -0.002090391647700486,
                0.000103975453939057,
                -0.000003369747162423,
                0.000000077293842676,
                -0.000000001324976772,
                0.000000000017648232,
                -0.000000000000188105,
                0.000000000000001641,
                -0.000000000000000011};

// series for bi1        on the interval  0.          to  9.00000d+00
//                                        with weighted error   2.40e-17
//                                         log weighted error  16.62
//                               significant figures required  16.23
//                                    decimal places required  17.14

        private final static double bi1cs[]={
                -0.001971713261099859,
                0.40734887667546481,
                0.034838994299959456,
                0.001545394556300123,
                0.000041888521098377,
                0.000000764902676483,
                0.000000010042493924,
                0.000000000099322077,
                0.000000000000766380,
                0.000000000000004741,
                0.000000000000000024};

// series for bj1        on the interval  0.          to  1.60000d+01
//                                        with weighted error   4.48e-17
//                                         log weighted error  16.35
//                               significant figures required  15.77
//                                    decimal places required  16.89

        private final static double bj1cs[]={
                -0.11726141513332787,
                -0.25361521830790640,
                0.050127080984469569,
                -0.004631514809625081,
                0.000247996229415914,
                -0.000008678948686278,
                0.000000214293917143,
                -0.000000003936093079,
                0.000000000055911823,
                -0.000000000000632761,
                0.000000000000005840,
                -0.000000000000000044};

// series for bm1        on the interval  0.          to  6.25000d-02
//                                        with weighted error   5.61e-17
//                                         log weighted error  16.25
//                               significant figures required  14.97
//                                    decimal places required  16.91

        private final static double bm1cs[]={
                0.1047362510931285,
                0.00442443893702345,
                -0.00005661639504035,
                0.00000231349417339,
                -0.00000017377182007,
                0.00000001893209930,
                -0.00000000265416023,
                0.00000000044740209,
                -0.00000000008691795,
                0.00000000001891492,
                -0.00000000000451884,
                0.00000000000116765,
                -0.00000000000032265,
                0.00000000000009450,
                -0.00000000000002913,
                0.00000000000000939,
                -0.00000000000000315,
                0.00000000000000109,
                -0.00000000000000039,
                0.00000000000000014,
                -0.00000000000000005};

// series for bth1       on the interval  0.          to  6.25000d-02
//                                        with weighted error   4.10e-17
//                                         log weighted error  16.39
//                               significant figures required  15.96
//                                    decimal places required  17.08

        private final static double bth1cs[]={
                0.74060141026313850,
                -0.004571755659637690,
                0.000119818510964326,
                -0.000006964561891648,
                0.000000655495621447,
                -0.000000084066228945,
                0.000000013376886564,
                -0.000000002499565654,
                0.000000000529495100,
                -0.000000000124135944,
                0.000000000031656485,
                -0.000000000008668640,
                0.000000000002523758,
                -0.000000000000775085,
                0.000000000000249527,
                -0.000000000000083773,
                0.000000000000029205,
                -0.000000000000010534,
                0.000000000000003919,
                -0.000000000000001500,
                0.000000000000000589,
                -0.000000000000000237,
                0.000000000000000097,
                -0.000000000000000040};

// series for by1        on the interval  0.          to  1.60000d+01
//                                        with weighted error   1.87e-18
//                                         log weighted error  17.73
//                               significant figures required  17.83
//                                    decimal places required  18.30

        private final static double by1cs[]={
                0.03208047100611908629,
                1.262707897433500450,
                0.00649996189992317500,
                -0.08936164528860504117,
                0.01325088122175709545,
                -0.00089790591196483523,
                0.00003647361487958306,
                -0.00000100137438166600,
                0.00000001994539657390,
                -0.00000000030230656018,
                0.00000000000360987815,
                -0.00000000000003487488,
                0.00000000000000027838,
                -0.00000000000000000186};



        /**
        * Evaluates a Chebyshev series.
        * @param x value at which to evaluate series
        * @param series the coefficients of the series
        */
        public static double chebyshev(double x, double series[]) {
                double twox,b0=0.0,b1=0.0,b2=0.0;
                twox=2*x;
                for(int i=series.length-1;i>-1;i--) {
                        b2=b1;
                        b1=b0;
                        b0=twox*b1-b2+series[i];
                }
                return 0.5*(b0-b2);
        }
        /**
        * Airy function.
        * Based on the NETLIB Fortran function ai written by W. Fullerton.
        */
        public static double airy(double x) {
                if(x<-1.0) {
                        double mp[]=airyModPhase(x);
                        return mp[0]*Math.cos(mp[1]);
                } else if(x>1.0)
                        return expAiry(x)*Math.exp(-2.0*x*Math.sqrt(x)/3.0);
                else {
                        final double z=x*x*x;
                        return 0.375+(chebyshev(z,aifcs)-x*(0.25+chebyshev(z,aigcs)));
                }
        }
        /**
        * Airy modulus and phase.
        * Based on the NETLIB Fortran subroutine r9aimp written by W. Fullerton.
        * @return an array with [0] containing the modulus and [1] containing the phase.
        */
        private static double[] airyModPhase(double x) {
                double z,mp[]=new double[2];
                if(x<-2.0) {
                        z=16.0/(x*x*x)+1.0;
                        mp[0]=0.3125+chebyshev(z,am21cs);
                        mp[1] =-0.625+chebyshev(z,ath1cs);
                } else {
                        z=(16.0/(x*x*x)+9.0)/7.0;
                        mp[0]=0.3125+chebyshev(z,am22cs);
                        mp[1]=-0.625+chebyshev(z,ath2cs);
                }
                final double sqrtx=Math.sqrt(-x);
                mp[0]=Math.sqrt(mp[0]/sqrtx);
                mp[1]=Math.PI/4.0-x*sqrtx*mp[1];
                return mp;
        }
        /**
        * Exponential scaled Airy function.
        * Based on the NETLIB Fortran function aie written by W. Fullerton.
        */
        private static double expAiry(double x) {
                if(x<-1.0) {
                        double mp[]=airyModPhase(x);
                        return mp[0]*Math.cos(mp[1]);
                } else if(x<=1.0) {
                        final double z=x*x*x;
                        return 0.375+(chebyshev(z,aifcs)-x*(0.25+chebyshev(z,aigcs)))*Math.exp(2.0*x*Math.sqrt(x)/3.0);
                } else {
                        final double sqrtx=Math.sqrt(x);
                        final double z=2.0/(x*sqrtx)-1.0;
                        return (0.28125+chebyshev(z,aipcs))/Math.sqrt(sqrtx);
                }
        }
        /**
        * Bessel function of first kind, order zero.
        * Based on the NETLIB Fortran function besj0 written by W. Fullerton.
        */
        public static double besselFirstZero(double x) {
                double y=Math.abs(x);
                if(y>4.0) {
                        final double z=32/(y*y)-1;
                        final double amplitude=(0.75+chebyshev(z,bm0cs))/Math.sqrt(y);
                        final double theta=y-Math.PI/4.0+chebyshev(z,bth0cs)/y;
                        return amplitude*Math.cos(theta);
                } else if(y==0.0)
                        return 1.0;
                else
                        return chebyshev(0.125*y*y-1,bj0cs);
        }
        /**
        * Modified Bessel function of first kind, order zero.
        * Based on the NETLIB Fortran function besi0 written by W. Fullerton.
        */
        public static double modBesselFirstZero(double x) {
                double y=Math.abs(x);
                if(y>3.0)
                        return Math.exp(y)*expModBesselFirstZero(x);
                else
                        return 2.75+chebyshev(y*y/4.5-1.0,bi0cs);
        }
        /**
        * Exponential scaled modified Bessel function of first kind, order zero.
        * Based on the NETLIB Fortran function besi0e written by W. Fullerton.
        */
        private static double expModBesselFirstZero(double x) {
                final double y=Math.abs(x);
                if(y>3.0) {
                        if(y>8.0)
                                return (0.375+chebyshev(16.0/y-1.0,ai02cs))/Math.sqrt(y);
                        else
                                return (0.375+chebyshev((48.0/y-11.0)/5.0,ai0cs))/Math.sqrt(y);
                } else
                        return Math.exp(-y)*(2.75+chebyshev(y*y/4.5-1.0,bi0cs));
        }
        /**
        * Bessel function of first kind, order one.
        * Based on the NETLIB Fortran function besj1 written by W. Fullerton.
        */
        public static double besselFirstOne(double x) {
                double y=Math.abs(x);
                if(y>4.0) {
                        final double z=32.0/(y*y)-1.0;
                        final double amplitude=(0.75+chebyshev(z,bm1cs))/Math.sqrt(y);
                        final double theta=y-3.0*Math.PI/4.0+chebyshev(z,bth1cs)/y;
                        return Math.abs(amplitude)*x*Math.cos(theta)/Math.abs(x);
                } else if(y==0.0)
                        return 0.0;
                else
                        return x*(0.25+chebyshev(0.125*y*y-1.0,bj1cs));
        }
        /**
        * Modified Bessel function of first kind, order one.
        * Based on the NETLIB Fortran function besi1 written by W. Fullerton.
        */
        public static double modBesselFirstOne(double x) {
                final double y=Math.abs(x);
                if(y>3.0)
                        return Math.exp(y)*expModBesselFirstOne(x);
                else if(y==0.0)
                        return 0.0;
                else
                        return x*(0.875+chebyshev(y*y/4.5-1.0,bi1cs));
        }
        /**
        * Exponential scaled modified Bessel function of first kind, order one.
        * Based on the NETLIB Fortran function besi1e written by W. Fullerton.
        */
        private static double expModBesselFirstOne(double x) {
                final double y=Math.abs(x);
                if(y>3.0) {
                        if(y>8.0)
                                return x/y*(0.375+chebyshev(16.0/y-1.0,ai12cs))/Math.sqrt(y);
                        else
                                return x/y*(0.375+chebyshev((48.0/y-11.0)/5.0,ai1cs))/Math.sqrt(y);
                } else if(y==0.0)
                        return 0.0;
                else
                        return Math.exp(-y)*x*(0.875+chebyshev(y*y/4.5-1.0,bi1cs));
        }
        /**
        * Bessel function of second kind, order zero.
        * Based on the NETLIB Fortran function besy0 written by W. Fullerton.
        */
        public static double besselSecondZero(double x) {
                if(x>4.0) {
                        final double z=32.0/(x*x)-1.0;
                        final double amplitude=(0.75+chebyshev(z,bm0cs))/Math.sqrt(x);
                        final double theta=x-Math.PI/4+chebyshev(z,bth0cs)/x;
                        return amplitude*Math.sin(theta);
                } else
                        return (Math.log(0.5)+Math.log(x))*besselFirstZero(x)+0.375+chebyshev(0.125*x*x-1.0,by0cs)*2.0/Math.PI;
        }
        /**
        * Bessel function of second kind, order one.
        * Based on the NETLIB Fortran function besy1 written by W. Fullerton.
        */
        public static double besselSecondOne(double x) {
                if(x>4.0) {
                        final double z=32.0/(x*x)-1.0;
                        final double amplitude=(0.75+chebyshev(z,bm1cs))/Math.sqrt(x);
                        final double theta=x-3.0*Math.PI/4.0+chebyshev(z,bth1cs)/x;
                        return amplitude*Math.sin(theta);
                } else
                        return 2.0*Math.log(0.5*x)*besselFirstOne(x)/Math.PI+(0.5+chebyshev(0.125*x*x-1.0,by1cs))/x;
        }

        private final static double LOGSQRT2PI=Math.log(SQRT2PI);

        /**
        * Gamma function.
        * Based on public domain NETLIB (Fortran) code by W. J. Cody and L. Stoltz<BR>
        * Applied Mathematics Division<BR>
        * Argonne National Laboratory<BR>
        * Argonne, IL 60439<BR>
        * <P>
        * References:
        * <OL>
        * <LI>"An Overview of Software Development for Special Functions", W. J. Cody, Lecture Notes in Mathematics, 506, Numerical Analysis Dundee, 1975, G. A. Watson (ed.), Springer Verlag, Berlin, 1976.
        * <LI>Computer Approximations, Hart, Et. Al., Wiley and sons, New York, 1968.
        * </OL></P><P>
        * From the original documentation:
        * </P><P>
        * This routine calculates the GAMMA function for a real argument X. 
	* Computation is based on an algorithm outlined in reference 1. 
	* The program uses rational functions that approximate the GAMMA 
	* function to at least 20 significant decimal digits.  Coefficients 
	* for the approximation over the interval (1,2) are unpublished. 
	* Those for the approximation for X .GE. 12 are from reference 2. 
	* The accuracy achieved depends on the arithmetic system, the 
	* compiler, the intrinsic functions, and proper selection of the 
	* machine-dependent constants. 
        * </P><P>
	* Error returns:<BR>
	* The program returns the value XINF for singularities or when overflow would occur.
        * The computation is believed to be free of underflow and overflow.
	* </P>
        * @return Double.MAX_VALUE if overflow would occur, i.e. if abs(x) > 171.624
        * @author Jaco van Kooten
	*/
        public static double gamma(double x) {
// Gamma function related constants
                final double g_p[] = { -1.71618513886549492533811,
                        24.7656508055759199108314,-379.804256470945635097577,
                        629.331155312818442661052,866.966202790413211295064,
                        -31451.2729688483675254357,-36144.4134186911729807069,
                        66456.1438202405440627855 };
                final double g_q[] = { -30.8402300119738975254353,
                        315.350626979604161529144,-1015.15636749021914166146,
                        -3107.77167157231109440444,22538.1184209801510330112,
                        4755.84627752788110767815,-134659.959864969306392456,
                        -115132.259675553483497211 };
                final double g_c[] = { -.001910444077728,8.4171387781295e-4,
                        -5.952379913043012e-4,7.93650793500350248e-4,
                        -.002777777777777681622553,.08333333333333333331554247,
                        .0057083835261 };
                final double g_xbig = 171.624;

                double fact=1.0, xden, xnum;
                int i, n=0;
                double y=x, z, y1;
                boolean parity=false;
                double res, sum, ysq;

                if (y <= 0.0) {
// ----------------------------------------------------------------------
//  Argument is negative
// ----------------------------------------------------------------------
                        y = -(x);
                        y1 = (int)y;
                        res = y - y1;
                        if (res != 0.0) {
                                if (y1 != (((int)(y1*0.5)) * 2.0))
                                        parity = true;
                                fact = -Math.PI/ Math.sin(Math.PI * res);
                                y++;
                        } else 
                                return Double.MAX_VALUE;
                }
// ----------------------------------------------------------------------
//  Argument is positive
// ----------------------------------------------------------------------
                if (y < EPS) {
// ----------------------------------------------------------------------
//  Argument .LT. EPS
// ----------------------------------------------------------------------
                        if (y >= XMININ) 
                                res = 1.0 / y;
                        else 
                                return Double.MAX_VALUE;
                } else if (y < 12.0) {
                        y1 = y;
                        if (y < 1.0) {
// ----------------------------------------------------------------------
//  0.0 .LT. argument .LT. 1.0
// ----------------------------------------------------------------------
                                z = y;
                                y++;
                        } else {
// ----------------------------------------------------------------------
//  1.0 .LT. argument .LT. 12.0, reduce argument if necessary
// ----------------------------------------------------------------------
                                n = (int)y - 1;
                                y -= (double) n;
                                z = y - 1.0;
                        }
// ----------------------------------------------------------------------
//  Evaluate approximation for 1.0 .LT. argument .LT. 2.0
// ----------------------------------------------------------------------
                        xnum = 0.0;
                        xden = 1.0;
                        for (i = 0; i < 8; ++i) {
                                xnum = (xnum + g_p[i]) * z;
                                xden = xden * z + g_q[i];
                        }
                        res = xnum / xden + 1.0;
                        if (y1 < y) 
// ----------------------------------------------------------------------
//  Adjust result for case  0.0 .LT. argument .LT. 1.0
// ----------------------------------------------------------------------
                                res /= y1;
                        else if (y1 > y) {
// ----------------------------------------------------------------------
//  Adjust result for case  2.0 .LT. argument .LT. 12.0
// ----------------------------------------------------------------------
                                for (i = 0; i < n; ++i) {
                                        res *= y;
                                        y++;
                                }
                        }
                } else {
// ----------------------------------------------------------------------
//  Evaluate for argument .GE. 12.0
// ----------------------------------------------------------------------
                        if (y <= g_xbig) {
                                ysq = y * y;
                                sum = g_c[6];
                                for (i = 0; i < 6; ++i)
                                        sum = sum / ysq + g_c[i];
                                sum = sum / y - y + LOGSQRT2PI;
                                sum += (y - 0.5) * Math.log(y);
                                res = Math.exp(sum);
                        } else 
                                return Double.MAX_VALUE;
                }
// ----------------------------------------------------------------------
//  Final adjustments and return
// ----------------------------------------------------------------------
                if (parity)
                        res = -res;
                if (fact != 1.0)
                        res = fact / res;
                return res;
        } 

        /**
        * The largest argument for which logGamma(x) is representable in the machine.
        */
        private final static double logGamma_xBig=2.55e305;

// Function cache for logGamma
        private static double logGammaCache_res=0.0;
        private static double logGammaCache_x=0.0;

	/**
        * The natural logarithm of the gamma function.
        * Based on public domain NETLIB (Fortran) code by W. J. Cody and L. Stoltz<BR>
        * Applied Mathematics Division<BR>
        * Argonne National Laboratory<BR>
        * Argonne, IL 60439<BR>
        * <P>
        * References:
        * <OL>
        * <LI>W. J. Cody and K. E. Hillstrom, 'Chebyshev Approximations for the Natural Logarithm of the Gamma Function,' Math. Comp. 21, 1967, pp. 198-203.
        * <LI>K. E. Hillstrom, ANL/AMD Program ANLC366S, DGAMMA/DLGAMA, May, 1969.
        * <LI>Hart, Et. Al., Computer Approximations, Wiley and sons, New York, 1968.
        * </OL></P><P>
        * From the original documentation:
        * </P><P>
        * This routine calculates the LOG(GAMMA) function for a positive real argument X.
        * Computation is based on an algorithm outlined in references 1 and 2.
        * The program uses rational functions that theoretically approximate LOG(GAMMA)
        * to at least 18 significant decimal digits.  The approximation for X > 12 is from reference 3,
        * while approximations for X < 12.0 are similar to those in reference 1, but are unpublished.
        * The accuracy achieved depends on the arithmetic system, the compiler, the intrinsic functions,
        * and proper selection of the machine-dependent constants.
        * </P><P>
        * Error returns:<BR>
        * The program returns the value XINF for X .LE. 0.0 or when overflow would occur.
        * The computation is believed to be free of underflow and overflow.
        * </P>
        * @return Double.MAX_VALUE for x < 0.0 or when overflow would occur, i.e. x > 2.55E305
        * @author Jaco van Kooten
	*/
        public static double logGamma(double x) {
// Log Gamma related constants
                final double lg_d1 = -.5772156649015328605195174,
                        lg_d2 = .4227843350984671393993777,
         		lg_d4 = 1.791759469228055000094023;
                final double lg_p1[] = { 4.945235359296727046734888,
                        201.8112620856775083915565,2290.838373831346393026739,
                        11319.67205903380828685045,28557.24635671635335736389,
                        38484.96228443793359990269,26377.48787624195437963534,
                        7225.813979700288197698961 };
                final double lg_q1[] = { 67.48212550303777196073036,
                        1113.332393857199323513008,7738.757056935398733233834,
                        27639.87074403340708898585,54993.10206226157329794414,
                        61611.22180066002127833352,36351.27591501940507276287,
                        8785.536302431013170870835 };
                final double lg_p2[] = { 4.974607845568932035012064,
                        542.4138599891070494101986,15506.93864978364947665077,
                        184793.2904445632425417223,1088204.76946882876749847,
                        3338152.967987029735917223,5106661.678927352456275255,
                        3074109.054850539556250927 };
                final double lg_q2[] = { 183.0328399370592604055942,
                        7765.049321445005871323047,133190.3827966074194402448,
                        1136705.821321969608938755,5267964.117437946917577538,
                        13467014.54311101692290052,17827365.30353274213975932,
                        9533095.591844353613395747 };
                final double lg_p4[] = { 14745.02166059939948905062,
                        2426813.369486704502836312,121475557.4045093227939592,
                        2663432449.630976949898078,29403789566.34553899906876,
                        170266573776.5398868392998,492612579337.743088758812,
                        560625185622.3951465078242 };
                final double lg_q4[] = { 2690.530175870899333379843,
                        639388.5654300092398984238,41355999.30241388052042842,
                        1120872109.61614794137657,14886137286.78813811542398,
                        101680358627.2438228077304,341747634550.7377132798597,
                        446315818741.9713286462081 };
                final double lg_c[] = { -0.001910444077728,8.4171387781295e-4,
                        -5.952379913043012e-4,7.93650793500350248e-4,
                        -0.002777777777777681622553,0.08333333333333333331554247,
                        0.0057083835261 };
//       Rough estimate of the fourth root of logGamma_xBig
                final double lg_frtbig = 2.25e76;

                final double pnt68 = 0.6796875;

                double xden, corr, xnum;
                int i;
                double y, xm1, xm2, xm4, res, ysq;

                if (x==logGammaCache_x)
                        return logGammaCache_res;
                y = x;
                if (y > 0.0 && y <= logGamma_xBig) {
                        if (y <= EPS) {
                                res = -Math.log(y);
                        } else if (y <= 1.5) {
// ----------------------------------------------------------------------
//  EPS .LT. X .LE. 1.5
// ----------------------------------------------------------------------
                                if (y < pnt68) {
                                        corr = -Math.log(y);
                                        xm1 = y;
                                } else {
                                        corr = 0.0;
                                        xm1 = y - 1.0;
                                }
                                if (y <= 0.5 || y >= pnt68) {
                                        xden = 1.0;
                                        xnum = 0.0;
                                        for (i = 0; i < 8; i++) {
                                                xnum = xnum * xm1 + lg_p1[i];
                                                xden = xden * xm1 + lg_q1[i];
                                        }
                                        res = corr + xm1 * (lg_d1 + xm1 * (xnum / xden));
                                } else {
                                        xm2 = y - 1.0;
                                        xden = 1.0;
                                        xnum = 0.0;
                                        for (i = 0; i < 8; i++) {
                                                xnum = xnum * xm2 + lg_p2[i];
                                                xden = xden * xm2 + lg_q2[i];
                                        }
                                        res = corr + xm2 * (lg_d2 + xm2 * (xnum / xden));
                                }
                        } else if (y <= 4.0) {
// ----------------------------------------------------------------------
//  1.5 .LT. X .LE. 4.0
// ----------------------------------------------------------------------
                                xm2 = y - 2.0;
                                xden = 1.0;
                                xnum = 0.0;
                                for (i = 0; i < 8; i++) {
                                        xnum = xnum * xm2 + lg_p2[i];
                                        xden = xden * xm2 + lg_q2[i];
                                }
                                res = xm2 * (lg_d2 + xm2 * (xnum / xden));
                        } else if (y <= 12.0) {
// ----------------------------------------------------------------------
//  4.0 .LT. X .LE. 12.0
// ----------------------------------------------------------------------
                                xm4 = y - 4.0;
                                xden = -1.0;
                                xnum = 0.0;
                                for (i = 0; i < 8; i++) {
                                        xnum = xnum * xm4 + lg_p4[i];
                                        xden = xden * xm4 + lg_q4[i];
                                }
                                res = lg_d4 + xm4 * (xnum / xden);
                        } else {
// ----------------------------------------------------------------------
//  Evaluate for argument .GE. 12.0
// ----------------------------------------------------------------------
                                res = 0.0;
                                if (y <= lg_frtbig) {
                			res = lg_c[6];
                			ysq = y * y;
                			for (i = 0; i < 6; i++)
                        		    	res = res / ysq + lg_c[i];
                                }
                                res /= y;
                                corr = Math.log(y);
                                res = res + LOGSQRT2PI - 0.5 * corr;
                                res += y * (corr - 1.0);
                        }
                } else {
// ----------------------------------------------------------------------
//  Return for bad arguments
// ----------------------------------------------------------------------
                        res = Double.MAX_VALUE;
                }
// ----------------------------------------------------------------------
//  Final adjustments and return
// ----------------------------------------------------------------------
                logGammaCache_x = x;
                logGammaCache_res = res;
                return res;
        }

        private final static int MAX_ITERATIONS = 150;
        private final static double PRECISION = 4.0*EPS;

        /**
        * Incomplete gamma function.
        * The computation is based on approximations presented in Numerical Recipes, Chapter 6.2 (W.H. Press et al, 1992).
        * @param a require a>=0
        * @param x require x>=0
        * @return 0 if x<0, a<=0 or a>2.55E305 to avoid errors and over/underflow
        * @author Jaco van Kooten
        */
        public static double incompleteGamma(double a, double x) {
                if (x <= 0.0 || a <= 0.0 || a > logGamma_xBig)
                        return 0.0;
                if (x < (a+1.0))
                        return gammaSeriesExpansion(a,x);
                else 
                        return 1.0-gammaFraction(a,x);
        }
        /**
        * @author Jaco van Kooten
        */
        private static double gammaSeriesExpansion(double a, double x) {
                double ap=a;
                double del=1.0/a;
                double sum = del;
                for (int n=1; n < MAX_ITERATIONS; n++) {
                        ++ap;
                        del *= x/ap;
                        sum += del;
                        if (del < sum*PRECISION)
                                return sum*Math.exp(-x + a*Math.log(x) - logGamma(a));
                }
                return 0.0;
        }
        /**
        * @author Jaco van Kooten
        */
        private static double gammaFraction(double a, double x) {
                double b=x+1.0-a;
                double c=1.0/XMININ;
                double d=1.0/b;
                double h=d;
                double del=0.0;
                double an; 
                for (int i=1; i<MAX_ITERATIONS && Math.abs(del-1.0)>PRECISION; i++) {
                        an = -i*(i-a);
                        b += 2.0;
                        d=an*d+b;
                        c=b+an/c;
                        if (Math.abs(c) < XMININ)
                                c=XMININ;
                        if (Math.abs(d) < XMININ)
                                c=XMININ;
                        d=1.0/d;
                        del=d*c;
                        h *= del;
                }
                return Math.exp(-x + a*Math.log(x) - logGamma(a))*h;
        }
        /**
        * Beta function.
        * @param p require p>0
        * @param q require q>0
        * @return 0 if p<=0, q<=0 or p+q>2.55E305 to avoid errors and over/underflow
        * @author Jaco van Kooten
        */
        public static double beta(double p, double q) {
                if (p <= 0.0 || q <= 0.0 || (p+q) > logGamma_xBig)
                        return 0.0;
                else
                        return Math.exp(logBeta(p,q));
	}

// Function cache for logBeta
        private static double logBetaCache_res=0.0;
        private static double logBetaCache_p=0.0;
        private static double logBetaCache_q=0.0;

        /**
        * The natural logarithm of the beta function.
        * @param p require p>0
        * @param q require q>0
        * @return 0 if p<=0, q<=0 or p+q>2.55E305 to avoid errors and over/underflow
        * @author Jaco van Kooten
        */
        public static double logBeta(double p, double q) {
                if (p != logBetaCache_p || q != logBetaCache_q) {
                        logBetaCache_p = p;
                        logBetaCache_q = q;
                        if (p <= 0.0 || q <= 0.0 || (p+q) > logGamma_xBig)
                                logBetaCache_res = 0.0;
                        else
                                logBetaCache_res = logGamma(p)+logGamma(q)-logGamma(p+q);
                }
                return logBetaCache_res;
        }
        /**
        * Incomplete beta function.
        * The computation is based on formulas from Numerical Recipes, Chapter 6.4 (W.H. Press et al, 1992).
        * @param x require 0<=x<=1
        * @param p require p>0
        * @param q require q>0
        * @return 0 if x<0, p<=0, q<=0 or p+q>2.55E305 and 1 if x>1 to avoid errors and over/underflow
        * @author Jaco van Kooten
        */
	public static double incompleteBeta(double x, double p, double q) {
                if (x <= 0.0) 
        		return 0.0;
                else if (x >= 1.0)
                        return 1.0;
                else if (p <= 0.0 || q <= 0.0 || (p+q) > logGamma_xBig)
        		return 0.0;
                else {
                        final double beta_gam=Math.exp(-logBeta(p,q) + p*Math.log(x) + q*Math.log(1.0-x));
                        if (x < (p+1.0)/(p+q+2.0))
                                return beta_gam*betaFraction(x,p,q)/p;
        		else
        			return 1.0-(beta_gam*betaFraction(1.0-x,q,p)/q);
                }
        }
        /**
	* Evaluates of continued fraction part of incomplete beta function.
        * Based on an idea from Numerical Recipes (W.H. Press et al, 1992).
        * @author Jaco van Kooten
        */
	private static double betaFraction(double x, double p, double q) {
        	int m, m2;
        	double sum_pq, p_plus, p_minus, c =1.0 , d, delta, h, frac;
        	sum_pq  = p + q;
        	p_plus  = p + 1.0;
        	p_minus = p - 1.0;
        	h=1.0-sum_pq*x/p_plus;
        	if (Math.abs(h) < XMININ)
                        h=XMININ;
        	h=1.0/h;
                frac = h;
        	m=1;
        	delta = 0.0;
        	while (m <= MAX_ITERATIONS && Math.abs(delta-1.0) > PRECISION ) {
                        m2=2*m;
                // even index for d 
        		d=m*(q-m)*x/((p_minus+m2)*(p+m2));
        		h=1.0+d*h;
        		if (Math.abs(h) < XMININ)
                                h=XMININ;
        		h=1.0/h;
        		c=1.0+d/c;
        		if (Math.abs(c) < XMININ)
                                c=XMININ;
        		frac *= h*c;
                // odd index for d
        		d = -(p+m)*(sum_pq+m)*x/((p+m2)*(p_plus+m2));
        		h=1.0+d*h;
        		if (Math.abs(h) < XMININ)
                                h=XMININ;
        		h=1.0/h;
        		c=1.0+d/c;
        		if (Math.abs(c) < XMININ)
                                c=XMININ;
                        delta=h*c;
                        frac *= delta;
                        m++;
                }
                return frac;
        }

// ====================================================
// Copyright (C) 1993 by Sun Microsystems, Inc. All rights reserved.
//
// Developed at SunSoft, a Sun Microsystems, Inc. business.
// Permission to use, copy, modify, and distribute this
// software is freely granted, provided that this notice 
// is preserved.
// ====================================================
//
//			     x
//		      2      |\
//     erf(x)  =  ---------  | exp(-t*t)dt
//	 	   sqrt(pi) \| 
//			     0
//
//     erfc(x) =  1-erf(x)
//  Note that 
//		erf(-x) = -erf(x)
//		erfc(-x) = 2 - erfc(x)
//
// Method:
//	1. For |x| in [0, 0.84375]
//	    erf(x)  = x + x*R(x^2)
//          erfc(x) = 1 - erf(x)           if x in [-.84375,0.25]
//                  = 0.5 + ((0.5-x)-x*R)  if x in [0.25,0.84375]
//	   where R = P/Q where P is an odd poly of degree 8 and
//	   Q is an odd poly of degree 10.
//						 -57.90
//			| R - (erf(x)-x)/x | <= 2
//	
//
//	   Remark. The formula is derived by noting
//          erf(x) = (2/sqrt(pi))*(x - x^3/3 + x^5/10 - x^7/42 + ....)
//	   and that
//          2/sqrt(pi) = 1.128379167095512573896158903121545171688
//	   is close to one. The interval is chosen because the fix
//	   point of erf(x) is near 0.6174 (i.e., erf(x)=x when x is
//	   near 0.6174), and by some experiment, 0.84375 is chosen to
// 	   guarantee the error is less than one ulp for erf.
//
//      2. For |x| in [0.84375,1.25], let s = |x| - 1, and
//         c = 0.84506291151 rounded to single (24 bits)
//         	erf(x)  = sign(x) * (c  + P1(s)/Q1(s))
//         	erfc(x) = (1-c)  - P1(s)/Q1(s) if x > 0
//			  1+(c+P1(s)/Q1(s))    if x < 0
//         	|P1/Q1 - (erf(|x|)-c)| <= 2**-59.06
//	   Remark: here we use the taylor series expansion at x=1.
//		erf(1+s) = erf(1) + s*Poly(s)
//			 = 0.845.. + P1(s)/Q1(s)
//	   That is, we use rational approximation to approximate
//			erf(1+s) - (c = (single)0.84506291151)
//	   Note that |P1/Q1|< 0.078 for x in [0.84375,1.25]
//	   where 
//		P1(s) = degree 6 poly in s
//		Q1(s) = degree 6 poly in s
//
//      3. For x in [1.25,1/0.35(~2.857143)], 
//         	erfc(x) = (1/x)*exp(-x*x-0.5625+R1/S1)
//         	erf(x)  = 1 - erfc(x)
//	   where 
//		R1(z) = degree 7 poly in z, (z=1/x^2)
//		S1(z) = degree 8 poly in z
//
//      4. For x in [1/0.35,28]
//         	erfc(x) = (1/x)*exp(-x*x-0.5625+R2/S2) if x > 0
//			= 2.0 - (1/x)*exp(-x*x-0.5625+R2/S2) if -6<x<0
//			= 2.0 - tiny		(if x <= -6)
//         	erf(x)  = sign(x)*(1.0 - erfc(x)) if x < 6, else
//         	erf(x)  = sign(x)*(1.0 - tiny)
//	   where
//		R2(z) = degree 6 poly in z, (z=1/x^2)
//		S2(z) = degree 7 poly in z
//
//      Note1:
//	   To compute exp(-x*x-0.5625+R/S), let s be a single
//	   precision number and s := x; then
//		-x*x = -s*s + (s-x)*(s+x)
//	        exp(-x*x-0.5626+R/S) = 
//			exp(-s*s-0.5625)*exp((s-x)*(s+x)+R/S);
//      Note2:
//	   Here 4 and 5 make use of the asymptotic series
//			  exp(-x*x)
//		erfc(x) ~ ---------- * ( 1 + Poly(1/x^2) )
//			  x*sqrt(pi)
//	   We use rational approximation to approximate
//      	g(s)=f(1/x^2) = log(erfc(x)*x) - x*x + 0.5625
//	   Here is the error bound for R1/S1 and R2/S2
//      	|R1/S1 - f(x)|  < 2**(-62.57)
//      	|R2/S2 - f(x)|  < 2**(-61.52)
//
//      5. For inf > x >= 28
//         	erf(x)  = sign(x) *(1 - tiny)  (raise inexact)
//         	erfc(x) = tiny*tiny (raise underflow) if x > 0
//			= 2 - tiny if x<0
//
//      7. Special case:
//         	erf(0)  = 0, erf(inf)  = 1, erf(-inf) = -1,
//         	erfc(0) = 1, erfc(inf) = 0, erfc(-inf) = 2, 
//	   	erfc/erf(NaN) is NaN
//

	/**
        * Error function.
        * Based on C-code for the error function developed at Sun Microsystems.
        * @author Jaco van Kooten
	*/
        public static double error(double x) {
// Coefficients for approximation to  erf on [0,0.84375]
                final double e_efx=1.28379167095512586316e-01;
	  	//double efx8=1.02703333676410069053e00;
	  	final double ePp[]={
                        1.28379167095512558561e-01,
                        -3.25042107247001499370e-01,
                        -2.84817495755985104766e-02,
                        -5.77027029648944159157e-03,
                        -2.37630166566501626084e-05};
                final double eQq[]={
                        3.97917223959155352819e-01,
                        6.50222499887672944485e-02,
                        5.08130628187576562776e-03,
                        1.32494738004321644526e-04,
                        -3.96022827877536812320e-06};
// Coefficients for approximation to  erf  in [0.84375,1.25] 
                final double ePa[]={
                        -2.36211856075265944077e-03,
                        4.14856118683748331666e-01,
                        -3.72207876035701323847e-01,
                        3.18346619901161753674e-01,
                        -1.10894694282396677476e-01,
                        3.54783043256182359371e-02,
                        -2.16637559486879084300e-03};
                final double eQa[]={
                        1.06420880400844228286e-01,
                        5.40397917702171048937e-01,
                        7.18286544141962662868e-02,
                        1.26171219808761642112e-01,
                        1.36370839120290507362e-02,
                        1.19844998467991074170e-02};
 	  	final double e_erx=8.45062911510467529297e-01;

        	double P,Q,s,retval;
        	final double abs_x = (x >= 0.0 ? x : -x);
                if ( abs_x < 0.84375 ) {                               // 0 < |x| < 0.84375
                        if (abs_x < 3.7252902984619141e-9 )     // |x| < 2**-28
                                retval = abs_x + abs_x*e_efx;
                        else {
        			s = x*x;
         			P = ePp[0]+s*(ePp[1]+s*(ePp[2]+s*(ePp[3]+s*ePp[4])));
        			Q = 1.0+s*(eQq[0]+s*(eQq[1]+s*(eQq[2]+s*(eQq[3]+s*eQq[4]))));
        			retval = abs_x + abs_x*(P/Q);
           		}
        	} else if (abs_x < 1.25) {                             // 0.84375 < |x| < 1.25
        		s = abs_x-1.0;
                        P = ePa[0]+s*(ePa[1]+s*(ePa[2]+s*(ePa[3]+s*(ePa[4]+s*(ePa[5]+s*ePa[6])))));
                        Q = 1.0+s*(eQa[0]+s*(eQa[1]+s*(eQa[2]+s*(eQa[3]+s*(eQa[4]+s*eQa[5])))));
                        retval = e_erx + P/Q;
	       } else if (abs_x >= 6.0) 
                        retval = 1.0;
                else                                                    // 1.25 < |x| < 6.0 
                        retval = 1.0-complementaryError(abs_x);
                return (x >= 0.0) ? retval : -retval;
        }
	/**
        * Complementary error function.
        * Based on C-code for the error function developed at Sun Microsystems.
        * @author Jaco van Kooten
	*/
	public static double complementaryError(double x) {
// Coefficients for approximation to  erfc in [1.25,1/.35]
                final double eRa[]={
                        -9.86494403484714822705e-03,
                        -6.93858572707181764372e-01,
                        -1.05586262253232909814e01, 
                        -6.23753324503260060396e01, 
                        -1.62396669462573470355e02, 
                        -1.84605092906711035994e02, 
                        -8.12874355063065934246e01, 
                        -9.81432934416914548592e00};
                final double eSa[]={
                        1.96512716674392571292e01, 
                        1.37657754143519042600e02,
                        4.34565877475229228821e02,
                        6.45387271733267880336e02,
                        4.29008140027567833386e02,
                        1.08635005541779435134e02,
                        6.57024977031928170135e00,
                        -6.04244152148580987438e-02};
// Coefficients for approximation to  erfc in [1/.35,28]
                final double eRb[]={
                        -9.86494292470009928597e-03, 
                        -7.99283237680523006574e-01, 
                        -1.77579549177547519889e01, 
                        -1.60636384855821916062e02, 
                        -6.37566443368389627722e02, 
                        -1.02509513161107724954e03, 
                        -4.83519191608651397019e02};
                final double eSb[]={
                        3.03380607434824582924e01, 
                        3.25792512996573918826e02, 
                        1.53672958608443695994e03,
                        3.19985821950859553908e03,
                        2.55305040643316442583e03,
                        4.74528541206955367215e02,
                        -2.24409524465858183362e01};

          	double s,retval,R,S;
                final double abs_x =(x>=0.0 ? x : -x);
                if (abs_x < 1.25)
        		retval = 1.0-error(abs_x);
                else if (abs_x > 28.0)
                        retval=0.0;
                else {						// 1.25 < |x| < 28 
                        s = 1.0/(abs_x*abs_x);
                        if (abs_x < 2.8571428) {                // ( |x| < 1/0.35 ) 
                                R=eRa[0]+s*(eRa[1]+s*(eRa[2]+s*(eRa[3]+s*(eRa[4]+s*(eRa[5]+s*(eRa[6]+s*eRa[7]))))));
        	    		S=1.0+s*(eSa[0]+s*(eSa[1]+s*(eSa[2]+s*(eSa[3]+s*(eSa[4]+s*(eSa[5]+s*(eSa[6]+s*eSa[7])))))));
        		} else {	  				// ( |x| > 1/0.35 )
                                R=eRb[0]+s*(eRb[1]+s*(eRb[2]+s*(eRb[3]+s*(eRb[4]+s*(eRb[5]+s*eRb[6])))));
        	    		S=1.0+s*(eSb[0]+s*(eSb[1]+s*(eSb[2]+s*(eSb[3]+s*(eSb[4]+s*(eSb[5]+s*eSb[6]))))));
 			}
                        retval =  Math.exp(-x*x - 0.5625 + R/S)/abs_x;
                }
                return (x >= 0.0) ? retval : 2.0-retval;
	}
}

