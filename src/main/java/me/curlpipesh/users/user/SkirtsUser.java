package me.curlpipesh.users.user;

import lombok.Data;
import lombok.NonNull;
import me.curlpipesh.users.attribute.Attribute;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Representation of a user.
 *
 * @author audrey
 * @since 12/22/15.
 */
@Data
@SuppressWarnings({"FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"})
public class SkirtsUser {
    /**
     * UUID of the user
     */
    @NonNull
    private final UUID uuid;

    /**
     * Last known username for the user
     */
    @NonNull
    private String lastName;

    /**
     * Number of kills the user has
     */
    @NonNull
    private int kills;

    /**
     * Number of deaths the user has
     */
    @NonNull
    private int deaths;

    /**
     * Last known IP for the user
     */
    @NonNull
    private InetAddress ip;

    private final Map<String, Attribute<?>> attributes;

    public SkirtsUser(final UUID uuid, final String lastName, final int kills, final int deaths, final InetAddress ip) {
        this.uuid = uuid;
        this.lastName = lastName;
        this.kills = kills;
        this.deaths = deaths;
        this.ip = ip;
        attributes = new ConcurrentHashMap<>();
    }

    public void addAttribute(final String name, final Attribute<?> attribute) {
        attributes.put(name, attribute);
    }
}
