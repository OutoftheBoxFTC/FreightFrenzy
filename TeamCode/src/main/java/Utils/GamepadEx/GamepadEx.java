package Utils.GamepadEx;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.function.BooleanSupplier;

import State.Action.Action;

public class GamepadEx implements Action {
    private final Gamepad gamepad;

    public Joystick right_joystick, left_joystick;
    public Trigger left_trigger, right_trigger;
    public Button dpad_up, dpad_down, dpad_left, dpad_right,
            a, b, x, y,
            guide, start, back,
            left_bumper, right_bumper,
            left_stick_button, right_stick_button;

    public GamepadEx(Gamepad gamepad){
        this.gamepad = gamepad;
        right_joystick = new Joystick();
        left_joystick = new Joystick();
        left_trigger = new Trigger();
        right_trigger = new Trigger();
        dpad_up = new Button();
        dpad_down = new Button();
        dpad_left = new Button();
        dpad_right = new Button();
        a = new Button();
        b = new Button();
        x = new Button();
        y = new Button();
        guide = new Button();
        start = new Button();
        back = new Button();
        left_bumper = new Button();
        right_bumper = new Button();
        left_stick_button = new Button();
        right_stick_button = new Button();
    }

    @Override
    public void update() {
        right_joystick.update(gamepad.right_stick_x, gamepad.right_stick_y);
        left_joystick.update(gamepad.left_stick_x, gamepad.left_stick_y);

        left_trigger.update(gamepad.left_trigger);
        right_trigger.update(gamepad.right_trigger);

        dpad_up.update(gamepad.dpad_up);
        dpad_down.update(gamepad.dpad_down);
        dpad_left.update(gamepad.dpad_left);
        dpad_right.update(gamepad.dpad_right);

        a.update(gamepad.a);
        b.update(gamepad.b);
        x.update(gamepad.x);
        y.update(gamepad.y);

        guide.update(gamepad.guide);
        start.update(gamepad.start);
        back.update(gamepad.back);

        left_bumper.update(gamepad.left_bumper);
        right_bumper.update(gamepad.right_bumper);

        left_stick_button.update(gamepad.left_stick_button);
        right_stick_button.update(gamepad.right_stick_button);
    }

    @Override
    public boolean shouldDeactivate() {
        return false;
    }
}
