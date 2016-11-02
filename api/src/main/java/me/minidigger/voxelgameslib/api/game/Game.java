package me.minidigger.voxelgameslib.api.game;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.minidigger.voxelgameslib.api.feature.Feature;
import me.minidigger.voxelgameslib.api.lang.LangKey;
import me.minidigger.voxelgameslib.api.phase.Phase;
import me.minidigger.voxelgameslib.api.tick.Tickable;
import me.minidigger.voxelgameslib.api.user.User;
import me.minidigger.voxelgameslib.libs.net.md_5.bungee.api.chat.BaseComponent;

/**
 * A {@link Game} is the representation of an instance of a {@link GameMode}. Handles everything
 * that is related to that {@link GameMode}: starting, stopping, {@link Phase}s etc.
 */
public interface Game extends Tickable {
    
    /**
     * @param uuid the uuid for this game
     */
    void setUuid(@Nonnull UUID uuid);
    
    /**
     * @return a unique identifier for this game
     */
    UUID getUuid();
    
    /**
     * initialises this game and all phases
     */
    void initGame();
    
    /**
     * Sends a message to every {@link me.minidigger.voxelgameslib.api.user.User} that
     * is related to this game. This could be a participant in the game or a spectator.
     *
     * @param message the message to be send
     */
    void broadcastMessage(@Nonnull BaseComponent... message);
    
    /**
     * Sends a message to everr User that
     * is related to this game. This could be a participant in the game or a spectator.
     *
     * @param key  the message to be send
     * @param args the arguments for the key
     */
    void broadcastMessage(@Nonnull LangKey key, Object... args);
    
    /**
     * Ends the current {@link Phase} and starts the next one.
     */
    void endPhase();
    
    /**
     * Ends the game, handles who has won.
     */
    void endGame();
    
    /**
     * @return the gamemode that is played in this game
     */
    @Nonnull
    GameMode getGameMode();
    
    /**
     * @return returns the {@link Phase} that is currently active
     */
    @Nonnull
    Phase getActivePhase();
    
    /**
     * Lets a user join this game
     *
     * @param user the user that wants to join this game
     */
    void join(@Nonnull User user);
    
    /**
     * lets a user spectate this game
     *
     * @param user the user which wants to spectate
     */
    void spectate(@Nonnull User user);
    
    /**
     * Lets a user leave this game
     *
     * @param user the user that wants to leave this game
     */
    void leave(@Nonnull User user);
    
    /**
     * Checks if that user is playing (not spectating!) this game
     *
     * @param user the user to check
     * @return if the user is playing this game
     */
    boolean isPlaying(@Nonnull User user);
    
    /**
     * Checks if that user is spectating (not playing!) this game
     *
     * @param user the user to check
     * @return if the user is spectating this game
     */
    boolean isSpectating(@Nonnull User user);
    
    /**
     * Creates a new feature class (using guice and stuff)
     *
     * @param featureClass the class of the feature that should be created
     * @param <T>          the feature
     * @param phase        the phase that the new feature should be attached to
     * @return the created feature instance
     */
    @Nonnull
    <T extends Feature> T createFeature(@Nonnull Class<T> featureClass, @Nonnull Phase phase);
    
    /**
     * Creates a new phase class (using guice and stuff)
     *
     * @param phaseClass the class of the phase that should be created
     * @param <T>        the phase
     * @return the created phase instance
     */
    @Nonnull
    <T extends Phase> T createPhase(@Nonnull Class<T> phaseClass);
    
    /**
     * @return the minimum amount of players for this game
     */
    int getMinPlayers();
    
    /**
     * @param minPlayers the minimum amount of players for this game
     */
    void setMinPlayers(int minPlayers);
    
    /**
     * @return the maximum amount of players for this game
     */
    int getMaxPlayers();
    
    /**
     * @param maxPlayers the maximum amount of players for this game
     */
    void setMaxPlayers(int maxPlayers);
    
    /**
     * @return the list of users that are currently playing this game
     */
    List<User> getPlayers();
    
    /**
     * @return the list of users that are currently spectating this game
     */
    List<User> getSpectators();
    
    /**
     * Saves a object with a key into the gamedata map
     *
     * @param key  the key
     * @param data the data
     */
    void putGameData(@Nonnull String key, @Nonnull Object data);
    
    /**
     * @param key the key to get the data for
     * @return the game data for that key, may be null
     */
    @Nullable
    Object getGameData(@Nonnull String key);
}
