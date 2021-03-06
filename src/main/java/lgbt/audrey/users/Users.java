package lgbt.audrey.users;

import lgbt.audrey.users.command.CommandPlaytime;
import lgbt.audrey.users.listeners.PlayerListener;
import lgbt.audrey.users.tasks.PlayerMapTask;
import lgbt.audrey.users.user.AudreyUserMap;
import lombok.Getter;
import lgbt.audrey.users.attribute.Attribute;
import lgbt.audrey.users.command.CommandKD;
import lgbt.audrey.users.user.AudreyUser;
import lgbt.audrey.util.command.AudreyCommand;
import lgbt.audrey.util.database.IDatabase;
import lgbt.audrey.util.database.impl.SQLiteDatabase;
import lgbt.audrey.util.plugin.AudreyPlugin;
import org.bukkit.Bukkit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

/**
 * TODO: Make an actual API ;-;
 *
 * @author audrey
 * @since 12/21/15.
 */
@SuppressWarnings({"Duplicates", "unused"})
public class Users extends AudreyPlugin {
    @Getter
    private IDatabase userDb;
    @Getter
    private final String userDbName = "users";
    @Getter
    private final String attributeDbName = "attributes";

    @SuppressWarnings("StaticVariableOfConcreteClass")
    @Getter
    private static Users instance;

    @Getter
    private AudreyUserMap audreyUserMap;

    public Users() {
        instance = this;
    }

