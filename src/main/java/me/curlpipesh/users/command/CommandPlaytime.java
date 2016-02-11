package me.curlpipesh.users.command;

import me.curlpipesh.users.user.SkirtsUser;
import me.curlpipesh.users.Users;
import me.curlpipesh.util.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author audrey
 * @since 2/3/16.
 */
public class CommandPlaytime implements CommandExecutor {
    private final Users users;

    public CommandPlaytime(final Users users) {
        this.users = users;
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if(commandSender instanceof Player) {
            if(args.length > 0) {
                if(commandSender.hasPermission("skirtsusers.playtime.others")) {
                    final Optional<SkirtsUser> skirtsUserOptional = users.getSkirtsUserMap().getUserByName(args[0]);
                    if(skirtsUserOptional.isPresent()) {
                        try {
                            final long time = Bukkit.getPlayer(skirtsUserOptional.get().getUuid()).getStatistic(Statistic.PLAY_ONE_TICK) / 20L;
                            final long days = TimeUnit.SECONDS.toDays(time);
                            final long hours = TimeUnit.SECONDS.toHours(time - TimeUnit.DAYS.toSeconds(days));
                            final long minutes = TimeUnit.SECONDS.toMinutes(time - TimeUnit.HOURS.toSeconds(hours));
                            // No one sane cares about seconds.
                            MessageUtil.sendMessages(commandSender,
                                    ChatColor.GRAY + "Play time: " + ChatColor.RED +
                                            String.format("%d day(s), %d hour(s), %d minute(s)",
                                                    days,
                                                    hours,
                                                    minutes));
                        } catch(final Exception e) {
                            //e.printStackTrace();
                            MessageUtil.sendMessage(commandSender, ChatColor.GRAY + "Couldn't find statistics for '"
                                    + skirtsUserOptional.get().getLastName() + "'. Try again when he/she is online?");
                        }
                    } else {
                        MessageUtil.sendMessage(commandSender, ChatColor.GRAY + "I don't know who that is.");
                    }
                } else {
                    MessageUtil.sendMessage(commandSender, ChatColor.RED + "You don't have permssion to do that!");
                }
            } else {
                final long time = ((Player) commandSender).getStatistic(Statistic.PLAY_ONE_TICK) / 20L;
                final long days = TimeUnit.SECONDS.toDays(time);
                final long hours = TimeUnit.SECONDS.toHours(time - TimeUnit.DAYS.toSeconds(days));
                final long minutes = TimeUnit.SECONDS.toMinutes(time - TimeUnit.HOURS.toSeconds(hours));
                // No one sane cares about seconds.
                MessageUtil.sendMessages(commandSender,
                        ChatColor.GRAY + "Play time: " + ChatColor.RED +
                                String.format("%d day(s), %d hour(s), %d minute(s)",
                                        days, hours, minutes));
            }
        } else {
            MessageUtil.sendMessage(commandSender, "Silly console, you have no statistics @_@");
        }
        return true;
    }
}
