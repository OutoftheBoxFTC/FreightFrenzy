package MathSystems;

import androidx.annotation.NonNull;

import MathSystems.Vector.Vector2;
import MathSystems.Vector.Vector3;

public class Position {
    private double x, y;
    private Angle r;
    public Position(double x, double y, Angle r){
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public Position(Vector2 pos, Angle r){
        this.x = pos.getA();
        this.y = pos.getB();
        this.r = r;
    }

    public void setR(Angle r) {
        this.r = r;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public Angle getR() {
        return r;
    }

    public void set(Position position){
        this.x = position.x;
        this.y = position.y;
        this.r = position.r;
    }

    public Position add(double x, double y, Angle r){
        return new Position(this.x + x, this.y + y, Angle.radians(this.r.radians() + r.radians()));
    }

    public Position add(Vector2 v, Angle r){
        return add(v.getA(), v.getB(), r);
    }

    public Position add(Position p){
        return add(p.x, p.y, p.r);
    }

    public Position invert(){
        return new Position(-x, -y, Angle.radians(-r.radians()));
    }

    public Position scale(double scale){
        return new Position(x * scale, y * scale, Angle.radians(r.radians() * scale));
    }

    public Vector3 toVector3(){
        return new Vector3(x, y, r.radians());
    }

    public Vector2 getPos() {
        return new Vector2(x, y);
    }

    public Position clone(){
        return new Position(x, y, r);
    }

    public static Position ZERO(){
        return new Position(0, 0, Angle.ZERO());
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + r.degrees() + ")";
    }
}
