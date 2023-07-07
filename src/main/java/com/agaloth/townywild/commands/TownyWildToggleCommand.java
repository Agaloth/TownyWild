package com.agaloth.townywild.commands;

import com.agaloth.townywild.enums.TownyWildPermissionNodes;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.agaloth.townywild.hooks.TownyWildPlaceholderExpansion.protectionExpirationTime;
import static com.agaloth.townywild.listeners.TownyWildTownEventListener.*;
import static com.agaloth.townywild.tasks.UpdateBossBarProgress.createBossBar;

public class TownyWildToggleCommand implements TabExecutor {

    private static final List<String> townyWildToggleTabCompletes = Collections.singletonList("toggle");

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        // If the argument's length is equal to 1.
        if (args.length == 1)

            // Show "toggle" as tab completion.
            return NameUtil.filterByStart(townyWildToggleTabCompletes, args[0]);

        else

            // Show nothing.
            return Collections.emptyList();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {

        // Execute the parseToggleCommand method when running the command.
        parseToggleCommand(sender, args);
        return true;
    }

    private void parseToggleCommand(CommandSender sender, String[] args) {

        // If the arguments is greater than 0.
        if (args.length > 0) {

            // If the sender is an instance of Player and if the sender doesn't have the permission to reload the plugin.
            if (sender instanceof Player && !sender.hasPermission(TownyWildPermissionNodes.TOWNYWILD_TOGGLE_COMMAND.getNode(args[0]))) {

                // Send a message to the player telling him that he doesn't have permission.
                TownyMessaging.sendErrorMsg(sender, Translatable.of("plugin_prefix").append(Translatable.of("msg_err_command_disable")));
                return;
            }

            // If the first argument is "toggle".
            if (args[0].equals("toggle")) {

                // Run the parseToggleCommand method.
                parseToggleCommand(sender);

            } else {

                // Show the help message.
                showHelp(sender);
            }

        } else {

            // If the sender is an instance of Player and if the sender doesn't have the permission to reload the plugin.
            if (sender instanceof Player && !sender.hasPermission(TownyWildPermissionNodes.TOWNYWILD_TOGGLE_COMMAND.getNode())) {

                // Send a message to the player telling him that he doesn't have permission.
                TownyMessaging.sendErrorMsg(sender, Translatable.of("plugin_prefix").append(Translatable.of("msg_err_command_disable")));
                return;
            }
            // Show the help message.
            showHelp(sender);
        }
    }

    private void showHelp(CommandSender sender) {

        // Send a message to the player with /townywildprotection as the title.
        sender.sendMessage(ChatTools.formatTitle("/townywildprotection"));

        // Send a message to the player with more information about the command.
        sender.sendMessage(ChatTools.formatCommand("Eg", "/twp", "toggle", Translatable.of("toggle_help").forLocale(sender)));
    }

    private void parseToggleCommand(CommandSender sender) {

        // Gets the player's name from the CommandSender.
        Player player = (Player) sender;

        // Gets the player's UUID
        UUID uniqueId = player.getUniqueId();

        // If the toggledProtection HashSet contains the player's UUID
        if (toggledProtection.contains(uniqueId)) {

            // Send a message telling the player that his protection is enabled.
            TownyMessaging.sendMessage(sender, Translatable.of("plugin_prefix").append(Translatable.of("player_toggled_protection_on")));

            // Remove the player from the toggledProtection HashSet
            toggledProtection.remove(uniqueId);

        } else {

            // Send a message telling the player that his protection is disabled.
            TownyMessaging.sendMessage(sender, Translatable.of("plugin_prefix").append(Translatable.of("player_toggled_protection_off")));

            // Add the player to the toggledProtection HashSet
            toggledProtection.add(uniqueId);

            // Gets the Bukkit task and cancels the protection task.
            if (cancelProtectionTask.containsKey(uniqueId)) {
                cancelProtectionTask.get(uniqueId).cancel();
            }

            // Gets the Bukkit task and cancels the bossbar progress task and sets the progress to 1 to prevent the progress bar from starting at random numbers.
            if (runningBossBars.containsKey(uniqueId)) {
                runningBossBars.get(uniqueId).cancel();
                runningBossBars.remove(uniqueId);

                // Removes the bossbar if a player enters a town.
                BossBar timeLeftBar = createBossBar.remove(uniqueId);

                // If the timeLeftBar expired don't do anything but if it's still running then remove the timeLeftBar bossbar when entering a town.
                if (timeLeftBar == null) {
                    return;
                } else {
                    timeLeftBar.removePlayer(player);
                }

                // Removes a player's UUID from the protectionExpirationTime list when entering a town.
                protectionExpirationTime.remove(uniqueId);
            }
        }
    }
}
