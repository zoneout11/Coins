package me.justeli.coins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.justeli.coins.cancel.CancelInventories;
import me.justeli.coins.cancel.CoinPlace;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.economy.CoinStorage;
import me.justeli.coins.economy.CoinsEconomy;
import me.justeli.coins.economy.CoinsEffect;
import me.justeli.coins.events.BukkitEvents;
import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.events.DropCoin;
import me.justeli.coins.events.PaperEvents;
import me.justeli.coins.main.CoinCommands;
import me.justeli.coins.main.TabComplete;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Messages;
import me.justeli.coins.settings.Settings;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

/**
 * Created by Eli on 12/13/2016.
 */

public class Coins
        extends JavaPlugin
{
    private static Coins instance;
    private static Economy economy;

    private Settings settings;

    public Settings getSettings ()
    {
        return settings;
    }

    private static String update;

    public static String getUpdate ()
    {
        return update;
    }

    // todo use integrated bstats metrics from spigot
    // todo host on a maven repository
    // https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-apache-maven-for-use-with-github-packages
    // todo proper formatting before saving to disk i.e.  %.2f
    // todo extending the /withdraw command to /witdraw [amount to withdraw] [how many times] so "/withdraw 5 64" would give me a stack of 5coin sunflowers
    // todo add option to not let balance go negative (with dropOnDeath: true)
    // todo coin and/or bill textures using NBT data and a resource pack
    // todo Can you add config for specific blocks for mining?
    // todo set different materials as different worths, ex: you could have bronze, silver and gold coins
    // todo generating of coins in dungeons chests
    // todo don't drop/take coins when player has balance below 0
    // todo Coins Protection : true #when coins drop they can only be picked up for a short moment by the one who make them drop.

    // https://www.spigotmc.org/resources/pickupmoney.11334/
    // chinese site: https://www.mcbbs.net/thread-1051835-1-1.html

    @Override
    public void onEnable ()
    {
        Locale.setDefault(Locale.US);
        instance = this;

        if (Bukkit.getVersion().contains("Bukkit"))
        {
            getLogger().severe(ChatColor.RED.toString() + ChatColor.UNDERLINE + Messages.USING_BUKKIT);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        else if (!Bukkit.getVersion().contains("Paper"))
        {
            getLogger().warning(ChatColor.YELLOW.toString() + ChatColor.UNDERLINE + Messages.USE_PAPER);
        }

        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            getLogger().severe(ChatColor.RED.toString() + ChatColor.UNDERLINE + Messages.INSTALL_VAULT);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        settings = new Settings(this);
        settings.initConfig();

        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (Config.get(Config.BOOLEAN.COINS_ECONOMY) || provider == null)
        {
            Bukkit.getServicesManager().register(Economy.class, new CoinsEconomy(), this, ServicePriority.Highest);
            CoinStorage.initPlayerData();

            provider = getServer().getServicesManager().getRegistration(Economy.class);
            Settings.setCoinsEconomy(true);
        }

        economy = provider.getProvider();

        registerCommands();
        registerEvents(new PreventSpawner(), new CoinsPickup(), new DropCoin(), new CoinPlace(), new CancelInventories(), new CoinsEffect());

        checkVersion();
        addMetrics();
    }

    private void addMetrics ()
    {
        Metrics metrics = new Metrics(this, 831);

        for (Config.STRING s : Config.STRING.values())
        {
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () ->
            {
                if (s.equals(Config.STRING.SKULL_TEXTURE))
                {
                    String texture = Config.get(s);
                    return String.valueOf(texture != null && !texture.isEmpty());
                }
                return Config.get(s);
            }));
        }

        for (Config.DOUBLE s : Config.DOUBLE.values())
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () -> String.valueOf(Config.get(s))));

        for (Config.BOOLEAN s : Config.BOOLEAN.values())
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () -> String.valueOf(Config.get(s))));
    }

    private void checkVersion ()
    {
        async(() ->
        {
            String version;
            try
            {
                URL url = new URL("https://api.github.com/repos/JustEli/Coins/releases/latest");
                URLConnection request = url.openConnection();
                request.connect();

                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(new InputStreamReader((InputStream) request.getContent()));
                JsonObject json = root.getAsJsonObject();
                version = json.get("tag_name").getAsString();

            }
            catch (IOException ex)
            {
                version = getDescription().getVersion();
            }

            Coins.update = version;

            if (!getDescription().getVersion().equals(version))
            {
                getLogger().warning(Messages.CONSIDER_UPDATING.format(version));
                getLogger().warning("https://www.spigotmc.org/resources/coins.33382/");
            }
        });
    }

    public static Economy getEconomy ()
    {
        return economy;
    }

    public static Coins getInstance ()
    {
        return instance;
    }

    private void registerEvents (Listener... listeners)
    {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(Bukkit.getVersion().contains("Paper")? new PaperEvents() : new BukkitEvents(), this);

        for (Listener listener : listeners)
            manager.registerEvents(listener, this);
    }

    private CoinCommands coinCommands;

    public CoinCommands getCoinCommands ()
    {
        return coinCommands;
    }

    private void registerCommands ()
    {
        coinCommands = new CoinCommands(this);

        getCommand("coins").setExecutor(coinCommands);
        getCommand("coins").setTabCompleter(new TabComplete());

        if (Config.get(Config.BOOLEAN.ENABLE_WITHDRAW))
        {
            getCommand("withdraw").setExecutor(coinCommands);
            getCommand("withdraw").setTabCompleter(new TabComplete());
        }
    }

    private static void async (Runnable runnable)
    {
        new BukkitRunnable()
        {
            @Override
            public void run ()
            {
                runnable.run();
            }
        }.runTaskAsynchronously(getInstance());
    }

    public static void later (final int ticks, Runnable runnable)
    {
        new BukkitRunnable()
        {
            @Override
            public void run ()
            {
                runnable.run();
            }
        }.runTaskLater(getInstance(), ticks);
    }
}
