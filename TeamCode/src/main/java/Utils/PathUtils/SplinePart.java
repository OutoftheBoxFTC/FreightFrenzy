package Utils.PathUtils;

import MathSystems.Position;
import MathSystems.Vector.Vector3;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class SplinePart {
    RealMatrix COEF_MATRIX = MatrixUtils.createRealMatrix(new double[][]{
                    {0.0, 0.0, 0.0, 0.0, 0.0, 1.0},
                    {0.0, 0.0, 0.0, 0.0, 1.0, 0.0},
                    {0.0, 0.0, 0.0, 2.0, 0.0, 0.0},
                    {1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                    {5.0, 4.0, 3.0, 2.0, 1.0, 0.0},
                    {20.0, 12.0, 6.0, 2.0, 0.0, 0.0}
            }
    );

    double[] coefs;

    public SplinePart(double start, double end, double startDeriv, double endDeriv, double startAccel, double endAccel){
        RealMatrix target = MatrixUtils.createRealMatrix(new double[][]{{start}, {startDeriv}, {startAccel}, {end}, {endDeriv}, {endAccel}});

        DecompositionSolver solver = new LUDecomposition(COEF_MATRIX).getSolver();
        RealMatrix m = solver.solve(target);

        coefs = m.getColumn(0);
    }

    public double get(double t){
        return (coefs[0] * t + coefs[1]) * (t*t*t*t) + coefs[2] * (t*t*t) + coefs[3] * (t * t) + coefs[4] * t + coefs[5];
    }

    public double getDeriv(double t){
        return (5 * coefs[0] * t + 4 * coefs[1]) * (t * t * t) + (3 * coefs[2] * t + 2 * coefs[3]) * t + coefs[4];
    }

    public double getSecondDeriv(double t){
        return (60 * coefs[0] * t + 24 * coefs[1]) * t + 6 * coefs[2];
    }
}
