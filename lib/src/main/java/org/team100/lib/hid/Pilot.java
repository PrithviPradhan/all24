package org.team100.lib.hid;

import static org.team100.lib.hid.ControlUtil.clamp;
import static org.team100.lib.hid.ControlUtil.deadband;
import static org.team100.lib.hid.ControlUtil.expo;

import org.team100.lib.geometry.GeometryUtil;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.wpilibj2.command.button.CommandGenericHID;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/**
 * The RC joystick thing joel made.
 * X, Y, and twist should work.
 * POV rotation should work.
 * Only one joystick is required.
 * Operator features are not implemented.
 * Command buttons are not implemented.
 */
public class Pilot implements DriverControl {
    public static class Config {
        public double kDeadband = 0.02;
        public double kExpo = 0.5;
    }

    private final Config m_config = new Config();
    private final CommandGenericHID m_controller;
    private Rotation2d previousRotation = GeometryUtil.kRotationZero;

    public Pilot() {
        m_controller = new CommandGenericHID(0);
    }

    @Override
    public String getHIDName() {
        return m_controller.getHID().getName();
    }

    @Override
    public Trigger resetRotation0() {
        return button(2);
    }

    @Override
    public Trigger resetRotation180() {
        return button(3);
    }

    @Override
    public Twist2d twist() {
        double dx = expo(deadband(-1.0 * clamp(axis(1), 1), m_config.kDeadband, 1), m_config.kExpo);
        double dy = expo(deadband(-1.0 * clamp(axis(0), 1), m_config.kDeadband, 1), m_config.kExpo);
        double dtheta = 0; // there is no rotational velocity control.
        return new Twist2d(dx, dy, dtheta);
    }

    @Override
    public Rotation2d desiredRotation() {
        // the control goes from -1 to 1 in one turn
        double rotControl = m_controller.getHID().getRawAxis(5);
        previousRotation = Rotation2d.fromRotations(rotControl / 2);
        return previousRotation;
    }

    private double axis(int axis) {
        return m_controller.getHID().getRawAxis(axis);
    }

    private JoystickButton button(int button) {
        return new JoystickButton(m_controller.getHID(), button);
    }
}
