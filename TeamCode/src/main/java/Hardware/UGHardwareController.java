package Hardware;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import Hardware.HardwareSystems.UGSystems.DrivetrainSystem;
import Hardware.HardwareSystems.UGSystems.IntakeSystem;
import Hardware.HardwareSystems.UGSystems.OdometrySystem;
import Hardware.HardwareSystems.UGSystems.ShooterSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;

public class UGHardwareController extends HardwareController {
    private DrivetrainSystem drivetrainSystem;
    private ShooterSystem shooterSystem;
    private IntakeSystem intakeSystem;
    private OdometrySystem odometrySystem;

    public UGHardwareController(HardwareMap hardwareMap) {
        super(hardwareMap);
    }

    @Override
    public void setupSystems(HardwareMap hardwareMap, SmartLynxModule controlHub, SmartLynxModule revHub) {
        drivetrainSystem = new DrivetrainSystem(hardwareMap.get(DcMotorEx.class,"bl"), hardwareMap.get(DcMotorEx.class,"br"),
                hardwareMap.get(DcMotorEx.class,"fl"), hardwareMap.get(DcMotorEx.class,"fr"), hardwareMap.getAll(LynxModule.class).get(0));

        shooterSystem = new ShooterSystem(hardwareMap.get(DcMotorEx.class,"ol"), hardwareMap.get(DcMotorEx.class,"shooter"),
                hardwareMap.servo.get("shooterLoadArm"), hardwareMap.servo.get("turret"), hardwareMap.servo.get("shooterTilt"));

        intakeSystem = new IntakeSystem(hardwareMap.get(DcMotorEx.class,"intake"), hardwareMap.get(DcMotorEx.class,"oa"), hardwareMap.servo.get("intakeShield"));
        odometrySystem = new OdometrySystem(hardwareMap.get(DcMotorEx.class, "intake"), hardwareMap.get(DcMotorEx.class, "oa"), hardwareMap.get(DcMotorEx.class, "br"));

        hardwareSystems.add(drivetrainSystem);
        hardwareSystems.add(shooterSystem);
        hardwareSystems.add(intakeSystem);
        hardwareSystems.add(odometrySystem);
    }

    public DrivetrainSystem getDrivetrainSystem() {
        return drivetrainSystem;
    }

    public ShooterSystem getShooterSystem() {
        return shooterSystem;
    }

    public IntakeSystem getIntakeSystem() {
        return intakeSystem;
    }

    public OdometrySystem getOdometrySystem() {
        return odometrySystem;
    }
}
