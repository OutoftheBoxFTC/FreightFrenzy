package Hardware;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import Hardware.HardwareController;
import Hardware.HardwareSystems.FFSystems.TurretSystem;
import Hardware.HardwareSystems.UGSystems.DrivetrainSystem;
import Hardware.HardwareSystems.UGSystems.IntakeSystem;
import Hardware.HardwareSystems.UGSystems.OdometrySystem;
import Hardware.HardwareSystems.UGSystems.ShooterSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;

public class FFHardwareController extends HardwareController {
    private TurretSystem turretSystem;

    public FFHardwareController(HardwareMap hardwareMap) {
        super(hardwareMap);
    }

    @Override
    public void setupSystems(HardwareMap hardwareMap, SmartLynxModule controlHub, SmartLynxModule revHub) {
        turretSystem = new TurretSystem(controlHub, revHub, hardwareMap);

        hardwareSystems.add(turretSystem);
    }

    public TurretSystem getTurretSystem() {
        return turretSystem;
    }
}
