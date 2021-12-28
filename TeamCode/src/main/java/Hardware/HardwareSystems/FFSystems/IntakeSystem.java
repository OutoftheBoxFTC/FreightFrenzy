package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;

public class IntakeSystem implements HardwareSystem {
    private SmartMotor intakeMotor;
    private DigitalChannel intakeStop;
    private double power;

    public IntakeSystem(SmartLynxModule chub, SmartLynxModule revHub){
        intakeMotor = revHub.getMotor(FFConstants.ExpansionPorts.INTAKE_MOTOR_PORT);
        intakeStop = chub.getDigitalController(1);
    }

    @Override
    public void initialize() {
        intakeStop.setMode(DigitalChannel.Mode.INPUT);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void update() {
        intakeMotor.setPower(power);
    }

    public void setPower(double power){
        this.power = power;
    }

    public boolean getIntakeStop(){
        return !intakeStop.getState();
    }
}
