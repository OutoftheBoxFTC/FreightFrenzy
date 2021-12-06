package Opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import Hardware.FFHardwareController;
import State.Action.ActionController;
import Utils.OpmodeStatus;
import Utils.ProgramClock;

public abstract class BasicOpmode extends LinearOpMode {
    public FFHardwareController hardware;

    private Thread initThread, startThread;

    public abstract void setup();

    public void init_async(){ }

    public void start_async(){ }


    @Override
    public void runOpMode() throws InterruptedException {
        initThread = new Thread(this::init_async);
        startThread = new Thread(this::start_async);

        ActionController.initialize();

        OpmodeStatus.attach(this);

        hardware = new FFHardwareController(hardwareMap);
        hardware.initialize();

        setup();

        boolean started = false;

        initThread.start();
        while(!isStopRequested()){
            if(isStarted() && !started){
                started = true;
                OpmodeStatus.triggerStartActions();
                startThread.start();
            }
            hardware.update();
            ActionController.update();
            telemetry.update();
            ProgramClock.update();
        }
        initThread.interrupt();
        startThread.interrupt();
    }
}
