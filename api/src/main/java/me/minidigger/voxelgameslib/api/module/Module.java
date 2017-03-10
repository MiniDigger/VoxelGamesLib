package me.minidigger.voxelgameslib.api.module;

import javax.annotation.Nonnull;

/**
 * A module as a extension to the framework. it could be a servermod implementation, a gamemode or just a general lib.
 */
public interface Module {

    /**
     * Called when this module is enabled
     */
    void enable();

    /**
     * Called when this module is disabled
     */
    void disable();

    /**
     * @return the info annotation for this module, which contains additional info like the name or the author
     */
    @Nonnull
    ModuleInfo getModuleInfo();
}
