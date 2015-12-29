package me.curlpipesh.users.command;

import me.curlpipesh.users.SkirtsUser;
import me.curlpipesh.users.Users;
import me.curlpipesh.util.plugin.SkirtsPlugin;
import me.curlpipesh.util.utils.MessageUtil;
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
        if(commandSender instanceof Player) {
            if(args.length > 0) {
                if(commandSender.hasPermission("skirtsusers.kdr.others")) {
                    final Optional<SkirtsUser> skirtsUserOptional = Users.getInstance().getSkirtsUserMap().getUserByName(args[0]);
                    if(skirtsUserOptional.isPresent()) {
                        final int kills = skirtsUserOptional.get().getKills();
                        final int deaths = skirtsUserOptional.get().getDeaths();
                        if(deaths == 0) {
                            MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX,
                                    ChatColor.GRAY + skirtsUserOptional.get().getLastName() + "'s K/D is " +
                                            ChatColor.RED + "perfect" + ChatColor.GRAY + '!');
                        } else {
                            final String ratio = String.format("%.2f", (float) kills / (float) deaths);
                            MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX,
                                    ChatColor.GRAY + skirtsUserOptional.get().getLastName() + "'s K/D is " +
                                            ChatColor.RED + ratio + ChatColor.GRAY + '!');
                        }
                    } else {
                        MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, ChatColor.GRAY + "Couldn't find that K/D ratio. " +
                                "Try again when that user is online?");
                    }
                } else {
                    MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, ChatColor.RED + command.getPermissionMessage());
                    return true;
                }
            } else {
                final Optional<SkirtsUser> skirtsUserOptional = Users.getInstance().getSkirtsUserMap().getUser(((Player) commandSender).getUniqueId());
                if(skirtsUserOptional.isPresent()) {
                    final int kills = skirtsUserOptional.get().getKills();
                    final int deaths = skirtsUserOptional.get().getDeaths();
                    if(deaths == 0) {
                        MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX,
                                ChatColor.GRAY + "Your K/D is " + ChatColor.RED + "perfect" + ChatColor.GRAY + '!');
                    } else {
                        final String ratio = String.format("%.2f", (float) kills / (float) deaths);
                        MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX,
                                ChatColor.GRAY + "Your K/D is " + ChatColor.RED + ratio + ChatColor.GRAY + '!');
                    }
                } else {
                    MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, ChatColor.GRAY + "Couldn't find your K/D ratio!?");
                }
            }
        } else {
            MessageUtil.sendMessage(commandSender, SkirtsPlugin.PREFIX, "You must be a player to use that!");
        }
        return true;
    }
}
