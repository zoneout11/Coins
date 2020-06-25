package me.justeli.coins.main;

import me.justeli.coins.Coins;
import me.justeli.coins.api.Complete;
import me.justeli.coins.api.Extras;
import me.justeli.coins.api.Format;
import me.justeli.coins.economy.CoinsAPI;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Messages;
import me.justeli.coins.settings.Settings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class CoinCommands
        implements CommandExecutor
{
    private final Coins intance;

    public CoinCommands (Coins intance)
    {
        this.intance = intance;
    }

    @Override
    public boolean onCommand (CommandSender sender, Command cmd, String l, String[] args)
    {
        if (l.equalsIgnoreCase("coins") || l.equalsIgnoreCase("coin"))
        {
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
                            boolean success = intance.getSettings().initConfig();
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
                sendHelp(sender);

            return true;
        }

        else if (l.equalsIgnoreCase("withdraw"))
        {
            if (!Config.get(Config.BOOLEAN.ENABLE_WITHDRAW))
                return false;

            if (!sender.hasPermission("coins.withdraw") || !(sender instanceof Player))
            {
                noPerm(sender);
                return true;
            }

            if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains(((Player)sender).getWorld().getName()))
            {
                sender.sendMessage(Messages.COINS_DISABLED.toString());
                return true;
            }

            Player p = (Player) sender;

            if (args.length >= 1)
            {
                long amount;

                try { amount = Integer.parseInt(args[0]); }
                catch (NumberFormatException e)
                {
                    sender.sendMessage(Messages.INVALID_AMOUNT.toString());
                    return true;
                }

                if (amount > 0 && amount <= Config.get(Config.DOUBLE.MAX_WITHDRAW_AMOUNT) && Coins.getEconomy().getBalance(p) >= amount)
                {
                    if (p.getInventory().firstEmpty() == -1)
                    {
                        p.sendMessage(Messages.INVENTORY_FULL.toString());
                        return true;
                    }
                    p.getInventory().addItem(new Coin(amount).withdraw().create());
                    Coins.getEconomy().withdrawPlayer(p, amount);
                    p.sendMessage(Messages.WITHDRAW_COINS.toString().replace("{0}", Long.toString(amount)));

                    if (!Config.get(Config.BOOLEAN.COINS_ECONOMY))
                    {
                        String bar = Format.color(Config.get(Config.STRING.WITHDRAW_MESSAGE).replace("{%amount}", String.valueOf(amount))
                                .replace("{$}", Config.get(Config.STRING.CURRENCY_SYMBOL)));
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar));
                    }
                }
                else p.sendMessage(Messages.NOT_THAT_MUCH.toString());
            }
            else p.sendMessage(Messages.WITHDRAW_USAGE.toString());
        }
        return false;
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
            sender.sendMessage(Messages.SPAWNED_COINS.toString().replace("{0}", Long.toString(amount)).replace("{1}", Long.toString(radius)).replace("{2}", name));

        }
        else
            sender.sendMessage(Messages.DROP_USAGE.toString());

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
        String notice = "";
        if (!update.equals(version))
            notice = " (outdated; /coins update)";

        sender.sendMessage(Messages.COINS_HELP.toString() + " " + version + notice);

        if (sender.hasPermission("coins.drop"))
            sender.sendMessage(Messages.DROP_USAGE.toString());

        if (sender.hasPermission("coins.remove"))
            sender.sendMessage(Messages.REMOVE_USAGE.toString());

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
