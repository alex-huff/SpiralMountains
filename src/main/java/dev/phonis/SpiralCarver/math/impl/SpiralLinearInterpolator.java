package dev.phonis.SpiralCarver.math.impl;

import dev.phonis.SpiralCarver.math.LinearInterpolator;
import dev.phonis.SpiralCarver.math.SpiralSample;

public class SpiralLinearInterpolator implements LinearInterpolator<SpiralSample>
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
    public SpiralSample lerp(double percent)
    {
        return new SpiralSample(
            this.heightInterpolator.lerp(percent),
            this.radiusInterpolator.lerp(percent),
            this.pathWidthInterpolator.lerp(percent)
        );
    }

}
