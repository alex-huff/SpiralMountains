package dev.phonis.SpiralCarver.commands;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import dev.phonis.SpiralCarver.canvas.CuboidByteWorld;
import dev.phonis.SpiralCarver.canvas.CuboidByteWorldUtil;
import dev.phonis.SpiralCarver.canvas.impl.AutoExpandingCuboidByteWorld;
import dev.phonis.SpiralCarver.math.SpiralSample;
import dev.phonis.SpiralCarver.spiral.SpiralUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class CommandCarve extends SubCommand
{

	public static final BlockState[] wallMaterials = new BlockState[]{
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.BASALT)).build().getFullState(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.BASALT)).build().getFullState(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.BASALT)).build().getFullState(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.DEEPSLATE)).build().getFullState(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.COBBLED_DEEPSLATE)).build(),
		};

	public static final BlockState[] pathMaterials = new BlockState[]{
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.BLACKSTONE)).build(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.BLACKSTONE)).build(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.BLACKSTONE)).build(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS)).build(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS)).build(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.POLISHED_BLACKSTONE_BRICKS)).build(),
		};

	public static final BlockState[] aqueductMaterials = new BlockState[]{
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.STONE_BRICKS)).build(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.STONE_BRICKS)).build(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.STONE_BRICKS)).build(),
		FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.CRACKED_STONE_BRICKS)).build(),
		};

	public static BlockState[] materials;
	public static byte         wallStart;
	public static byte         wallEnd;
	public static byte         pathStart;
	public static byte         pathEnd;
	public static byte         aqueductStart;
	public static byte         aqueductEnd;
	public static byte         slab;
	public static byte         lightSourceBase;
	public static byte         lightSource;

	static
	{
		CommandCarve.materials    = new BlockState[1 + CommandCarve.wallMaterials.length +
												   CommandCarve.pathMaterials.length +
												   CommandCarve.aqueductMaterials.length + 3];
		CommandCarve.materials[0] = FuzzyBlockState.builder().type(BukkitAdapter.asBlockType(Material.COBBLESTONE))
												   .build();
		CommandCarve.wallStart    = 1;
		System.arraycopy(CommandCarve.wallMaterials, 0, CommandCarve.materials, CommandCarve.wallStart,
						 CommandCarve.wallMaterials.length
		);
		CommandCarve.wallEnd   = (byte) CommandCarve.wallMaterials.length;
		CommandCarve.pathStart = (byte) (CommandCarve.wallEnd + 1);
		System.arraycopy(CommandCarve.pathMaterials, 0, CommandCarve.materials, CommandCarve.pathStart,
						 CommandCarve.pathMaterials.length
		);
		CommandCarve.pathEnd       = (byte) (CommandCarve.pathStart + CommandCarve.pathMaterials.length - 1);
		CommandCarve.aqueductStart = (byte) (pathEnd + 1);
		System.arraycopy(CommandCarve.aqueductMaterials, 0, CommandCarve.materials, CommandCarve.aqueductStart,
						 CommandCarve.aqueductMaterials.length
		);
		CommandCarve.aqueductEnd                             = (byte) (
			CommandCarve.aqueductStart + CommandCarve.aqueductMaterials.length - 1
		);
		CommandCarve.slab                                    = (byte) (
			CommandCarve.aqueductEnd + 1
		);
		CommandCarve.materials[CommandCarve.slab]            = FuzzyBlockState.builder().type(
			BukkitAdapter.asBlockType(Material.POLISHED_DEEPSLATE_SLAB)).build();
		CommandCarve.lightSourceBase                         = (byte) (
			CommandCarve.slab + 1
		);
		CommandCarve.materials[CommandCarve.lightSourceBase] = FuzzyBlockState.builder().type(
			BukkitAdapter.asBlockType(Material.STONE_BRICK_WALL)).build().getFullState();
		CommandCarve.lightSource                             = (byte) (
			CommandCarve.lightSourceBase + 1
		);
		CommandCarve.materials[CommandCarve.lightSource]     = FuzzyBlockState.builder().type(
			BukkitAdapter.asBlockType(Material.LANTERN)).build().getFullState();
	}

	private final JavaPlugin javaPlugin;

	public
	CommandCarve(JavaPlugin javaPlugin)
	{
		super("carve");
		this.javaPlugin = javaPlugin;
	}

	@Override
	public
	List<String> topTabComplete(CommandSender sender, String[] args)
	{
		return null;
	}

	@Override
	public
	void execute(CommandSender sender, String[] args) throws CommandException
	{
		throw CommandException.consoleError;
	}

	@Override
	public
	void execute(Player player, String[] args) throws CommandException
	{
		player.sendMessage("Starting async spiral task.");

		final UUID playerUUID = player.getUniqueId();

		List<Integer> parameters = new ArrayList<>();
		for (String arg : args)
		{
			try
			{
				parameters.add(Integer.parseInt(arg));
			}
			catch (NumberFormatException ignored)
			{
				throw new CommandException(arg + " is not a valid number.");
			}
		}
		if (parameters.size() % 3 != 0)
		{
			throw new CommandException("Number of parameters should be divisible by three. (retard)");
		}
		final SpiralSample[] samples = new SpiralSample[parameters.size() / 3];
		int                  i       = 0;
		for (int at = 0; at < parameters.size(); at += 3)
		{
			samples[i] = new SpiralSample(parameters.get(at), parameters.get(at + 1), parameters.get(at + 2));
			i++;
		}

		Bukkit.getScheduler().runTaskAsynchronously(this.javaPlugin, () ->
		{
			CuboidByteWorld cuboidByteWorld = new AutoExpandingCuboidByteWorld(15);
			try
			{
				SpiralUtil.fillCuboidByteWorldWithSpiral(cuboidByteWorld, samples);
				final Clipboard clipboard = CuboidByteWorldUtil.cuboidByteWorldToClipboard(
					cuboidByteWorld, CommandCarve.materials);
				Bukkit.getScheduler()
					  .scheduleSyncDelayedTask(this.javaPlugin, () -> this.loadClipboard(playerUUID, clipboard));
			}
			catch (Throwable throwable)
			{
				Bukkit.broadcastMessage(
					"" + ChatColor.BOLD + ChatColor.DARK_RED + "AHHHH MASSIVE ERROR: " + throwable.getMessage());
				Bukkit.broadcastMessage("" + ChatColor.BOLD + ChatColor.DARK_RED + "Consider being less retarded.");
			}
		});
	}

	private
	void loadClipboard(UUID playerUUID, Clipboard clipboard)
	{
		Plugin          plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
		WorldEditPlugin wep;
		Player          player = Bukkit.getPlayer(playerUUID);

		if (plugin instanceof WorldEditPlugin)
		{
			wep = (WorldEditPlugin) plugin;
		}
		else
		{
			if (player != null)
			{
				player.sendMessage("WorldEdit not installed.");
			}
			return;
		}
		LocalSession localSession = wep.getSession(player);
		localSession.setClipboard(new ClipboardHolder(clipboard));
		if (player != null)
		{
			player.sendMessage(ChatColor.BLUE + "Clipboard loaded. Dimensions: " + ChatColor.WHITE +
							   clipboard.getDimensions().toString());
		}
	}

	private
	void timeCuboidByteWorld(CuboidByteWorld byteWorld, String typeName)
	{
		long startTime = System.currentTimeMillis();
		for (int x = -256; x <= 256; x++)
		{
			for (int y = -256; y <= 256; y++)
			{
				for (int z = -256; z <= 256; z++)
				{
					byteWorld.put((byte) (x + y + z), x, y, z);
				}
			}
		}
		long endTime = System.currentTimeMillis();
		Bukkit.broadcastMessage(typeName + " in : " + (endTime - startTime) / 1000D + " seconds.");
	}

	private
	void verifyCuboidByteWorld(CuboidByteWorld byteWorld, String typeName)
	{
		long startTime = System.currentTimeMillis();
		for (int x = -256; x <= 256; x++)
		{
			for (int y = -256; y <= 256; y++)
			{
				for (int z = -256; z <= 256; z++)
				{
					if (byteWorld.at(x, y, z) != (byte) (x + y + z))
					{
						Bukkit.broadcastMessage(typeName + " failed.");
						return;
					}
				}
			}
		}
		long endTime = System.currentTimeMillis();
		Bukkit.broadcastMessage(typeName + " passed in " + (endTime - startTime) / 1000D + " seconds.");
	}

}
