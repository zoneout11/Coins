package me.justeli.coins.commands;

import me.justeli.api.shaded.commands.Command;
import me.justeli.api.shaded.commands.CommandDetails;
import me.justeli.api.shaded.commands.Commander;
import me.justeli.api.shaded.tabcompletion.Completer;
import me.justeli.api.shaded.tabcompletion.Completion;
import me.justeli.coins.Coins;
import me.justeli.coins.api.Complete;
import me.justeli.coins.api.Extras;
import me.justeli.coins.economy.CoinsAPI;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Messages;
import me.justeli.coins.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoinCommands
        implements Commander, Completer
{
    private final Coins instance;

    public CoinCommands (Coins instance)
    {
        this.instance = instance;
    }

    //todo give command



    @Command (aliases = "coin")
    public boolean coins (CommandDetails details)
    {
        String[] args = details.getArgs();
        CommandSender sender = details.getSender();

        if (args.length >= 1)
        {
            switch (args[0])
            {
                case "reload":
                    if (sender.hasPermission("coins.admin"))
                    {
                        long ms = System.currentTimeMillis();
                        Settings.remove();
                        Settings.remove();
                        Extras.resetMultiplier();
                        boolean success = instance.getSettings().initConfig();
                        sender.sendMessage(Messages.RELOAD_SUCCESS.toString().replace("{0}", Long.toString(System.currentTimeMillis() - ms)));

                        if (!success) sender.sendMessage(Messages.MINOR_ISSUES.toString());
                        else sender.sendMessage(Messages.CHECK_SETTINGS.toString());
                    }
                    else
                        noPerm(sender);
                    break;
                case "settings":
                    if (sender.hasPermission("coins.admin"))
                    {
                        String settings = Settings.getSettings();
                        sender.sendMessage(settings);
                    }
                    else
                        noPerm(sender);
                    break;
                case "drop":
                    if (sender.hasPermission("coins.drop"))
                        dropCoins(sender, args);
                    else
                        noPerm(sender);
                    break;
                case "give":
                    if (sender.hasPermission("coins.give")) {}
                        //giveCoins (sender, args);
                    else
                        noPerm(sender);
                    break;
                case "remove":
                    if (sender.hasPermission("coins.remove"))
                        removeCoins(sender, args);
                    else
                        noPerm(sender);
                    break;
                case "lang":
                    for (Messages m : Messages.values())
                        sender.sendMessage(m.toString());
                    break;
                case "version":
                case "update":
                    if (sender.hasPermission("coins.admin"))
                    {
                        String version = Coins.getUpdate();
                        String current = Coins.getInstance().getDescription().getVersion();
                        sender.sendMessage(Messages.VERSION_CURRENTLY.format(current));
                        sender.sendMessage(Messages.LATEST_VERSION.format(version));
                        if (version.equals(current))
                            sender.sendMessage(Messages.UP_TO_DATE.format(current));
                        else
                        {
                            sender.sendMessage(Messages.CONSIDER_UPDATING.format(version));
                            sender.sendMessage("https://www.spigotmc.org/resources/coins.33382/");
                        }
                    }
                    break;
                default:
                    sendHelp(sender);
                    break;
            }
        }
        else
        {
            sendHelp(sender);
        }
        return true;
    }

    @Completion (commands = {"coin", "coins"})
    public List<String> completion (CommandSender sender)
    {/*
        List<String> list = new ArrayList<>();
        if (args.length == 1)
        {
            if (sender.hasPermission("coins.drop"))
                list.add("drop");
            if (sender.hasPermission("coins.admin"))
            {
                list.add("reload");
                list.add("settings");
                list.add("version");
            }
            if (sender.hasPermission("coins.remove"))
                list.add("remove");
        }
        else if (args.length == 2)
        {
            if (args[0].equalsIgnoreCase("remove"))
                if (sender.hasPermission("coins.remove"))
                {
                    list.add("all");
                    list.add("[radius]");
                }
            if (args[0].equalsIgnoreCase("drop"))
            {
                for (Player p : Bukkit.getOnlinePlayers())
                    list.add(p.getName());
                list.add("<x,y,z>");
                list.add("<x,y,z,world>");
            }
        }
        else if (sender.hasPermission("coins.remove"))
        {
            if (args.length == 3)
                list.add("<amount>");
            if (args.length == 4)
                list.add("[radius]");
        }

        Collections.sort(list);
        return list;*/
        return new ArrayList<>();
    }

    private void dropCoins (CommandSender sender, String[] args)
    {
        if (args.length >= 3)
        {
            Player p = Complete.onlinePlayer(args[1]);

            int amount;
            try {amount = Integer.parseInt(args[2]); }
            catch (NumberFormatException e)
            {
                sender.sendMessage(Messages.INVALID_NUMBER.toString());
                return;
            }

            int radius = amount / 20;
            if (radius < 2)
                radius = 2;

            if (args.length >= 4)
            {
                try {radius = Integer.parseInt(args[3]);}
                catch (NumberFormatException e)
                {
                    sender.sendMessage(Messages.INVALID_NUMBER.toString());
                    return;
                }
            }

            Location location;
            String name;
            if (p == null)
            {
                if (!args[1].contains(","))
                {
                    sender.sendMessage(Messages.PLAYER_NOT_FOUND.toString());
                    return;
                }
                else
                {
                    try
                    {
                        String[] coords = args[1].split(",");
                        double x = Double.parseDouble(coords[0]);
                        double y = Double.parseDouble(coords[1]);
                        double z = Double.parseDouble(coords[2]);

                        location = new Location(coords.length == 4? Bukkit.getWorld(coords[3]) : (sender instanceof Player? ((Player) sender)
                                .getWorld() : Bukkit.getWorlds().get(0)), x, y, z);
                        name = x + ", " + y + ", " + z;
                    }
                    catch (NumberFormatException | ArrayIndexOutOfBoundsException | NullPointerException e)
                    {
                        sender.sendMessage(Messages.COORDS_NOT_FOUND.toString());
                        return;
                    }
                }

            }
            else
            {
                location = p.getLocation();
                name = p.getName();
            }

            if (p != null || sender instanceof Player)
            {
                if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains(((Player)sender).getWorld().getName()))
                {
                    sender.sendMessage(Messages.COINS_DISABLED.toString());
                    return;
                }
            }

            if (radius < 1 || radius > 80)
            {
                sender.sendMessage(Messages.INVALID_RADIUS.toString());
                return;
            }

            if (amount < 1 || amount > 1000)
            {
                sender.sendMessage(Messages.INVALID_AMOUNT.toString());
                return;
            }

            CoinsAPI.particles(location, radius, amount);
            sender.sendMessage(Messages.SPAWNED_COINS.toString().replace("{0}", Long.toString(amount)).replace("{1}",
                    Long.toString(radius)).replace("{2}", name));
        }
        else
        {
            sender.sendMessage(Messages.DROP_USAGE.toString());
        }

    }

    private void removeCoins (CommandSender sender, String[] args)
    {
        double r = 0;
        List<Entity> mobs = Bukkit.getWorlds().get(0).getEntities();
        if (args.length >= 2 && sender instanceof Player)
        {
            if (!args[1].equalsIgnoreCase("all"))
            {
                try {r = Integer.parseInt(args[1]);}
                catch (NumberFormatException e)
                {
                    sender.sendMessage(Messages.INVALID_RADIUS.toString());
                    return;
                }
                if (r < 1 || r > 80)
                {
                    sender.sendMessage(Messages.INVALID_RADIUS.toString());
                    return;
                }
            }

        }

        if (sender instanceof Player)
        {
            Player p = (Player) sender;
            mobs = p.getWorld().getEntities();
            if (r != 0)
                mobs = new ArrayList<>(p.getWorld().getNearbyEntities(p.getLocation(), r, r, r));
        }

        long amount = 0;
        for (Entity m : mobs)
        {
            if (m instanceof Item)
            {
                Item i = (Item) m;
                if (i.getItemStack().getItemMeta() != null && i.getItemStack().getItemMeta().hasDisplayName())
                {
                    if (i.getItemStack().getItemMeta().getDisplayName().equals((Config.get(Config.STRING.NAME_OF_COIN))))
                    {
                        amount++;
                        double random = (Math.random() * 3);
                        long rand = (long) random * 5;
                        i.setVelocity(new Vector(0, random, 0));
                        new BukkitRunnable()
                        {
                            int a = 0;

                            public void run ()
                            {
                                a += 1;
                                if (a >= 1)
                                {
                                    i.remove();
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(Coins.getInstance(), rand, rand);
                    }
                }
            }
        }
        sender.sendMessage(Messages.REMOVED_COINS.toString().replace("{0}", Long.toString(amount)));
    }

    private void sendHelp (CommandSender sender)
    {
        String version = Coins.getInstance().getDescription().getVersion();
        String update = Coins.getUpdate();
        StringBuilder notice = new StringBuilder(Messages.COINS_HELP.toString()).append(" ").append(version);

        if (!update.equals(version))
            notice.append(" (outdated → /coins update)");

        sender.sendMessage(notice.toString());

        if (sender.hasPermission("coins.drop"))
            sender.sendMessage(Messages.DROP_USAGE.toString());

        if (sender.hasPermission("coins.remove"))
            sender.sendMessage(Messages.REMOVE_USAGE.toString());

        if (sender.hasPermission("coins.give"))
        {
            //sender.sendMessage(Messages.GIVE_USAGE.toString());
        }

        if (sender.hasPermission("coins.admin"))
        {
            sender.sendMessage(Messages.SETTINGS_USAGE.toString());
            sender.sendMessage(Messages.RELOAD_USAGE.toString());
            sender.sendMessage(Messages.VERSION_CHECK.toString());
        }

        if (Config.get(Config.BOOLEAN.ENABLE_WITHDRAW) && sender.hasPermission("coins.withdraw"))
            sender.sendMessage(Messages.WITHDRAW_USAGE.toString());
    }

    private void noPerm (CommandSender sender)
    {
        sender.sendMessage(Messages.NO_PERMISSION.toString());
    }
}
