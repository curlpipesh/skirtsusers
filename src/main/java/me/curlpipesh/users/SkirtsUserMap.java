package me.curlpipesh.users;

import lombok.Getter;
import lombok.NonNull;
import me.curlpipesh.util.plugin.SkirtsPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author audrey
 * @since 12/22/15.
 */
public class SkirtsUserMap {
    @Getter
    private List<SkirtsUser> skirtsUsers;

    private final SkirtsPlugin plugin;

    SkirtsUserMap(@NonNull final SkirtsPlugin plugin) {
        this.plugin = plugin;
        skirtsUsers = new ArrayList<>();
    }

    public void addUser(@NonNull final SkirtsUser user) {
        if(skirtsUsers.stream().filter(u -> u.getUuid().equals(user.getUuid())).count() == 0) {
            plugin.getLogger().info("Added SkirtsUser '" + user.getLastName() + "'");
            skirtsUsers.add(user);
        } else {
            plugin.getLogger().warning("Not adding duplicate SkirtsUser: " + user.getLastName());
        }
    }

    public Optional<SkirtsUser> getUser(@NonNull final UUID uuid) {
        return skirtsUsers.stream().filter(u -> u.getUuid().equals(uuid)).findFirst();
    }

    public Optional<SkirtsUser> getUserByName(@NonNull final String name) {
        return skirtsUsers.stream().filter(u -> u.getLastName().equalsIgnoreCase(name)).findFirst();
    }
}
