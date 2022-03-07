package Hardware.SmartDevices.SmartAnalogInput;

import com.qualcomm.robotcore.hardware.AnalogInput;

public class SmartAnalogInput {
    private final AnalogInput analogInput;
    private double maxVoltage = 3.3;

    public SmartAnalogInput(AnalogInput analogInput){
        this.analogInput = analogInput;
    }

    public SmartAnalogInput(AnalogInput analogInput, double maxVoltage){
        this.analogInput = analogInput;
        this.maxVoltage = maxVoltage;
    }

    public double getVoltage(){
        return this.analogInput.getVoltage();
    }

    public double getValue(){
        return this.analogInput.getVoltage() / maxVoltage;
    }

    public AnalogInput getAnalogInput() {
        return analogInput;
    }
}