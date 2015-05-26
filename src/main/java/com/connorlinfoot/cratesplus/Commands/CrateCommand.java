package com.connorlinfoot.cratesplus.Commands;

import com.connorlinfoot.cratesplus.Crate;
import com.connorlinfoot.cratesplus.CratesPlus;
import com.connorlinfoot.cratesplus.Handlers.CrateHandler;
import com.connorlinfoot.cratesplus.Handlers.MessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CrateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("cratesplus.admin")) {
            sender.sendMessage(CratesPlus.pluginPrefix + MessageHandler.getMessage(CratesPlus.getPlugin(), "Command No Permission", (Player) sender, "Unknown"));
            return false;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            // Didn't really wanna add this, but so many requests so why the hell not

            CratesPlus.getPlugin().reloadConfig();
            CratesPlus.crates.clear();
            // Register Crates
            for (String crate : CratesPlus.getPlugin().getConfig().getConfigurationSection("Crates").getKeys(false)) {
                CratesPlus.crates.put(crate, new Crate(crate));
            }

            sender.sendMessage(ChatColor.GREEN + "CratesPlus configuration was reloaded - This feature is not fully tested and may not work");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("create")) {
            // /crate crate <name>
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Correct Usage: /crate create <name>");
                return false;
            }

            String name = args[1];
            FileConfiguration config = CratesPlus.getPlugin().getConfig();
            if (config.isSet("Crates." + name)) {
                sender.sendMessage(ChatColor.RED + name + " crate already exists");
                return false;
            }

            List<String> items = new ArrayList<String>();
            items.add("IRON_SWORD:1:DAMAGE_ALL-3");
            config.set("Crates." + name + ".Items", items);
            config.set("Crates." + name + ".Knockback", 0.0);
            config.set("Crates." + name + ".Broadcast", false);
            config.set("Crates." + name + ".Firework", false);
            config.set("Crates." + name + ".Color", "WHITE");
            CratesPlus.getPlugin().saveConfig();

            sender.sendMessage(ChatColor.GREEN + name + " crate has been created");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("delete")) {
            // /crate delete <name>
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Correct Usage: /crate delete <name>");
                return false;
            }

            String name = args[1];
            FileConfiguration config = CratesPlus.getPlugin().getConfig();
            if (!config.isSet("Crates." + name)) {
                sender.sendMessage(ChatColor.RED + name + " crate doesn't exist");
                return false;
            }

            config.set("Crates." + name, null);
            CratesPlus.getPlugin().saveConfig();

            sender.sendMessage(ChatColor.GREEN + name + " crate has been deleted");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("key")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Correct Usage: /crate key <player> [type]");
                return false;
            }

            Player player = null;
            if (!args[1].equalsIgnoreCase("all")) {
                player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "The player " + args[1] + " was not found");
                    return false;
                }
            }

            String crateType = null;
            if (args.length >= 3) {
                crateType = args[2];
            }

            if (crateType != null) {
                if (player == null) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        CrateHandler.giveCrateKey(p, crateType);
                    }
                } else {
                    CrateHandler.giveCrateKey(player, crateType);
                }
            } else {
                if (player == null) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        CrateHandler.giveCrateKey(p);
                    }
                } else {
                    CrateHandler.giveCrateKey(player);
                }
            }

            if (player == null) {
                sender.sendMessage(ChatColor.GREEN + "Given all players " + ChatColor.RESET + ChatColor.GREEN + " a crate key");
            } else {
                sender.sendMessage(ChatColor.GREEN + "Given " + player.getDisplayName() + ChatColor.RESET + ChatColor.GREEN + " a crate key");
            }
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("crate")) {
            Player player;
            String crateType;
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Correct Usage: /crate crate <type> [player]");
                return false;
            }

            if (args.length == 3) {
                player = Bukkit.getPlayer(args[2]);
            } else if (sender instanceof Player) {
                player = (Player) sender;
            } else {
                sender.sendMessage(ChatColor.RED + "Correct Usage: /crate crate <type> [player]");
                return false;
            }

            if (player == null) {
                sender.sendMessage(ChatColor.RED + "The player " + args[1] + " was not found");
                return false;
            }

            try {
                crateType = args[1];
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Please specify a valid crate type");
                return false;
            }
            CrateHandler.giveCrate(player, crateType);

            sender.sendMessage(ChatColor.GREEN + "Given " + player.getDisplayName() + ChatColor.RESET + ChatColor.GREEN + " a crate");
            return true;
        }

        // Help Messages
        sender.sendMessage(CratesPlus.pluginPrefix + ChatColor.AQUA + "----- CratePlus Help -----");
        sender.sendMessage(CratesPlus.pluginPrefix + ChatColor.AQUA + "/crate reload - Reload configuration for CratesPlus (Experimental)");
        sender.sendMessage(CratesPlus.pluginPrefix + ChatColor.AQUA + "/crate key <player> [type] - Give player a random crate key");
        sender.sendMessage(CratesPlus.pluginPrefix + ChatColor.AQUA + "/crate crate <type> [player] - Give player a crate to be placed");

        return true;
    }

}
