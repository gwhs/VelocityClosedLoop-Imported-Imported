// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.CANBus.CANBusStatus;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
//import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.VelocityTorqueCurrentFOC;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  //change device id as needed
  private final TalonFX m_fx = new TalonFX(0);

  /* retrieve bus utilization for the CANivore named TestCanivore */
  // CANBusStatus canInfo = CANBus.getStatus("TestCanivore");
  // float busUtil = canInfo.BusUtilization;
 
  /* Be able to switch which control request to use based on a button press */
  /* Start at velocity 0, enable FOC, no feed forward, use slot 0 */

  /* set FOC true/false to enable */
  private final VelocityVoltage m_voltageVelocity = new VelocityVoltage( 0,0, false, 0, 0, false, false, false);
  /* Keep a neutral out so we can disable the motor */
  private final NeutralOut m_brake = new NeutralOut();

  private final XboxController m_joystick = new XboxController(0);

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    TalonFXConfiguration configs = new TalonFXConfiguration();

    /* retrieve bus utilization for the CANivore named TestCanivore */
//   CANBusStatus canInfo = CANBus.getStatus("TestCanivore");
//   float busUtil = canInfo.BusUtilization;

// if (busUtil > 0.8) {
//    System.out.println("CAN bus utilization is greater than 80%!");
// }

    /* Voltage-based velocity requires a feed forward to account for the back-emf of the motor */
    configs.Slot0.kP = 0.11; // An error of 1 rotation per second results in 2V output
    configs.Slot0.kI = 0.5; // An error of 1 rotation per second increases output by 0.5V every second
    configs.Slot0.kD = 0.0001; // A change of 1 rotation per second squared results in 0.01 volts output
    configs.Slot0.kV = 0.12; // Falcon 500 is a 500kV motor, 500rpm per V = 8.333 rps per V, 1/8.33 = 0.12 volts / Rotation per second
    // Peak output of 8 volts
    configs.Voltage.PeakForwardVoltage = 8;
    configs.Voltage.PeakReverseVoltage = -8;
    
    /* Torque-based velocity does not require a feed forward, as torque will accelerate the rotor up to the desired velocity by itself */
    configs.Slot1.kP = 5; // An error of 1 rotation per second results in 5 amps output
    configs.Slot1.kI = 0.1; // An error of 1 rotation per second increases output by 0.1 amps every second
    configs.Slot1.kD = 0.001; // A change of 1000 rotation per second squared results in 1 amp output

    // Peak output of 40 amps
    configs.TorqueCurrent.PeakForwardTorqueCurrent = 40;
    configs.TorqueCurrent.PeakReverseTorqueCurrent = -40;

    /* Retry config apply up to 5 times, report if failure */
    StatusCode status = StatusCode.StatusCodeNotInitialized;
    for (int i = 0; i < 5; ++i) {
      status = m_fx.getConfigurator().apply(configs);
      if (status.isOK()) break;
    }
    if(!status.isOK()) {
      System.out.println("Could not apply configs, error code: " + status.toString());
    }

  }

  @Override
  public void robotPeriodic() {}

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {

    double joyValueX = m_joystick.getLeftX();
    double joyValueY = m_joystick.getLeftY();

    double joyValue = joyValueX + joyValueY;

    if (joyValue > -0.1 && joyValue < 0.1) joyValue = 0;

    if (m_joystick.getLeftY() > 0.0 || m_joystick.getLeftX() > 0.0) {
      //System.out.pfrintln(joyValue);
    }

    double desiredRotationsPerSecond = joyValue * 10; // Go for plus/minus 10 rotations per second
    // if (m_joystick.getLeftBumper()) {
    //   /* Use voltage velocity */
    //   m_fx.setControl(m_voltageVelocity.withVelocity(desiredRotationsPerSecond));
    //   System.out.println(desiredRotationsPerSecond);
    // }
    //should hold and spin ; might need to hold left bumper
    if(m_joystick.getXButton()) {
      m_fx.setControl(m_voltageVelocity.withVelocity(desiredRotationsPerSecond));
    }
    else {
      /* Disable the motor instead */
      m_fx.setControl(m_brake);
    }

  }

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationInit() {}

  @Override
  public void simulationPeriodic() {}
}
