package Hardware.HardwareSystems.UGSystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.RobotLog;

import Hardware.HardwareSystems.HardwareSystem;
import MathSystems.Vector.Vector3;
import Odometry.Odometer;

public class OdometrySystem implements HardwareSystem {
    private final DcMotorEx ol;
    private final DcMotorEx oa;
    private final DcMotorEx or;
    private double olOffset, oaOffset, orOffset;
    private double olPos, oaPos, orPos;
    private double lastOl, lastOa, lastOr;
    private volatile Odometer attachedOdometer = null;
    private final Object odometerLock = new Object();

    public OdometrySystem(DcMotorEx ol, DcMotorEx oa, DcMotorEx or){
        this.ol = ol;
        this.oa = oa;
        this.or = or;
    }

    @Override
    public void initialize() {
        olOffset = ol.getCurrentPosition();
        oaOffset = oa.getCurrentPosition();
        orOffset = or.getCurrentPosition();
    }

    @Override
    public void update() {
        lastOl = olPos;
        lastOa = oaPos;
        lastOr = orPos;
        olPos = ol.getCurrentPosition() - olOffset;
        oaPos = oa.getCurrentPosition() - oaOffset;
        orPos = or.getCurrentPosition() - orOffset;
        synchronized (odometerLock){
            if(attachedOdometer != null){
                attachedOdometer.update(this);
            }
        }
    }

    public double getOdometryLeft(){
        return olPos * -1;
    }

    public double getOdometryRight(){
        return orPos;
    }

    public double getOdometryAux(){
        return oaPos;
    }

    public double getOdometryLeftInc(){
        return (olPos - lastOl) * -1;
    }

    public double getOdometryRightInc(){
        return orPos - lastOr;
    }

    public double getOdometryAuxInc(){
        return oaPos - lastOa;
    }

    public void attachOdometer(Odometer odometer){
        synchronized (odometerLock) {
            this.attachedOdometer = odometer;
        }
    }
}
