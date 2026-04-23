package sert2521.offseason2026

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Commands.runOnce
import edu.wpi.first.wpilibj2.command.button.CommandGenericHID
import edu.wpi.first.wpilibj2.command.button.CommandJoystick
import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import sert2521.offseason2026.subsystems.Flywheels
import sert2521.offseason2026.subsystems.IntakeIndexer
import sert2521.offseason2026.subsystems.drivetrain.Drivetrain

object Input {
    private val driverController = CommandXboxController(0)
    private val gunnerController = CommandGenericHID(1)

    /* DRIVER */
    private val intake = driverController.rightTrigger()
    private val flywheelIntake = driverController.rightBumper()
    private val reverse = driverController.a()

    private val shoot = driverController.leftTrigger()
    private val rev = driverController.leftBumper()
    private val resetRotOffset = driverController.start().and(driverController.back())

    /* GUNNER */
    private val gunnerIntake = gunnerController.button(0)

    private val wristStow = gunnerController.button(0) // TODO: Set
    private val wristPodium = gunnerController.button(0)
    private val wristAmp = gunnerController.button(0)

    private val gunnerFlywheelIntake = gunnerController.button(0)

    init {
        intake.whileTrue(IntakeIndexer.intake())
        flywheelIntake.whileTrue(Flywheels.intakeFlywheels().withDeadline(IntakeIndexer.recenter()))
        reverse.whileTrue(IntakeIndexer.reverse())

        shoot.onTrue(IntakeIndexer.kick())
        rev.whileTrue(Flywheels.rev())



        resetRotOffset.onTrue(runOnce(Drivetrain::setRotationOffset))
    }

    fun getLeftX(): Double {
        return -driverController.leftX
    }

    fun getLeftY(): Double {
        return -driverController.leftY
    }

    fun getRightRot(): Double {
        return -driverController.rightX
    }

    fun getGunnerSlider(): Double {
        return (-gunnerController.getRawAxis(3) + 1.0) / 2.0
    }
}