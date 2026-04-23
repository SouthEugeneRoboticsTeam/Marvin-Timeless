package sert2521.offseason2026

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Commands.runOnce
import edu.wpi.first.wpilibj2.command.button.CommandJoystick
import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import sert2521.offseason2026.subsystems.IntakeIndexer
import sert2521.offseason2026.subsystems.drivetrain.Drivetrain

object Input {
    private val driverController = CommandXboxController(0)
    private val gunnerController = CommandJoystick(1)

    private val intake = driverController.rightTrigger()
    private val reverse = driverController.a()

    private val shoot = driverController.leftTrigger()
    private val resetRotOffset = driverController.start().and(driverController.back())

    init {
        intake.whileTrue(IntakeIndexer.intake())
        reverse.whileTrue(IntakeIndexer.reverse())

        shoot.onTrue(IntakeIndexer.kick())

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