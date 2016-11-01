package me.minidigger.voxelgameslib.api.event.events.game;

import me.minidigger.voxelgameslib.api.game.Game;
import me.minidigger.voxelgameslib.api.user.User;

/**
 * Called when a user left a game
 */
public class GameLeaveEvent extends GameEvent {
    
    private User user;
    
    /**
     * @param game the game the user left
     * @param user the user that left the game
     */
    public GameLeaveEvent(Game game, User user) {
        super(game);
    }
    
    /**
     * @return the user that left the game
     */
    public User getUser() {
        return user;
    }
}
