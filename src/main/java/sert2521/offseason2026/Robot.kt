package sert2521.offseason2026

import edu.wpi.first.hal.FRCNetComm.tInstances
import edu.wpi.first.hal.FRCNetComm.tResourceType
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.TimedRobot
import edu.wpi.first.wpilibj.util.WPILibVersion
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import sert2521.offseason2026.subsystems.Wrist
import sert2521.offseason2026.subsystems.drivetrain.Drivetrain

/**
 * The VM is configured to automatically run this object (which basically functions as a singleton class),
 * and to call the functions corresponding to each mode, as described in the TimedRobot documentation.
 * This is written as an object rather than a class since there should only ever be a single instance, and
 * it cannot take any constructor arguments. This makes it a natural fit to be an object in Kotlin.
 *
 * If you change the name of this object or its package after creating this project, you must also update
 * the `Main.kt` file in the project. (If you use the IDE's Rename or Move refactorings when renaming the
 * object or package, it will get changed everywhere.)
 */
object Robot : TimedRobot() {
    override fun robotInit() {
        Input
        Drivetrain
    }

    override fun robotPeriodic() {
        CommandScheduler.getInstance().run()
        Telemetry.updateTelemetry()
    }

    override fun teleopInit() {
        CommandScheduler.getInstance().cancelAll()
        CommandScheduler.getInstance().schedule(Wrist.rezero())
    }

    override fun teleopPeriodic() {}
}