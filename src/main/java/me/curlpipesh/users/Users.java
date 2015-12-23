package me.curlpipesh.users;

import lombok.Getter;
import lombok.NonNull;
import me.curlpipesh.util.chat.MessageUtil;
import me.curlpipesh.util.command.SkirtsCommand;
import me.curlpipesh.util.database.IDatabase;
import me.curlpipesh.util.database.impl.SQLiteDatabase;
import me.curlpipesh.util.plugin.SkirtsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

/**
 * @author audrey
 * @since 12/21/15.
 */
public class Users extends SkirtsPlugin {
    @Getter
    private IDatabase userDb;
    @SuppressWarnings("FieldCanBeLocal")
    private final String userDbName = "users";

    @Getter
    private static Users instance;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private boolean welcomeTitleEnabled = false;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private String serverName = "";
    @Getter
    private SkirtsUserMap skirtsUserMap;

    public Users() {
        super();
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
            PreparedStatement s = userDb.getConnection().prepareStatement(String.format("SELECT * FROM %s", userDbName));
            s.execute();
            ResultSet rs = s.getResultSet();
            getLogger().info("ResultSet worked?: " + rs.isBeforeFirst());
            while(rs.next()) {
                String uuid = rs.getString("uuid");
                String lastName = rs.getString("lastName");
                int kills = rs.getInt("kills");
                int deaths = rs.getInt("deaths");
                String ip = rs.getString("ip");
                if(uuid != null && lastName != null && kills != -1 && deaths != -1 && ip != null) {
                    skirtsUserMap.addUser(new SkirtsUser(UUID.fromString(uuid), lastName, kills, deaths, InetAddress.getByName(ip.replaceAll("/", ""))));
                }
            }
            rs.close();
        } catch(SQLException | UnknownHostException e) {
            throw new IllegalStateException(e);
        }