    @Override
    public void onEnable() {
        if(!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }
        //saveDefaultConfig();
        // Create database for users. IF NOT EXISTS because we're fucking lazy
        // and don't check if the table actually exists first. ;-;
        userDb = new SQLiteDatabase(this, userDbName, "CREATE TABLE IF NOT EXISTS " + userDbName
                + " (uuid TEXT PRIMARY KEY NOT NULL UNIQUE, lastName TEXT NOT NULL," +
                "kills INT NOT NULL, deaths INT NOT NULL, ip TEXT NOT NULL)");
        if(userDb.connect()) {
            if(userDb.initialize()) {
                getLogger().info("Successfully attached to SQLite DB!");
            } else {
                Bukkit.getPluginManager().disablePlugin(this);
                getLogger().severe("Couldn't initialize SQLite DB!");
                return;
            }
        } else {
            Bukkit.getPluginManager().disablePlugin(this);
            getLogger().severe("Couldn't connect to SQLite DB!");
            return;
        }
        try {
            //noinspection SqlDialectInspection
            final PreparedStatement s = userDb.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS "
                    + attributeDbName + " (uuid TEXT NOT NULL, attr_name TEXT NOT NULL, attr_type TEXT NOT NULL, " +
                    "attr_value TEXT NOT NULL, FOREIGN KEY(uuid) REFERENCES " + userDbName + "(uuid))");
            userDb.execute(s);
        } catch(final SQLException e) {
            e.printStackTrace();
            getLogger().severe("Couldn't make attr. table, disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        audreyUserMap = new AudreyUserMap(this);

        // Grab all users
        try {
            final PreparedStatement s = userDb.getConnection().prepareStatement(String.format("SELECT * FROM %s", userDbName));
            s.execute();
            final ResultSet rs = s.getResultSet();
            //getLogger().info("ResultSet worked?: " + rs.isBeforeFirst());
            while(rs.next()) {
                final String uuid = rs.getString("uuid");
                final String lastName = rs.getString("lastName");
                final int kills = rs.getInt("kills");
                final int deaths = rs.getInt("deaths");
                final String ip = rs.getString("ip");
                if(uuid != null && lastName != null && kills != -1 && deaths != -1 && ip != null) {
                    //audreyUserMap.addUser(new AudreyUser(UUID.fromString(uuid), lastName, kills, deaths, InetAddress.getByName(ip.replaceAll("/", ""))));
                    final AudreyUser audreyUser = new AudreyUser(UUID.fromString(uuid), lastName, kills, deaths,
                            InetAddress.getByName(ip.replaceAll("/", "")));
                    final PreparedStatement s2 = userDb.getConnection()
                            .prepareStatement(String.format("SELECT * FROM %s WHERE uuid = ?", attributeDbName));
                    s2.setString(1, uuid);
                    final ResultSet rs2 = s2.executeQuery();
                    //getLogger().info("ResultSet2 worked?: " + rs2.isBeforeFirst());
                    while(rs2.next()) {
                        final String uuid2 = rs2.getString("uuid");
                        final String attrName = rs2.getString("attr_name");
                        final String attrType = rs2.getString("attr_type");
                        final String attrValue = rs2.getString("attr_value");
                        //System.out.println(String.format("Loaded: %s %s (%s: %s)", uuid2, attrName, attrType, attrValue));
                        audreyUser.addAttribute(attrName, Attribute.fromString(attrType, attrValue));
                    }
                    audreyUserMap.addUser(audreyUser);
                }
            }
            rs.close();
        } catch(SQLException | UnknownHostException e) {
            getServer().getPluginManager().disablePlugin(this);
            throw new IllegalStateException(e);
        }

        registerEvents();
        // Register custom commands
        getCommandManager().registerCommand(AudreyCommand.builder().setName("killdeath")
                .setDescription("Shows you your K/D ratio")
                .addAlias("kd").addAlias("killdeathratio").addAlias("kdr")
                .setPermissionNode("audreyusers.kdr")
                .setPlugin(this)
                .setExecutor(new CommandKD()).build());
        getCommandManager().registerCommand(AudreyCommand.builder().setName("playtime")
                .setDescription("Shows you your playtime")
                .setPermissionNode("audreyusers.playtime")
                .setUsage("/playtime [user]")
                .setPlugin(this)
                .setExecutor(new CommandPlaytime(this))
                .build());
        // Map users already online. This is for when a /reload happens, or the plugin is
        // loaded in while the server is running
        getServer().getScheduler().scheduleSyncDelayedTask(this, new PlayerMapTask(this), 600L);
    }

    @Override
    @SuppressWarnings("SqlResolve")
    public void onDisable() {
        getLogger().info("Writing user data...");
        for(final AudreyUser user : audreyUserMap.getAudreyUsers()) {
            // Write to DB
            try {
                final PreparedStatement s = userDb.getConnection()
                        // Such bad practice with INSERT OR REPLACE ;_;
                        .prepareStatement(String.format("INSERT OR REPLACE INTO %s (uuid, lastName, kills, deaths, ip) " +
                                "VALUES (?, ?, ?, ?, ?)", userDbName));
                s.setString(1, user.getUuid().toString());
                s.setString(2, user.getLastName());
                s.setInt(3, user.getKills());
                s.setInt(4, user.getDeaths());
                s.setString(5, user.getIp().toString());
                userDb.execute(s);
                for(final Entry<String, Attribute<?>> e : user.getAttributes().entrySet()) {
                    final PreparedStatement s2 = userDb.getConnection().prepareStatement("DELETE FROM " +
                            attributeDbName + " WHERE uuid = ? AND attr_name = ? AND attr_type = ?");
                    s2.setString(1, user.getUuid().toString());
                    s2.setString(2, e.getKey());
                    s2.setString(3, e.getValue().getType());
                    userDb.execute(s2);
                    final PreparedStatement s3 = userDb.getConnection()
                            .prepareStatement("INSERT INTO " + attributeDbName +
                                    " (uuid, attr_name, attr_type, attr_value) VALUES (?, ?, ?, ?)");
                    s3.setString(1, user.getUuid().toString());
                    s3.setString(2, e.getKey());
                    s3.setString(3, e.getValue().getType());
                    s3.setString(4, e.getValue().get().toString());
                    userDb.execute(s3);
                }
            } catch(final SQLException e) {
                e.printStackTrace();
            }
        }
        userDb.disconnect();
        getLogger().info("Done!");
    }

    /**
     * Return the user with the given UUID
     *
     * @param uuid UUID string
     *
     * @return AudreyUser with the given uuid
     */
    @SuppressWarnings("unused")
    public Optional<AudreyUser> getUserForUUID(final String uuid) {
        return getUserForUUID(UUID.fromString(uuid));
    }

    /**
     * Return the user with the given UUID
     *
     * @param uniqueId UUID
     *
     * @return AudreyUser with the given UUID
     */
    public Optional<AudreyUser> getUserForUUID(final UUID uniqueId) {
        final Optional<AudreyUser> audreyUserOptional = audreyUserMap.getUser(uniqueId);
        if(!audreyUserOptional.isPresent()) {
            try {
                final PreparedStatement s = userDb.getConnection().prepareStatement(String.format("SELECT * FROM %s WHERE uuid = ?", userDbName));
                s.setString(1, uniqueId.toString());
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
                    return Optional.<AudreyUser>empty();
                } else {
                    final AudreyUser audreyUser = new AudreyUser(UUID.fromString(uuid), lastName, kills, deaths,
                            InetAddress.getByName(ip.replaceAll("/", "")));
                    // TODO: ATTR
                    final PreparedStatement s2 = userDb.getConnection()
                            .prepareStatement(String.format("SELECT * FROM %s WHERE uuid = ?", attributeDbName));
                    final ResultSet rs2 = s2.executeQuery();
                    while(rs2.next()) {
                        final String uuid2 = rs2.getString("uuid");
                        final String attrName = rs2.getString("attr_name");
                        final String attrType = rs2.getString("attr_type");
                        final String attrValue = rs2.getString("attr_value");
                        audreyUser.addAttribute(attrName, Attribute.fromString(attrType, attrValue));
                    }
                    audreyUserMap.addUser(audreyUser);
                    return Optional.of(audreyUser);
                }
            } catch(SQLException | UnknownHostException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return audreyUserOptional;
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        // TODO: Proper logout handling of users. Need to serialize in onDisable, because those logouts aren't registered from /stop
    }
}
