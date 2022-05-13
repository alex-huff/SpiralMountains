package dev.phonis.SpiralCarver.math.impl;

import dev.phonis.SpiralCarver.math.LinearInterpolator;
import org.bukkit.util.Vector;

public class UnitVectorLinearInterpolator implements LinearInterpolator<Vector>
{

    private final DoubleLinearInterpolator radianLerper = new DoubleLinearInterpolator(0, 2 * Math.PI);

    @Override
    public Vector lerp(double percent)
    {
        double radians = this.radianLerper.lerp(percent);
        return new Vector(Math.cos(radians), 0, Math.sin(radians));
    }

}
