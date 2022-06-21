package dev.phonis.SpiralCarver.math.impl;

import dev.phonis.SpiralCarver.math.Interpolator;

public
class DomeLinearInterpolater implements Interpolator<Double>
{

	@Override
	public
	Double interpolate(double percent)
	{
		return Math.sqrt(1 - Math.pow((2 * percent) - 1, 2));
	}

}
