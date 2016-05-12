package lgbt.audrey.users.user;

import lombok.Getter;
import lombok.NonNull;
import lgbt.audrey.util.plugin.AudreyPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Tracks users who have logged in since the last restart
 *
 * @author audrey
 * @since 12/22/15.
 */
public class AudreyUserMap {
    /**
     * All users known to the plugin
     */
    @Getter
    private final List<AudreyUser> audreyUsers;

    /**
     * Plugin using this.
     */
    private final AudreyPlugin plugin;

    public AudreyUserMap(@NonNull final AudreyPlugin plugin) {
        this.plugin = plugin;
        audreyUsers = new ArrayList<>();
    }

    /**
     * Add a user to the map, or ignore if dup.
     *
     * @param user The user to add
     */
    public void addUser(@NonNull final AudreyUser user) {
        if(audreyUsers.stream().filter(u -> u.getUuid().equals(user.getUuid())).count() == 0) {
            plugin.getLogger().info("Added AudreyUser '" + user.getLastName() + '\'');
            audreyUsers.add(user);
        } else {
            plugin.getLogger().warning("Not adding duplicate AudreyUser: " + user.getLastName());
        }
    }

    /**
     * Gets the user with the given UUID
     *
     * @param uuid The UUID to find
     * @return The user with that UUID
     */
    public Optional<AudreyUser> getUser(@NonNull final UUID uuid) {
        return audreyUsers.stream().filter(u -> u.getUuid().equals(uuid)).findFirst();
    }

    /**
     * Gets the user with the given name. Checks against last known name for
     * every known user.
     *
     * @param name The name to look for
     * @return The user with that name
     */
    public Optional<AudreyUser> getUserByName(@NonNull final String name) {
        return audreyUsers.stream().filter(u -> u.getLastName().equalsIgnoreCase(name)).findFirst();
    }
}
