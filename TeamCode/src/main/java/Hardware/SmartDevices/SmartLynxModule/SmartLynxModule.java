package Hardware.SmartDevices.SmartLynxModule;

import com.qualcomm.hardware.lynx.LynxAnalogInputController;
import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.hardware.lynx.LynxDigitalChannelController;
import com.qualcomm.hardware.lynx.LynxI2cColorRangeSensor;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorImplEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelImpl;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.util.HashMap;

import Hardware.SmartDevices.SmartAnalogInput.SmartAnalogInput;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import Hardware.SmartDevices.SmartServo.SmartServo;

/**
 * This class exists so that we can get devices by port instead of name
 * Because mechanical keeps messing around with where things are
 * and i hate using phone keyboards
 * so basically this exists because of pure spite
 */
public class SmartLynxModule {
    private LynxModule module;

    private LynxDcMotorController motorController;
    private LynxServoController servoController;
    private LynxAnalogInputController lynxAnalogInputController;
    private LynxDigitalChannelController lynxDigitalChannelController;

    private HashMap<Integer, SmartMotor> cachedMotors;
    private HashMap<Integer, SmartServo> cachedServos;
    private HashMap<Integer, SmartAnalogInput> cachedAI;
    private HashMap<Integer, DigitalChannel> cachedDC;

    public SmartLynxModule(LynxModule module, HardwareMap map) {
        this.module = module;
        try {
            motorController = new LynxDcMotorController(AppUtil.getDefContext(), module);
            servoController = new LynxServoController(AppUtil.getDefContext(), module);
            lynxAnalogInputController = new LynxAnalogInputController(AppUtil.getDefContext(), module);
            lynxDigitalChannelController = new LynxDigitalChannelController(AppUtil.getDefContext(), module);
        } catch (RobotCoreException | InterruptedException e) {
            e.printStackTrace();
        }

        cachedMotors = new HashMap<>();
        cachedServos = new HashMap<>();
        cachedAI = new HashMap<>();
        cachedDC = new HashMap<>();

        for(DcMotor motor : map.getAll(DcMotor.class)){
            if(motor.getController().getConnectionInfo().equals(module.getConnectionInfo())){
                cachedMotors.put(motor.getPortNumber(), new SmartMotor(motor));
            }
        }

        for(Servo servo : map.getAll(Servo.class)){
            if(servo.getController().getConnectionInfo().equals(module.getConnectionInfo())){
                cachedServos.put(servo.getPortNumber(), new SmartServo(servo));
            }
        }

        for(AnalogInput ai : map.getAll(AnalogInput.class)){
            if(ai.getConnectionInfo().split(";")[0].equals(module.getConnectionInfo())){
                cachedAI.put(Integer.valueOf(ai.getConnectionInfo().split(";")[1].replace(" analog port ", "")), new SmartAnalogInput(ai));
            }
        }

        for(DigitalChannel dc : map.getAll(DigitalChannel.class)){
            if(dc.getConnectionInfo().split(";")[0].equals(module.getConnectionInfo())){
                cachedDC.put(Integer.valueOf(dc.getConnectionInfo().split(";")[1].replace(" digital port ", "")), dc);
            }
        }
    }

    public SmartMotor getMotor(int port){
        if(port > 4 || port < 0){
            throw new RuntimeException("You fool. You absolute buffoon. HOW MANY MOTOR PORTS DO YOU THINK THERE ARE ON AN EHUB?????? " + port + " ISNT VALID 5HEAD");
        }
        if(!cachedMotors.containsKey(port)){
            cachedMotors.put(port, new SmartMotor(new DcMotorImplEx(motorController, port)));
        }
        return cachedMotors.get(port);
    }

    public SmartServo getServo(int port){
        if(!cachedServos.containsKey(port)){
            cachedServos.put(port, new SmartServo(new ServoImplEx(servoController, port, ServoConfigurationType.getStandardServoType())));
        }
        return cachedServos.get(port);
    }

    public SmartAnalogInput getAnalogInput(int port){
        if(!cachedAI.containsKey(port)){
            cachedAI.put(port, new SmartAnalogInput(new AnalogInput(lynxAnalogInputController, port)));
        }
        return cachedAI.get(port);
    }

    public DigitalChannel getDigitalController(int port){
        if(!cachedDC.containsKey(port)){
            cachedDC.put(port, new DigitalChannelImpl(lynxDigitalChannelController, port));
        }
        return cachedDC.get(port);
    }

    public LynxModule getModule() {
        return module;
    }
}
