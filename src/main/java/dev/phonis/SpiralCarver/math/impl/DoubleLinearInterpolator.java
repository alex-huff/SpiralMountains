package dev.phonis.SpiralCarver.math.impl;

import dev.phonis.SpiralCarver.math.Interpolator;

public class DoubleLinearInterpolator implements Interpolator<Double>
{

    private final double start;
    private final double range;

    public DoubleLinearInterpolator(double start, double finish)
    {
        this.start = start;
        this.range = finish - start;
    }

    @Override
    public Double interpolate(double percent)
    {
        return this.start + percent * range;
    }

}
