package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.RobotLog;

import Drive.DriveConstants;
import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Vector.Vector3;
import Utils.PID.PIDFSystem;

@Config
public class DrivetrainSystem implements HardwareSystem {
    public static double P = 2, I = 0.5, D = 0, F = 11.1;

    public static double API_POWER = 32767, TICKS_PER_SEC = 2800;

    private final SmartMotor bl, br, tl, tr;
    private final SmartMotor tlE, trE, blE, brE;

    private final PIDFSystem tlPID, trPID, blPID, brPID;

    private final BNO055IMU imu;
    private double angleOffset;
    private double blPower, brPower, tlPower, trPower;
    private double blAccel = 0, brAccel = 0, tlAccel = 0, trAccel = 0;
    private boolean useVelCorrection = false;

    private final LynxModule module;

    public DrivetrainSystem(SmartLynxModule module, HardwareMap map){
        this.tl = module.getMotor(0);//2
        this.tr = module.getMotor(2);//3
        this.bl = module.getMotor(1);//0
        this.br = module.getMotor(3);//1

        this.tlE = module.getMotor(0);
        this.trE = module.getMotor(1);
        this.blE = module.getMotor(2);
        this.brE = module.getMotor(3);

        this.tlPID = new PIDFSystem(P, I, D, F);
        this.trPID = new PIDFSystem(P, I, D, F);
        this.blPID = new PIDFSystem(P, I, D, F);
        this.brPID = new PIDFSystem(P, I, D, F);

        this.module = module.getModule();
        this.imu = map.get(BNO055IMU.class, "imu");
    }

    @Override
    public void initialize() {
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        imu.initialize(new BNO055IMU.Parameters());

        angleOffset = getImuAngle().radians();

        setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        setZeroPowerBehaviour(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void update() {
        if(useVelCorrection){
            double tlCorr = tlPID.getCorrection((tlPower * TICKS_PER_SEC) - tl.getMotor().getVelocity(), tlPower * TICKS_PER_SEC);
            double trCorr = trPID.getCorrection((trPower * TICKS_PER_SEC) - tr.getMotor().getVelocity(), trPower * TICKS_PER_SEC);
            double blCorr = blPID.getCorrection((blPower * TICKS_PER_SEC) - bl.getMotor().getVelocity(), blPower * TICKS_PER_SEC);
            double brCorr = brPID.getCorrection((brPower * TICKS_PER_SEC) - br.getMotor().getVelocity(), brPower * TICKS_PER_SEC);

            tl.setPower(tlCorr / API_POWER);
            tr.setPower(trCorr / API_POWER);
            bl.setPower(blCorr / API_POWER);
            br.setPower(brCorr / API_POWER);
        }else {
            bl.setPower(MathUtils.signedMax((blPower + (FFConstants.DRIVETRAIN_KACCEL * blAccel)), DriveConstants.minVoltage));
            br.setPower(MathUtils.signedMax((brPower + (FFConstants.DRIVETRAIN_KACCEL * brAccel)), DriveConstants.minVoltage));
            tl.setPower(MathUtils.signedMax((tlPower + (FFConstants.DRIVETRAIN_KACCEL * tlAccel)), DriveConstants.minVoltage));
            tr.setPower(MathUtils.signedMax((trPower + (FFConstants.DRIVETRAIN_KACCEL * trAccel)), DriveConstants.minVoltage));

            tlPID.reset();
            trPID.reset();
            blPID.reset();
            brPID.reset();
        }

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

    public void setMode(DcMotor.RunMode mode){
        bl.getMotor().setMode(mode);
        br.getMotor().setMode(mode);
        tl.getMotor().setMode(mode);
        tr.getMotor().setMode(mode);
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

    public void useVelCorrection(){
        useVelCorrection = true;
    }

    public void disableVelCorrection(){
        useVelCorrection = false;
    }

    public Angle getImuAngle(){
        double ang = imu.getAngularOrientation().firstAngle;
        double tau = 2 * Math.PI;
        ang = ((((ang % tau) + tau) % tau));
        return Angle.radians(ang);
    }
}
