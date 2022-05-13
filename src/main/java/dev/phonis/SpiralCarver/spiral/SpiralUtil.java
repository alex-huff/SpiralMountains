package dev.phonis.SpiralCarver.spiral;

import com.sk89q.worldedit.math.BlockVector3;
import dev.phonis.SpiralCarver.canvas.CuboidByteWorld;
import dev.phonis.SpiralCarver.math.SpiralSample;
import dev.phonis.SpiralCarver.math.impl.DoubleLinearInterpolator;
import dev.phonis.SpiralCarver.math.impl.SpiralLinearInterpolator;
import dev.phonis.SpiralCarver.math.impl.UnitVectorLinearInterpolator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class SpiralUtil
{

    public static void fillCuboidByteWorldWithSpiral(CuboidByteWorld cuboidByteWorld, SpiralSample[] spiral)
    {
        SpiralSample                 lastSample       = null;
        SpiralSample                 currentSample;
        SpiralSample                 nextSample;
        UnitVectorLinearInterpolator unitVectorLerper = new UnitVectorLinearInterpolator();

        for (int i = 0; i < spiral.length - 1; i++)
        {
            currentSample = spiral[i];
            nextSample    = spiral[i + 1];

            SpiralLinearInterpolator spiralLerper = new SpiralLinearInterpolator(currentSample, nextSample);
            SpiralLinearInterpolator lastSpiralLerper =
                (lastSample == null) ? null :
                new SpiralLinearInterpolator(
                    lastSample,
                    currentSample
                );
            double approximateRadius        = (currentSample.radius() + nextSample.radius()) / 2;
            double approximateCircumference = 2 * Math.PI * approximateRadius;
            int    numSamples               = (int) (approximateCircumference * 20);
            for (int s = 1; s <= numSamples; s++)
            {
                double       percentage       = (1.0D * s / numSamples);
                SpiralSample lerpedSample     = spiralLerper.lerp(percentage);
                SpiralSample lastLerpedSample = (lastSpiralLerper == null) ? null : lastSpiralLerper.lerp(percentage);
                Vector       unitVector       = unitVectorLerper.lerp(percentage);
                Vector pathStart = unitVector.clone().multiply(lerpedSample.radius() - lerpedSample.pathWidth())
                                             .setY(lerpedSample.height());
                Vector pathEnd = unitVector.clone().multiply(lerpedSample.radius()).setY(lerpedSample.height());
                SpiralUtil.drawLine(cuboidByteWorld, pathStart, pathEnd);
                Vector belowPathStart =
                    (lastLerpedSample == null) ?
                    unitVector.clone().multiply(lerpedSample.radius()).setY(0) :
                    unitVector.clone().multiply(lastLerpedSample.radius() - lastLerpedSample.pathWidth())
                              .setY(lastLerpedSample.height());
                SpiralUtil.drawLine(cuboidByteWorld, pathEnd, belowPathStart);
            }

            lastSample = currentSample;
        }
    }

    private static void drawLine(CuboidByteWorld cuboidByteWorld, Vector start, Vector finish)
    {
        int         startX           = (int) Math.floor(start.getX());
        int         startY           = (int) Math.floor(start.getY());
        int         startZ           = (int) Math.floor(start.getZ());
        int         finishX          = (int) Math.floor(finish.getX());
        int         finishY          = (int) Math.floor(finish.getY());
        int         finishZ          = (int) Math.floor(finish.getZ());
        int         minX             = Math.min(startX, finishX);
        int         maxX             = Math.max(startX, finishX);
        int         minY             = Math.min(startY, finishY);
        int         maxY             = Math.max(startY, finishY);
        int         minZ             = Math.min(startZ, finishZ);
        int         maxZ             = Math.max(startZ, finishZ);
        Vector      direction        = finish.clone().subtract(start).normalize();
        double      distance         = start.distance(finish);
        BoundingBox blockBoundingBox = new BoundingBox();
        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++)
            {
                for (int z = minZ; z <= maxZ; z++)
                {
                    blockBoundingBox.resize(x, y, z, x + 1, y + 1, z + 1);
                    if (blockBoundingBox.rayTrace(start, direction, distance) != null)
                    {
                        cuboidByteWorld.put((byte) 0x1, x, y, z);
                    }
                }
            }
        }
    }

}
