package org.team100.frc2024.motion.intake;

import org.team100.lib.config.FeedforwardConstants;
import org.team100.lib.config.Identity;
import org.team100.lib.config.PIDConstants;
import org.team100.lib.config.SysParam;
import org.team100.lib.motion.components.LimitedVelocityServo;
import org.team100.lib.motion.components.ServoFactory;
import org.team100.lib.motion.simple.SpeedingVisualization;
import org.team100.lib.units.Distance100;
import org.team100.lib.util.Names;

/**
 * Four-axle wheeled intake with reduction
 * 
 * Typical free speed of 6k rpm => 100 turn/sec
 * Reduction of 3:1 so 33 turn/s wheel speed.
 * diameter of 0.05m => 0.15 m/turn
 * therefore top speed is around 5 m/s.
 * 
 * This system has low intertia but a lot of friction,
 * and it's fragile. guess at a reasonable accel limit.
 */
public class IntakeWheel extends Intake {
    // TODO: tune the current limit
    private static final int kCurrentLimit = 30;
    /**
     * Surface velocity of whatever is turning in the intake.
     */
    private static final double kIntakeVelocityM_S = 10;
    private final String m_name;
    private final LimitedVelocityServo<Distance100> intakeMotor;
    private final SpeedingVisualization m_viz;
    private final PIDConstants m_velocityConstants;
    private final FeedforwardConstants m_lowLevelFeedforwardConstants;


    public IntakeWheel(int wheelID) {
        m_velocityConstants = new PIDConstants(0.0001, 0, 0);
        m_lowLevelFeedforwardConstants = new FeedforwardConstants(0.122,0,0.1,0.065);

        m_name = Names.name(this);

        SysParam params = SysParam.limitedNeoVelocityServoSystem(
                9.0,
                0.05,
                10.0,
                20.0,
                -20.0);
        switch (Identity.instance) {
            case COMP_BOT:
            //TODO tune kV
                intakeMotor = ServoFactory.limitedNeoVelocityServo(
                        m_name, wheelID, false, kCurrentLimit, params, m_lowLevelFeedforwardConstants, m_velocityConstants);
                break;
            case BLANK:
            default:
                intakeMotor = ServoFactory.limitedSimulatedVelocityServo(
                        m_name, params);
        }
        m_viz = new SpeedingVisualization(m_name, this);
    }

    @Override
    public void intake() {
        intakeMotor.setVelocity(kIntakeVelocityM_S);
    }

    @Override
    public void outtake() {
        intakeMotor.setVelocity(-1.0 * kIntakeVelocityM_S);
    }

    @Override
    public void stop() {
        intakeMotor.stop();
    }


    @Override
    public void periodic() {
        intakeMotor.periodic();
        m_viz.periodic();
    }

    @Override
    public double getVelocity() {
        return intakeMotor.getVelocity();
    }
}