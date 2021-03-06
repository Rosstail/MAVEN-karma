package fr.rosstail.karma.commands;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.commands.list.Commands;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import fr.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;


import java.util.*;

import static fr.rosstail.karma.commands.list.Commands.*;
/**
 * Checking what method/class will be used on command, depending of command Sender and number of args.
 */
public class KarmaCommand implements CommandExecutor, TabExecutor {
    private final AdaptMessage adaptMessage;
    private final CheckKarmaCommand checkKarmaCommand;
    private final EditKarmaCommand editKarmaCommand;

    public KarmaCommand(Karma plugin) {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
        this.checkKarmaCommand = new CheckKarmaCommand(this);
        this.editKarmaCommand = new EditKarmaCommand(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String string = String.join(" ", args);
        if (!canLaunchCommand(sender, COMMAND_KARMA)) {
            return false;
        }

        if (string.startsWith(COMMAND_KARMA_CHECK.getCommand())) {
            if (args.length == 2) {
                checkKarmaCommand.karmaOther(sender, args[1]);
            } else {
                checkKarmaCommand.karmaSelf(sender);
            }
        } else if (string.startsWith(COMMAND_KARMA_SET.getCommand())) {
            editKarmaCommand.karmaSet(sender, args);
        } else if (string.startsWith(COMMAND_KARMA_ADD.getCommand())) {
            editKarmaCommand.karmaAdd(sender, args);
        } else if (string.startsWith(COMMAND_KARMA_REMOVE.getCommand())) {
            editKarmaCommand.karmaRemove(sender, args);
        } else if (string.startsWith(COMMAND_KARMA_RESET.getCommand())) {
            editKarmaCommand.karmaReset(sender, args);
        } else if (string.startsWith(COMMAND_KARMA_HELP.getCommand())) {
            if (canLaunchCommand(sender, COMMAND_KARMA_HELP)) {
                sender.sendMessage(adaptMessage.listMessage(null, LangManager.getListMessage(LangMessage.HELP)));
            }
        } else {
            Player playerSender = null;
            if (sender instanceof Player) {
                playerSender = ((Player) sender).getPlayer();
            }
            sender.sendMessage(adaptMessage.listMessage(playerSender, LangManager.getListMessage(LangMessage.HELP)));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        String string = String.join(" ", args);

        if (args.length <= 1) {
            commands.add("set");
            commands.add("check");
            commands.add("add");
            commands.add("remove");
            commands.add("reset");
            commands.add("help");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            if (!string.startsWith(COMMAND_KARMA_HELP.getCommand())) {
                Bukkit.getOnlinePlayers().forEach(player -> commands.add(player.getName()));
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    boolean canLaunchCommand(CommandSender sender, Commands command) {
        if (!(sender instanceof Player) || sender.hasPermission(command.getPermission())) {
            return true;
        }
        permissionDenied(sender, command);
        return false;
    }

    private void permissionDenied(CommandSender sender, Commands command) {
        String message = LangManager.getMessage(LangMessage.PERMISSION_DENIED);
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            message = message.replaceAll("%command%", command.getCommand());
            message = message.replaceAll("%permission%", command.getPermission());
            sender.sendMessage(message);
        }
    }

    /**
     * @param sender
     */
    void disconnectedPlayer(CommandSender sender) {
        sender.sendMessage(adaptMessage.message(null, LangManager.getMessage(LangMessage.DISCONNECTED), null));
    }

    void errorMessage(CommandSender sender, Exception e) {
        if (e instanceof ArrayIndexOutOfBoundsException) {
            sender.sendMessage(adaptMessage.message(null, LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS), null));
        }
        if (e instanceof NumberFormatException) {
            sender.sendMessage(adaptMessage.message(null, LangManager.getMessage(LangMessage.WRONG_VALUE), null));
        }
    }
}
