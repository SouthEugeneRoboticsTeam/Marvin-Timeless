package sert2521.offseason2026

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import sert2521.offseason2026.constants.WristConstants

object DemoConfiguration {
    const val ENABLE_NETWORK_TUNING = true
    const val PDH_TELEMETRY = false

    // Meters Per Second
    var driveSpeed = 0.0

    // Radians Per Second
    var rotationSpeed = 0.0

    var flywheelSpeed = 0.0
    var flywheelSpeedIsDefault = true

    var wristMaxHeight = WristConstants.AMP_POSITION
    var enableWrist = true
    var enableWristToSlider = true

    var enableDrive = true

    // This is basically how much telemetry is there
    // Modes are: HIGH, MEDIUM, LOW, NONE
    var telemetryVerbosity = Telemetry.Verbosity.NONE
}