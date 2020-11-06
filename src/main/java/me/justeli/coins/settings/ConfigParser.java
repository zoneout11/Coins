package me.justeli.coins.settings;

import me.justeli.coins.Coins;
import me.justeli.coins.item.Coin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * Created by Eli on November 04, 2020.
 * Coins: me.justeli.coins.settings
 */
public class ConfigParser
{
    private final Coins instance;

    public ConfigParser (Coins instance)
    {
        this.instance = instance;
        updateConfig();
    }

    public void updateConfig ()
    {
        parseCoins();
    }

    private boolean parseCoins ()
    {
        ConfigurationSection coinSection = instance.getConfig().getConfigurationSection("coins");

        if (coinSection == null)
            return throwError("There is no section in your config defining coins.");

        for (String coinName : coinSection.getKeys(false))
        {
            ConfigurationSection options = instance.getConfig().getConfigurationSection("coins." + coinName);

            if (options == null)
                return throwError("Something is not right at the coin called '" + coinName + "'.");

            List<Double> worth = options.getDoubleList("worth");
            if (worth.size() != 2)
                return throwError("The worth range of coin '" + coinName + "' can only be 2 values.");

            Coin coin = new Coin(
                    instance,
                    coinName,
                    valueOfEnum(Material.class, options.getString("item", "GOLD_NUGGET")),
                    options.getString("skull", null),
                    valueOfEnum(Sound.class, options.getString("sound", "ITEM_ARMOR_EQUIP_GOLD")),
                    worth.get(0),
                    worth.get(1),
                    options.getBoolean("stack", true),
                    options.getInt("data", 0),
                    options.getBoolean("glow", false),
                    (float) options.getDouble("pitch", 0.5f)
            );

            if (!coin.isValid())
                return throwError(coin.getError());

            Config.addCoin(coinName, coin);
        }

        return true;
    }

    private void parseBoolean (String path)
    {

    }

    private void parseDouble (String path)
    {

    }

    private void parseLong (String path)
    {

    }

    private boolean throwError (String message)
    {
        instance.getLogger().severe("Config of 'Coins' has an error: " + message);
        return false;
    }

    private <E extends Enum<E>> E valueOfEnum (Class<E> passed, String name)
    {
        if (name == null)
            return null;

        try
        {
            return Enum.valueOf(passed, name.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException exception)
        {
            throwError(String.format("There is no %s called '%s'.", passed.getName(), name));
        }
        return null;
    }
}
