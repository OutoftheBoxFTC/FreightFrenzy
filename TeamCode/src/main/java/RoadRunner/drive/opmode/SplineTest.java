package RoadRunner.drive.opmode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import MathSystems.Angle;
import Odometry.FFFusionOdometer;
import Opmodes.BasicOpmode;
import RoadRunner.drive.SampleMecanumDrive;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;

/*
 * This is an example of a more complex path to really test the tuning.
 */
@Autonomous(group = "drive")
public class SplineTest extends BasicOpmode {
    @Override
    public void setup() {
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        //drive.setLocalizer(new FFFusionOdometer(hardware.getOdometrySystem(), hardware.getDrivetrainSystem()));

        hardware.getDrivetrainSystem().disable();

        Trajectory traj = drive.trajectoryBuilder(new Pose2d())
                .splineTo(new Vector2d(40, 0), Math.toRadians(0))
                .build();

        ActionController.addAction(() -> telemetry.addData("Pose", drive.getPoseEstimate()));

        ActionQueue queue = new ActionQueue();
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getDrivetrainSystem().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
        });
        Action action1 = new Action() {
            @Override
            public void initialize() {
                drive.followTrajectoryAsync(traj);
            }

            @Override
            public void update() {
                drive.update();
            }

            @Override
            public boolean shouldDeactivate() {
                return !drive.isBusy();
            }
        };
        Action action2 = new Action() {
            @Override
            public void initialize() {
                drive.followTrajectoryAsync((
                        drive.trajectoryBuilder(traj.end(), true)
                                .splineTo(new Vector2d(0, 0), Math.toRadians(180))
                                .build()
                ));
            }

            @Override
            public void update() {
                drive.update();
            }

            @Override
            public boolean shouldDeactivate() {
                return !drive.isBusy();
            }
        };
        queue.submitAction(action1);
        queue.submitAction(new DelayAction(500));
        queue.submitAction(action2);
        queue.submitAction(new DelayAction(500));
        queue.submitAction(action1);
        queue.submitAction(new DelayAction(500));
        queue.submitAction(action2);
        queue.submitAction(new DelayAction(500));
        queue.submitAction(action1);
        queue.submitAction(new DelayAction(500));
        queue.submitAction(action2);
        OpmodeStatus.bindOnStart(queue);

    }
}
