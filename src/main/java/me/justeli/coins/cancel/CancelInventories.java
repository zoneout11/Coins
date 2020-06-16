package me.justeli.coins.cancel;

import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.item.CheckCoin;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Eli on 2 mei 2019.
 * spigotPlugins: me.justeli.coins.cancel
 */
public class CancelInventories
        implements Listener
{
    @EventHandler
    public void avoidCraftingTable (CraftItemEvent e)
    {
        for (ItemStack stack : e.getInventory().getContents())
            if (new CheckCoin(stack).is())
                e.setCancelled(true);
    }

    @EventHandler
    public void on (PrepareAnvilEvent e)
    {
        if (e.getResult() != null && new CheckCoin(e.getResult()).is())
            e.setResult(null);
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHopper (InventoryPickupItemEvent e)
    {
        if (!e.getInventory().getType().equals(InventoryType.HOPPER))
            return;

        CheckCoin coin = new CheckCoin(e.getItem().getItemStack());
        if (!coin.is())
            return;

        if (Config.get(Config.BOOLEAN.DISABLE_HOPPERS))
        {
            e.setCancelled(true);
        }
        else if (coin.isUnique())
        {
            e.getItem().setItemStack(new Coin(coin.worth()).create());
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void coinInventory (InventoryClickEvent e)
    {
        if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains(e.getWhoClicked().getWorld().getName()))
            return;

        if (!(e.getWhoClicked() instanceof Player))
            return;

        ItemStack item = e.getCurrentItem();
        if (item == null)
            return;

        CheckCoin coin = new CheckCoin(item);

        if (coin.is())
        {
            if (!e.getWhoClicked().hasPermission("coins.creative") && e.getWhoClicked().getGameMode().equals(GameMode.CREATIVE))
            {
                removeCreativeCoins(e, e.getCurrentItem());
                return;
            }

            Player p = (Player) e.getWhoClicked();

            e.setCancelled(true);
            CoinsPickup.giveReward(item.getAmount(), coin, p);
            e.getCurrentItem().setAmount(0);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onMiddleClick2 (InventoryCreativeEvent e)
    {
        if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains(e.getWhoClicked().getWorld().getName()))
            return;

        if (e.getWhoClicked() instanceof Player)
            if (!e.getWhoClicked().hasPermission("coins.creative"))
                removeCreativeCoins(e, e.getCursor());
    }

    private void removeCreativeCoins (InventoryClickEvent e, ItemStack item)
    {
        e.setCancelled(true);

        e.getInventory().remove(item);
        e.setCurrentItem(new ItemStack(Material.AIR));

        if (e.getClickedInventory() != null)
            e.getClickedInventory().remove(item);
    }
}
