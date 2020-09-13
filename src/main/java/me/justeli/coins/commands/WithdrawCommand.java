package me.justeli.coins.commands;

import me.justeli.api.shaded.commands.Command;
import me.justeli.api.shaded.commands.Commander;
import me.justeli.api.shaded.tabcompletion.Completer;
import me.justeli.api.shaded.tabcompletion.Completion;
import me.justeli.coins.Coins;
import me.justeli.coins.api.Format;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Messages;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Eli on August 05, 2020.
 * Coins: me.justeli.coins.commands
 */
public class WithdrawCommand implements Commander, Completer
{
    @Command (permission = "coins.withdraw")
    public boolean withdraw (Player player, String[] args)
    {
        if (!Config.get(Config.BOOLEAN.ENABLE_WITHDRAW))
            return false;

        if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains((player).getWorld().getName()))
        {
            player.sendMessage(Messages.COINS_DISABLED.toString());
            return true;
        }

        if (args.length == 0)
        {
            player.sendMessage(Messages.WITHDRAW_USAGE.toString());
            return true;
        }

        if (player.getInventory().firstEmpty() == -1)
        {
            player.sendMessage(Messages.INVENTORY_FULL.toString());
            return true;
        }

        int coins;
        int amount;

        try
        {
            coins = Integer.parseInt(args[0]);
            amount = args.length == 2? Integer.parseInt(args[1]) : 1;
        }
        catch (NumberFormatException e)
        {
            player.sendMessage(Messages.INVALID_AMOUNT.toString());
            return true;
        }

        int cost = coins * amount;

        if (coins <= 0 || amount <= 0 || coins >= Config.get(Config.DOUBLE.MAX_WITHDRAW_AMOUNT) || Coins.getEconomy().getBalance(player) <= cost)
        {
            player.sendMessage(Messages.NOT_THAT_MUCH.toString());
            return true;
        }

        ItemStack coin = new Coin(coins).withdraw().create();
        coin.setAmount(amount);

        player.getInventory().addItem(coin);
        Coins.getEconomy().withdrawPlayer(player, cost);
        player.sendMessage(Messages.WITHDRAW_COINS.toString().replace("{0}", Long.toString(coins)));

        if (!Config.get(Config.BOOLEAN.COINS_ECONOMY))
        {
            String bar = Format.color(Config.get(Config.STRING.WITHDRAW_MESSAGE).replace("{display}", String.valueOf(coins))
                    .replace("{$}", Config.get(Config.STRING.CURRENCY_SYMBOL)));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar));
        }

        return true;
    }

    @Completion (commands = {"withdraw"})
    public List<String> completion (CommandSender sender)
    {
        List<String> numbers = new ArrayList<>();
        numbers.add("1-" + Config.get(Config.DOUBLE.MAX_WITHDRAW_AMOUNT).intValue());
        Collections.sort(numbers);
        return numbers;
    }
}
