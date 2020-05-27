package me.justeli.coins.economy;

import java.util.UUID;

/**
 * Created by Eli on May 17, 2020.
 * Coins: me.justeli.coins.economy
 */
public class CoinsAPI
{
    // setBalance
    // getOfflineReceived
    public static double receivedWhileOffline (UUID uuid)
    {
        return CoinStorage.getStorage(uuid).getDouble("offlineBalance");
    }
}
