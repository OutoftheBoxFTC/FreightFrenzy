package Hardware.HardwareSystems.FFSystems.Actions;

import RoadRunner.drive.SampleMecanumDrive;
import RoadRunner.trajectorysequence.TrajectorySequence;
import State.Action.Action;

public class FollowTrajectoryAction implements Action {
    private SampleMecanumDrive drive;
    private TrajectorySequence sequence;

    public FollowTrajectoryAction(SampleMecanumDrive drive, TrajectorySequence sequence){
        this.drive = drive;
        this.sequence = sequence;
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
        return !drive.isBusy();
    }
}
