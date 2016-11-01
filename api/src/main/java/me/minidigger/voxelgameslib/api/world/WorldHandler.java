package me.minidigger.voxelgameslib.api.world;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import me.minidigger.voxelgameslib.api.config.ConfigHandler;
import me.minidigger.voxelgameslib.api.exception.MapException;
import me.minidigger.voxelgameslib.api.exception.WorldException;
import me.minidigger.voxelgameslib.api.handler.Handler;
import me.minidigger.voxelgameslib.api.map.Map;
import me.minidigger.voxelgameslib.api.utils.FileUtils;

import lombok.Getter;
import lombok.extern.java.Log;

/**
 * Handles the worlds (loading, unloading etc)
 */
@Log
public abstract class WorldHandler implements Handler, Provider<WorldConfig> {
    
    @Inject
    @Named("WorldsFolder")
    private File worldsFolder;
    
    @Getter
    @Inject
    @Named("WorldContainer")
    private File worldContainer;
    
    @Inject
    private ConfigHandler configHandler;
    
    @Inject
    private Gson gson;
    
    private WorldConfig config;
    private File configFile;
    
    private final List<Map> maps = new ArrayList<>();
    
    /**
     * Gets a map from a list of loaded maps
     *
     * @param name the map to search for
     * @return the map, if present
     */
    @Nonnull
    public Optional<Map> getMap(@Nonnull String name) {
        return maps.stream().filter(map -> map.getWorldName().equalsIgnoreCase(name)).findAny();
    }
    
    /**
     * Tries to load the map data for a name
     *
     * @param name the name of the map to load
     * @return the loaded map
     * @throws MapException when
     */
    @Nonnull
    public Map loadMap(@Nonnull String name) {
        if (!getMap(name).isPresent()) {
            if (!config.maps.contains(name)) {
                throw new MapException("Unknown map " + name + ". Did you register it into the world config?");
            }
            
            try {
                for (Path path : FileSystems.newFileSystem(new File(worldsFolder, name + ".zip").toURI(), Collections.emptyMap()).getRootDirectories()) {
                    if (path.endsWith("map.json")) {
                        return gson.fromJson(new JsonReader(new FileReader(path.toFile())), Map.class);
                    }
                }
            } catch (IOException e) {
                throw new MapException("Error while trying to load map config", e);
            }
        }
        
        throw new MapException("Could not load map config for map " + name + ". Does it has a map.json?");
    }
    
    /**
     * Loads a world. Needs to copy the file from the repo, unzip it and let the implementation load
     * it <br><b>Always needs to call super! Super needs to be called first (because it copies the
     * world)</b>
     *
     * @param map the map that should be loaded
     * @throws WorldException something goes wrong
     */
    public void loadWorld(@Nonnull Map map) {
        map.setLoaded(true);
        
        try {
            ZipFile zip = new ZipFile(new File(worldsFolder, map.getWorldName() + ".zip"));
            zip.extractAll(new File(worldContainer, map.getWorldName()).getAbsolutePath());
        } catch (ZipException e) {
            throw new WorldException("Could not unzip world " + map.getWorldName() + ".", e);
        }
    }
    
    /**
     * Unloads a world. Needs to lets the implementation unload the world and delete the folder
     * <br><b>Always needs to call super! Super needs to be called last (because it deletes the
     * world folder)</b>
     *
     * @param map the map that should be unloaded.
     */
    public void unloadWorld(@Nonnull Map map) {
        map.setLoaded(false);
        
        FileUtils.delete(new File(worldContainer, map.getWorldName()));
    }
    
    @Override
    public void start() {
        configFile = new File(worldsFolder, "worlds.json");
        if (!worldsFolder.exists()) {
            log.warning("Could not find worlds folder " + worldsFolder.getAbsolutePath() + ". Creating...");
            worldsFolder.mkdirs();
        }
        
        if (!configFile.exists()) {
            log.warning("Did not found world config, saving default");
            config = WorldConfig.getDefault();
            configHandler.saveConfig(configFile, config);
        } else {
            log.info("Loading world config");
            config = configHandler.loadConfig(configFile, WorldConfig.class);
            
            if (configHandler.checkMigrate(config)) {
                configHandler.migrate(configFile, config);
            }
        }
    }
    
    @Override
    public void stop() {
        
    }
    
    @Nonnull
    @Override
    public WorldConfig get() {
        return config;
    }
    
    /**
     * Loads a local world
     *
     * @param name the world to load
     * @throws WorldException if the world is not found or something else goes wrong
     */
    public abstract void loadLocalWorld(@Nonnull String name);
    
    /**
     * Unloads a local world
     *
     * @param name the world to load
     * @throws WorldException if the world is not found or something else goes wrong
     */
    public abstract void unloadLocalWorld(@Nonnull String name);
    
    /**
     * @return the folder where the playerable worlds are saved in (think about it as a repo for
     * worlds/maps)
     */
    @Nonnull
    public File getWorldsFolder() {
        return worldsFolder;
    }
    
    /**
     * saves the worldconfig
     */
    public void saveConfig() {
        configHandler.saveConfig(configFile, config);
    }
}
