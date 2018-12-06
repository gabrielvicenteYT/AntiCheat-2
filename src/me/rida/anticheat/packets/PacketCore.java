package me.rida.anticheat.packets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.data.DataPlayer;
import me.rida.anticheat.packets.events.PacketAttackEvent;
import me.rida.anticheat.packets.events.PacketBlockPlacementEvent;
import me.rida.anticheat.packets.events.PacketEntityActionEvent;
import me.rida.anticheat.packets.events.PacketHeldItemChangeEvent;
import me.rida.anticheat.packets.events.PacketKeepAliveEvent;
import me.rida.anticheat.packets.events.PacketKillauraEvent;
import me.rida.anticheat.packets.events.PacketPlayerEvent;
import me.rida.anticheat.packets.events.PacketSwingArmEvent;
import me.rida.anticheat.packets.events.PacketUseEntityEvent;

public class PacketCore {
	private static AntiCheat AntiCheat;
	private HashSet<EntityType> enabled;
	public static Map<UUID, Integer> movePackets;

	public PacketCore(AntiCheat AntiCheat) {
		super();
		PacketCore.AntiCheat = AntiCheat;
		enabled = new HashSet<EntityType>();
		enabled.add(EntityType.valueOf((String) "PLAYER"));
		movePackets = new HashMap<UUID, Integer>();

		ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) new PacketAdapter(PacketCore.AntiCheat,
				new PacketType[] { PacketType.Play.Client.USE_ENTITY }) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				final PacketContainer packet = event.getPacket();
				final Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				try {
					Object playEntity = getNMSClass("PacketPlayInUseEntity");
					String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
					if (version.contains("1_7")) {
						if (packet.getHandle() == playEntity) {
							if (playEntity.getClass().getMethod("c") == null) {
								return;
							}
						}
					} else {
						if (packet.getHandle() == playEntity) {
							if (playEntity.getClass().getMethod("a") == null) {
								return;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				EnumWrappers.EntityUseAction type;
				try {
					type = packet.getEntityUseActions().read(0);
				} catch (Exception ex) {
					return;
				}

				Player entity = event.getPlayer();

				if(entity == null) {
					return;
				}

				Bukkit.getServer().getPluginManager().callEvent((Event) new PacketUseEntityEvent(type, player, entity));
				if (type == EntityUseAction.ATTACK) {
					Bukkit.getServer().getPluginManager()
					.callEvent(new PacketKillauraEvent(player, PacketPlayerType.USE));
				}
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) new PacketAdapter(PacketCore.AntiCheat,
				new PacketType[] { PacketType.Play.Client.POSITION_LOOK }) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				Bukkit.getServer().getPluginManager().callEvent((Event) new PacketPlayerEvent(player,
						(double) event.getPacket().getDoubles().read(0),
						(double) event.getPacket().getDoubles().read(1),
						(double) event.getPacket().getDoubles().read(2), (float) event.getPacket().getFloat().read(0),
						(float) event.getPacket().getFloat().read(1), PacketPlayerType.POSLOOK));
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener(
				(PacketListener) new PacketAdapter(PacketCore.AntiCheat, new PacketType[] { PacketType.Play.Client.LOOK }) {
					@Override
					public void onPacketReceiving(final PacketEvent event) {
						Player player = event.getPlayer();

						if (player == null) {
							return;
						}

						Bukkit.getServer().getPluginManager()
						.callEvent(new PacketPlayerEvent(player, event.getPacket().getDoubles().read(0),
								event.getPacket().getDoubles().read(1), event.getPacket().getDoubles().read(2),
								event.getPacket().getFloat().read(0), event.getPacket().getFloat().read(1),
								PacketPlayerType.POSLOOK));
					}
				});
		ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) new PacketAdapter(PacketCore.AntiCheat,
				new PacketType[] { PacketType.Play.Client.POSITION }) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				Bukkit.getServer().getPluginManager().callEvent(
						(Event) new PacketPlayerEvent(player, (double) event.getPacket().getDoubles().read(0),
								(double) event.getPacket().getDoubles().read(1),
								(double) event.getPacket().getDoubles().read(2), player.getLocation().getYaw(),
								player.getLocation().getPitch(), PacketPlayerType.POSITION));
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) new PacketAdapter(PacketCore.AntiCheat,
				new PacketType[] { PacketType.Play.Server.POSITION}) {
			@Override
			public void onPacketSending(final PacketEvent event) {
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}

				int i = movePackets.getOrDefault(player.getUniqueId(), 0);
				i++;
				movePackets.put(player.getUniqueId(), i);
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) new PacketAdapter(PacketCore.AntiCheat,
				new PacketType[] { PacketType.Play.Client.ENTITY_ACTION }) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				PacketContainer packet = event.getPacket();
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				Bukkit.getServer().getPluginManager()
				.callEvent((Event) new PacketEntityActionEvent(player, (int) packet.getIntegers().read(1)));
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) new PacketAdapter(PacketCore.AntiCheat,
				new PacketType[] { PacketType.Play.Client.KEEP_ALIVE }) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				Bukkit.getServer().getPluginManager().callEvent((Event) new PacketKeepAliveEvent(player));
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) new PacketAdapter(PacketCore.AntiCheat,
				new PacketType[] { PacketType.Play.Client.ARM_ANIMATION }) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				final Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				Bukkit.getServer().getPluginManager()
				.callEvent(new PacketKillauraEvent(player, PacketPlayerType.ARM_SWING));
				Bukkit.getServer().getPluginManager().callEvent((Event) new PacketSwingArmEvent(event, player));
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) new PacketAdapter(PacketCore.AntiCheat,
				new PacketType[] { PacketType.Play.Client.HELD_ITEM_SLOT }) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				Bukkit.getServer().getPluginManager().callEvent((Event) new PacketHeldItemChangeEvent(event, player));
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) new PacketAdapter(PacketCore.AntiCheat,
				new PacketType[] { PacketType.Play.Client.BLOCK_PLACE }) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				Bukkit.getServer().getPluginManager().callEvent((Event) new PacketBlockPlacementEvent(event, player));
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener(
				(PacketListener) new PacketAdapter(PacketCore.AntiCheat, new PacketType[] { PacketType.Play.Client.FLYING }) {
					@Override
					public void onPacketReceiving(final PacketEvent event) {
						final Player player = event.getPlayer();
						if (player == null) {
							return;
						}
						Bukkit.getServer().getPluginManager()
						.callEvent((Event) new PacketPlayerEvent(player, player.getLocation().getX(),
								player.getLocation().getY(), player.getLocation().getZ(),
								player.getLocation().getYaw(), player.getLocation().getPitch(),
								PacketPlayerType.FLYING));
					}
				});
	}

	public Class<?> getNMSClass(String name) {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		}

		catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void init() {
		movePackets = new HashMap<>();

		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(me.rida.anticheat.AntiCheat.getInstance(), PacketType.Play.Server.POSITION) {
			@Override
			public void onPacketSending(final PacketEvent event) {
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}

				movePackets.put(player.getUniqueId(), movePackets.getOrDefault(player.getUniqueId(), 0) + 1);
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(me.rida.anticheat.AntiCheat.getInstance(), PacketType.Play.Client.USE_ENTITY) {
					@Override
					public void onPacketReceiving(PacketEvent event) {
						PacketContainer packet = event.getPacket();
						Player player = event.getPlayer();
						if (player == null) {
							return;
						}

						EnumWrappers.EntityUseAction type;
						try {
							type = packet.getEntityUseActions().read(0);
						} catch (Exception ex) {
							return;
						}

						Entity entity = event.getPacket().getEntityModifier(player.getWorld()).read(0);

						if (entity == null) {
							return;
						}

						if (type == EnumWrappers.EntityUseAction.ATTACK) {
							Bukkit.getServer().getPluginManager().callEvent(new PacketAttackEvent(player, entity, PacketPlayerType.USE));
						}
					}
				});
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(me.rida.anticheat.AntiCheat.getInstance(), PacketType.Play.Client.LOOK) {
			@Override
			public void onPacketReceiving(PacketEvent packetEvent) {
				Player player = packetEvent.getPlayer();
				if (player == null) {
					return;
				}
				Bukkit.getServer().getPluginManager().callEvent(new PacketPlayerEvent(player, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), packetEvent.getPacket().getFloat().read(0), packetEvent.getPacket().getFloat().read(1), PacketPlayerType.LOOK));

				DataPlayer data = me.rida.anticheat.AntiCheat.getInstance().getDataManager().getData(player);

				if(data != null) {
					data.setLastPacket(System.currentTimeMillis());
				}
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(me.rida.anticheat.AntiCheat.getInstance(), PacketType.Play.Client.POSITION) {
			@Override
			public void onPacketReceiving(PacketEvent packetEvent) {
				Player player = packetEvent.getPlayer();
				if (player == null) {
					return;
				}
				Bukkit.getServer().getPluginManager().callEvent(new PacketPlayerEvent(player, packetEvent.getPacket().getDoubles().read(0), packetEvent.getPacket().getDoubles().read(1), packetEvent.getPacket().getDoubles().read(2), player.getLocation().getYaw(), player.getLocation().getPitch(), PacketPlayerType.POSITION));

				DataPlayer data = me.rida.anticheat.AntiCheat.getInstance().getDataManager().getData(player);

				if(data != null) {
					data.setLastPacket(System.currentTimeMillis());
				}
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(me.rida.anticheat.AntiCheat.getInstance(), PacketType.Play.Client.POSITION_LOOK) {
			@Override
			public void onPacketReceiving(PacketEvent packetEvent) {
				Player player = packetEvent.getPlayer();
				if (player == null) {
					return;
				}

				Bukkit.getServer().getPluginManager().callEvent(new PacketPlayerEvent(player, packetEvent.getPacket().getDoubles().read(0), packetEvent.getPacket().getDoubles().read(1), packetEvent.getPacket().getDoubles().read(2), packetEvent.getPacket().getFloat().read(0), packetEvent.getPacket().getFloat().read(1), PacketPlayerType.POSLOOK));

				DataPlayer data = me.rida.anticheat.AntiCheat.getInstance().getDataManager().getData(player);

				if(data != null) {
					data.setLastKillauraPitch(packetEvent.getPacket().getFloat().read(1));
					data.setLastKillauraYaw(packetEvent.getPacket().getFloat().read(0));
					data.setLastPacket(System.currentTimeMillis());
				}
			}
		});
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(me.rida.anticheat.AntiCheat.getInstance(), PacketType.Play.Client.FLYING) {
			@Override
			public void onPacketReceiving(PacketEvent packetEvent) {
				Player player = packetEvent.getPlayer();
				if (player == null) {
					return;
				}
				Bukkit.getServer().getPluginManager().callEvent(new PacketPlayerEvent(player, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch(), PacketPlayerType.FLYING));

				DataPlayer data = me.rida.anticheat.AntiCheat.getInstance().getDataManager().getData(player);

				if(data != null) {
					data.setLastPacket(System.currentTimeMillis());
				}
			}
		});
	}
}