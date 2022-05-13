package dev.phonis.SpiralCarver.math.impl;

import dev.phonis.SpiralCarver.math.LinearInterpolator;

public class DoubleLinearInterpolator implements LinearInterpolator<Double>
{

    private final double start;
    private final double range;

    public DoubleLinearInterpolator(double start, double finish)
    {
        this.start = start;
        this.range = finish - start;
    }

    @Override
    public Double lerp(double percent)
    {
        return this.start + percent * range;
    }

}
