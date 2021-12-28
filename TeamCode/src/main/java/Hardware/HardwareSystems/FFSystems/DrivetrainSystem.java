package Hardware.HardwareSystems.FFSystems;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import Drive.DriveConstants;
import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Vector.Vector3;

public class DrivetrainSystem implements HardwareSystem {
    private final SmartMotor bl, br, tl, tr;
    private final BNO055IMU imu;
    private double blPower, brPower, tlPower, trPower;
    private double blAccel = 0, brAccel = 0, tlAccel = 0, trAccel = 0;

    private final LynxModule module;

    public DrivetrainSystem(SmartLynxModule module, HardwareMap map){
        this.bl = module.getMotor(1);//0
        this.br = module.getMotor(3);//1
        this.tl = module.getMotor(0);//2
        this.tr = module.getMotor(2);//3
        this.module = module.getModule();
        this.imu = map.get(BNO055IMU.class, "imu");
    }

    @Override
    public void initialize() {
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(new BNO055IMU.Parameters());
    }

    @Override
    public void update() {
        bl.setPower(MathUtils.signedMax((blPower + (FFConstants.DRIVETRAIN_KACCEL * blAccel)), DriveConstants.minVoltage));
        br.setPower(MathUtils.signedMax((brPower + (FFConstants.DRIVETRAIN_KACCEL * brAccel)), DriveConstants.minVoltage));
        tl.setPower(MathUtils.signedMax((tlPower + (FFConstants.DRIVETRAIN_KACCEL * tlAccel)), DriveConstants.minVoltage));
        tr.setPower(MathUtils.signedMax((trPower + (FFConstants.DRIVETRAIN_KACCEL * trAccel)), DriveConstants.minVoltage));

        blAccel = 0;
        brAccel = 0;
        tlAccel = 0;
        trAccel = 0;
        //We wipe the accel values here in case the user forgot to reset them
        //So we aren't just doing an infinite feedforward
    }

    public void setZeroPowerBehaviour(DcMotor.ZeroPowerBehavior zeroPowerBehaviour){
        bl.getMotor().setZeroPowerBehavior(zeroPowerBehaviour);
        br.getMotor().setZeroPowerBehavior(zeroPowerBehaviour);
        tl.getMotor().setZeroPowerBehavior(zeroPowerBehaviour);
        tr.getMotor().setZeroPowerBehavior(zeroPowerBehaviour);
    }

    public void setPower(Vector3 direction){
        blPower = -direction.getB() + direction.getA() - direction.getC();
        brPower = direction.getB() + direction.getA() - direction.getC();
        tlPower = -direction.getB() - direction.getA() - direction.getC();
        trPower = direction.getB() - direction.getA() - direction.getC();
    }

    public void setPower(double bl, double br, double tl, double tr){
        blPower = bl;
        brPower = br;
        tlPower = tl;
        trPower = tr;
    }

    public void setAccel(Vector3 accel){
        blAccel = -accel.getB() + accel.getA() - accel.getC();
        brAccel = accel.getB() + accel.getA() - accel.getC();
        tlAccel = -accel.getB() - accel.getA() - accel.getC();
        trAccel = accel.getB() - accel.getA() - accel.getC();
    }

    public void enableVoltageCorrection(){
        bl.enableVoltageCorrection(module);
        br.enableVoltageCorrection(module);
        tl.enableVoltageCorrection(module);
        tr.enableVoltageCorrection(module);
    }

    public void disableVoltageCorrection(){
        bl.disableVoltageCorrection();
        br.disableVoltageCorrection();
        tl.disableVoltageCorrection();
        tr.disableVoltageCorrection();
    }

    public Angle getImuAngle(){
        return Angle.radians(imu.getAngularOrientation().firstAngle);
    }
}
