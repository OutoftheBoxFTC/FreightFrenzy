package MathSystems;

import androidx.annotation.Nullable;

public class Angle {
    private final double angle;
    private Angle(double angle){
        this.angle = angle;
    }

    public double degrees(){
        return Math.toDegrees(angle);
    }

    public double radians(){
        return angle;
    }

    public static Angle degrees(double angle){
        return new Angle(Math.toRadians(angle));
    }

    public static Angle radians(double angle){
        return new Angle(angle);
    }

    public static Angle ZERO(){
        return Angle.radians(0);
    }

    public boolean equals(Angle angle){
        return MathUtils.epsilonEquals(MathUtils.getRotDist(this, angle).degrees(), 0);
    }

    @Override
    public String toString() {
        return angle + " Randians | " + Math.toDegrees(angle) + " Degrees";
    }
}