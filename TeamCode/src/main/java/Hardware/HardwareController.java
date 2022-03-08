package Hardware;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;

public abstract class HardwareController {
    private static final long HEARTBEAT_TIMEOUT_MS = 2000;

    protected LynxModule controlHub, expansionHub = null;
    protected SmartLynxModule smartChub, smartEhub = null;
    private final HardwareMap hardwareMap;
    public ArrayList<HardwareSystem> hardwareSystems;
    private final AtomicLong heartbeat;
    private final AtomicLong chubLatency, ehubLatency;

    public HardwareController(HardwareMap hardwareMap){
        this.hardwareMap = hardwareMap;
        this.hardwareSystems = new ArrayList<>();
        this.heartbeat = new AtomicLong(-1);
        this.chubLatency = new AtomicLong(1);
        this.ehubLatency = new AtomicLong(1);
        List<LynxModule> modules = hardwareMap.getAll(LynxModule.class);
        for(LynxModule lynx : modules){
            lynx.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
            if(lynx.isParent() && LynxConstants.isEmbeddedSerialNumber(lynx.getSerialNumber())){
                controlHub = lynx;
                smartChub = new SmartLynxModule(lynx, hardwareMap);
            }else{
                expansionHub = lynx;
                smartEhub = new SmartLynxModule(lynx, hardwareMap);
            }
        }
        controlHub.getCurrent(CurrentUnit.AMPS);
    }

    public abstract void setupSystems(HardwareMap hardwareMap, SmartLynxModule controlHub, SmartLynxModule expansionHub);

    public void initialize(){
        setupSystems(hardwareMap, smartChub, smartEhub);

        controlHub.clearBulkCache();
        if(!(expansionHub == null)) {
            expansionHub.clearBulkCache();
        }
        for(HardwareSystem system : hardwareSystems){
            system.initialize();
        }

        heartbeat.set(System.currentTimeMillis());

        new Thread(() -> {
            long lastTime = System.currentTimeMillis();
            while((System.currentTimeMillis() - heartbeat.get()) < HEARTBEAT_TIMEOUT_MS){
                controlHub.clearBulkCache();
                if(!(expansionHub == null)) {
                    expansionHub.clearBulkCache();
                }
                String s = "";
                for(HardwareSystem system : hardwareSystems){
                    long start = System.currentTimeMillis();
                    system.update();
                    long end = System.currentTimeMillis();
                    s += (system.getClass().getSimpleName()) + ": " + (end - start) + " | ";
                }
                //RobotLog.ii("Timings", s);
                long now = System.currentTimeMillis();
                chubLatency.set(now - lastTime);
                lastTime = now;
            }
        }).start();
    }

    public void update(){
        heartbeat.set(System.currentTimeMillis());
    }

    public long getChubLatency(){
        return chubLatency.get();
    }

    public long getEhubLatency(){
        return ehubLatency.get();
    }

    public SmartLynxModule getControlHub() {
        return smartChub;
    }

    public SmartLynxModule getExpansionHub() {
        return smartEhub;
    }
}
