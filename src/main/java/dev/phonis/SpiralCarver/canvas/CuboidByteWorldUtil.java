package dev.phonis.SpiralCarver.canvas;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import org.bukkit.Material;

public class CuboidByteWorldUtil
{

    public static Clipboard cuboidByteWorldToClipboard(CuboidByteWorld cuboidByteWorld)
    {
        FuzzyBlockState cobbleBlock = FuzzyBlockState.builder().type(
            BukkitAdapter.asBlockType(
                Material.COBBLESTONE
            )
        ).build();
        CuboidRegion cuboidRegion = new CuboidRegion(
            BlockVector3.ZERO,
            BlockVector3.at(
                cuboidByteWorld.getLogicalXAxisSize() - 1,
                cuboidByteWorld.getLogicalYAxisSize() - 1,
                cuboidByteWorld.getLogicalXAxisSize() - 1
            )
        );
        BlockArrayClipboard blockArrayClipboard = new BlockArrayClipboard(cuboidRegion);
        for (int x = cuboidByteWorld.getMinX(); x <= cuboidByteWorld.getMaxX(); x++)
        {
            int xIndex = x - cuboidByteWorld.getMinX();
            for (int y = cuboidByteWorld.getMinY(); y <= cuboidByteWorld.getMaxY(); y++)
            {
                int yIndex = y - cuboidByteWorld.getMinY();
                for (int z = cuboidByteWorld.getMinZ(); z <= cuboidByteWorld.getMaxZ(); z++)
                {
                    int zIndex = z - cuboidByteWorld.getMinZ();
                    try
                    {
                        if (cuboidByteWorld.at(x, y, z) == 1)
                            blockArrayClipboard.setBlock(
                                BlockVector3.at(xIndex, yIndex, zIndex),
                                cobbleBlock
                            );
                    }
                    catch (WorldEditException ignored) { }
                }
            }
        }
        return blockArrayClipboard;
    }

}
