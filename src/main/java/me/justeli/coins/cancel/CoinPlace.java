package me.justeli.coins.cancel;

import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.item.CheckCoin;
import org.bukkit.ChatColor;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by Eli on 2/4/2017.
 */

public class CoinPlace
        implements Listener
{
    @EventHandler
    public void coinPlace (PlayerInteractEvent e)
    {
        if (e.getAction().equals(Action.PHYSICAL) || e.getItem() == null)
            return;

        CheckCoin coin = new CheckCoin(e.getItem());
        if (!coin.is())
            return;

        Player p = e.getPlayer();
        if (!p.hasPermission("coins.withdraw"))
        {
            e.setCancelled(true);
            return;
        }

        if (e.getClickedBlock() == null || !(e.getClickedBlock().getState() instanceof Container))
        {
            e.setCancelled(true);
            int multi = e.getItem().getAmount();
            e.getItem().setAmount(0);

            double amount = coin.worth();
            CoinsPickup.addMoney(p, amount * multi);
        }
    }
}
