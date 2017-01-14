package me.minidigger.voxelgameslib.api.feature.features;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

import me.minidigger.voxelgameslib.api.event.EventListener;
import me.minidigger.voxelgameslib.api.event.events.game.GameJoinEvent;
import me.minidigger.voxelgameslib.api.event.events.user.UserRespawnEvent;
import me.minidigger.voxelgameslib.api.feature.AbstractFeature;
import me.minidigger.voxelgameslib.api.feature.Feature;
import me.minidigger.voxelgameslib.api.map.Map;
import me.minidigger.voxelgameslib.api.map.Marker;
import me.minidigger.voxelgameslib.api.map.Vector3D;
import me.minidigger.voxelgameslib.api.user.User;

/**
 * Handles (re)spawning
 */
public class SpawnFeature extends AbstractFeature {
    
    @Expose
    private boolean isRespawn = true;
    @Expose
    private boolean isInitialSpawn = true;
    
    private List<Vector3D> spawns = new ArrayList<>();
    private Map map;
    
    @Override
    public void start() {
        map = getPhase().getFeature(MapFeature.class).getMap();
        for (Marker marker : map.getMarkers()) {
            if (marker.getData().startsWith("spawn")) {
                spawns.add(marker.getLoc());
            }
        }
        if (isInitialSpawn) {
            for (User user : getPhase().getGame().getPlayers()) {
                user.teleport(map.getWorldName(), getSpawn(user));
            }
        }
    }
    
    /**
     * Generates a spawn location for that user
     *
     * @param user the user which wants to spawn
     * @return the location he should spawn
     */
    public Vector3D getSpawn(@Nonnull User user) {
        //TODO super fancy spawn algorithm
        return spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()));
    }
    
    @EventListener
    @SuppressWarnings("JavaDoc")
    public void onRespawn(UserRespawnEvent e) {
        if (getPhase().getGame().isPlaying(e.getUser())) {
            e.setRespawnLocation(getSpawn(e.getUser()));
        }
    }
    
    @EventListener
    @SuppressWarnings("JavaDoc")
    public void onJoin(GameJoinEvent e) {
        if (getPhase().getGame().isPlaying(e.getUser())) {
            e.getUser().teleport(map.getWorldName(), getSpawn(e.getUser()));
        }
    }
    
    @Override
    public void stop() {
    
    }
    
    @Override
    public void tick() {
    
    }
    
    @Override
    public void init() {
    
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Feature>[] getDependencies() {
        return new Class[]{MapFeature.class};
    }
    
    /**
     * @param respawn if true, players will respawn after they died
     */
    public void setRespawn(boolean respawn) {
        isRespawn = respawn;
    }
    
    /**
     * @return if true, players will respawn after they died
     */
    public boolean isRespawn() {
        return isRespawn;
    }
    
    /**
     * @return if true, players will be spawned at the start of this phase
     */
    public boolean isInitialSpawn() {
        return isInitialSpawn;
    }
    
    /**
     * @param initialSpawn if true, players will be spawned at the start of this phase
     */
    public void setInitialSpawn(boolean initialSpawn) {
        isInitialSpawn = initialSpawn;
    }
}
