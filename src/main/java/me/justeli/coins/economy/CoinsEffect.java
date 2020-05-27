package me.justeli.coins.economy;

import me.justeli.coins.Coins;
import me.justeli.coins.api.ActionBar;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Eli on 26 aug 2019.
 * survivalRocks: me.justeli.payment
 */
public class CoinsEffect implements Listener
{
    @EventHandler (priority = EventPriority.LOWEST)
    public void createAccount (PlayerJoinEvent e)
    {
        if (Settings.get(Config.BOOLEAN.coinsEconomy) && !Coins.getEconomy().hasAccount(e.getPlayer()))
            Coins.getEconomy().createPlayerAccount(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void deleteOfflineBalance (PlayerJoinEvent e)
    {
        CoinStorage.setStorage(e.getPlayer().getUniqueId(), "offlineBalance", null);
    }

    private final static HashMap<UUID, Double> pickup = new HashMap<>();

    @EventHandler
    public void coins (BalanceChangeEvent e)
    {
        if (!e.getPlayer().isOnline() || e.getPlayer().getPlayer() == null)
            return;

        Player p = e.getPlayer().getPlayer();
        double amount = e.getTransactionAmount();

        if (amount == 0)
            return;

        final UUID u = p.getUniqueId();
        pickup.put(u, amount + (pickup.containsKey(u)? pickup.get(u) : 0));
        final Double newAmount = pickup.get(u);

        String format = Coins.getEconomy().format(Math.abs(newAmount));
        new ActionBar(Settings.get(amount > 0? Config.STRING.depositMessage : Config.STRING.withdrawMessage).replace("{display}", format)).send(p);

        Runnable task = () ->
        {
            if (pickup.containsKey(u) && pickup.get(u).equals(newAmount))
                pickup.remove(u);
        };
        Bukkit.getScheduler().runTaskLater(Coins.getInstance(), task, Settings.get(Config.BOOLEAN.dropEachCoin)? 30L : 10L);

        if (amount < 0 && Settings.get(Config.BOOLEAN.coinsEffect))
            coinsEffect(p.getEyeLocation(), (int) -amount);
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHopper (InventoryPickupItemEvent e)
    {
        if (e.getInventory().getType().equals(InventoryType.HOPPER))
        {
            ItemStack item = e.getItem().getItemStack();
            if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName())
            {
                String pickupName = item.getItemMeta().getDisplayName();
                if (pickupName.startsWith("Glitch Coin")) e.setCancelled(true);
            }
        }
    }

    private static final ItemStack coin = new Coin().item();

    private static ItemStack getCoin ()
    {
        ItemMeta meta = coin.getItemMeta();
        if (meta != null) meta.setDisplayName("Glitch Coin " + ThreadLocalRandom.current().nextDouble());
        coin.setItemMeta(meta);

        return coin;
    }

    private static void coinsEffect (Location location, int amount)
    {
        int calculation = amount < 10? amount : (int) Math.log10(amount) * 10;

        World world = location.getWorld();
        if (world == null)
            return;

        for (int i = 0; i < calculation; i++)
        {
            Item item = world.dropItem(location.clone().subtract(0, 0.7, 0), getCoin());
            item.setPickupDelay(10000);
            item.setVelocity(location.getDirection()
                    .add(new Vector((Math.random() - 0.5) / 4, 0.5 + ((Math.random() - 0.5) / 4), (Math.random() - 0.5) / 4)).multiply(0.3));

            Coins.later(ThreadLocalRandom.current().nextInt(1, 8), item::remove);
        }
    }
}
