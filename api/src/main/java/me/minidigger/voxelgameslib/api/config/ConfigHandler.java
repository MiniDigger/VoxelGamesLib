package me.minidigger.voxelgameslib.api.config;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import me.minidigger.voxelgameslib.api.exception.ConfigException;
import me.minidigger.voxelgameslib.api.handler.Handler;
import me.minidigger.voxelgameslib.api.log.LoggerHandler;

import lombok.extern.java.Log;

/**
 * the config handler handles all configs (uhh)
 */
@Log
@Singleton
public class ConfigHandler implements Handler, Provider<GlobalConfig> {
    
    @Inject
    @Named("ConfigFolder")
    private File configFolder;
    
    @Inject
    private Gson gson;
    @Inject
    private LoggerHandler logHandler;
    
    private File globalConfigFile;
    private GlobalConfig globalConfig;
    
    @Override
    public void start() {
        globalConfigFile = new File(configFolder, "config.json");
        if (!globalConfigFile.exists()) {
            log.warning("Did not found global config, saving default");
            globalConfig = GlobalConfig.getDefault();
            saveConfig(globalConfigFile, globalConfig);
        } else {
            log.info("Loading global config");
            globalConfig = loadConfig(globalConfigFile, GlobalConfig.class);
            
            if (checkMigrate(globalConfig)) {
                migrate(globalConfigFile, globalConfig);
            }
    
            // setting of the log level. its placed here since the log handler is loaded before the config
            try {
                logHandler.setLevel(Level.parse(globalConfig.logLevel));
            } catch (IllegalArgumentException ex) {
                log.warning("Unknown log level " + globalConfig.logLevel + " speficied via config, setting back to INFO");
                logHandler.setLevel(Level.INFO);
            }
        }
    }
    
    /**
     * Checks if the config needs to be migrated
     *
     * @param config the config that should be checked
     * @return if the config needs to be migrated
     */
    public boolean checkMigrate(@Nonnull Config config) {
        return config.getConfigVersion() != config.getCurrentVersion();
    }
    
    /**
     * Migrates the config to a new config version.
     *
     * @param configFile the file to migrate
     * @param config     the config to migrate
     * @throws ConfigException if there was an error while creating an backup
     */
    public void migrate(@Nonnull File configFile, @Nonnull Config config) {
        log.info("Migrating config from v" + config.getCurrentVersion() + " to v" + config.getConfigVersion());
        try {
            File backup = new File(configFile.getParent(), configFile.getName() + ".v" + config.getCurrentVersion() + ".backup");
            Files.copy(configFile, backup);
            log.info("Saved backup to " + backup.getAbsolutePath());
        } catch (IOException e) {
            throw new ConfigException("Error while migrating config", e);
        }
        
        config.setCurrentVersion(config.getConfigVersion());
        saveConfig(configFile, config);
        log.info("Done migrating");
    }
    
    @Override
    public void stop() {
        
    }
    
    /**
     * (Re)Loads the config
     *
     * @param clazz      the class of the config
     * @param configFile the file to load
     * @return the loaded config
     * @throws ConfigException if something went wrong
     */
    @Nonnull
    public <T extends Config> T loadConfig(@Nonnull File configFile, @Nonnull Class<T> clazz) {
        log.finer("Loading " + clazz.getName() + " from " + configFile.getAbsolutePath());
        try {
            return gson.fromJson(new JsonReader(new FileReader(configFile)), clazz);
        } catch (Exception e) {
            throw new ConfigException("Error while loading config", e);
        }
    }
    
    /**
     * saves the config
     *
     * @param configFile the file that should be saved to
     * @param config     the config that should be saved
     * @throws ConfigException if something went wrong
     */
    public void saveConfig(@Nonnull File configFile, @Nonnull Config config) {
        log.finer("Saving " + config.getClass().getName() + " to " + configFile.getAbsolutePath());
        if (!configFile.exists()) {
            try {
                if (configFile.getParentFile() != null) {
                    configFile.getParentFile().mkdirs();
                }
                configFile.createNewFile();
            } catch (Exception e) {
                throw new ConfigException("Error while creating config file. Does that server has rw-rights to '" + configFile.getAbsolutePath() + "'?", e);
            }
        }
        
        try {
            Writer writer = new FileWriter(configFile, false);
            gson.toJson(config, writer);
            writer.close();
        } catch (Exception e) {
            throw new ConfigException("Error while saving config", e);
        }
    }
    
    @Override
    public GlobalConfig get() {
        return globalConfig;
    }
}
