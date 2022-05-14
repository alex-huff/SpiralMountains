package dev.phonis.SpiralCarver.commands;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import dev.phonis.SpiralCarver.canvas.impl.AutoExpandingCuboidByteWorld;
import dev.phonis.SpiralCarver.canvas.CuboidByteWorld;
import dev.phonis.SpiralCarver.canvas.CuboidByteWorldUtil;
import dev.phonis.SpiralCarver.math.SpiralSample;
import dev.phonis.SpiralCarver.spiral.SpiralUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class CommandCarve extends SubCommand
{

    private final JavaPlugin javaPlugin;

    public CommandCarve(JavaPlugin javaPlugin)
    {
        super("carve");
        this.javaPlugin = javaPlugin;
    }

    @Override
    public List<String> topTabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) throws CommandException
    {
        throw CommandException.consoleError;
    }

    @Override
    public void execute(Player player, String[] args) throws CommandException
    {
        player.sendMessage("Starting async spiral task.");

        final UUID playerUUID = player.getUniqueId();

//        CuboidByteWorld autoExpandingCuboidByteWorld = new AutoExpandingCuboidByteWorld(256);
//        CuboidByteWorld chunkBasedAutoExpandingCuboidByteWorld = new ChunkBasedAutoExpandingCuboidByteWorld();
//        this.timeCuboidByteWorld(autoExpandingCuboidByteWorld, "AutoExpandingCuboidByteWorld");
//        this.timeCuboidByteWorld(chunkBasedAutoExpandingCuboidByteWorld, "ChunkBasedAutoExpandingCuboidByteWorld");
//        this.verifyCuboidByteWorld(autoExpandingCuboidByteWorld, "AutoExpandingCuboidByteWorld");
//        this.verifyCuboidByteWorld(chunkBasedAutoExpandingCuboidByteWorld, "ChunkBasedAutoExpandingCuboidByteWorld");

        Bukkit.getScheduler().runTaskAsynchronously(
            this.javaPlugin,
            () ->
            {
                CuboidByteWorld cuboidByteWorld = new AutoExpandingCuboidByteWorld(15);
                SpiralSample[] spiral = new SpiralSample[]
                    {
                        new SpiralSample(0, 300, 100),
                        new SpiralSample(20, 210, 50),
                        new SpiralSample(40, 170, 50),
                        new SpiralSample(80, 125, 50),
                        new SpiralSample(120, 80, 25),
                        new SpiralSample(160, 60, 25)
                    };
                SpiralUtil.fillCuboidByteWorldWithSpiral(cuboidByteWorld, spiral);
                final Clipboard clipboard = CuboidByteWorldUtil.cuboidByteWorldToClipboard(cuboidByteWorld);
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.javaPlugin, () -> this.loadClipboard(
                    playerUUID,
                    clipboard
                ));
            }
        );
    }

    private void loadClipboard(UUID playerUUID, Clipboard clipboard)
    {
        Plugin          plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
        WorldEditPlugin wep;
        Player          player = Bukkit.getPlayer(playerUUID);

        if (plugin instanceof WorldEditPlugin)
            wep = (WorldEditPlugin) plugin;
        else
        {
            if (player != null) player.sendMessage("WorldEdit not installed.");
            return;
        }
        LocalSession localSession = wep.getSession(player);
        localSession.setClipboard(new ClipboardHolder(clipboard));
        if (player != null) player.sendMessage(
            ChatColor.BLUE + "Clipboard loaded. Dimensions: " + ChatColor.WHITE + clipboard.getDimensions().toString());
    }

    private void timeCuboidByteWorld(CuboidByteWorld byteWorld, String typeName)
    {
        long startTime = System.currentTimeMillis();
        for (int x = -256; x <= 256; x++)
            for (int y = -256; y <= 256; y++)
                for (int z = -256; z <= 256; z++)
                    byteWorld.put((byte) (x + y + z), x, y, z);
        long endTime = System.currentTimeMillis();
        Bukkit.broadcastMessage(typeName + " in : " + (endTime - startTime) / 1000D + " seconds.");
    }

    private void verifyCuboidByteWorld(CuboidByteWorld byteWorld, String typeName)
    {
        long startTime = System.currentTimeMillis();
        for (int x = -256; x <= 256; x++)
            for (int y = -256; y <= 256; y++)
                for (int z = -256; z <= 256; z++)
                    if (byteWorld.at(x, y, z) != (byte) (x + y + z))
                    {
                        Bukkit.broadcastMessage(typeName + " failed.");
                        return;
                    }
        long endTime = System.currentTimeMillis();
        Bukkit.broadcastMessage(typeName + " passed in " + (endTime - startTime) / 1000D + " seconds.");
    }

}
