package sert2521.offseason2026.subsystems

import com.revrobotics.PersistMode
import com.revrobotics.ResetMode
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkMaxConfig
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import sert2521.offseason2026.constants.ElectronicIDs
import sert2521.offseason2026.constants.IntakeIndexerConstants

object IntakeIndexer : SubsystemBase() {
    private val rollerMotor = SparkMax(ElectronicIDs.INTAKE_ROLLER_ID, SparkLowLevel.MotorType.kBrushless)
    private val alignmentMotor = SparkMax(ElectronicIDs.INTAKE_ALIGN_ID, SparkLowLevel.MotorType.kBrushless)
    private val indexerMotor = SparkMax(ElectronicIDs.INDEXER_ID, SparkLowLevel.MotorType.kBrushless)

    private val beambreak = DigitalInput(ElectronicIDs.BEAMBREAK_ID)

    private val doubleTelemetries = mapOf(
        "_ Intake/Roller Motor/Speed" to { rollerMotor.encoder.velocity },
        "^ Intake/Roller Motor/Current" to { rollerMotor.outputCurrent },
        "_ Intake/Roller Motor/Voltage" to { rollerMotor.appliedOutput * rollerMotor.busVoltage },

        "_ Intake/Alignment Motor/Speed" to { alignmentMotor.encoder.velocity },
        "^ Intake/Alignment Motor/Current" to { alignmentMotor.outputCurrent },
        "_ Intake/Alignment Motor/Voltage" to { alignmentMotor.appliedOutput * rollerMotor.busVoltage },

        "_ Intake/Indexer Motor/Speed" to { indexerMotor.encoder.velocity },
        "^ Intake/Indexer Motor/Current" to { indexerMotor.outputCurrent },
        "_ Intake/Indexer Motor/Voltage" to { indexerMotor.appliedOutput * rollerMotor.busVoltage }
    )

    private val booleanTelemetries = mapOf(
        "* Intake/Beambreak Active (not blocked)" to { getBeambreakClear() },
    )

    init {
        val rollerConfig = SparkMaxConfig()
            .inverted(false)
            .smartCurrentLimit(40)
            .idleMode(SparkBaseConfig.IdleMode.kBrake)

        val alignmentConfig = SparkMaxConfig().apply(rollerConfig)

        val indexerConfig = SparkMaxConfig().apply(rollerConfig)

        rollerMotor.configure(
            rollerConfig,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters
        )
        alignmentMotor.configure(
            alignmentConfig,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters
        )
        indexerMotor.configure(
            indexerConfig,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters
        )

        defaultCommand = stop()
    }

    private fun setVoltages(roller: Double, align: Double, indexer: Double) {
        rollerMotor.setVoltage(roller)
        alignmentMotor.setVoltage(align)
        indexerMotor.setVoltage(indexer)
    }

    private fun getBeambreakClear(): Boolean {
        return beambreak.get()
    }

    fun intake(): Command {
        return run {
            setVoltages(
                IntakeIndexerConstants.INTAKING_ROLLER_VOLTAGE,
                IntakeIndexerConstants.INTAKING_ALIGNMENT_VOLTAGE,
                IntakeIndexerConstants.INTAKING_INDEXER_VOLTAGE
            )
        }.until {
            !getBeambreakClear()
        }
    }

    fun kick(): Command {
        return run {
            setVoltages(
                IntakeIndexerConstants.KICKING_ROLLER_VOLTAGE,
                IntakeIndexerConstants.KICKING_ALIGNMENT_VOLTAGE,
                IntakeIndexerConstants.KICKING_INDEXER_VOLTAGE
            )
        }.withTimeout(IntakeIndexerConstants.KICK_TIME)
    }

    fun reverse(): Command {
        return run {
            setVoltages(
                IntakeIndexerConstants.REVERSING_ROLLER_VOLTAGE,
                IntakeIndexerConstants.REVERSING_ALIGNMENT_VOLTAGE,
                IntakeIndexerConstants.REVERSING_INDEXER_VOLTAGE
            )
        }
    }

    fun recenter(): Command {
        return intake()
            .andThen(
                reverse().until {
                    getBeambreakClear()
                }
            ).andThen(intake())
    }

    fun stop(): Command {
        return run {
            setVoltages(0.0, 0.0, 0.0)
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