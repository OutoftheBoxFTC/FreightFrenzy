package Hardware.SmartDevices.SmartAnalogInput;

import com.qualcomm.robotcore.hardware.AnalogInput;

public class SmartAnalogInput {
    private final AnalogInput analogInput;

    public SmartAnalogInput(AnalogInput analogInput){
        this.analogInput = analogInput;
    }

    public double getVoltage(){
        return this.analogInput.getVoltage();
    }

    public double getValue(){
        return this.analogInput.getVoltage() / this.analogInput.getMaxVoltage();
    }

    public AnalogInput getAnalogInput() {
        return analogInput;
    }
}