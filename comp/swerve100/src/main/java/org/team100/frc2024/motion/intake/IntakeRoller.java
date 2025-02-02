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
 * Direct-drive roller intake
 * 
 * Typical free speed of 6k rpm => 100 turn/sec
 * diameter of 0.05m => 0.15 m/turn
 * therefore top speed is around 15 m/s.
 * 
 * This system has very low intertia, so can spin up
 * very fast, but it's fragile: limit accel to avoid stressing it.
 */
public class IntakeRoller extends Intake {
    // TODO: tune the current limit
    private static final int kCurrentLimit = 30;

    /**
     * Surface velocity of whatever is turning in the intake.
     */
    private static final double kIntakeVelocityM_S = 3;
    private final String m_name;
    private final LimitedVelocityServo<Distance100> topRoller;
    private final LimitedVelocityServo<Distance100> bottomRoller;
    private final SpeedingVisualization m_viz;
    private final PIDConstants m_velocityConstants;
    private final FeedforwardConstants m_lowLevelFeedforwardConstants;

    public IntakeRoller(int topCAN, int bottomCAN) {
        m_velocityConstants = new PIDConstants(0.0001, 0, 0);
        m_lowLevelFeedforwardConstants = new FeedforwardConstants(0.122,0,0.1,0.065);
        m_name = Names.name(this);
        SysParam rollerParameter = SysParam.limitedNeoVelocityServoSystem(
                1,
                0.05,
                15,
                10,
                -10);

        switch (Identity.instance) {
            case COMP_BOT:
            //TODO tune kV
                topRoller = ServoFactory.limitedNeoVelocityServo(
                        m_name + "/Top Roller",
                        topCAN,
                        false,
                        kCurrentLimit,
                        rollerParameter,
                        m_lowLevelFeedforwardConstants,
                        m_velocityConstants);
                bottomRoller = ServoFactory.limitedNeoVelocityServo(
                        m_name + "/Bottom Roller",
                        bottomCAN,
                        false,
                        kCurrentLimit,
                        rollerParameter,
                        m_lowLevelFeedforwardConstants,
                        m_velocityConstants);
                break;
            case BLANK:
            default:
                topRoller = ServoFactory.limitedSimulatedVelocityServo(
                        m_name + "/Top Roller",
                        rollerParameter);
                bottomRoller = ServoFactory.limitedSimulatedVelocityServo(
                        m_name + "/Bottom Roller",
                        rollerParameter);
        }
        m_viz = new SpeedingVisualization(m_name, this);
    }

    @Override
    public void intake() {
        topRoller.setVelocity(kIntakeVelocityM_S);
        bottomRoller.setVelocity(kIntakeVelocityM_S);
    }

    @Override
    public void outtake() {
        topRoller.setVelocity(-1.0 * kIntakeVelocityM_S);
        bottomRoller.setVelocity(-1.0 * kIntakeVelocityM_S);
    }

    @Override
    public void stop() {
        topRoller.stop();
        bottomRoller.stop();
    }

    @Override
    public void periodic() {
        topRoller.periodic();
        bottomRoller.periodic();
        m_viz.periodic();
    }

    @Override
    public double getVelocity() {
        return (topRoller.getVelocity() + bottomRoller.getVelocity()) / 2;
    }
}
