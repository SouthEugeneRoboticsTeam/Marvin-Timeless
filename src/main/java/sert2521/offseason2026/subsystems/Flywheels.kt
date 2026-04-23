package sert2521.offseason2026.subsystems

import com.revrobotics.PersistMode
import com.revrobotics.ResetMode
import com.revrobotics.spark.SparkBase
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkMaxConfig
import edu.wpi.first.networktables.BooleanTopic
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.Commands
import edu.wpi.first.wpilibj2.command.SubsystemBase
import sert2521.offseason2026.DemoConfiguration
import sert2521.offseason2026.constants.ElectronicIDs
import sert2521.offseason2026.constants.FlywheelConstants

object Flywheels : SubsystemBase() {
    private val topMotor = SparkMax(ElectronicIDs.FLYWHEEL_TOP_ID, SparkLowLevel.MotorType.kBrushless)
    private val bottomMotor = SparkMax(ElectronicIDs.FLYWHEEL_BOTTOM_ID, SparkLowLevel.MotorType.kBrushless)

    private val doubleTelemetries = mapOf(
        "* Flywheels/Average speed" to { (topMotor.encoder.velocity + bottomMotor.encoder.velocity) / 2 },
        "_ Flywheels/Top/Speed" to { topMotor.encoder.velocity },
        "_ Flywheels/Top/Voltage" to { topMotor.appliedOutput * topMotor.busVoltage },
        "_ Flywheels/Bottom/Speed" to { bottomMotor.encoder.velocity },
        "_ Flywheels/Bottom/Voltage" to { bottomMotor.appliedOutput * bottomMotor.busVoltage }
    )

    private val booleanTelemetries = mapOf<String, () -> Boolean>()


    init {
        val topMotorConfig = SparkMaxConfig()
            .idleMode(SparkBaseConfig.IdleMode.kCoast)
            .inverted(false)
            .smartCurrentLimit(40)
        topMotorConfig.encoder
            .positionConversionFactor(1.0 / FlywheelConstants.GEARING)
            .velocityConversionFactor(1.0 / FlywheelConstants.GEARING)
        topMotorConfig.closedLoop.feedForward
            .kV(FlywheelConstants.V)

        val bottomMotorConfig = SparkMaxConfig().apply(topMotorConfig)
            .inverted(true)

        topMotor.configure(topMotorConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters)
        bottomMotor.configure(bottomMotorConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters)

        setDefaultCommand()
    }

    private fun setSetpoints(rpm: Double) {
        topMotor.closedLoopController.setSetpoint(
            rpm,
            SparkBase.ControlType.kVelocity
        )
        bottomMotor.closedLoopController.setSetpoint(
            rpm,
            SparkBase.ControlType.kVelocity
        )
    }

    fun rev(): Command {
        return run {
            setSetpoints(DemoConfiguration.flywheelSpeed)
        }
    }

    private fun stop(): Command {
        return run {
            topMotor.stopMotor()
            bottomMotor.stopMotor()
        }
    }

    fun intakeFlywheels(): Command {
        return run {
            setSetpoints(FlywheelConstants.INTAKE_FROM_FLYWHEELS_SPEED)
        }
    }

    fun updateTelemetry(doubles: MutableMap<String, Double>, bools: MutableMap<String, Boolean>,
                        includes:String){
        doubleTelemetries.keys.forEach {
            for (type in includes) {
                if (type in it) {
                    val telemetryKey = it.drop(2)
                    doubles[telemetryKey] = doubleTelemetries[it]!!.invoke()
                }
            }
        }

        booleanTelemetries.keys.forEach {
            for (type in includes) {
                if (type in it) {
                    val telemetryKey = it.drop(2)
                    bools[telemetryKey] = booleanTelemetries[it]!!.invoke()
                }
            }
        }
    }

    fun setDefaultCommand() {
        defaultCommand = if (DemoConfiguration.flywheelSpeedIsDefault) {
            rev()
        } else {
            stop()
        }
    }
}