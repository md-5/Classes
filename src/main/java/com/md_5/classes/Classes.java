package com.md_5.classes;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;
import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.interfaces.PermissionSet;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Classes extends JavaPlugin {

    public String world;
    public ArrayList<Class> classes = new ArrayList<Class>();
    public PermissionSet worldPermissions;

    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration conf = getConfig();
        conf.options().copyDefaults(true);
        world = conf.getString("world");
        ConfigurationSection cs = conf.getConfigurationSection("classes");
        for (String key : cs.getKeys(false)) {
            classes.add(new Class(key.toLowerCase(), cs.getString(key + ".description"), cs.getDouble(key + ".cost")));
        }
        worldPermissions = Permissions.getWorldPermissionsManager().getPermissionSet(world);
        System.out.println("Classes by md_5 enabled");
    }

    public void onDisable() {
        System.out.println("Classes by md_5 disabled");
    }

    private Class getPlayerClass(String player) {
        for (String group : getGroups(player)) {
            final Class clazz = getClass(group);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    private String getName(Class clazz) {
        if (clazz != null) {
            return clazz.name;
        } else {
            return "<not selected>";
        }
    }

    private String getDescription(Class clazz) {
        if (clazz != null) {
            return clazz.description;
        } else {
            return "";
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String player = sender.getName();
        final Class prevClass = getPlayerClass(player);
        // Help
        if (args.length == 0) {
            sender.sendMessage(ChatColor.DARK_GRAY + "---====" + ChatColor.GRAY + "[ " + ChatColor.RED + "DarkVoid" + ChatColor.GRAY + " - " + ChatColor.GOLD + "Classes Help" + ChatColor.GRAY + " ]" + ChatColor.DARK_GRAY + "====---");
            sender.sendMessage(ChatColor.GREEN + "Name: " + ChatColor.WHITE + player);
            sender.sendMessage(ChatColor.GREEN + "Current Class: " + ChatColor.WHITE + getName(prevClass) + ChatColor.GRAY + " - " + ChatColor.WHITE + "[ " + getDescription(prevClass) + " ]");
            sender.sendMessage(ChatColor.GREEN + " /class list");
            sender.sendMessage(ChatColor.GREEN + " /class choose " + ChatColor.BLUE + "<class name>");
            sender.sendMessage(ChatColor.GREEN + " /class who " + ChatColor.BLUE + "<player name>");
            return true;
        }
        // List
        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(ChatColor.BLUE + "You can join the following classes:");
            for (Class clazz : classes) {
                sender.sendMessage(ChatColor.GREEN + clazz.name + " - " + clazz.description + " - $" + clazz.cost);
            }
            sender.sendMessage(ChatColor.AQUA + "To choose a class, type /class choose <class name>");
            return true;
        }
        // Choose
        if (args[0].equalsIgnoreCase("choose")) {
            Class clazz = getClass(args[1]);
            if (clazz != null) {
                Holdings account = iConomy.getAccount(player).getHoldings();
                if (!account.hasEnough(clazz.cost)) {
                    sender.sendMessage(ChatColor.YELLOW + "Error! You cannot afford to change your class!");
                    return true;
                }
                if (!sender.hasPermission("classes." + clazz.name)) {
                    sender.sendMessage(ChatColor.RED + "You are not allowed to be in that class!");
                    return true;
                }
                for (String group : getGroups(sender.getName())) {
                    if (getClass(group) != null) {
                        worldPermissions.removeGroup(sender.getName(), group);
                    }
                }
                sender.sendMessage(ChatColor.YELLOW + "All your previous classes have been removed");
                worldPermissions.addGroup(sender.getName(), args[1]);
                sender.sendMessage(ChatColor.YELLOW + "You have been added to your new class");
                account.subtract(clazz.cost);
                sender.sendMessage(ChatColor.YELLOW + "$" + clazz.cost + " has been removed from your account");
            } else {
                sender.sendMessage(ChatColor.RED + "The specified class does not exist");
            }
            return true;
        }
        // Who
        if (args[0].equalsIgnoreCase("who")) {
            Class clazz = getPlayerClass(args[1]);
            if (clazz != null) {
                sender.sendMessage(ChatColor.YELLOW + args[1] + " is in class: " + clazz.name);
            } else {
                sender.sendMessage(ChatColor.RED + args[1] + " is in no classes");
            }
            return true;
        }
        // Invalid
        sender.sendMessage(ChatColor.RED + "Classes: That is not a valid command! Use /class to get help");
        return true;
    }

    private Class getClass(String className) {
        for (Class clazz : classes) {
            if (className.equalsIgnoreCase(clazz.name)) {
                return clazz;
            }
        }
        return null;
    }

    private List<String> getGroups(String player) {
        return worldPermissions.getGroups(player);
    }

    private class Class {

        String name, description;
        double cost;

        Class(String name, String description, double cost) {
            this.name = name;
            this.description = description;
            this.cost = cost;
        }
    }
}
