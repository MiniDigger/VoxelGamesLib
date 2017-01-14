package me.minidigger.voxelgameslib.api.exception;

import javax.annotation.Nonnull;

/**
 * Used for problems related to scoreboards
 */
public class ScoreboardException extends VoxelGameLibException {
    
    /**
     * Used for problems related to scoreboards
     *
     * @param message the message that explains the problem
     */
    public ScoreboardException(@Nonnull String message) {
        super(message);
    }
}
