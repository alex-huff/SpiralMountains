package dev.phonis.SpiralCarver.math.impl;

import dev.phonis.SpiralCarver.math.Interpolator;
import org.bukkit.util.Vector;

public class UnitVectorLinearInterpolator implements Interpolator<Vector>
{

    private final DoubleLinearInterpolator radianLerper = new DoubleLinearInterpolator(0, 2 * Math.PI);

    @Override
    public Vector interpolate(double percent)
    {
        double radians = this.radianLerper.interpolate(percent);
        return new Vector(Math.cos(radians), 0, Math.sin(radians));
    }

}
