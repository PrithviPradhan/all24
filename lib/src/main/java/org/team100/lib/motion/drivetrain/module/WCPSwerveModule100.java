package org.team100.lib.motion.drivetrain.module;

import org.team100.lib.config.FeedforwardConstants;
import org.team100.lib.config.PIDConstants;
import org.team100.lib.encoder.Encoder100;
import org.team100.lib.encoder.turning.AnalogTurningEncoder;
import org.team100.lib.encoder.turning.Drive;
import org.team100.lib.encoder.turning.DutyCycleTurningEncoder;
import org.team100.lib.motion.components.PositionServo;
import org.team100.lib.motion.components.PositionServoInterface;
import org.team100.lib.motion.components.SelectableVelocityServo;
import org.team100.lib.motion.components.VelocityServo;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.motor.Motor100;
import org.team100.lib.motor.MotorPhase;
import org.team100.lib.motor.MotorWithEncoder100;
import org.team100.lib.motor.drive.DriveMotorFactory;
import org.team100.lib.profile.Profile100;
import org.team100.lib.units.Angle100;
import org.team100.lib.units.Distance100;
import org.team100.lib.util.Names;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;

public class WCPSwerveModule100 extends SwerveModule100 {
    private static final String m_name = Names.name(WCPSwerveModule100.class);

    // WCP 4 inch wheel
    private static final double kWheelDiameterM = 0.1015;
    // see wcproducts.com, this is the "fast" ratio.
    private static final double kDriveReduction = 5.50;

    /**
     * @param name                  like "front left" or whatever
     * @param curerntLimit          in amps
     * @param driveMotorCanId
     * @param encoderClass          select the type of encoder that exists on the
     *                              robot
     * @param turningMotorCanId
     * @param turningEncoderChannel
     * @param turningOffset
     * @param kinodynamics
     */
    public static WCPSwerveModule100 get(
            String name,
            double currentLimit,
            int driveMotorCanId,
            Class<? extends Encoder100<Angle100>> encoderClass,
            int turningMotorCanId,
            int turningEncoderChannel,
            double turningOffset,
            SwerveKinodynamics kinodynamics,
            Drive drive,
            MotorPhase motorPhase) {
        name = m_name + "/" + name;
        PIDConstants drivePidConstants = new PIDConstants(8);
        PIDConstants turningPidConstants = new PIDConstants(5);
        FeedforwardConstants turningFeedforwardConstants = FeedforwardConstants.makeWCPSwerveTurningFalcon6();
        FeedforwardConstants driveFeedforwardConstants = FeedforwardConstants.makeWCPSwerveDriveFalcon6();
        VelocityServo<Distance100> driveServo = driveServo(
                name + "/Drive",
                currentLimit,
                driveMotorCanId,
                drivePidConstants,
                driveFeedforwardConstants);

        PositionServoInterface<Angle100> turningServo = turningServo(
                name + "/Turning",
                encoderClass,
                turningMotorCanId,
                turningEncoderChannel,
                turningOffset,
                10.29,
                kinodynamics,
                drive,
                motorPhase,
                turningPidConstants,
                turningFeedforwardConstants);

        return new WCPSwerveModule100(name, driveServo, turningServo);
    }

    private static VelocityServo<Distance100>  driveServo(
            String name,
            double currentLimit,
            int driveMotorCanId,
            PIDConstants pidConstants,
            FeedforwardConstants feedforwardConstants) {
        MotorWithEncoder100<Distance100> driveMotor = DriveMotorFactory.driveMotor(
                name,
                currentLimit,
                driveMotorCanId,
                pidConstants,
                feedforwardConstants,
                kDriveReduction,
                kWheelDiameterM);
        PIDController driveController = new PIDController( //
                0.1, // kP //1.2
                0, // kI //0.3
                0.0); // kD
        // Note very low windup limit.
        driveController.setIntegratorRange(-0.01, 0.01);
        SimpleMotorFeedforward driveFeedforward = new SimpleMotorFeedforward( //
                0.06, // kS
                0.3, // kV
                0.025); // kA
        return new SelectableVelocityServo<>(
                name,
                driveMotor,
                driveMotor,
                driveController,
                driveFeedforward);
    }

    private static PositionServoInterface<Angle100> turningServo(
            String name,
            Class<? extends Encoder100<Angle100>> encoderClass,
            int turningMotorCanId,
            int turningEncoderChannel,
            double turningOffset,
            double gearRatio,
            SwerveKinodynamics kinodynamics,
            Drive drive,
            MotorPhase motorPhase,
            PIDConstants lowLevelPID,
            FeedforwardConstants lowLevelFeedforward) {
        final double turningGearRatio = 1.0;
        Motor100<Angle100> turningMotor = DriveMotorFactory.turningMotor(
                name,
                turningMotorCanId,
                motorPhase,
                gearRatio,
                lowLevelPID,
                lowLevelFeedforward);
        Encoder100<Angle100> turningEncoder = turningEncoder(
                encoderClass,
                name,
                turningEncoderChannel,
                turningOffset,
                turningGearRatio,
                drive);
        PIDController angleVelocityController = new PIDController(
                2.86, // kP
                0, // kI
                0, // kD
                dt);
        SimpleMotorFeedforward turningFeedforward = new SimpleMotorFeedforward( //
                0.0006, // kS: Multiplied by around 20 of previous value as that is how much we changed
                        // P by 0.0005
                0.005, // kV: Since we are decreasing the value of how much the PID system does we need
                       // to conpensate for making feedforward larger as well
                0); // kA
        VelocityServo<Angle100> turningVelocityServo = new SelectableVelocityServo<>(
                name,
                turningMotor,
                turningEncoder,
                angleVelocityController,
                turningFeedforward);

        PIDController turningPositionController = new PIDController(
                2.86, // kP
                0.06, // kI
                0, // kD
                dt);
        turningPositionController.enableContinuousInput(-Math.PI, -Math.PI);
        turningPositionController.setTolerance(0.1, 0.1);

        Profile100 profile = kinodynamics.getSteeringProfile();
        PositionServoInterface<Angle100> turningServo = new PositionServo<>(
                name,
                turningVelocityServo,
                turningEncoder,
                kinodynamics.getMaxSteeringVelocityRad_S(),
                turningPositionController,
                profile,
                Angle100.instance);
        turningServo.reset();
        return turningServo;
    }

    private static Encoder100<Angle100> turningEncoder(
            Class<?> encoderClass,
            String name,
            int channel,
            double inputOffset,
            double gearRatio,
            Drive drive) {
        if (encoderClass == AnalogTurningEncoder.class) {
            return new AnalogTurningEncoder(name,
                    channel,
                    inputOffset,
                    gearRatio,
                    drive);
        }
        if (encoderClass == DutyCycleTurningEncoder.class) {
            return new DutyCycleTurningEncoder(name,
                    channel,
                    inputOffset,
                    gearRatio,
                    drive);
        }
        throw new IllegalArgumentException("unknown encoder class: " + encoderClass.getName());

    }

    private WCPSwerveModule100(
            String name,
            VelocityServo<Distance100> driveServo,
            PositionServoInterface<Angle100> turningServo) {
        super(name, driveServo, turningServo);
        //
    }
}
