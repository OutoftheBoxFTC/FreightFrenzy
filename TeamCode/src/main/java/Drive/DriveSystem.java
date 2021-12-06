package Drive;

import Drive.AdvDrive.GVF.GVFDrive;
import Hardware.HardwareSystems.UGSystems.DrivetrainSystem;
import MathSystems.Position;
import State.Action.Action;
import Utils.PathUtils.ContinousPathBuilder;
import Utils.PathUtils.Path;
import Utils.PathUtils.Profiling.LinearProfile;
import Utils.PathUtils.Profiling.LinearProfiler;

public class DriveSystem {
    private final DrivetrainSystem drivetrainSystem;
    private final Position position;

    public DriveSystem(DrivetrainSystem drivetrainSystem, Position position){
        this.drivetrainSystem = drivetrainSystem;
        this.position = position;
    }

    public void followGvf(Path path){
        LinearProfile profile = LinearProfiler.profile(path, 60, 10, 30, 5, 100);
        GVFDrive drive = new GVFDrive(drivetrainSystem, position, path, profile);
        drive.runBlocking();
    }

    public void gotoGvf(Position position){
        Path path = new ContinousPathBuilder(this.position).lineTo(position).build();
        followGvf(path);
    }

    public void gotoGvf(Position... position){
        ContinousPathBuilder builder = new ContinousPathBuilder(this.position);
        for(Position p : position){
            builder.lineTo(p);
        }
        followGvf(builder.build());
    }
}
