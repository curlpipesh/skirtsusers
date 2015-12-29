package me.curlpipesh.users;

import lombok.Getter;
import lombok.NonNull;
import me.curlpipesh.users.command.CommandKD;
import me.curlpipesh.util.command.SkirtsCommand;
import me.curlpipesh.util.database.IDatabase;
import me.curlpipesh.util.database.impl.SQLiteDatabase;
import me.curlpipesh.util.plugin.SkirtsPlugin;
import me.curlpipesh.util.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author audrey
 * @since 12/21/15.
 */
@SuppressWarnings("Duplicates")
public class Users extends SkirtsPlugin {
    @Getter
    private IDatabase userDb;
    @SuppressWarnings("FieldCanBeLocal")
    private final String userDbName = "users";

    @Getter
    private static Users instance;

    private boolean welcomeTitleEnabled;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private String serverName = "";
    @Getter
    private SkirtsUserMap skirtsUserMap;

    public Users() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        welcomeTitleEnabled = getConfig().getBoolean("welcome-title-enabled");
        serverName = getConfig().getString("server-name");
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
        skirtsUserMap = new SkirtsUserMap(this);

        try {
            final PreparedStatement s = userDb.getConnection().prepareStatement(String.format("SELECT * FROM %s", userDbName));
            s.execute();
            final ResultSet rs = s.getResultSet();
            getLogger().info("ResultSet worked?: " + rs.isBeforeFirst());
            while(rs.next()) {
                final String uuid = rs.getString("uuid");
                final String lastName = rs.getString("lastName");
                final int kills = rs.getInt("kills");
                final int deaths = rs.getInt("deaths");
                final String ip = rs.getString("ip");
                if(uuid != null && lastName != null && kills != -1 && deaths != -1 && ip != null) {
                    skirtsUserMap.addUser(new SkirtsUser(UUID.fromString(uuid), lastName, kills, deaths, InetAddress.getByName(ip.replaceAll("/", ""))));
                }
            }
            rs.close();
        } catch(SQLException | UnknownHostException e) {
            throw new IllegalStateException(e);
        }

