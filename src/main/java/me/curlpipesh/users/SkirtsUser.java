package me.curlpipesh.users;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author audrey
 * @since 12/22/15.
 */
@Data
@RequiredArgsConstructor
public class SkirtsUser {
    @NonNull
    private final UUID uuid;
    @NonNull
    private String lastName;
    @NonNull
    private int kills;
    @NonNull
    private int deaths;
    @NonNull
    private InetAddress ip;
}
