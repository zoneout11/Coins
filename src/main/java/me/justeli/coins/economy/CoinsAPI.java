package me.justeli.coins.economy;

import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.item.CoinParticles;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * Created by Eli on May 17, 2020.
 * Coins: me.justeli.coins.economy
 */
public class CoinsAPI
{
    // setBalance
    public static double receivedWhileOffline (UUID uuid)
    {
        return CoinStorage.getStorage(uuid).getDouble("offlineBalance");
    }

    public static void playEffect (Location location, int amount)
    {
        CoinsEffect.coinsEffect(location, amount);
    }

    public static boolean mobFromSpawner (Entity entity)
    {
        return PreventSpawner.fromSpawner(entity);
    }

    public static void particles (Location location, int radius, int amount)
    {
        CoinParticles.dropCoins(location, radius, amount);
    }
}
