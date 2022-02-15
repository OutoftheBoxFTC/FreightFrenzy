package Hardware;

import com.qualcomm.robotcore.hardware.HardwareMap;

import Hardware.HardwareSystems.FFSystems.DuckSystem;
import Hardware.HardwareSystems.FFSystems.IntakeSystem;
import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import Hardware.HardwareSystems.FFSystems.DrivetrainSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;

public class FFHardwareController extends HardwareController {
    private ScoutSystem turretSystem;
    private DrivetrainSystem drivetrainSystem;
    private IntakeSystem intakeSystem;
    private DuckSystem duckSystem;

    public FFHardwareController(HardwareMap hardwareMap) {
        super(hardwareMap);
    }

    @Override
    public void setupSystems(HardwareMap hardwareMap, SmartLynxModule controlHub, SmartLynxModule revHub) {
        drivetrainSystem = new DrivetrainSystem(revHub, hardwareMap);
        intakeSystem = new IntakeSystem(controlHub, revHub, hardwareMap);
        //turretSystem = new ScoutSystem(controlHub, revHub, intakeSystem);
        //this.duckSystem = new DuckSystem(controlHub);

        //hardwareSystems.add(turretSystem);
        hardwareSystems.add(drivetrainSystem);
        hardwareSystems.add(intakeSystem);
       // hardwareSystems.add(duckSystem);
    }

    public ScoutSystem getTurretSystem() {
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
}
