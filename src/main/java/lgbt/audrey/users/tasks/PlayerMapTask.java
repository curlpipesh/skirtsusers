package lgbt.audrey.users.tasks;

import lgbt.audrey.users.user.AudreyUser;
import lgbt.audrey.users.Users;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * @author audrey
 * @since 2/10/16.
 */
public class PlayerMapTask implements Runnable {
    private final Users users;

    public PlayerMapTask(final Users users) {
        this.users = users;
    }

    @Override
    public void run() {
        for(final Player player : users.getServer().getOnlinePlayers()) {
            final Optional<AudreyUser> skirtsUserOptional = users.getAudreyUserMap().getUser(player.getUniqueId());
            if(!skirtsUserOptional.isPresent()) {
                try {
                    final PreparedStatement s = users.getUserDb().getConnection()
                            .prepareStatement(String.format("SELECT * FROM %s WHERE uuid = ?", users.getUserDbName()));
                    s.setString(1, player.getUniqueId().toString());
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
                                player.getUniqueId(), player.getName(), 0, 0,
                                player.getAddress().getAddress());
                        users.getAudreyUserMap().addUser(skirtsUser);
                    } else {
                        final String name = player.getName().equals(lastName) ? lastName : player.getName();
                        final AudreyUser skirtsUser = new AudreyUser(UUID.fromString(uuid), name, kills, deaths,
                                player.getAddress().getAddress());
                        users.getAudreyUserMap().addUser(skirtsUser);
                    }
                } catch(final SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
}
