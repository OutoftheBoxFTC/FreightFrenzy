package Hardware;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import Hardware.HardwareController;
import Hardware.HardwareSystems.FFSystems.DuckSystem;
import Hardware.HardwareSystems.FFSystems.IntakeSystem;
import Hardware.HardwareSystems.FFSystems.OdometrySystem;
import Hardware.HardwareSystems.FFSystems.TurretSystem;
import Hardware.HardwareSystems.FFSystems.DrivetrainSystem;
import Hardware.HardwareSystems.UGSystems.ShooterSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;

public class FFHardwareController extends HardwareController {
    private TurretSystem turretSystem;
    private DrivetrainSystem drivetrainSystem;
    private IntakeSystem intakeSystem;
    private DuckSystem duckSystem;
    private OdometrySystem odometrySystem;

    public FFHardwareController(HardwareMap hardwareMap) {
        super(hardwareMap);
    }

    @Override
    public void setupSystems(HardwareMap hardwareMap, SmartLynxModule controlHub, SmartLynxModule revHub) {
        turretSystem = new TurretSystem(controlHub, revHub, hardwareMap);
        drivetrainSystem = new DrivetrainSystem(controlHub, hardwareMap);
        intakeSystem = new IntakeSystem(controlHub, revHub);
        this.duckSystem = new DuckSystem(controlHub);
        odometrySystem = new OdometrySystem(controlHub, hardwareMap);

        hardwareSystems.add(turretSystem);
        hardwareSystems.add(drivetrainSystem);
        hardwareSystems.add(intakeSystem);
        hardwareSystems.add(duckSystem);
        hardwareSystems.add(odometrySystem);
    }

    public TurretSystem getTurretSystem() {
        return turretSystem;
    }

    public DrivetrainSystem getDrivetrainSystem() {
        return drivetrainSystem;
    }

    public IntakeSystem getIntakeSystem() {
        return intakeSystem;
    }

    public DuckSystem getDuckSystem() {
        return duckSystem;
    }

    public OdometrySystem getOdometrySystem() {
        return odometrySystem;
    }
}
