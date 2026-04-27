package sert2521.offseason2026

import sert2521.offseason2026.constants.WristConstants

object DemoConfiguration {
    const val ENABLE_NETWORK_CONFIG = true
    const val PDH_TELEMETRY = false

    // This is basically how much telemetry there is
    // Modes are: HIGH, MEDIUM, LOW, NONE
    var telemetryVerbosity = Telemetry.Verbosity.NONE

    // Meters Per Second
    var driveSpeed = 0.0
    // Radians Per Second
    var rotationSpeed = 0.0

    // RPM
    var flywheelSpeed = 0.0
    var flywheelSpeedIsDefault = true

    // Rotations
    var wristMaxHeight = WristConstants.AMP_POSITION
    var enableWrist = true
    var enableWristToSlider = true

    var enableDrive = true
}