        registerEvents();
        getCommandManager().registerCommand(new SkirtsCommand.Builder().setName("killdeath")
                .setDescription("Shows you your K/D ratio")
                .addAlias("kd").addAlias("killdeathratio").addAlias("kdr")
                .setPermissionNode("skirtsusers.kdr")
                .setExecutor((commandSender, command, s, args) -> {
                    if(commandSender.hasPermission("skirtsusers.kdr")) {
                        MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, command.getPermissionMessage());
                        return true;
                    }
                    if(commandSender instanceof Player) {
                        if(args.length > 0) {
                            Optional<SkirtsUser> skirtsUserOptional = skirtsUserMap.getUserByName(args[0]);
                            if(skirtsUserOptional.isPresent()) {
                                int kills = skirtsUserOptional.get().getKills();
                                int deaths = skirtsUserOptional.get().getDeaths();
                                if(deaths == 0) {
                                    MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX,
                                            ChatColor.GRAY + skirtsUserOptional.get().getLastName() + "'s K/D is " +
                                                    ChatColor.RED + "perfect" + ChatColor.GRAY + "!");
                                } else {
                                    MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX,
                                            ChatColor.GRAY + skirtsUserOptional.get().getLastName() + "'s K/D is " +
                                                    ChatColor.RED + ((float) kills / (float) deaths) + ChatColor.GRAY + "!");
                                }
                            } else {
                                MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, "Couldn't find your K/D ratio!?");
                            }
                        } else {
                            Optional<SkirtsUser> skirtsUserOptional = skirtsUserMap.getUser(((Player) commandSender).getUniqueId());
                            if(skirtsUserOptional.isPresent()) {
                                int kills = skirtsUserOptional.get().getKills();
                                int deaths = skirtsUserOptional.get().getDeaths();
                                if(deaths == 0) {
                                    MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX,
                                            ChatColor.GRAY + "Your K/D is " + ChatColor.RED + "perfect" + ChatColor.GRAY + "!");
                                } else {
                                    MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX,
                                            ChatColor.GRAY + "Your K/D is " + ChatColor.RED + ((float) kills / (float) deaths) + ChatColor.GRAY + "!");
                                }
                            } else {
                                MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, "Couldn't find your K/D ratio!?");
                            }
                        }
                    } else {
                        MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, "You must be a player to use that!");
                    }
                    return true;
                }).build());
    }

    @Override
    public void onDisable() {
        userDb.disconnect();
    }

    @SuppressWarnings("unused")
    public Optional<SkirtsUser> getUserForUUID(String uuid) {
        return getUserForUUID(UUID.fromString(uuid));
    }

    public Optional<SkirtsUser> getUserForUUID(UUID uniqueId) {
        Optional<SkirtsUser> skirtsUserOptional = skirtsUserMap.getUser(uniqueId);
        if(!skirtsUserOptional.isPresent()) {
            try {
                PreparedStatement s = userDb.getConnection().prepareStatement(String.format("SELECT * FROM %s WHERE uuid = ?", userDbName));
                s.setString(1, uniqueId.toString());
                ResultSet rs = s.executeQuery();
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
                Optional<SkirtsUser> skirtsUserOptional = skirtsUserMap.getUser(event.getPlayer().getUniqueId());
                if(!skirtsUserOptional.isPresent()) {
                    try {
                        PreparedStatement s = userDb.getConnection().prepareStatement(String.format("SELECT * FROM %s WHERE uuid = ?", userDbName));
                        s.setString(1, event.getPlayer().getUniqueId().toString());
                        ResultSet rs = s.executeQuery();
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
                            //MessageUtil.sendTitle(event.getPlayer(), 1, 3, 1, ChatColor.GRAY + "Welcome, " + ChatColor.RED + "%player%" + ChatColor.GRAY + ",",
                            //        ChatColor.GRAY + "to " + ChatColor.RED + serverName + ChatColor.GRAY + "!");
                        } else {
                            final String name = event.getPlayer().getName().equals(lastName) ? lastName : event.getPlayer().getName();
                            final SkirtsUser skirtsUser = new SkirtsUser(UUID.fromString(uuid), name, kills, deaths, event.getPlayer().getAddress().getAddress());
                            skirtsUserMap.addUser(skirtsUser);
                            //MessageUtil.sendTitle(event.getPlayer(), 1, 3, 1, ChatColor.GRAY + "Welcome back, " + ChatColor.RED + "%player%" + ChatColor.GRAY + ",",
                            //        ChatColor.GRAY + "to " + ChatColor.RED + serverName + ChatColor.GRAY + "!");
                        }
                    } catch(SQLException e) {
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
                    getLogger().warning("Couldn't update deaths for user '" + event.getEntity().getName() + "' (UUID: " + killed.toString() + ")");
                }
                if(event.getEntity().getKiller() != null) {
                    final UUID killer = event.getEntity().getKiller().getUniqueId();
                    final Optional<SkirtsUser> killerSkirtsUser = skirtsUserMap.getUser(killer);
                    if(killerSkirtsUser.isPresent()) {
                        killerSkirtsUser.get().setKills(killerSkirtsUser.get().getKills() + 1);
                    } else {
                        getLogger().warning("Couldn't update deaths for user '" + event.getEntity().getKiller().getName() + "' (UUID: " + killer.toString() + ")");
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

            private void handleDisconnect(@NonNull final Player player) {
                final Optional<SkirtsUser> skirtsUserOptional = skirtsUserMap.getUser(player.getUniqueId());
                if(skirtsUserOptional.isPresent()) {
                    // Write to DB
                    try {
                        PreparedStatement s = userDb.getConnection()
                                // Such bad practice with INSERT OR REPLACE ;_;
                                .prepareStatement(String.format("INSERT OR REPLACE INTO %s (uuid, lastName, kills, deaths, ip) " +
                                        "VALUES (?, ?, ?, ?, ?)", userDbName));
                        s.setString(1, player.getUniqueId().toString());
                        s.setString(2, player.getName());
                        s.setInt(3, skirtsUserOptional.get().getKills());
                        s.setInt(4, skirtsUserOptional.get().getDeaths());
                        s.setString(5, skirtsUserOptional.get().getIp().toString());
                        userDb.execute(s);
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    getLogger().warning("Couldn't write stats for user '" + player.getName() + "' (UUID: " + player.getUniqueId() + ") to DB!");
                }
            }
        }, this);
    }
}
