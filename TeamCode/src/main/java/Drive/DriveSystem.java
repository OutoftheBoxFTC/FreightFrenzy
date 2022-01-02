package Drive;

import Drive.AdvDrive.GVF.GVFDrive;
import Hardware.HardwareSystems.FFSystems.DrivetrainSystem;
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

    public GVFDrive followGvf(Path path){
        return new GVFDrive(drivetrainSystem, position, path);
    }

    public GVFDrive gotoGvf(Position position){
        Path path = new ContinousPathBuilder(this.position).lineTo(position).build();
        return followGvf(path);
    }

    public GVFDrive gotoGvf(Position... position){
        ContinousPathBuilder builder = new ContinousPathBuilder(this.position);
        for(Position p : position){
            builder.lineTo(p);
        }
        return followGvf(builder.build());
    }
}
