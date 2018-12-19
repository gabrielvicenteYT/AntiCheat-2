package me.rida.anticheat.checks.other;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;
import me.rida.anticheat.checks.CheckType;

public class InvMoveB extends Check {
	public InvMoveB(AntiCheat AntiCheat) {
		super("InvMoveB", "InvMove", CheckType.Other, true, false, false, false, true, 15, 1, 600000L, AntiCheat);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	private void attack(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getDamager();
		InventoryView view = p.getOpenInventory();
		Inventory top = view.getTopInventory();
		if (view !=null) {
			if (top.toString().contains("CraftInventoryCrafting")
					|| getAntiCheat().getLag().getTPS() < getAntiCheat().getTPSCancel()
					|| getAntiCheat().getLag().getPing(p) > getAntiCheat().getPingCancel()
					|| p.getAllowFlight()
					|| p.getGameMode().equals(GameMode.CREATIVE)) {
				return;
			} 
			getAntiCheat().logCheat(this, p, "Attacking while having a gui open!", "(Type: B)");
		}
	}
}