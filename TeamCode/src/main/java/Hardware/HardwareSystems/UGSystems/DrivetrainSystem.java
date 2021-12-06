package Hardware.HardwareSystems.UGSystems;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;

import Drive.DriveConstants;
import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import MathSystems.MathUtils;
import MathSystems.Vector.Vector3;

public class DrivetrainSystem implements HardwareSystem {
    private final SmartMotor bl, br, tl, tr;
    private double blPower, brPower, tlPower, trPower;

    private final LynxModule module;

    public DrivetrainSystem(DcMotor bl, DcMotor br, DcMotor tl, DcMotor tr, LynxModule module){
        this.bl = new SmartMotor(bl);
        this.br = new SmartMotor(br);
        this.tl = new SmartMotor(tl);
        this.tr = new SmartMotor(tr);
        this.module = module;
    }

    @Override
    public void update() {
        bl.setPower(MathUtils.signedMax(blPower, DriveConstants.minVoltage));
        br.setPower(MathUtils.signedMax(brPower, DriveConstants.minVoltage));
        tl.setPower(MathUtils.signedMax(tlPower, DriveConstants.minVoltage));
        tr.setPower(MathUtils.signedMax(trPower, DriveConstants.minVoltage));
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
}
