package lgbt.audrey.users.listeners;

import lombok.NonNull;
import lgbt.audrey.users.Users;
import lgbt.audrey.users.attribute.Attribute;
import lgbt.audrey.users.user.AudreyUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * @author audrey
 * @since 2/10/16.
 */
public class PlayerListener implements Listener {
    private final Users users;

    public PlayerListener(final Users users) {
        this.users = users;
    }

    // Deal with user login. If we don't have them already loaded in memory, grab them from the database.
    // If we can't find them in the database, assume that it's a new user and work from there
    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onPlayerLogin(@NonNull final PlayerJoinEvent event) {
        final Optional<AudreyUser> skirtsUserOptional = users.getAudreyUserMap().getUser(event.getPlayer().getUniqueId());
        if(!skirtsUserOptional.isPresent()) {
            try {
                final PreparedStatement s = users.getUserDb().getConnection()
                        .prepareStatement(String.format("SELECT * FROM %s WHERE uuid = ?", users.getUserDbName()));
                s.setString(1, event.getPlayer().getUniqueId().toString());
                final ResultSet rs = s.executeQuery();
                String uuid = null;
                String lastName = null;
                int kills = -1;
                int deaths = -1;
                String ip = null;
                while(rs.next()) {
                    uuid = rs.getString("uuid");
                    lastName = rs.getString("lastName");
                    kills = rs.getInt("kills");
                    deaths = rs.getInt("deaths");
                    ip = rs.getString("ip");
                }
                if(uuid == null || lastName == null || kills == -1 || deaths == -1 || ip == null) {
                    // Assume not seen before
                    final AudreyUser skirtsUser = new AudreyUser(
                            event.getPlayer().getUniqueId(), event.getPlayer().getName(), 0, 0,
                            event.getPlayer().getAddress().getAddress());
                    users.getAudreyUserMap().addUser(skirtsUser);
                } else {
                    final String name = event.getPlayer().getName().equals(lastName) ? lastName : event.getPlayer().getName();
                    final AudreyUser skirtsUser = new AudreyUser(UUID.fromString(uuid), name, kills, deaths,
                            event.getPlayer().getAddress().getAddress());
                    final PreparedStatement s2 = users.getUserDb().getConnection()
                            .prepareStatement(String.format("SELECT * FROM %s WHERE uuid = ?", users.getAttributeDbName()));
                    final ResultSet rs2 = s2.executeQuery();
                    while(rs2.next()) {
                        final String uuid2 = rs2.getString("uuid");
                        final String attrName = rs2.getString("attr_name");
                        final String attrType = rs2.getString("attr_type");
                        final String attrValue = rs2.getString("attr_value");
                        skirtsUser.addAttribute(attrName, Attribute.fromString(attrType, attrValue));
                    }
                    users.getAudreyUserMap().addUser(skirtsUser);
                }
            } catch(final SQLException e) {
                throw new IllegalStateException(e);
            }
        } else {
            skirtsUserOptional.get().setLastName(event.getPlayer().getName());
        }
    }

    // Update K/D ratios when people die
    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerDeath(@NonNull final PlayerDeathEvent event) {
        final UUID killed = event.getEntity().getUniqueId();
        final Optional<AudreyUser> killedAudreyUser = users.getAudreyUserMap().getUser(killed);
        if(killedAudreyUser.isPresent()) {
            killedAudreyUser.get().setDeaths(killedAudreyUser.get().getDeaths() + 1);
        } else {
            users.getLogger().warning("Couldn't update deaths for user '" + event.getEntity().getName()
                    + "' (UUID: " + killed + ')');
        }
        if(event.getEntity().getKiller() != null) {
            final UUID killer = event.getEntity().getKiller().getUniqueId();
            final Optional<AudreyUser> killerAudreyUser = users.getAudreyUserMap().getUser(killer);
            if(killerAudreyUser.isPresent()) {
                killerAudreyUser.get().setKills(killerAudreyUser.get().getKills() + 1);
            } else {
                users.getLogger().warning("Couldn't update kills for user '" + event.getEntity().getKiller().getName()
                        + "' (UUID: " + killer + ')');
            }
        }
    }

    /*// Fill in row in DB when someone leaves
    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerQuit(@NonNull final PlayerQuitEvent event) {
        handleDisconnect(event.getPlayer());
    }

    // Fill in row in DB when someone leaves
    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerKick(@NonNull final PlayerKickEvent event) {
        handleDisconnect(event.getPlayer());
    }

    // Fill in row in DB when someone leaves. Error if something bad happens
    @SuppressWarnings({"TypeMayBeWeakened", "SqlResolve"})
    private void handleDisconnect(@NonNull final Player player) {
        final Optional<AudreyUser> skirtsUserOptional = users.getAudreyUserMap().getUser(player.getUniqueId());
        if(skirtsUserOptional.isPresent()) {
            final AudreyUser user = skirtsUserOptional.get();
            // Write to DB
            try {
                final PreparedStatement s = users.getUserDb().getConnection()
                        // Such bad practice with INSERT OR REPLACE ;_;
                        .prepareStatement(String.format("INSERT OR REPLACE INTO %s (uuid, lastName, kills, deaths, ip) " +
                                "VALUES (?, ?, ?, ?, ?)", users.getUserDbName()));
                s.setString(1, player.getUniqueId().toString());
                s.setString(2, player.getName());
                s.setInt(3, user.getKills());
                s.setInt(4, user.getDeaths());
                s.setString(5, user.getIp().toString());
                users.getUserDb().execute(s);
                for(final Entry<String, Attribute<?>> e : user.getAttributes().entrySet()) {
                    final PreparedStatement s2 = users.getUserDb().getConnection().prepareStatement("DELETE FROM " +
                            users.getAttributeDbName() + " WHERE uuid = ? AND attr_name = ? AND attr_type = ?");
                    s2.setString(1, player.getUniqueId().toString());
                    s2.setString(2, e.getKey());
                    s2.setString(3, e.getValue().getType());
                    users.getUserDb().execute(s2);
                    final PreparedStatement s3 = users.getUserDb().getConnection()
                            .prepareStatement("INSERT INTO " + users.getAttributeDbName() +
                                    " (uuid, attr_name, attr_type, attr_value) VALUES (?, ?, ?, ?)");
                    s3.setString(1, player.getUniqueId().toString());
                    s3.setString(2, e.getKey());
                    s3.setString(3, e.getValue().getType());
                    s3.setString(4, e.getValue().get().toString());
                    users.getUserDb().execute(s3);
                }
            } catch(final SQLException e) {
                e.printStackTrace();
            }
        } else {
            users.getLogger().warning("Couldn't write stats for user '" + player.getName() + "' (UUID: "
                    + player.getUniqueId() + ") to DB!");
        }
    }*/
}
