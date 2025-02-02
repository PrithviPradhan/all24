package org.team100.lib.hid;

/**
 * Represents the HID used by the "operator" role, which typically controls
 * everything other than the drivetrain.
 */
public interface OperatorControl {

    default String getHIDName() {
        return "No HID Found!!";
    }

    default boolean doSomething() {
        return false;
    }

    default boolean index() {
        return false;
    }

    default boolean shooter() {
        return false;
    }

    default boolean pivotToAmpPosition(){
        return false;
    }

    default double shooterSpeed() {
        return 0;
    }

    default boolean outtake() {
        return false;
    }

    default boolean intake() {
        return false;
    }

    default boolean indexState() {
        return false;
    }

    /** @return position in range [0,1] */
    default double ampPosition(){
        return 0;
    }

    default double climberState() {
        return 0;
    }


    default double lower() {
        return 0;
    }

    default double upper() {
        return 0;
    }

    default double elevator() {
        return 0;
    }

    // this exists to bind to commands we don't want to run,
    // but we don't want them to rot either.
    default boolean never() {
        return false;
    }

    default boolean selfTestEnable() {
        return false;
    }
}
