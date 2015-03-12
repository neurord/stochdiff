package org.textensor.stochdiff.numeric.grid;

import org.textensor.util.ArrayUtil;

public abstract class SecondOrderSolver {
    /* Return solution for equation
       dA/dt = aA^2 + bA + c
     */

    int[] stoichiometry;
    int[] X0;
    String description;
    double a, b, c;

    public SecondOrderSolver(int[] stoichiometry, int[] X0,
                             String description, double a, double b, double c) {
        this.stoichiometry = stoichiometry;
        this.X0 = X0;
        this.description = description;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Calculate the copy count of @specie, given @A of first specie.
     *
     *  The relationship between specie numbers is detereministic — we
     *  must be able to calculate copy count of any specie from any
     *  other specie, given the initial numbers (X0_left, X0_right).
     */
    double extent_to_count(double extent, int specie) {
        assert specie < this.stoichiometry.length;

        double coeff = this.stoichiometry[specie] / this.stoichiometry[0];
        return coeff * (extent - this.X0[0]) + this.X0[specie];
    }

    abstract double extent(double t);

    double mean(double t, int specie) {
        double a = this.extent(t);
        return this.extent_to_count(a, specie);
    }

    abstract double variance(double t, int specie);

    /**
     * First order equation.
     */
    static class Equation1 extends SecondOrderSolver {
        Equation1(int[] stoichiometry, int[] X0,
                  String description, double a, double b, double c) {
            super(stoichiometry, X0, description, a, b, c);
            assert this.a == 0;
            assert this.b != 0;
        }

        double extent(double t) {
            return (this.X0[0] + this.c / this.b) * Math.exp(this.b * t) - this.c / this.b;
        }

        double variance(double t, int specie) {
            int X0 = this.X0[specie];
            double X = this.mean(t, specie);
            double p = Math.abs(X - X0) / X0;
            return X0 * p * (1-p);
        }
    }

    /**
     * Second order equation.
     */
    static class Equation2 extends SecondOrderSolver {
        final double _I0;

        Equation2(int[] stoichiometry, int[] X0,
                  String description, double a, double b, double c) {
            super(stoichiometry, X0, description, a, b, c);
            assert this.a != 0;
            this._I0 = I(new double[]{X0[0]}, a, b, c)[0];
        }

        double extent(double t) {
            return I_reverse(new double[]{t + this._I0}, this.a, this.b, this.c, this.X0[0])[0];
        }

        double variance(double t, int specie) {
            int X0 = this.X0[specie];
            double X = this.mean(t, specie);
            return (X - X0) * this.stoichiometry[specie] / this.stoichiometry[0];
        }
    }

    static double[] calc_abc(double r_left, int[] rp, int[] rs, int[] X0_left,
                             double r_right, int[] pp, int[] ps, int[] X0_right) {
        assert ArrayUtil.sum(rp) <= 2;
        assert ArrayUtil.sum(pp) <= 2;
        assert rp.length == rs.length;
        assert rp.length == X0_left.length;
        assert pp.length == ps.length;
        assert pp.length == X0_right.length;
        for (int s: rs)
            assert s > 0: rs;
        for (int s: ps)
            assert s > 0: ps;

        double a, b, c;
        double da, db, dc;

        int sA = rs[0];
        int A0 = X0_left[0]; // ???

        if (rp.length == 1 && rp[0] == 2) {
            da = 1;
            db = -1;
            dc = 0;
        } else if (rp.length == 2) {
            assert rp[0] == 1;
            assert rp[1] == 1;
            int sY = rp[1];
            int Y0 = X0_left[1];
            da = sY / sA;
            db = Y0 - A0*sY/sA;
            dc = 0;
        } else if (rp.length == 1) {
            assert rp[0] == 1;
            da = 0;
            db = 1;
            dc = 0;
        } else {
            assert rp.length == 0;
            da = db = dc = 0;
        }

        a = r_left * sA * da;
        b = r_left * sA * db;
        c = r_left * sA * dc;

        if (rp.length == 1 && rp[0] == 2) {
            int sX = ps[0];
            int X0 = X0_right[0];
            da = sX*sX /sA/sA;
            db = sX/sA * (2*(X0 - A0*sX/sA)-1);
            dc = (X0 - A0*sX/sA) * (X0 - A0*sX/sA - 1);
        } else if (rp.length == 2) {
            assert rp[0] == 1;
            assert rp[1] == 1;
            int sX = ps[0];
            int X0 = X0_right[0];
            int sY = ps[1];
            int Y0 = X0_right[1];
            da = sX * sY /sA/sA;
            db = sY/sA * (X0-A0*sX/sA) + sX/sA * (Y0 - A0*sY/sA);
            dc = (X0 - A0*sX/sA) * (Y0 - A0*sY / sA);
        } else if (rp.length == 1) {
            assert rp[0] == 1;
            int sX = ps[0];
            int X0 = X0_right[0];
            da = 0;
            db = sX / sA;
            dc = X0 - A0*sX/sA;
        } else {
            assert rp.length == 0;
            da = db = dc = 0;
        }

        a += r_right * sA * da;
        b += r_right * sA * db;
        c += r_right * sA * dc;

        return new double[] {a, b, c};
    }

    static double[] I(double[] x, double a, double b, double c) {
        double Delta = b*b - 4*a*c;
        double[] ans = new double[x.length];
        if (Delta > 0) {
            double delta = Math.sqrt(Delta);

            double p1, p2;
            if (a > 0) {
                p1 = (-b-delta)/2/a;
                p2 = (-b+delta)/2/a;
            } else {
                p1 = (-b+delta)/2/a;
                p2 = (-b-delta)/2/a;
            }
            assert p1 < p2;

            for (int i = 0; i < ans.length; i++)
                ans[i] = -Math.log(Math.abs((2*a*x[i]+b+delta) / (2*a*x[i]+b-delta))) / delta;
        } else if (Delta == 0) {
            for (int i = 0; i < ans.length; i++)
                ans[i] = -2 / (2*a*x[i] + b);
        } else {
            double delta = Math.sqrt(-Delta);

            for (int i = 0; i < ans.length; i++)
                ans[i] = 2 * Math.atan((2*a*x[i] + b) / delta) / delta;
        }

        return ans;
    }

    static double[] I_reverse(double[] I, double a, double b, double c, double x0) {
        double Delta = b*b - 4*a*c;
        double[] ans = new double[I.length];
        if (Delta > 0) {
            double delta = Math.sqrt(Delta);
            double p1, p2;
            if (a > 0) {
                p1 = (-b-delta)/2/a;
                p2 = (-b+delta)/2/a;
            } else {
                p1 = (-b+delta)/2/a;
                p2 = (-b-delta)/2/a;
            }
            assert p1 < p2;

            for (int i = 0; i < ans.length; i++) {
                double frac = (1 + Math.exp(delta * I[i])) / (1 - Math.exp(delta * I[i]));
                double y = (-b + delta * frac) / 2 / a;
                if ((x0 <= p1 && y <= p1) ||
                    (p1 <= x0 && x0 <= p2 && p1 <= y && y <= p2) ||
                    (p2 <= x0 && p2 <= y)) {
                    ans[i] = y;
                    continue;
                }
                
                y = (-b + delta / frac) / 2 / a;
                if ((x0 <= p1 && y <= p1) ||
                    (p1 <= x0 && x0 <= p2 && p1 <= y && y <= p2) ||
                    (p2 <= x0 && p2 <= y)) {
                    ans[i] = y;
                    continue;
                }
                
                assert false;
            }
        } else if (Delta == 0) {
            for (int i = 0; i < ans.length; i++)
                ans[i] = -(b + 2/I[i]) /2/a;
        } else {
            double delta = Math.sqrt(-Delta);
            for (int i = 0; i < ans.length; i++)
                ans[i] = (delta * Math.tan(delta/2*I[i]) - b) /2/a;
        }
        return ans;
    }

    public static SecondOrderSolver make_equation(double r_left, double r_right,
                                                  int[] rp, int[] rs, int[] X0_left,
                                                  int[] pp, int[] ps, int[] X0_right,
                                                  int[] ss, int[] X0,
                                                  String description) {
        assert ss[0] != 0;

        double[] abc = calc_abc(r_left, rp, rs, X0_left,
                                r_right, pp, ps, X0_right);
        if (abc[0] != 0)
            return new Equation2(ss, X0, description, abc[0], abc[1], abc[2]);
        else if (abc[1] != 0)
            return new Equation1(ss, X0, description, abc[0], abc[1], abc[2]);

        throw new RuntimeException("not implemented");
    }
}
