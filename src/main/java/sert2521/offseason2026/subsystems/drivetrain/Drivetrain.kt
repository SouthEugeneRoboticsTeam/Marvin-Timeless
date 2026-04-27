package sert2521.offseason2026.subsystems.drivetrain

import com.studica.frc.AHRS
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.kinematics.SwerveDriveKinematics
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.Commands
import edu.wpi.first.wpilibj2.command.SubsystemBase
import sert2521.offseason2026.DemoConfiguration
import sert2521.offseason2026.Input
import sert2521.offseason2026.constants.SwerveConstants
import java.util.function.DoubleSupplier
import kotlin.math.*


object Drivetrain : SubsystemBase() {
    private val modules = Array(4) {
        SwerveModule(it)
    }
    private val gyro = AHRS(AHRS.NavXComType.kMXP_SPI)

    private val kinematics = SwerveDriveKinematics(*SwerveConstants.moduleTranslations)
    private var currentRotation = Rotation2d.kZero

    private var rotationOffset = Rotation2d.kZero

    init {
        setDefaultCommand()
    }

    override fun periodic() {
        currentRotation = gyro.rotation2d

        modules.forEach {
            it.seedPosition()
        }
    }

    private fun driveRobotOriented(chassisSpeeds: ChassisSpeeds) {
        val states = kinematics.toSwerveModuleStates(chassisSpeeds)

        modules.forEachIndexed { index, module ->
            module.setState(states[index])
        }
    }

    private fun driveFieldOriented(chassisSpeeds: ChassisSpeeds) {
        driveRobotOriented(
            ChassisSpeeds.fromFieldRelativeSpeeds(
                chassisSpeeds,
                currentRotation.minus(rotationOffset)
            )
        )
    }

    private fun joystickDrive(vx: DoubleSupplier, vy: DoubleSupplier, omega: DoubleSupplier): Command {
        return run {
            val theta = atan2(vy.asDouble, vx.asDouble)
            val mag = min(hypot(vx.asDouble, vy.asDouble), 1.0)

            driveFieldOriented(
                ChassisSpeeds(
                    sin(theta) * mag * DemoConfiguration.driveSpeed,
                    cos(theta) * mag * DemoConfiguration.driveSpeed,
                    omega.asDouble * DemoConfiguration.rotationSpeed
                )
            )
        }
    }

    fun setRotationOffset() {
        rotationOffset = currentRotation
    }

    fun setDefaultCommand() {
        defaultCommand = if (DemoConfiguration.enableDrive) {
            joystickDrive(Input::getLeftX, Input::getLeftY, Input::getRightRot)
        } else {
            Commands.idle(this)
        }
    }

    // Advanced telemetry stuff, you'll never have to edit this I'm pretty sure
    fun updateTelemetry(doubles: MutableMap<String, Double>, bools: MutableMap<String, Boolean>,
                        includes: String) {
        modules.forEach {
            it.updateTelemetry(doubles, bools, includes)
        }
    }
}