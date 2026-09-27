package com.zenuxs.customweapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WeaponListeners implements Listener {

    private final Map<UUID, String> emeraldHammerModes = new HashMap<>();
    private final Set<UUID> witherChargedPlayers = new HashSet<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta() || item.getItemMeta().displayName() == null) return;

        String name = item.getItemMeta().getDisplayName();
        Action action = event.getAction();

        // 1. Dragon Blade
        if (name.contains("Dragon Blade")) {
            if (player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
                Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(2));
                player.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 50, 1, 0.5, 1, 0.1);
                player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                for (Entity e : player.getNearbyEntities(5, 3, 5)) {
                    if (e instanceof LivingEntity target && target != player) {
                        target.damage(8.0, player);
                    }
                }
            } else if (!player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
                Location targetLoc = player.getTargetBlockExact(15) != null ? 
                        player.getTargetBlockExact(15).getLocation().add(0, 1, 0) : player.getLocation().add(player.getLocation().getDirection().multiply(8));
                targetLoc.setPitch(player.getLocation().getPitch());
                targetLoc.setYaw(player.getLocation().getYaw());
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 40, 0.5, 1, 0.5);
                player.teleport(targetLoc);
                player.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        }

        // 2. Bow Shooter
        if (name.contains("Bow Shooter") && player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            Arrow arrow = player.launchProjectile(Arrow.class);
            arrow.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.5f);
        }

        // 3. Frozen Blade
        if (name.contains("Frozen Blade") && player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            LivingEntity target = getTargetEntity(player, 10);
            if (target != null) {
                target.setFreezeTicks(140);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 10));
                target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5);
                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.8f);
                player.sendMessage(Component.text("Enemy Frozen!", NamedTextColor.AQUA));
            }
        }

        // 4. Emerald Hammer
        if (name.contains("Emerald Hammer")) {
            if (player.isSneaking() && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                String current = emeraldHammerModes.getOrDefault(player.getUniqueId(), "BREACH");
                String next = current.equals("BREACH") ? "DENSITY" : "BREACH";
                emeraldHammerModes.put(player.getUniqueId(), next);

                if (next.equals("BREACH")) {
                    player.sendActionBar(Component.text("⚔ BREACH MODE", NamedTextColor.GOLD, TextDecoration.BOLD));
                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1.0f, 1.2f);
                } else {
                    player.sendActionBar(Component.text("💥 DENSITY MODE", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.8f);
                }
            } else if (player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
                LivingEntity target = getTargetEntity(player, 8);
                if (target != null) {
                    target.setVelocity(new Vector(0, 1.6, 0));
                    target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, target.getLocation(), 30, 0.5, 1, 0.5);
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 1.0f, 0.8f);
                }
            } else if (!player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
                Vector dir = player.getLocation().getDirection().multiply(1.8).setY(0.4);
                player.setVelocity(dir);
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 20, 0.3, 0.3, 0.3);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.2f);
            }
        }

        // 5. Wither Hammer
        if (name.contains("Wither Hammer")) {
            if (player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
                witherChargedPlayers.add(player.getUniqueId());
                player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.8f, 1.2f);
                player.sendMessage(Component.text("Next hit charged with Wither!", NamedTextColor.DARK_GRAY));
            } else if (player.isSneaking() && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 400, 1));
                player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation().add(0, 1.5, 0), 15, 0.3, 0.3, 0.3);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);
            }
        }

        // 6. Wings of Allays
        if (name.contains("Wings of Allays") && player.isGliding() && player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            Vector boost = player.getLocation().getDirection().multiply(1.2).setY(0.6);
            player.setVelocity(boost);
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 25, 0.4, 0.4, 0.4);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1.0f, 1.5f);
        }
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item != null && item.hasItemMeta() && item.getItemMeta().displayName() != null) {
                String name = item.getItemMeta().getDisplayName();

                if (name.contains("Wither Hammer") && witherChargedPlayers.contains(player.getUniqueId())) {
                    if (event.getEntity() instanceof LivingEntity target) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 160, 1));
                        target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation(), 30, 0.5, 1, 0.5);
                        witherChargedPlayers.remove(player.getUniqueId());
                    }
                }

                if (name.contains("Emerald Hammer")) {
                    String mode = emeraldHammerModes.getOrDefault(player.getUniqueId(), "BREACH");
                    if (mode.equals("DENSITY")) {
                        event.setDamage(event.getDamage() + 6.0);
                    } else if (mode.equals("BREACH")) {
                        event.setDamage(event.getDamage() * 1.3);
                    }
                }
            }
        }
    }

    private LivingEntity getTargetEntity(Player player, int range) {
        for (Entity e : player.getNearbyEntities(range, range, range)) {
            if (e instanceof LivingEntity target && target != player) {
                Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector());
                if (player.getLocation().getDirection().angle(toTarget) < 0.4) {
                    return target;
                }
            }
        }
        return null;
    }
}
