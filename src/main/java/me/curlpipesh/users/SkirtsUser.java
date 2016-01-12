package me.curlpipesh.users;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Representation of a user.
 *
 * TODO: Attributes, so that we stop making 57 different user maps for different plugins
 *
 * @author audrey
 * @since 12/22/15.
 */
@Data
@RequiredArgsConstructor
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
}
