package me.curlpipesh.users;

import lombok.Getter;
import lombok.NonNull;
import me.curlpipesh.util.plugin.SkirtsPlugin;

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
public class SkirtsUserMap {
    /**
     * All users known to the plugin
     */
    @Getter
    private final List<SkirtsUser> skirtsUsers;

    /**
     * Plugin using this.
     */
    private final SkirtsPlugin plugin;

    SkirtsUserMap(@NonNull final SkirtsPlugin plugin) {
        this.plugin = plugin;
        skirtsUsers = new ArrayList<>();
    }

    /**
     * Add a user to the map, or ignore if dup.
     *
     * @param user The user to add
     */
    public void addUser(@NonNull final SkirtsUser user) {
        if(skirtsUsers.stream().filter(u -> u.getUuid().equals(user.getUuid())).count() == 0) {
            plugin.getLogger().info("Added SkirtsUser '" + user.getLastName() + '\'');
            skirtsUsers.add(user);
        } else {
            plugin.getLogger().warning("Not adding duplicate SkirtsUser: " + user.getLastName());
        }
    }

    /**
     * Gets the user with the given UUID
     *
     * @param uuid The UUID to find
     * @return The user with that UUID
     */
    public Optional<SkirtsUser> getUser(@NonNull final UUID uuid) {
        return skirtsUsers.stream().filter(u -> u.getUuid().equals(uuid)).findFirst();
    }

    /**
     * Gets the user with the given name. Checks against last known name for
     * every known user.
     *
     * @param name The name to look for
     * @return The user with that name
     */
    public Optional<SkirtsUser> getUserByName(@NonNull final String name) {
        return skirtsUsers.stream().filter(u -> u.getLastName().equalsIgnoreCase(name)).findFirst();
    }
}
