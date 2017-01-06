package me.minidigger.voxelgameslib.api.game;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.inject.Injector;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import me.minidigger.voxelgameslib.api.event.VGLEventHandler;
import me.minidigger.voxelgameslib.api.event.events.game.GameStartEvent;
import me.minidigger.voxelgameslib.api.exception.GameModeNotAvailableException;
import me.minidigger.voxelgameslib.api.exception.GameStartException;
import me.minidigger.voxelgameslib.api.handler.Handler;
import me.minidigger.voxelgameslib.api.tick.TickHandler;
import me.minidigger.voxelgameslib.api.user.User;

import lombok.extern.java.Log;

/**
 * Handles all {@link Game} instances and all {@link GameMode}s.
 */
@Log
@Singleton
public class GameHandler implements Handler {

    @Inject
    private TickHandler tickHandler;
    @Inject
    private Injector injector;
    @Inject
    private VGLEventHandler eventHandler;
    @Inject
    @Named("GameDefinitionFolder")
    private File gameDefinitionFolder;
    @Inject
    private Gson gson;

    private final List<Game> games = new ArrayList<>();
    private final List<GameMode> modes = new ArrayList<>();
    private final List<GameDefinition> gameDefinitions = new ArrayList<>();

    @Override
    public void start() {
        loadGameDefinitons();
    }

    @Override
    public void stop() {
        games.forEach(Game::stop);
        games.clear();
    }

    /**
     * Loads all game definitions that are found in the game definition folder and registers a new,
     * custom gamemode if needed
     */
    public void loadGameDefinitons() {
        File[] files = gameDefinitionFolder.listFiles();
        if (files == null) {
            log.warning("Could not load game definitions: could not list files!");
            return;
        }

        for (File file : files) {
            if (!file.getName().endsWith(".json")) {
                continue;
            }
            try {
                GameDefinition definition = gson.fromJson(new JsonReader(new FileReader(file)), GameDefinition.class);
                if (definition != null) {
                    gameDefinitions.add(definition);
                    registerGameMode(definition.getGameMode());
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Could not load game definition " + file.getName(), e);
            }
        }

        log.info("Loaded " + gameDefinitions.size() + " game definitions");
    }

    /**
     * Registers a new {@link GameMode}. Fails silently if that {@link GameMode} is already
     * registered.<br>
     *
     * @param mode the new mode to be registered
     */
    public void registerGameMode(@Nonnull GameMode mode) {
        if (!modes.contains(mode)) {
            modes.add(mode);
        }
    }

    /**
     * Starts a new {@link Game} instance of that {@link GameMode}.
     *
     * @param mode the {@link GameMode} that should be started.
     * @return the started {@link Game}
     * @throws GameModeNotAvailableException if that {@link GameMode} is not registered on this
     *                                       server
     * @throws GameStartException            if something goes wrong while starting
     */
    @Nonnull
    public Game startGame(@Nonnull GameMode mode) {
        if (!modes.contains(mode)) {
            throw new GameModeNotAvailableException(mode);
        }

        Game game = injector.getInstance(mode.getGameClass());
        game.setUuid(UUID.randomUUID());
        games.add(game);

        Optional<GameDefinition> def = getGameDefinition(mode);
        if (def.isPresent()) {
            game.initGameFromDefinition(def.get());
        } else {
            game.initGameFromModule();

            GameDefinition definition = game.saveGameDefinition();
            File file = new File(gameDefinitionFolder, mode.getName() + ".json");
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Writer writer = new FileWriter(file, false);
                gson.toJson(definition, writer);
                writer.close();
            } catch (Exception ex) {
                log.log(Level.WARNING, "Could not save game definition to file " + file.getAbsolutePath(), ex);
            }
        }
    
        // registering calles start
        tickHandler.registerTickable(game);

        eventHandler.callEvent(new GameStartEvent(game));

        return game;
    }

    /**
     * @return a list with all registered gamemodes
     */
    @Nonnull
    public List<GameMode> getGameModes() {
        return modes;
    }

    /**
     * Gets a list of all games the user is playing in or spectating (if spectating is set to true)
     *
     * @param user     the user which games should be returned
     * @param spectate if we should include games where the user spectates
     * @return the games of that users
     */
    @Nonnull
    public List<Game> getGames(@Nonnull User user, boolean spectate) {
        List<Game> result = new ArrayList<>();
        for (Game game : games) {
            if (game.isPlaying(user)) {
                result.add(game);
                continue;
            }

            if (spectate && game.isSpectating(user)) {
                result.add(game);
            }
        }
        return result;
    }

    /**
     * Gets a game definition based on the game mode
     *
     * @param mode the game mode
     * @return the game definition that matches the game mode, if present
     */
    @Nonnull
    public Optional<GameDefinition> getGameDefinition(GameMode mode) {
        return gameDefinitions.stream().filter(gameDefinition -> gameDefinition.getGameMode().equals(mode)).findAny();
    }
}
