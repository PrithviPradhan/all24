package org.team100.lib.swerve;

import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.telemetry.Telemetry;
import org.team100.lib.telemetry.Telemetry.Level;
import org.team100.lib.util.Names;

/**
 * Enforces a fixed limit on delta v.
 */
public class CapsizeAccelerationLimiter {
    private static final Telemetry t = Telemetry.get();
    private final SwerveKinodynamics m_limits;
    private final String m_name;

    public CapsizeAccelerationLimiter(String parent, SwerveKinodynamics limits) {
        m_name = Names.append(parent, this);
        m_limits = limits;
    }

    public double enforceCentripetalLimit(double dx, double dy, double kDtSec) {
        double s = 1.0;
        double dv = Math.hypot(dx, dy);
        if (Math.abs(dv) > 1e-6) {
            s = kDtSec * m_limits.getMaxCapsizeAccelM_S2() / dv;
        }
        t.log(Level.DEBUG, m_name, "s", s);
        return s;
    }
}
