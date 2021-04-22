package Simulation;

/**
 *
 * @author simon
 */
public interface Mask {

    public enum MaskProtection {
        FFP1, FFP2, FFP3
    }

    public enum MaskStatus {
        DOWN, UP
    }

    public MaskProtection getProtection();

    public MaskStatus getStatus();

    public void maskDown();
}
