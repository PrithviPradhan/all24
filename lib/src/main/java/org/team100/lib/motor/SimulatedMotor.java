package org.team100.lib.motor;

import org.team100.lib.telemetry.Telemetry;
import org.team100.lib.telemetry.Telemetry.Level;
import org.team100.lib.units.Measure100;
import org.team100.lib.util.Names;

import edu.wpi.first.math.MathUtil;

/**
 * Very simple simulated motor.
 */
public class SimulatedMotor<T extends Measure100> implements Motor100<T> {
    private final Telemetry t = Telemetry.get();
    private final String m_name;

    /**
     * @param name may not start with slash
     */
    public SimulatedMotor(String name) {
        if (name.startsWith("/"))
            throw new IllegalArgumentException();
        m_name = Names.append(name, this);
    }

    private double m_velocity = 0;

    @Override
    public void setDutyCycle(double output) {
        output = MathUtil.clamp(output, -1, 1);
        t.log(Level.DEBUG, m_name, "duty_cycle", output);
        // 100% output => about 6k rpm
        setVelocity(output * 600, 0);
    }

    @Override
    public void stop() {
        m_velocity = 0;
    }

    /**
     * Ignores accel, because the simulated motor responds instantly to the velocity
     * command, i.e. the accel is effectively infinite.
     */
    @Override
    public void setVelocity(double velocity, double accel) {
        if (Double.isNaN(velocity))
            throw new IllegalArgumentException("velocity is NaN");
        m_velocity = velocity;
        // ignore accel
        t.log(Level.DEBUG, m_name, "velocity", m_velocity);
    }

    public double getVelocity() {
        return m_velocity;
    }

    @Override
    public void close() {
        //
    }

    @Override
    public void periodic() {
        //
    }
}
