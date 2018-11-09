package me.rida.anticheat.checks.other;

import org.bukkit.event.*;
import java.util.*;
import org.bukkit.*;
import org.bukkit.event.block.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

import me.rida.anticheat.utils.Color;
import me.rida.anticheat.utils.UtilVelocity;
import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;

public class BlockInteractD extends Check {
    public BlockInteractD(AntiCheat AntiCheat) {
        super("BlockInteractD", "BlockInteract", AntiCheat);
		setEnabled(true);
		setMaxViolations(20);
		setViolationResetTime(1000);
		setBannable(true);
		setViolationsToNotify(2);
    }
    @EventHandler
    public void onPlaceBlock(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block target = player.getTargetBlock((Set)null, 5);
        if (player.getGameMode().equals(GameMode.CREATIVE)
                || player.getAllowFlight()
                || event.getPlayer().getVehicle() != null
				|| !getAntiCheat().isEnabled()
				|| getAntiCheat().isSotwMode()
                || UtilVelocity.didTakeVelocity(player)) return;
        if (event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation().subtract(0.0, 1.0, 0.0)).getType() == Material.AIR) {
            
            if (!player.isSneaking() && !player.isFlying() && groundAround(player.getLocation()) && event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation().subtract(0.0, 1.0, 0.0)).getType() == Material.AIR && player.getWorld().getBlockAt(player.getLocation().subtract(0.0, 1.0, 0.0)).equals(event.getBlock())) {
                	getAntiCheat().logCheat(this, player, Color.Red + "Experemental", "(Type: D)");
            }
        }
    }
    public static boolean groundAround(final Location loc) {
        for (int radius = 2, x = -radius; x < radius; ++x) {
            for (int y = -radius; y < radius; ++y) {
                for (int z = -radius; z < radius; ++z) {
                    final Material mat = loc.getWorld().getBlockAt(loc.add((double)x, (double)y, (double)z)).getType();
                    if (mat.isSolid() || mat == Material.WATER || mat == Material.STATIONARY_WATER || mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
                        loc.subtract((double)x, (double)y, (double)z);
                        return true;
                    }
                    loc.subtract((double)x, (double)y, (double)z);
                }
            }
        }
        return false;
    }
}