package me.curlpipesh.users.command;

import me.curlpipesh.users.SkirtsUser;
import me.curlpipesh.users.Users;
import me.curlpipesh.util.utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Command to show a user's K/D ratio
 *
 * @author audrey
 * @since 12/22/15.
 */
public class CommandKD implements CommandExecutor {
    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        // Bad console! No bisquit!
        if(commandSender instanceof Player) {
            // Targeting another player?
            if(args.length > 0) {
                // Allowed to do that?
                if(commandSender.hasPermission("skirtsusers.kdr.others")) {
                    final Optional<SkirtsUser> skirtsUserOptional = Users.getInstance().getSkirtsUserMap().getUserByName(args[0]);
                    // User exists?
                    if(skirtsUserOptional.isPresent()) {
                        final int kills = skirtsUserOptional.get().getKills();
                        final int deaths = skirtsUserOptional.get().getDeaths();
                        // Perfect!
                        if(deaths == 0) {
                            MessageUtil.sendMessage(commandSender,
                                    ChatColor.GRAY + skirtsUserOptional.get().getLastName() + "'s K/D is " +
                                            ChatColor.RED + "perfect" + ChatColor.GRAY + '!');
                        } else {
                            // Not perfect
                            final String ratio = String.format("%.2f", (float) kills / (float) deaths);
                            MessageUtil.sendMessage(commandSender,
                                    ChatColor.GRAY + skirtsUserOptional.get().getLastName() + "'s K/D is " +
                                            ChatColor.RED + ratio + ChatColor.GRAY + '!');
                        }
                    } else {
                        // Try again
                        MessageUtil.sendMessage(commandSender, ChatColor.GRAY + "Couldn't find that K/D ratio. " +
                                "Try again when that user is online?");
                    }
                } else {
                    // Bad. Not allowed
                    MessageUtil.sendMessage(commandSender, ChatColor.RED + command.getPermissionMessage());
                    return true;
                }
            } else {
                // Self
                final Optional<SkirtsUser> skirtsUserOptional = Users.getInstance().getSkirtsUserMap().getUser(((Player) commandSender).getUniqueId());
                // I would honestly be surprised if this didn't work
                if(skirtsUserOptional.isPresent()) {
                    final int kills = skirtsUserOptional.get().getKills();
                    final int deaths = skirtsUserOptional.get().getDeaths();
                    if(deaths == 0) {
                        MessageUtil.sendMessage(commandSender,
                                ChatColor.GRAY + "Your K/D is " + ChatColor.RED + "perfect" + ChatColor.GRAY + '!');
                    } else {
                        final String ratio = String.format("%.2f", (float) kills / (float) deaths);
                        MessageUtil.sendMessage(commandSender,
                                ChatColor.GRAY + "Your K/D is " + ChatColor.RED + ratio + ChatColor.GRAY + '!');
                    }
                } else {
                    MessageUtil.sendMessage(commandSender, ChatColor.GRAY + "Couldn't find your K/D ratio!?");
                }
            }
        } else {
            MessageUtil.sendMessage(commandSender, "You must be a player to use that!");
        }
        return true;
    }
}
