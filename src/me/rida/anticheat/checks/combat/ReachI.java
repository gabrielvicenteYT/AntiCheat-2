package me.rida.anticheat.checks.combat;

import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;
import me.rida.anticheat.checks.CheckType;
import me.rida.anticheat.utils.MathUtil;

public class ReachI extends Check {
	public ReachI(AntiCheat AntiCheat) {
		super("ReachI", "Reach",  CheckType.Combat, true, false, false, false, false, 20, 1, 600000L, AntiCheat);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAttack(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			if (event.getEntity().getType() == EntityType.PLAYER) {
				final Player player = (Player) event.getDamager();
				if (player.getGameMode() != GameMode.CREATIVE) {
					int ping = getAntiCheat().getLag().getPing(player);
					double tps = getAntiCheat().getLag().getTPS();
					double distance = MathUtil.getDistance3D(player.getLocation(), event.getEntity().getLocation());
					double maxReach = 4.0;
					if (player.isSprinting()) {
						distance += 0.2;
					}
					for (final PotionEffect effect : player.getActivePotionEffects()) {
						if (effect.getType() == PotionEffectType.SPEED) {
							distance += 0.2 * (effect.getAmplifier() + 1);
						}
					}
					String dist = Double.toString(distance).substring(0, 3);
					if (maxReach < distance) {
						getAntiCheat().logCheat(this, player, "Interact too far away; distance: " + dist + "; Ping: " + ping + "; TPS: " + tps, "(Type: I)");
					}
				}
			}
		}
	}
}