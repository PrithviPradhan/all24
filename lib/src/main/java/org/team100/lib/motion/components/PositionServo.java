package org.team100.lib.motion.components;

import org.team100.lib.encoder.Encoder100;
import org.team100.lib.profile.ChoosableProfile;
import org.team100.lib.telemetry.Telemetry;
import org.team100.lib.telemetry.Telemetry.Level;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.math.util.Units;

/** Positional control on top of a velocity servo. */
public class PositionServo<T> {
    public static class Config {
        public double kDeadband = 0.03;
    }

    private final Config m_config = new Config();
    private final Telemetry t = Telemetry.get();
    private final VelocityServo<T> m_servo;
    private final Encoder100<T> m_encoder;
    // private final TrapezoidProfile.Constraints m_constraints;
    private final double m_maxVel;
    private final PIDController m_controller;
    private final double m_period;
    private final String m_name;
    private final ChoosableProfile m_profile;

    private TrapezoidProfile.State m_goal = new TrapezoidProfile.State();
    // TODO: use a profile that exposes acceleration and use it.
    private TrapezoidProfile.State m_setpoint = new TrapezoidProfile.State();

    public PositionServo(
            String name,
            VelocityServo<T> servo,
            Encoder100<T> encoder,
            double maxVel,
            PIDController controller,
            ChoosableProfile profile) {
        m_servo = servo;
        m_encoder = encoder;
        m_maxVel = maxVel;
        m_controller = controller;
        m_period = controller.getPeriod();
        m_name = String.format("/angle position servo %s", name);
        m_profile = profile;
    }

    /**
     * @param goal For distance, use meters, For angle, use radians.
     */
    public void setPosition(double goal) {
        double measurement = getTurningAngleRad();
        m_goal = new TrapezoidProfile.State(goal, 0.0);
        m_setpoint = m_profile.calculate(m_period, m_goal, m_setpoint);

        double u_FB = m_controller.calculate(measurement, m_setpoint.position);
        double u_FF = m_setpoint.velocity;
        double u_TOTAL = u_FB + u_FF;
        // TODO: should there be a deadband here?
        u_TOTAL = MathUtil.applyDeadband(u_TOTAL, m_config.kDeadband, m_maxVel);
        u_TOTAL = MathUtil.clamp(u_TOTAL, -m_maxVel, m_maxVel);
        m_servo.setVelocity(u_TOTAL);

        t.log(Level.DEBUG, m_name + "/u_FB ", u_FB);
        t.log(Level.DEBUG, m_name + "/u_FF", u_FF);
        t.log(Level.DEBUG, m_name + "/Measurement (rad)", getTurningAngleRad());
        t.log(Level.DEBUG, m_name + "/Measurement (deg)", Units.radiansToDegrees(getTurningAngleRad()));
        t.log(Level.DEBUG, m_name + "/Goal (rad)", m_goal.position);
        t.log(Level.DEBUG, m_name + "/Setpoint (rad)", m_setpoint.position);
        t.log(Level.DEBUG, m_name + "/Setpoint Velocity (rad/s)", m_setpoint.velocity);
        t.log(Level.DEBUG, m_name + "/Error (rad)", m_controller.getPositionError());
        t.log(Level.DEBUG, m_name + "/Error Velocity (rad/s)", m_controller.getVelocityError());
        t.log(Level.DEBUG, m_name + "/Actual Speed", m_servo.getVelocity());
    }

    /**
     * @return For distance this is meters, for angle this is radians.
     */
    public double getPosition() {
        return getTurningAngleRad();
    }

    public void stop() {
        m_servo.stop();
    }

    public void close() {
        m_encoder.close();
    }

    public State getSetpoint() {
        return m_setpoint;
    }

    /////////////////////////////////////////////

    private double getTurningAngleRad() {
        return MathUtil.angleModulus(m_encoder.getPosition());
    }
}