        registerEvents();
        getCommandManager().registerCommand(SkirtsCommand.builder().setName("killdeath")
                .setDescription("Shows you your K/D ratio")
                .addAlias("kd").addAlias("killdeathratio").addAlias("kdr")
                .setPermissionNode("skirtsusers.kdr")
                .setExecutor(new CommandKD()).build());
        getCommandManager().registerCommand(SkirtsCommand.builder().setName("playtime")
                .setDescription("Shows you your playtime")
                .setPermissionNode("skirtsusers.playtime")
                .setUsage("/playtime [user]")
                .setExecutor((commandSender, command, s, args) -> {
                    if(commandSender instanceof Player) {
                        if(args.length > 0) {
                            if(commandSender.hasPermission("skirtsusers.playtime.others")) {
                                Optional<SkirtsUser> skirtsUserOptional = skirtsUserMap.getUserByName(args[0]);
                                if(skirtsUserOptional.isPresent()) {
                                    try {
                                        long time = Bukkit.getPlayer(skirtsUserOptional.get().getUuid()).getStatistic(Statistic.PLAY_ONE_TICK) / 20L;
                                        long days = TimeUnit.SECONDS.toDays(time);
                                        long hours = TimeUnit.SECONDS.toHours(time - TimeUnit.DAYS.toSeconds(days));
                                        long minutes = TimeUnit.SECONDS.toMinutes(time - TimeUnit.HOURS.toSeconds(hours));
                                        // No one sane cares about seconds.
                                        MessageUtil.sendMessages(commandSender, SkirtsPlugin.PREFIX, 0,
                                                ChatColor.GRAY + "Play time: " + ChatColor.RED +
                                                        String.format("%d day(s), %d hour(s), %d minute(s)",
                                                                days,
                                                                hours,
                                                                minutes));
                                    } catch(Exception e) {
                                        //e.printStackTrace();
                                        MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, ChatColor.GRAY + "Couldn't find statistics for '"
                                                + skirtsUserOptional.get().getLastName() + "'. Try again when he/she is online?");
                                    }
                                } else {
                                    MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, ChatColor.GRAY + "I don't know who that is.");
                                }
                            } else {
                                MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, ChatColor.RED + "You don't have permssion to do that!");
                            }
                        } else {
                            long time = ((Player) commandSender).getStatistic(Statistic.PLAY_ONE_TICK) / 20L;
                            long days = TimeUnit.SECONDS.toDays(time);
                            long hours = TimeUnit.SECONDS.toHours(time - TimeUnit.DAYS.toSeconds(days));
                            long minutes = TimeUnit.SECONDS.toMinutes(time - TimeUnit.HOURS.toSeconds(hours));
                            // No one sane cares about seconds.
                            MessageUtil.sendMessages(commandSender, SkirtsPlugin.PREFIX, 0,
                                    ChatColor.GRAY + "Play time: " + ChatColor.RED +
                                            String.format("%d day(s), %d hour(s), %d minute(s)",
                                                    days, hours, minutes));
                        }
                    } else {
                        MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, "Silly console, you have no statistics @_@");
                    }
                    return true;
                })
                .build());
    }

    @Override
    public void onDisable() {
        userDb.disconnect();
    }

    @SuppressWarnings("unused")
    public Optional<SkirtsUser> getUserForUUID(final String uuid) {
        return getUserForUUID(UUID.fromString(uuid));
    }

    public Optional<SkirtsUser> getUserForUUID(final UUID uniqueId) {
        final Optional<SkirtsUser> skirtsUserOptional = skirtsUserMap.getUser(uniqueId);
        if(!skirtsUserOptional.isPresent()) {
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
                    return Optional.<SkirtsUser>empty();
                } else {
                    final SkirtsUser skirtsUser = new SkirtsUser(UUID.fromString(uuid), lastName, kills, deaths, InetAddress.getByName(ip.replaceAll("/", "")));
                    skirtsUserMap.addUser(skirtsUser);
                    return Optional.of(skirtsUser);
                }
            } catch(SQLException | UnknownHostException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return skirtsUserOptional;
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerLogin(@NonNull final PlayerJoinEvent event) {
                final Optional<SkirtsUser> skirtsUserOptional = skirtsUserMap.getUser(event.getPlayer().getUniqueId());
                if(!skirtsUserOptional.isPresent()) {
                    try {
                        final PreparedStatement s = userDb.getConnection().prepareStatement(String.format("SELECT * FROM %s WHERE uuid = ?", userDbName));
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
                            final SkirtsUser skirtsUser = new SkirtsUser(
                                    event.getPlayer().getUniqueId(), event.getPlayer().getName(), 0, 0,
                                    event.getPlayer().getAddress().getAddress());
                            skirtsUserMap.addUser(skirtsUser);
                            if(welcomeTitleEnabled) {
                                MessageUtil.sendTitle(event.getPlayer(), 1, 3, 1, ChatColor.GRAY + "Welcome, " + ChatColor.RED + "%player%" + ChatColor.GRAY + ',',
                                        ChatColor.GRAY + "to " + ChatColor.RED + serverName + ChatColor.GRAY + '!');
                            }
                        } else {
                            final String name = event.getPlayer().getName().equals(lastName) ? lastName : event.getPlayer().getName();
                            final SkirtsUser skirtsUser = new SkirtsUser(UUID.fromString(uuid), name, kills, deaths, event.getPlayer().getAddress().getAddress());
                            skirtsUserMap.addUser(skirtsUser);
                            if(welcomeTitleEnabled) {
                                MessageUtil.sendTitle(event.getPlayer(), 1, 3, 1, ChatColor.GRAY + "Welcome back, " + ChatColor.RED + "%player%" + ChatColor.GRAY + ',',
                                        ChatColor.GRAY + "to " + ChatColor.RED + serverName + ChatColor.GRAY + '!');
                            }
                        }
                    } catch(final SQLException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }

            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerDeath(@NonNull final PlayerDeathEvent event) {
                final UUID killed = event.getEntity().getUniqueId();
                final Optional<SkirtsUser> killedSkirtsUser = skirtsUserMap.getUser(killed);
                if(killedSkirtsUser.isPresent()) {
                    killedSkirtsUser.get().setDeaths(killedSkirtsUser.get().getDeaths() + 1);
                } else {
                    getLogger().warning("Couldn't update deaths for user '" + event.getEntity().getName() + "' (UUID: " + killed + ')');
                }
                if(event.getEntity().getKiller() != null) {
                    final UUID killer = event.getEntity().getKiller().getUniqueId();
                    final Optional<SkirtsUser> killerSkirtsUser = skirtsUserMap.getUser(killer);
                    if(killerSkirtsUser.isPresent()) {
                        killerSkirtsUser.get().setKills(killerSkirtsUser.get().getKills() + 1);
                    } else {
                        getLogger().warning("Couldn't update deaths for user '" + event.getEntity().getKiller().getName() + "' (UUID: " + killer + ')');
                    }
                }
            }

            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerQuit(@NonNull final PlayerQuitEvent event) {
                handleDisconnect(event.getPlayer());
            }

            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerKick(@NonNull final PlayerKickEvent event) {
                handleDisconnect(event.getPlayer());
            }

            @SuppressWarnings("TypeMayBeWeakened")
            private void handleDisconnect(@NonNull final Player player) {
                final Optional<SkirtsUser> skirtsUserOptional = skirtsUserMap.getUser(player.getUniqueId());
                if(skirtsUserOptional.isPresent()) {
                    // Write to DB
                    try {
                        final PreparedStatement s = userDb.getConnection()
                                // Such bad practice with INSERT OR REPLACE ;_;
                                .prepareStatement(String.format("INSERT OR REPLACE INTO %s (uuid, lastName, kills, deaths, ip) " +
                                        "VALUES (?, ?, ?, ?, ?)", userDbName));
                        s.setString(1, player.getUniqueId().toString());
                        s.setString(2, player.getName());
                        s.setInt(3, skirtsUserOptional.get().getKills());
                        s.setInt(4, skirtsUserOptional.get().getDeaths());
                        s.setString(5, skirtsUserOptional.get().getIp().toString());
                        userDb.execute(s);
                    } catch(final SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    getLogger().warning("Couldn't write stats for user '" + player.getName() + "' (UUID: " + player.getUniqueId() + ") to DB!");
                }
            }
        }, this);
    }
}
