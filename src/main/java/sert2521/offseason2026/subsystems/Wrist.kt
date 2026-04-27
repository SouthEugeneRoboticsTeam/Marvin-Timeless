package sert2521.offseason2026.subsystems

import com.revrobotics.PersistMode
import com.revrobotics.ResetMode
import com.revrobotics.spark.SparkBase
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkMaxConfig
import edu.wpi.first.math.MathUtil
import edu.wpi.first.math.filter.Debouncer
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.Commands
import edu.wpi.first.wpilibj2.command.SubsystemBase
import sert2521.offseason2026.DemoConfiguration
import sert2521.offseason2026.Input
import sert2521.offseason2026.constants.ElectronicIDs
import sert2521.offseason2026.constants.WristConstants
import java.util.function.DoubleSupplier

object Wrist : SubsystemBase() {
    private val leftMotor = SparkMax(ElectronicIDs.WRIST_LEFT_ID, SparkLowLevel.MotorType.kBrushless)
    private val rightMotor = SparkMax(ElectronicIDs.WRIST_RIGHT_ID, SparkLowLevel.MotorType.kBrushless)

    private val currentDebouncer = Debouncer(0.2, Debouncer.DebounceType.kRising)

    private val doubleTelemetries = mapOf(
        "* Wrist/Angle" to { (leftMotor.encoder.position + rightMotor.encoder.position) / 2 },
        "^ Wrist/Velocity" to { (leftMotor.encoder.velocity + rightMotor.encoder.velocity) / 2 },

        "_ Wrist/Left Motor/Speed" to { leftMotor.encoder.velocity },
        "_ Wrist/Left Motor/Voltage" to { leftMotor.appliedOutput * leftMotor.busVoltage },
        "_ Wrist/Left Motor/Current" to { leftMotor.outputCurrent },

        "_ Wrist/Right Motor/Speed" to { rightMotor.encoder.velocity },
        "_ Wrist/Right Motor/Voltage" to { rightMotor.appliedOutput * rightMotor.busVoltage },
        "_ Wrist/Right Motor/Current" to { rightMotor.outputCurrent }
    )

    private val booleanTelemetries = mapOf(
        "^ Is at setpoint" to { isAtSetpoint() }
    )

    init {
        val leftConfig = SparkMaxConfig()
            .inverted(false)
            .smartCurrentLimit(40)
            .idleMode(SparkBaseConfig.IdleMode.kBrake)
        leftConfig
            .encoder
            .positionConversionFactor(1.0 / WristConstants.GEARING)
            .velocityConversionFactor(60.0 / WristConstants.GEARING)
        leftConfig.closedLoop
            .pid(WristConstants.P, 0.0, WristConstants.D)
        leftConfig.closedLoop.feedForward
            .kS(WristConstants.S)
            .kCos(WristConstants.G)
            .kV(WristConstants.V)
            .kA(WristConstants.A)
        leftConfig.closedLoop.maxMotion
            .cruiseVelocity(WristConstants.MAX_VELOCITY)
            .maxAcceleration(WristConstants.MAX_ACCELERATION)
            .allowedProfileError(WristConstants.ALLOWED_PROFILE_ERROR)

        // Can't have a reverse soft limit because it enables at any position
        // and goes backward to rezero
        leftConfig.softLimit
            .forwardSoftLimitEnabled(true)
            .forwardSoftLimit(WristConstants.HARD_MAX)

        val rightConfig = SparkMaxConfig().apply(leftConfig)
        rightConfig
            .inverted(true)

        leftMotor.configure(leftConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters)
        rightMotor.configure(rightConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters)

        setDefaultCommand()
    }

    private fun setSetpoint(setpoint: Double) {
        if (DemoConfiguration.enableWrist) {
            leftMotor.closedLoopController.setSetpoint(
                setpoint,
                SparkBase.ControlType.kMAXMotionPositionControl
            )
            rightMotor.closedLoopController.setSetpoint(
                setpoint,
                SparkBase.ControlType.kMAXMotionPositionControl
            )
        } else {
            leftMotor.stopMotor()
            rightMotor.stopMotor()
        }
    }

    private fun setVoltage(voltage: Double) {
        if (DemoConfiguration.enableWrist) {
            leftMotor.setVoltage(voltage)
            rightMotor.setVoltage(voltage)
        } else {
            leftMotor.stopMotor()
            rightMotor.stopMotor()
        }
    }

    private fun isAtSetpoint(): Boolean {
        return leftMotor.closedLoopController.isAtSetpoint && rightMotor.closedLoopController.isAtSetpoint
    }

    fun rezero(): Command {
        return runOnce { currentDebouncer.calculate(false) }.andThen(
            run {
                setVoltage(WristConstants.REZERO_VOLTAGE)
            }
        ).until {
            currentDebouncer.calculate(leftMotor.outputCurrent > WristConstants.REZERO_CURRENT_THRESHOLD)
        }.andThen(
            runOnce {
                leftMotor.encoder.position = WristConstants.HARD_MIN
                rightMotor.encoder.position = WristConstants.HARD_MIN
                setVoltage(0.0)
            }
        )
    }

    fun toPosition(position: Double): Command {
        return run {
            setSetpoint(position)
        }
    }

    private fun toMovingPosition(position: DoubleSupplier): Command {
        return run {
            setSetpoint(position.asDouble)
        }
    }

    private fun wristToSlider(): Command {
        return Commands.waitUntil {
            MathUtil.isNear(0.0, Input.getGunnerSlider(), 0.02)
        }.andThen(
            toMovingPosition {
                MathUtil.interpolate(
                    WristConstants.STOW_POSITION,
                    DemoConfiguration.wristMaxHeight,
                    Input.getGunnerSlider()
                )
            }
        )
    }

    fun setDefaultCommand() {
        defaultCommand = if (DemoConfiguration.enableWristToSlider && DemoConfiguration.enableWrist) {
            wristToSlider()
        } else {
            Commands.idle(this)
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
}