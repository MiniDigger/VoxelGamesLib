package me.minidigger.voxelgameslib.api.exception;

import javax.annotation.Nonnull;

/**
 * Thrown when something goes wrong while generating dependency graphs
 */
public class DependencyGraphException extends VoxelGameLibException {

    /**
     * @param message the message
     */
    public DependencyGraphException(@Nonnull String message) {
        super(message);
    }
}
