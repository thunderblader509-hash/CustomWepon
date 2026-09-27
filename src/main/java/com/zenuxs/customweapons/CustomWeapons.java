package com.zenuxs.customweapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CustomWeapons extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new WeaponListeners(), this);
        if (getCommand("customweapons") != null) {
            getCommand("customweapons").setExecutor(this);
        }
        getLogger().info("CustomWeapons Loaded Successfully!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /customweapons <dragon|bow|frozen|emerald|wither|wings>", NamedTextColor.YELLOW));
            return true;
        }

        ItemStack item = null;
        switch (args[0].toLowerCase()) {
            case "dragon" -> item = createWeapon(Material.NETHERITE_SWORD, "🐉 Dragon Blade", List.of(
                    Component.text("Shift + Right Click: Dragon Breath Attack", NamedTextColor.LIGHT_PURPLE),
                    Component.text("Right Click: Enderman Teleport", NamedTextColor.DARK_PURPLE)
            ));
            case "bow" -> item = createWeapon(Material.BOW, "🏹 Bow Shooter", List.of(
                    Component.text("Shift + Right Click: Auto-Shoot Instant Damage Arrows", NamedTextColor.RED)
            ));
            case "frozen" -> item = createWeapon(Material.DIAMOND_SWORD, "❄️ Frozen Blade", List.of(
                    Component.text("Shift + Right Click: Freeze Targeted Enemy", NamedTextColor.AQUA)
            ));
            case "emerald" -> item = createWeapon(Material.MACE, "💚 Emerald Hammer", List.of(
                    Component.text("Right Click: Forward Dash", NamedTextColor.GREEN),
                    Component.text("Shift + Right Click: Launch Enemy Upwards", NamedTextColor.DARK_GREEN),
                    Component.text("Shift + Left Click: Switch Mode (BREACH / DENSITY)", NamedTextColor.GOLD)
            ));
            case "wither" -> item = createWeapon(Material.NETHERITE_AXE, "💀 Wither Hammer", List.of(
                    Component.text("Shift + Right Click: Charge Next Hit with Wither", NamedTextColor.DARK_GRAY),
                    Component.text("Shift + Left Click: Grant Strength II (20s)", NamedTextColor.RED)
            ));
            case "wings" -> item = createWeapon(Material.ELYTRA, "🪽 Wings of Allays", List.of(
                    Component.text("Shift + Right Click (Flying): Flight Boost Upward", NamedTextColor.LIGHT_PURPLE)
            ));
            default -> player.sendMessage(Component.text("Unknown weapon name!", NamedTextColor.RED));
        }

        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(Component.text("Given " + args[0] + " successfully!", NamedTextColor.GREEN));
        }
        return true;
    }

    private ItemStack createWeapon(Material material, String name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
