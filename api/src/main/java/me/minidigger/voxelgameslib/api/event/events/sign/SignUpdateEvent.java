package me.minidigger.voxelgameslib.api.event.events.sign;

import java.util.Optional;

import me.minidigger.voxelgameslib.api.event.Cancelable;
import me.minidigger.voxelgameslib.api.event.Event;
import me.minidigger.voxelgameslib.api.map.Vector3D;
import me.minidigger.voxelgameslib.api.user.User;

import lombok.Getter;
import lombok.Setter;

/**
 * Called when a sign updates. this could be called via a regular update task or a send packet,
 * depending on server mod implementation. This can also be called when a user first places a sign.
 */
@Getter
public class SignUpdateEvent implements Event, Cancelable {

    private boolean canceled = false;

    @Getter
    private String world;
    @Getter
    private Vector3D location;
    @Setter
    @Getter
    private String[] text;

    private User user;

    /**
     * Creates a new event
     *
     * @param world    the world where the sign is located in
     * @param location the location of the sign
     * @param text     the original text
     */
    public SignUpdateEvent(String world, Vector3D location, String[] text) {
        this(world, location, text, null);
    }

    /**
     * Creates a new event
     *
     * @param world    the world where the sign is located in
     * @param location the location of the sign
     * @param text     the original text
     * @param user     the user that updated the text
     */
    public SignUpdateEvent(String world, Vector3D location, String[] text, User user) {
        this.world = world;
        this.location = location;
        this.text = text;
        this.user = user;
    }

    /**
     * Gets the user that caused this update but editing a sign, if present
     *
     * @return the user
     */
    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }
}
