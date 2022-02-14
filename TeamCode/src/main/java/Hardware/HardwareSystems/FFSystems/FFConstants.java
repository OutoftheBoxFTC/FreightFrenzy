package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.config.Config;

import MathSystems.Angle;

public class FFConstants {
    public static double DRIVETRAIN_KACCEL = 0;

    public static class ExpansionPorts{
        public static int TURRET_POTENTIOMETER_PORT = 3;
        public static int TURRET_MOTOR_PORT = 3;

        public static int PITCH_POTENTIOMETER_PORT = 3;
        public static int PITCH_MOTOR_PORT = 0;

        public static int EXTENSION_MOTOR_PORT = 1;

        public static int INTAKE_MOTOR_PORT = 2;
    }
    @Config
    public static class Turret {
        public static double TURRET_MAX_VEL = 345;
        public static double TURRET_MAX_ACCEL = 10;
        public static double TURRET_KACCEL = 0;
        public static double TURRET_KSTATIC = 0.3;//0.33
        public static double TURRET_CORRECTION_SPEED = 0;
        public static Angle TURRET_MIN_ANGLE = Angle.degrees(0);
        public static Angle TURRET_MAX_ANGLE = Angle.degrees(300);
    }

    @Config
    public static class Pitch{
        public static double PITCH_MAX_VEL = 240;
        public static double PITCH_MAX_ACCEL = 10;
        public static double PITCH_KACCEL = 0.1;
        public static double PITCH_KSTATIC = 0.55;
        public static double PITCH_CORRECTION_SPEED = 0.2;
        public static Angle PITCH_MIN_ANGLE = Angle.degrees(0);
        public static Angle PITCH_MAX_ANGLE = Angle.degrees(300);
    }

    public class Extension {
        public static final double EXTENSION_KSTATIC = 0.65;
    }
}
