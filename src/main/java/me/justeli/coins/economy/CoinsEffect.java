package me.justeli.coins.economy;

import me.justeli.coins.Coins;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Eli on 26 aug 2019.
 * survivalRocks: me.justeli.payment
 */
public class CoinsEffect implements Listener
{
    @EventHandler
    public void coins (BalanceChangeEvent e)
    {
        if (!Settings.hB.get(Config.BOOLEAN.coinsEffect))
            return;

        long difference = -(long) (e.getNewAmount() - e.getPreviousAmount());
        if (difference > 0) coinsEffect(e.getPlayer().getEyeLocation(), (int) difference);
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
