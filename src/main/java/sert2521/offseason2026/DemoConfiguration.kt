package sert2521.offseason2026

import sert2521.offseason2026.constants.WristConstants

object DemoConfiguration {
    const val ENABLE_NETWORK_CONFIG = true
    const val PDH_TELEMETRY = false

    // This is basically how much telemetry there is
    // Modes are: HIGH, MEDIUM, LOW, NONE
    var TELEMETRY_VERBOSITY = Telemetry.Verbosity.NONE

    // Meters Per Second
    var DRIVE_SPEED = 0.0
    // Radians Per Second
    var ROTATION_SPEED = 0.0

    // RPM
    var FLYWHEEL_SPEED = 0.0
    var FLYWHEEL_SPEED_IS_DEFAULT = true

    // Rotations
    var WRIST_MAX_HEIGHT = WristConstants.AMP_POSITION
    var ENABLE_WRIST = true
    var ENABLE_WRIST_TO_SLIDER = true

    var ENABLE_DRIVE = true
}