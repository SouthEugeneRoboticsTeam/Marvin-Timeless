package sert2521.offseason2026

import edu.wpi.first.networktables.*
import edu.wpi.first.wpilibj.PowerDistribution
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.Commands
import sert2521.offseason2026.subsystems.Flywheels
import sert2521.offseason2026.subsystems.IntakeIndexer
import sert2521.offseason2026.subsystems.Wrist
import sert2521.offseason2026.subsystems.drivetrain.Drivetrain

object Telemetry {
    enum class Verbosity {
        HIGH,
        MEDIUM,
        LOW,
        NONE
    }

    private val driveSpeedSubscriber: DoubleSubscriber?
    private val rotationSpeedSubscriber: DoubleSubscriber?

    private val flywheelSpeedSubscriber: DoubleSubscriber?
    private val flywheelSpeedIsDefaultSubscriber: BooleanSubscriber?

    private val wristMaxHeightSubscriber: DoubleSubscriber?
    private val enableWristSubscriber: BooleanSubscriber?
    private val enableWristToSliderSubscriber: BooleanSubscriber?

    private val enableDriveSubscriber: BooleanSubscriber?
    private val applyConfigCommand: Command?
    private val telemetryVerbosityChooser: SendableChooser<Verbosity>?


    private val allDoubleTelemetries = mutableMapOf<String, Double>()
    private val allBooleanTelemetries = mutableMapOf<String, Boolean>()

    // "*" means necessary, even while directly in demo
    // "^" means priority, meaning useful data to know some of the time
    // "_" means debug values, meaning data only really useful for debugging
    private val includeLevels = when (DemoConfiguration.telemetryVerbosity) {
        Verbosity.HIGH -> "*^_"
        Verbosity.MEDIUM -> "*^"
        Verbosity.LOW -> "*"
        Verbosity.NONE -> ""
    }

    init {
        if (DemoConfiguration.PDH_TELEMETRY) {
            SmartDashboard.putData(PowerDistribution(20, PowerDistribution.ModuleType.kRev))
        }

        if (DemoConfiguration.ENABLE_NETWORK_TUNING) {
            driveSpeedSubscriber = DoubleTopic(
                NetworkTableInstance.getDefault()
                    .getDoubleTopic("Config/Drive Speed")
            ).getEntry(DemoConfiguration.driveSpeed)
            rotationSpeedSubscriber = DoubleTopic(
                NetworkTableInstance.getDefault()
                    .getDoubleTopic("Config/Rotation Speed")
            ).getEntry(DemoConfiguration.rotationSpeed)
            flywheelSpeedSubscriber = DoubleTopic(
                NetworkTableInstance.getDefault()
                    .getDoubleTopic("Config/Flywheel Speed")
            ).getEntry(DemoConfiguration.flywheelSpeed)
            flywheelSpeedIsDefaultSubscriber = BooleanTopic(
                NetworkTableInstance.getDefault()
                    .getBooleanTopic("Config/Flywheels Always Rev")
            ).getEntry(DemoConfiguration.flywheelSpeedIsDefault)
            wristMaxHeightSubscriber = DoubleTopic(
                NetworkTableInstance.getDefault()
                    .getDoubleTopic("Config/Wrist Slider Max Height")
            ).getEntry(DemoConfiguration.wristMaxHeight)
            enableWristSubscriber = BooleanTopic(
                NetworkTableInstance.getDefault()
                    .getBooleanTopic("Config/Enable Wrist")
            ).getEntry(DemoConfiguration.enableWrist)
            enableWristToSliderSubscriber = BooleanTopic(
                NetworkTableInstance.getDefault()
                    .getBooleanTopic("Config/Enable Wrist Slider Control")
            ).getEntry(DemoConfiguration.enableWristToSlider)
            enableDriveSubscriber = BooleanTopic(
                NetworkTableInstance.getDefault()
                    .getBooleanTopic("Config/Enable Driving")
            ).getEntry(DemoConfiguration.enableDrive)

            applyConfigCommand = Commands.runOnce({ applyConfig() })
            applyConfigCommand.name = "Apply Config"

            telemetryVerbosityChooser = SendableChooser<Verbosity>()

            telemetryVerbosityChooser.addOption("HIGH", Verbosity.HIGH)
            telemetryVerbosityChooser.addOption("MEDIUM", Verbosity.MEDIUM)
            telemetryVerbosityChooser.addOption("LOW", Verbosity.LOW)
            telemetryVerbosityChooser.addOption("NONE", Verbosity.NONE)

            SmartDashboard.putData("Config/Apply Config", applyConfigCommand)
            SmartDashboard.putData("Config/Telemetry Verbosity", telemetryVerbosityChooser)
        } else {
            driveSpeedSubscriber = null
            rotationSpeedSubscriber = null
            flywheelSpeedSubscriber = null
            flywheelSpeedIsDefaultSubscriber = null
            wristMaxHeightSubscriber = null
            enableWristSubscriber = null
            enableWristToSliderSubscriber = null
            enableDriveSubscriber = null
            applyConfigCommand = null
            telemetryVerbosityChooser = null
        }
    }

    fun updateTelemetry() {
        Wrist.updateTelemetry(allDoubleTelemetries, allBooleanTelemetries, includeLevels)
        IntakeIndexer.updateTelemetry(allDoubleTelemetries, allBooleanTelemetries, includeLevels)
        Flywheels.updateTelemetry(allDoubleTelemetries, allBooleanTelemetries, includeLevels)
        Drivetrain.updateTelemetry(allDoubleTelemetries, allBooleanTelemetries, includeLevels)

        allDoubleTelemetries.keys.forEach {
            SmartDashboard.putNumber(it, allDoubleTelemetries[it]!!)
        }

        allBooleanTelemetries.keys.forEach {
            SmartDashboard.putBoolean(it, allBooleanTelemetries[it]!!)
        }
    }

    private fun applyConfig() {
        DemoConfiguration.driveSpeed = driveSpeedSubscriber!!.get()
        DemoConfiguration.rotationSpeed = rotationSpeedSubscriber!!.get()
        DemoConfiguration.flywheelSpeed = flywheelSpeedSubscriber!!.get()
        DemoConfiguration.flywheelSpeedIsDefault = flywheelSpeedIsDefaultSubscriber!!.get()
        DemoConfiguration.wristMaxHeight = wristMaxHeightSubscriber!!.get()
        DemoConfiguration.enableWrist = enableWristSubscriber!!.get()
        DemoConfiguration.enableWristToSlider = enableWristToSliderSubscriber!!.get()
        DemoConfiguration.enableDrive = enableDriveSubscriber!!.get()
        DemoConfiguration.telemetryVerbosity = telemetryVerbosityChooser!!.selected

        Drivetrain.setDefaultCommand()
        Flywheels.setDefaultCommand()
        Wrist.setDefaultCommand()
    }
}