package sert2521.offseason2026.constants

import edu.wpi.first.math.geometry.Translation2d

object SwerveConstants {
    val moduleNames = arrayOf("FL", "FR", "BL", "BR")

    val zeroRotations = arrayOf(
        0.0, // FL
        0.0, // FR
        0.0, // BL
        0.0  // BR
    )

    val moduleTranslations = arrayOf(
        Translation2d.kZero,
        Translation2d.kZero,
        Translation2d.kZero,
        Translation2d.kZero
    )

    const val DRIVE_CURRENT_LIMIT = 40

    const val ANGLE_P = 0.0
    const val ANGLE_D = 0.0

    const val DRIVE_S = 0.0
    const val DRIVE_V = 0.0

    // Max drive speed in m/s
    const val MAX_SPEED = 0.0

    const val DRIVE_GEARING = 5.9
    const val ANGLE_GEARING = 150.0 / 7.0

    // 2 inches, units here are meters
    const val WHEEL_RADIUS = 0.0508
}