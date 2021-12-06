package Hardware.HardwareSystems.UGSystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import Hardware.SmartDevices.SmartServo.SmartServo;
import MathSystems.MathUtils;
import Utils.PID.PIDSystem;

public class ShooterSystem implements HardwareSystem {
    private final SmartMotor shooterLeft, shooterRight;
    private final SmartServo indexer, turret, tilt;

    private final PIDSystem shooterPID = new PIDSystem(1, 0, 0);

    private double shooterSpeed = 0, indexerPosition = 0, turretPosition = 0, tiltPosition = 0;

    public ShooterSystem(DcMotor shooterLeft, DcMotor shooterRight, Servo indexer, Servo turret, Servo tilt){
        this.shooterLeft = new SmartMotor(shooterLeft);
        this.shooterRight = new SmartMotor(shooterRight);

        this.indexer = new SmartServo(indexer);
        this.turret = new SmartServo(turret);
        this.tilt = new SmartServo(tilt);
    }


    @Override
    public void update() {
        double shooterPower = (shooterSpeed/290.0) + shooterPID.getCorrection(shooterSpeed - shooterLeft.getMotor().getVelocity(AngleUnit.DEGREES));
        shooterLeft.setPower(shooterPower);
        shooterRight.setPower(shooterPower);

        indexer.setPosition(indexerPosition);
        turret.setPosition(turretPosition);
        tilt.setPosition(tiltPosition);
    }

    public void setShooterSpeed(double shooterSpeed){
        this.shooterSpeed = shooterSpeed;
    }

    public void setTiltPosition(double tiltPosition) {
        this.tiltPosition = tiltPosition;
    }

    public void setIndexIn(){
        indexerPosition = 0.7;
    }

    public void setIndexOut(){
        indexerPosition = 0.925;
    }

    public void setTurretAngle(double angle){
        double minAngle = -22;
        double maxAngle = 27;
        double corrAngle = MathUtils.clamp(angle, minAngle, maxAngle);
        turretPosition = (((corrAngle + Math.abs(minAngle)) / (maxAngle + Math.abs(minAngle))) * 0.9) + 0.05;
    }

    public double clampTurretAngle(double angle){
        return MathUtils.clamp(angle, -22, 27);
    }

    public void setShooterPowershot() {
        setShooterSpeed(4.5);
        setTiltPosition(0.321);
    }

    public void setShooterHighgoal() {
        setShooterSpeed(4.2);
        setTiltPosition(0.335);
    }
}