package org.team100.lib.motion.components;

import org.team100.lib.controller.State100;
import org.team100.lib.units.Measure100;

public interface PositionServoInterface<T extends Measure100> {

    /**
     * It is essential to call this after a period of disuse, to prevent transients.
     * 
     * To prevent oscillation, the previous setpoint is used to compute the profile,
     * but there needs to be an initial setpoint.
     */
    void reset();

    /**
     * @param goal For distance, use meters, For angle, use radians.
     */
    void setPosition(double goal);

    /** Direct velocity control for testing */
    void setVelocity(double velocity);

    /** Direct duty cycle for testing */
    void setDutyCycle(double dutyCycle);

    /**
     * @return Current position measurement. For distance this is meters, for angle
     *         this is radians.
     */
    double getPosition();

    double getVelocity();

    boolean atSetpoint();

    boolean atGoal();

    double getGoal();

    void stop();

    void close();

    /** for testing only */
    State100 getSetpoint();

    void periodic();

}