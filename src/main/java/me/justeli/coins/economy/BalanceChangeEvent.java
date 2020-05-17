package me.justeli.coins.economy;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Eli on 5/20/2017.
 * Coins: me.justeli.coins.economy
 */
public class BalanceChangeEvent
        extends Event
        implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final double newAmount;
    private boolean cancelled;
    private final double previousAmount;

    public BalanceChangeEvent (Player eventPlayer, double newAmount, double previousAmount)
    {
        this.player = eventPlayer;
        this.newAmount = newAmount;
        this.previousAmount = previousAmount;
    }

    // ¢ //

    public Player getPlayer ()
    {
        return player;
    }

    public double getNewAmount ()
    {
        return newAmount;
    }

    public double getPreviousAmount ()
    {
        return previousAmount;
    }

    public HandlerList getHandlers ()
    {
        return handlers;
    }

    public static HandlerList getHandlerList ()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled ()
    {
        return cancelled;
    }

    @Override
    public void setCancelled (boolean cancel)
    {
        cancelled = cancel;
    }
}