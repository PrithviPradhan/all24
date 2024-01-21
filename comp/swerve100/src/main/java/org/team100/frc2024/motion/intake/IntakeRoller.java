package org.team100.frc2024.motion.intake;

import org.team100.lib.config.SysParam;
import org.team100.lib.motion.components.LimitedVelocityServo;
import org.team100.lib.motion.components.ServoFactory;
import org.team100.lib.units.Distance100;
import org.team100.lib.util.Names;


/**
 * TODO: add intake to selftest.
 */
public class IntakeRoller extends Intake {
    private final String m_name;

    private final LimitedVelocityServo<Distance100> topRoller;
    private final LimitedVelocityServo<Distance100> bottomRoller;
    private final SysParam rollerParameter;


    public IntakeRoller(int topCAN, int bottomCAN) {
        m_name = Names.name(this);

        rollerParameter = SysParam.limitedNeoVelocityServoSystem(
            1,
            1, 
            5, 
            5, 
            5
        );


        topRoller = ServoFactory.limitedNeoVelocityServo(
                m_name + "/Top Roller",
                topCAN,
                false,
                rollerParameter);

        bottomRoller = ServoFactory.limitedNeoVelocityServo(
                m_name + "/Bottom Roller",
                bottomCAN,
                false,
                rollerParameter);
    }

    @Override
    public void setIntake(double value) {
        topRoller.setVelocity(value);
        bottomRoller.setVelocity(value);
    }

    @Override
    public void periodic() {
        topRoller.periodic();
        bottomRoller.periodic();
    }
}