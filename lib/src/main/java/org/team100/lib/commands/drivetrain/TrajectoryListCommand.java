package org.team100.lib.commands.drivetrain;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.team100.lib.controller.HolonomicFieldRelativeController;
import org.team100.lib.motion.drivetrain.SwerveDriveSubsystem;
import org.team100.lib.motion.drivetrain.SwerveState;
import org.team100.lib.telemetry.Telemetry;
import org.team100.lib.telemetry.Telemetry.Level;
import org.team100.lib.trajectory.TrajectoryVisualization;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.Trajectory.State;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * Follow a list of trajectories.
 * 
 * The list can be relative to the current pose.
 * 
 * TODO: use the new holonomic trajectory type
 */
public class TrajectoryListCommand extends Command {
    private final Telemetry t = Telemetry.get();
    private final SwerveDriveSubsystem m_swerve;
    final Timer m_timer;
    private final HolonomicFieldRelativeController m_controller;
    private final Function<Pose2d, List<Trajectory>> m_trajectories;
    private Iterator<Trajectory> m_trajectoryIter;
    private Trajectory m_currentTrajectory;
    private boolean done;
    // this holds the current rotation
    // TODO: allow trajectory to specify it using the new type
    private Rotation2d m_rotation;
    private boolean m_aligned;

    public TrajectoryListCommand(
            SwerveDriveSubsystem swerve,
            HolonomicFieldRelativeController controller,
            Function<Pose2d, List<Trajectory>> trajectories) {
        m_swerve = swerve;
        m_controller = controller;
        m_timer = new Timer();
        m_trajectories = trajectories;
        addRequirements(m_swerve);
    }

    @Override
    public void initialize() {
        m_controller.reset();
        Pose2d currentPose = m_swerve.getPose();
        m_rotation = currentPose.getRotation();
        m_trajectoryIter = m_trajectories.apply(currentPose).iterator();
        m_currentTrajectory = null;
        m_timer.stop();
        m_timer.reset();
        done = false;
        m_aligned = false;
    }

    @Override
    public void execute() {
        if (m_currentTrajectory == null || m_timer.get() > m_currentTrajectory.getTotalTimeSeconds()) {
            // get the next trajectory
            if (m_trajectoryIter.hasNext()) {
                m_currentTrajectory = m_trajectoryIter.next();
                TrajectoryVisualization.setViz(m_currentTrajectory);
                m_timer.stop();
                m_timer.reset();
                m_aligned = false;
            } else {
                done = true;
                return;
            }
        }

        // now there is a trajectory to follow

        if (m_aligned) {
            State desiredState = m_currentTrajectory.sample(m_timer.get());
            Pose2d currentPose = m_swerve.getPose();
            SwerveState reference = SwerveState.fromState(desiredState, m_rotation);
            t.log(Level.DEBUG, "/trajectory list/reference", reference);
            Twist2d fieldRelativeTarget = m_controller.calculate(currentPose, reference);
            m_swerve.driveInFieldCoords(fieldRelativeTarget);
        } else {
            // look just one loop ahead
            State desiredState = m_currentTrajectory.sample(m_timer.get()+0.02);
            Pose2d currentPose = m_swerve.getPose();
            SwerveState reference = SwerveState.fromState(desiredState, m_rotation);
            t.log(Level.DEBUG, "/trajectory list/reference", reference);
            Twist2d fieldRelativeTarget = m_controller.calculate(currentPose, reference);
            boolean aligned = m_swerve.steerAtRest(fieldRelativeTarget);
            if (aligned) {
                m_aligned = true;
                m_timer.start();
            }
        }
    }

    @Override
    public boolean isFinished() {
        return done;
    }

    @Override
    public void end(boolean interrupted) {
        m_swerve.stop();
        TrajectoryVisualization.clear();
    }
}
