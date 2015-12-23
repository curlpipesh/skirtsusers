package me.curlpipesh.users.command;

import me.curlpipesh.users.SkirtsUser;
import me.curlpipesh.users.Users;
import me.curlpipesh.util.chat.MessageUtil;
import me.curlpipesh.util.plugin.SkirtsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author audrey
 * @since 12/22/15.
 */
public class CommandKD implements CommandExecutor {
    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if(commandSender.hasPermission("skirtsusers.kdr")) {
            MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, command.getPermissionMessage());
            return true;
        }
        if(commandSender instanceof Player) {
            if(args.length > 0) {
                Optional<SkirtsUser> skirtsUserOptional = Users.getInstance().getSkirtsUserMap().getUserByName(args[0]);
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
                Optional<SkirtsUser> skirtsUserOptional = Users.getInstance().getSkirtsUserMap().getUser(((Player) commandSender).getUniqueId());
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
    }
}
