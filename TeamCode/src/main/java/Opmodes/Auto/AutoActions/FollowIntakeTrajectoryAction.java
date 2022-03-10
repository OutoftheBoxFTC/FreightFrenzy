package Opmodes.Auto.AutoActions;

import Hardware.HardwareSystems.FFSystems.IntakeSystem;
import RoadRunner.drive.SampleMecanumDrive;
import RoadRunner.trajectorysequence.TrajectorySequence;
import State.Action.Action;

public class FollowIntakeTrajectoryAction implements Action {
    private SampleMecanumDrive drive;
    private TrajectorySequence sequence;
    private IntakeSystem intakeSystem;

    public FollowIntakeTrajectoryAction(SampleMecanumDrive drive, TrajectorySequence sequence, IntakeSystem intakeSystem){
        this.drive = drive;
        this.sequence = sequence;
        this.intakeSystem = intakeSystem;
    }

    @Override
    public void initialize() {
        drive.followTrajectorySequenceAsync(sequence);
    }

    @Override
    public void update() {
        drive.update();
    }

    @Override
    public boolean shouldDeactivate() {
        if(intakeSystem.itemInTransfer()){
            drive.breakFollowing();
            return true;
        }
        return !drive.isBusy();
    }
}
