/**
 *
 */
package ch.jason.android.noiseMonitor.object;

/**
 * Possible alert modes.
 *
 * @author j
 */
public enum AlertMode {

    TELEPHONE(0), SMS(1), TEL_SMS(2), BEEP(3);

    private int mode;

    /**
     * Instantiates a new alert mode.
     *
     * @param mode the mode
     */
    private AlertMode(int mode) {
        this.mode = mode;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public int getMode() {
        return mode;
    }

    /**
     * Parses the alert mode.
     *
     * @param mode the mode to parse
     * @return the alert mode
     * @throws InvalidAlertModeException when {@code mode} is unknown
     */
    public static AlertMode parseAlertMode(int mode)
            throws InvalidAlertModeException {
        switch (mode) {
            case 0:
                return TELEPHONE;
            case 1:
                return SMS;
            case 2:
                return TEL_SMS;
            case 3:
                return BEEP;
            default:
                throw new InvalidAlertModeException("Alert Mode unknown!!");
        }
    }

}
