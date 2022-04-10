package RoadRunner.drive.opmode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import Drive.BasicDrive.PurePursuit;
import MathSystems.Angle;
import MathSystems.Position;
import RoadRunner.drive.SampleMecanumDrive;
import RoadRunner.drive.SampleTankDrive;
import Utils.PathUtils.Path;
import Utils.PathUtils.PathBuilder;

/**
 * This is a simple teleop routine for testing localization. Drive the robot around like a normal
 * teleop routine and make sure the robot's estimated pose matches the robot's actual pose (slight
 * errors are not out of the ordinary, especially with sudden drive motions). The goal of this
 * exercise is to ascertain whether the localizer has been configured properly (note: the pure
 * encoder localizer heading may be significantly off if the track width has not been tuned).
 */
@TeleOp(group = "drive")
public class LocalizationTest extends LinearOpMode {
    Position position = Position.ZERO();
    @Override
    public void runOpMode() throws InterruptedException {
        SampleTankDrive drive = new SampleTankDrive(hardwareMap);

        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        Path path = new PathBuilder(Position.ZERO())
                .lineTo(new Position(0, 30, Angle.ZERO()))
                .lineTo(new Position(10, 40, Angle.ZERO())).build();

        PurePursuit pp = new PurePursuit(drive, position, path);

        waitForStart();

        while (!isStopRequested()) {
            drive.setDrivePower(
                    new Pose2d(
                            -gamepad1.left_stick_y,
                            0,
                            -gamepad1.right_stick_x
                    )
            );

            position.set(new Position(drive.getPoseEstimate().getX(), drive.getPoseEstimate().getY(), Angle.radians(drive.getPoseEstimate().getHeading())));

            pp.update();

            drive.update();

            Pose2d poseEstimate = drive.getPoseEstimate();
            telemetry.addData("x", poseEstimate.getX());
            telemetry.addData("y", poseEstimate.getY());
            telemetry.addData("heading", poseEstimate.getHeading());
            telemetry.update();
        }
    }
}
