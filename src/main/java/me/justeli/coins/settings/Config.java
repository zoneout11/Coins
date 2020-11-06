package me.justeli.coins.settings;

import me.justeli.coins.item.Coin;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;

/**
 * Created by Eli on November 04, 2020.
 * Coins: me.justeli.coins.settings
 */
public class Config
{
    private static String language;
    private static char currencySymbol;
    private static String singleCoinName;
    private static String pluralCoinName;
    private static ChatColor coinColor;
    private static int coinDecimals;

    private static final HashMap<String, Coin> coins = new HashMap<>();



    public static String getLanguage ()
    {
        return language;
    }

    public static char getCurrencySymbol ()
    {
        return currencySymbol;
    }

    public static String getSingleCoinName ()
    {
        return singleCoinName;
    }

    public static String getPluralCoinName ()
    {
        return pluralCoinName;
    }

    public static ChatColor getCoinColor ()
    {
        return coinColor;
    }

    public static Coin getCoin (String name)
    {
        return coins.computeIfAbsent(name, empty -> null);
    }

    public static int getCoinDecimals ()
    {
        return coinDecimals;
    }







    static void setLanguage (String language)
    {
        Config.language = language;
    }

    static void setCurrencySymbol (char currencySymbol)
    {
        Config.currencySymbol = currencySymbol;
    }

    static void setSingleCoinName (String singleCoinName)
    {
        Config.singleCoinName = singleCoinName;
    }

    static void setPluralCoinName (String pluralCoinName)
    {
        Config.pluralCoinName = pluralCoinName;
    }

    static void setCoinColor (ChatColor coinColor)
    {
        Config.coinColor = coinColor;
    }

    static void addCoin (String name, Coin coin)
    {
        Config.coins.put(name, coin);
    }

    static void setCoinDecimals (int coinDecimals)
    {
        Config.coinDecimals = coinDecimals;
    }
}
