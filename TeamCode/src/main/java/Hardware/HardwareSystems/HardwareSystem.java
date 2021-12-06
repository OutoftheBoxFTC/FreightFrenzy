package Hardware.HardwareSystems;

public interface HardwareSystem {
    default void initialize(){}
    void update();
}
