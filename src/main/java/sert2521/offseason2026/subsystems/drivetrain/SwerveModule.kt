package sert2521.offseason2026.subsystems.drivetrain

import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.signals.MagnetHealthValue
import com.revrobotics.PersistMode
import com.revrobotics.ResetMode
import com.revrobotics.spark.SparkBase
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkMaxConfig
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.kinematics.SwerveDriveKinematics
import edu.wpi.first.math.kinematics.SwerveModuleState
import edu.wpi.first.math.util.Units
import edu.wpi.first.units.Units.Radians
import sert2521.offseason2026.constants.ElectronicIDs
import sert2521.offseason2026.constants.SwerveConstants
import sert2521.offseason2026.subsystems.Wrist
import kotlin.math.PI

class SwerveModule(private val index: Int) {
    private val canCoder = CANcoder(ElectronicIDs.encoderIDs[index])
    private val driveMotor = SparkMax(ElectronicIDs.driveIDs[index], SparkLowLevel.MotorType.kBrushless)
    private val angleMotor = SparkMax(ElectronicIDs.angleIDs[index], SparkLowLevel.MotorType.kBrushless)

    private var currentAngle = Rotation2d()
    private val name = SwerveConstants.moduleNames[index]

    private val doubleTelemetries = mapOf(
        "^ Swerve/$name Module/Angle Position Radians" to { angleMotor.encoder.position },
        "^ Swerve/$name Module/Drive Speed MPS" to { driveMotor.encoder.velocity },
        "^ Swerve/$name Module/CANcoder Position" to { canCoder.position.value.`in`(Radians) }
    )

    private val booleanTelemetries = mapOf(
        "_ Swerve/$name Module/CANcoder Health Good" to { canCoder.magnetHealth.value == MagnetHealthValue.Magnet_Green }
    )

    init {
        val driveMotorConfig = SparkMaxConfig()
            .idleMode(SparkBaseConfig.IdleMode.kBrake)
            .inverted(false)
            .smartCurrentLimit(SwerveConstants.DRIVE_CURRENT_LIMIT)
        driveMotorConfig.encoder
            .positionConversionFactor(2 * PI / SwerveConstants.DRIVE_GEARING)
            .velocityConversionFactor(60.0 * 2 * PI / SwerveConstants.DRIVE_GEARING)
        driveMotorConfig.closedLoop.feedForward
            .kS(SwerveConstants.DRIVE_S)
            .kV(SwerveConstants.DRIVE_V)

        val angleMotorConfig = SparkMaxConfig()
            .idleMode(SparkBaseConfig.IdleMode.kBrake)
            .inverted(false)
            .smartCurrentLimit(40)
        angleMotorConfig.encoder // 2 * PI to work in radians
            .positionConversionFactor(2 * PI / SwerveConstants.ANGLE_GEARING)
            .velocityConversionFactor(60.0 * 2 * PI / SwerveConstants.ANGLE_GEARING)
        angleMotorConfig.closedLoop
            .pid(SwerveConstants.ANGLE_P, 0.0, SwerveConstants.ANGLE_D)
            .positionWrappingEnabled(true)
            .positionWrappingInputRange(-PI, PI)

        driveMotor.configure(driveMotorConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters)
        angleMotor.configure(angleMotorConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters)
    }

    fun setState(state: SwerveModuleState) {
        state.optimize(currentAngle)

        driveMotor.closedLoopController.setSetpoint(
            state.speedMetersPerSecond / SwerveConstants.WHEEL_RADIUS,
            SparkBase.ControlType.kVelocity
        )
        angleMotor.closedLoopController.setSetpoint(
            state.angle.radians,
            SparkBase.ControlType.kPosition
        )
    }

    fun getRotation(): Rotation2d {
        return Rotation2d.fromRadians(angleMotor.encoder.position)
    }

    fun getSpeed(): Double {
        return driveMotor.encoder.velocity * SwerveConstants.WHEEL_RADIUS
    }

    fun getState(): SwerveModuleState {
        return SwerveModuleState(
            driveMotor.encoder.velocity * SwerveConstants.WHEEL_RADIUS,
            Rotation2d.fromRadians(angleMotor.encoder.position)
        )
    }

    fun seedPosition() {
        angleMotor.encoder.position = canCoder.position.value.`in`(Radians) -
                Units.rotationsToRadians(SwerveConstants.zeroRotations[index])
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