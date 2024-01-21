// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.team100.frc2024.motion.intake;

import org.team100.lib.config.SysParam;
import org.team100.lib.motion.components.LimitedVelocityServo;
import org.team100.lib.motion.components.ServoFactory;
import org.team100.lib.units.Distance100;
import org.team100.lib.util.Names;

public class IntakeWheel extends Intake {
  /** Creates a new IntakeWheel. */

  private final String m_name;
  private final SysParam intakeParam;

  private final LimitedVelocityServo<Distance100> intakeMotor;

  public IntakeWheel(int wheelID) {
      m_name = Names.name(this);

      intakeParam = SysParam.limitedNeoVelocityServoSystem(
        1,
        1, 
        1, 
        1, 
        1
      );



      intakeMotor = ServoFactory.limitedNeoVelocityServo(m_name, wheelID, false, intakeParam);
  }

  @Override
  public void setIntake(double value){
    intakeMotor.setVelocity(value);

  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}