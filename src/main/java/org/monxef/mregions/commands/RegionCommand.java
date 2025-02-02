package org.monxef.mregions.commands;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.commands.impl.CreateCommand;
import org.monxef.mregions.commands.impl.DeleteCommand;
import org.monxef.mregions.commands.impl.FlagCommand;
import org.monxef.mregions.commands.impl.RedefineCommand;
import org.monxef.mregions.commands.impl.WandCommand;
import org.monxef.mregions.commands.impl.WhitelistCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionCommand implements CommandExecutor, TabCompleter {
    private final RegionPlugin plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public RegionCommand(RegionPlugin plugin) {
        this.plugin = plugin;
        registerSubCommands();
    }

    private void registerSubCommands() {
        // Register all subcommands
        addSubCommand(new CreateCommand(plugin));
        addSubCommand(new WandCommand(plugin));
        addSubCommand(new WhitelistCommand(plugin));
        addSubCommand(new FlagCommand(plugin));
        addSubCommand(new RedefineCommand(plugin));
        addSubCommand(new DeleteCommand(plugin));
    }

    private void addSubCommand(SubCommand command) {
        subCommands.put(command.getName().toLowerCase(), command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("region.browse")){
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    plugin.getConfig().getString("messages.no-permission"));
            return true;
        }
        if (args.length == 0) {
            // Open main menu if no arguments
            plugin.getMenuManager().openRegionsMenu(player);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            // Check if it's a region name and open its menu
            plugin.getRegionManager().getRegion(args[0]).ifPresentOrElse(
                    region -> plugin.getMenuManager().openRegionMenu(player, region),
                    () -> player.sendMessage(plugin.getConfig().getString("messages.prefix") + "§cUnknown command or region!")
            );
            return true;
        }

        if (!player.hasPermission(subCommand.getPermission())) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    plugin.getConfig().getString("messages.no-permission"));
            return true;
        }

        // Execute subcommand with remaining args
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);
        subCommand.execute(player, subArgs);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return completions;
        }

        if (args.length == 1) {
            // Add subcommands
            subCommands.values().stream()
                    .filter(cmd -> sender.hasPermission(cmd.getPermission()))
                    .map(SubCommand::getName)
                    .forEach(completions::add);

            // Add region names
            plugin.getRegionManager().getRegions().stream()
                    .map(region -> region.getName())
                    .forEach(completions::add);
        } else if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                completions.addAll(subCommand.getTabCompletions((Player) sender, args));
            }
        }

        return completions;
    }
}