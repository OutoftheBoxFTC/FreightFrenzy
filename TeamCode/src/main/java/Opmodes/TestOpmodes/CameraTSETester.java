package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@Config
public class CameraTSETester extends BasicOpmode {
    public static CAMERA_POSITION camera_position = CAMERA_POSITION.RED;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> {
            switch (camera_position){
                case RED:
                    hardware.getIntakeSystem().moveCameraRedTSE();
                    break;
                case BLUE:
                    hardware.getIntakeSystem().moveCameraBlueTSE();
                    break;
                case INSPECTION:
                    hardware.getIntakeSystem().moveCameraInspection();
                    break;
                case LINE:
                    hardware.getIntakeSystem().moveCameraLine();
                    break;
            }
        });
    }

    public enum CAMERA_POSITION{
        RED,
        BLUE,
        INSPECTION,
        LINE
    }
}
