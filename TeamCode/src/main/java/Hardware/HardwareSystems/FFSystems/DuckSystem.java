package Hardware.HardwareSystems.FFSystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartServo.SmartServo;

public class DuckSystem implements HardwareSystem {
    private CRServo duckServo1, duckServo2;

    public DuckSystem(SmartLynxModule chub){
        duckServo1 = chub.getCrServo(4);
        duckServo2 = chub.getCrServo(5);

        duckServo2.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    public void update() {

    }

    public void setDuckPower(double power){
        duckServo1.setPower(power);
        duckServo2.setPower(power);
    }
}
