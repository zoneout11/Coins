package me.justeli.coins.settings;

/**
 * Created by Eli on 12/14/2016.
 *
 */

public enum Config
{
    ;
    public enum BOOLEAN
    {
        stackCoins,
        spawnerDrop,
        passiveDrop,
        pickupSound,
        loseOnDeath,
        olderServer,
        playerDrop,
        preventAlts,
        enableWithdraw,
        dropEachCoin,
        preventSplits,
        newerServer,
        takePercentage,
        dropOnDeath,
        onlyExperienceBlocks,
        disableHoppers,
        dropWithAnyDeath,
        coinsEconomy,
        coinsEffect,
        trackEconomy,
        enchantedCoin,
    }

    public enum STRING
    {
        nameOfCoin,
        coinItem,
        depositMessage,
        withdrawMessage,
        soundName,
        mobMultiplier,
        currencySymbol,
        multiSuffix,
        skullTexture,
        displayCurrency,
    }

    public enum DOUBLE
    {
        dropChance,
        maxWithdrawAmount,
        moneyAmount_from,
        moneyAmount_to,
        moneyTaken_from,
        moneyTaken_to,
        moneyDecimals,
        minePercentage,
        soundPitch,
        soundVolume,
        limitForLocation,
        percentagePlayerHit,
        startingBalance,
        maximumAllowed,
    }

    public enum ARRAY
    {
        disabledWorlds,
    }
}
