// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.team100.frc2024.motion.amp;

import edu.wpi.first.wpilibj2.command.Command;

public class PivotToAmpPosition extends Command {
  /** Creates a new PivotToAmpPosition. */
  private final AmpSubsystem m_amp;
  public PivotToAmpPosition(AmpSubsystem amp) {
    // Use addRequirements() here to declare subsystem dependencies.
    m_amp = amp;
    addRequirements(amp);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    m_amp.setAmpPosition(2.066);

  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
