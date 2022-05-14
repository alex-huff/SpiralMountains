package dev.phonis.SpiralCarver.math.impl;

import dev.phonis.SpiralCarver.math.Interpolator;
import dev.phonis.SpiralCarver.math.SpiralSample;

public class SpiralLinearInterpolator implements Interpolator<SpiralSample>
{

    private final DoubleLinearInterpolator heightInterpolator;
    private final DoubleLinearInterpolator radiusInterpolator;
    private final DoubleLinearInterpolator pathWidthInterpolator;

    public SpiralLinearInterpolator(SpiralSample start, SpiralSample finish)
    {
        this.heightInterpolator = new DoubleLinearInterpolator(start.height(), finish.height());
        this.radiusInterpolator = new DoubleLinearInterpolator(start.radius(), finish.radius());
        this.pathWidthInterpolator = new DoubleLinearInterpolator(start.pathWidth(), finish.pathWidth());
    }

    @Override
    public SpiralSample interpolate(double percent)
    {
        return new SpiralSample(
            this.heightInterpolator.interpolate(percent),
            this.radiusInterpolator.interpolate(percent),
            this.pathWidthInterpolator.interpolate(percent)
        );
    }

}
