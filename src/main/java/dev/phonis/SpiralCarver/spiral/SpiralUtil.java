package dev.phonis.SpiralCarver.spiral;

import dev.phonis.SpiralCarver.canvas.CuboidByteWorld;
import dev.phonis.SpiralCarver.commands.CommandCarve;
import dev.phonis.SpiralCarver.math.SpiralSample;
import dev.phonis.SpiralCarver.math.impl.DomeLinearInterpolater;
import dev.phonis.SpiralCarver.math.impl.DoubleLinearInterpolator;
import dev.phonis.SpiralCarver.math.impl.SpiralLinearInterpolator;
import dev.phonis.SpiralCarver.math.impl.UnitVectorLinearInterpolator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Random;

public
class SpiralUtil
{

	private static final Random random = new Random();

	public static
	void fillCuboidByteWorldWithSpiral(CuboidByteWorld cuboidByteWorld, SpiralSample[] spiral)
	{
		SpiralSample lastSample = null;
		SpiralSample currentSample;
		SpiralSample nextSample;

		for (int i = 0; i < spiral.length - 1; i++)
		{
			currentSample = spiral[i];
			nextSample    = spiral[i + 1];

			SpiralLinearInterpolator spiralLerper = new SpiralLinearInterpolator(currentSample, nextSample);
			SpiralLinearInterpolator lastSpiralLerper = (lastSample == null) ? null
																			 : new SpiralLinearInterpolator(lastSample,
																				 currentSample);
			double averageRadius = (currentSample.radius() + nextSample.radius()) / 2;
			double arcLength     = 2 * Math.PI * averageRadius;
			int    numSamples    = (int) (arcLength * 20);
			SpiralUtil.drawPath(cuboidByteWorld, spiralLerper, numSamples);
			SpiralUtil.drawWall(cuboidByteWorld, spiralLerper, lastSpiralLerper, numSamples);
			// SpiralUtil.drawAqueducts(cuboidByteWorld, lastSpiralLerper, currentSample, nextSample);
			lastSample = currentSample;
		}
	}

	private static
	void drawWall(CuboidByteWorld cuboidByteWorld, SpiralLinearInterpolator spiralLerper,
				  SpiralLinearInterpolator lastSpiralLerper, int numSamples)
	{
		UnitVectorLinearInterpolator unitVectorLerper = new UnitVectorLinearInterpolator();
		for (int s = 1; s <= numSamples; s++)
		{
			double       percentage   = (1.0D * s / numSamples);
			SpiralSample lerpedSample = spiralLerper.interpolate(percentage);
			SpiralSample lastLerpedSample = (lastSpiralLerper == null) ? null
																	   : lastSpiralLerper.interpolate(percentage);
			Vector unitVector = unitVectorLerper.interpolate(percentage);
			Vector pathEnd    = unitVector.clone().multiply(lerpedSample.radius()).setY(lerpedSample.height());
			Vector belowPathStart = (lastLerpedSample == null) ? unitVector.clone().multiply(lerpedSample.radius())
																		   .setY(0) : unitVector.clone().multiply(
				lastLerpedSample.radius() - lastLerpedSample.pathWidth()).setY(lastLerpedSample.height());
			SpiralUtil.drawLine(cuboidByteWorld, pathEnd, belowPathStart, CommandCarve.wallStart, CommandCarve.wallEnd,
				false);
		}
	}

	private static
	void drawPath(CuboidByteWorld cuboidByteWorld, SpiralLinearInterpolator spiralLerper, int numSamples)
	{
		UnitVectorLinearInterpolator unitVectorLerper = new UnitVectorLinearInterpolator();
		for (int s = 1; s <= numSamples; s++)
		{
			double       percentage   = (1.0D * s / numSamples);
			SpiralSample lerpedSample = spiralLerper.interpolate(percentage);
			Vector       unitVector   = unitVectorLerper.interpolate(percentage);
			Vector pathStart = unitVector.clone().multiply(lerpedSample.radius() - lerpedSample.pathWidth())
										 .setY(lerpedSample.height());
			Vector pathEnd = unitVector.clone().multiply(lerpedSample.radius()).setY(lerpedSample.height());
			SpiralUtil.drawLine(cuboidByteWorld, pathStart, pathEnd, CommandCarve.pathStart, CommandCarve.pathEnd,
				true);
		}

		SpiralUtil.drawInnerPath(cuboidByteWorld, spiralLerper);
	}

	private static
	void drawInnerPath(CuboidByteWorld cuboidByteWorld, SpiralLinearInterpolator spiralLerper)
	{
		final double targetSpaceBetweenLamps = 6 * Math.sqrt(2);
		final double offWall                 = Math.sqrt(2);
		// final double targetSpaceBetweenLamps = .05;
		// Assuming height/radius are ints at start and end of sample
		SpiralSample startSample           = spiralLerper.interpolate(0);
		SpiralSample endSample             = spiralLerper.interpolate(1);
		int          startHeight           = (int) startSample.height();
		int          endHeight             = (int) endSample.height();
		double       startPathCenterRadius = startSample.radius() - startSample.pathWidth() / 2 + offWall;
		double       endPathCenterRadius   = endSample.radius() - endSample.pathWidth() / 2 + offWall;
		for (int h = startHeight; h < endHeight; h++)
		{
			int    segmentStart = h;
			int    segmentEnd   = h + 1;
			double startPercent = SpiralUtil.getPercentThroughSpiralAtHeight(startHeight, endHeight, segmentStart);
			double endPercent   = SpiralUtil.getPercentThroughSpiralAtHeight(startHeight, endHeight, segmentEnd);
			double startLength = SpiralUtil.getLengthThroughSpiralAtPercent(startPathCenterRadius, endPathCenterRadius,
				startPercent);
			double endLength = SpiralUtil.getLengthThroughSpiralAtPercent(startPathCenterRadius, endPathCenterRadius,
				endPercent);
			double segmentLength = endLength - startLength;
			// numSources * targetSpaceBetweenLamps = segmentLength
			double numSourcesApproximation = segmentLength / targetSpaceBetweenLamps;
			int    realNumSources          = (int) Math.round(numSourcesApproximation);
			// realNumSources * realSpaceBetweenLamps = segmentLength
			double realSpaceBetweenLamps = segmentLength / realNumSources;
			for (int s = 0; s < realNumSources; s++)
			{
				double lengthAtSource = startLength + (realSpaceBetweenLamps / 2) + realSpaceBetweenLamps * s;
				double percentAtSource = SpiralUtil.getPercentThroughSpiralAtLength(startPathCenterRadius,
					endPathCenterRadius, lengthAtSource);
				SpiralUtil.placeLightSourceAtPercentThroughSpiral(cuboidByteWorld, spiralLerper, percentAtSource,
					offWall);
			}
		}
	}

	private static
	void placeLightSourceAtPercentThroughSpiral(CuboidByteWorld cuboidByteWorld, SpiralLinearInterpolator spiralLerper,
												double percent, double offWall)
	{
		final double                 minInnerPathLength       = 6;
		final double                 minOuterPathForInnerPath = 10;
		UnitVectorLinearInterpolator unitVectorLerper         = new UnitVectorLinearInterpolator();
		SpiralSample                 sourceSample             = spiralLerper.interpolate(percent);
		double                       thirdOfPathWidth         = sourceSample.pathWidth() / 3;
		double                       innerPathWidth           = Math.max(thirdOfPathWidth, minInnerPathLength);
		Vector                       unitVector               = unitVectorLerper.interpolate(percent);
		double                       sourceHeight             = Math.ceil(sourceSample.height());
		if (sourceSample.pathWidth() <= minOuterPathForInnerPath)
		{
			double middleRadius   = sourceSample.radius() - sourceSample.pathWidth() / 4;
			Vector sourcePosition = unitVector.clone().multiply(middleRadius).setY(sourceHeight);
			SpiralUtil.drawPathLightSourceAt(cuboidByteWorld, sourcePosition);
			return;
		}
		double leftoverWidth     = sourceSample.pathWidth() - innerPathWidth;
		double sourceOneRadius   = (sourceSample.radius() - sourceSample.pathWidth()) + leftoverWidth / 2 + offWall;
		double sourceTwoRadius   = sourceOneRadius + innerPathWidth;
		Vector sourceOnePosition = unitVector.clone().multiply(sourceOneRadius).setY(sourceHeight);
		Vector sourceTwoPosition = unitVector.clone().multiply(sourceTwoRadius).setY(sourceHeight);
		SpiralUtil.drawPathLightSourceAt(cuboidByteWorld, sourceOnePosition);
		SpiralUtil.drawPathLightSourceAt(cuboidByteWorld, sourceTwoPosition);
	}

	private static
	void drawPathLightSourceAt(CuboidByteWorld cuboidByteWorld, Vector vector)
	{
		int blockX = (int) Math.floor(vector.getX());
		int blockY = (int) Math.floor(vector.getY());
		int blockZ = (int) Math.floor(vector.getZ());
		cuboidByteWorld.put(CommandCarve.lightSourceBase, blockX, blockY, blockZ);
		cuboidByteWorld.put(CommandCarve.lightSource, blockX, blockY + 1, blockZ);
	}

	private static
	double getPercentThroughSpiralAtHeight(double startHeight, double endHeight, double height)
	{
		double range = endHeight - startHeight;
		// startHeight + percent * range = height
		// percent * range = height - startHeight
		// percent = (height - startHeight) / range
		return (height - startHeight) / range;
	}

	private static
	void drawAqueducts(CuboidByteWorld cuboidByteWorld, SpiralLinearInterpolator lastSpiralLerper,
					   SpiralSample currentSample, SpiralSample nextSample)
	{
		final double aqueductWidth    = 10;
		final double aqueductHeight   = 15;
		final double distanceFromWall = Math.sqrt(2);
		SpiralSample aqueductStart = new SpiralSample(
			(lastSpiralLerper == null) ? 0 : lastSpiralLerper.interpolate(0).height(),
			currentSample.radius() + distanceFromWall, currentSample.pathWidth());
		SpiralSample aqueductEnd = new SpiralSample(
			(lastSpiralLerper == null) ? 0 : lastSpiralLerper.interpolate(1).height(),
			nextSample.radius() + distanceFromWall, nextSample.pathWidth());
		SpiralLinearInterpolator currentSpiralLerper = new SpiralLinearInterpolator(currentSample, nextSample);

		SpiralUtil.drawAlignedAqueducts(cuboidByteWorld, aqueductWidth, aqueductHeight, aqueductStart, aqueductEnd,
			currentSpiralLerper);
		SpiralUtil.drawOffsetAqueducts(cuboidByteWorld, aqueductWidth, aqueductHeight, aqueductStart, aqueductEnd,
			currentSpiralLerper);
	}

	private static
	void drawAlignedAqueducts(CuboidByteWorld cuboidByteWorld, double aqueductWidth, double aqueductHeight,
							  SpiralSample aqueductStart, SpiralSample aqueductEnd,
							  SpiralLinearInterpolator currentSpiralLerper)
	{
		final double                 aqueductArchStart     = (aqueductHeight - (aqueductWidth / 2));
		SpiralLinearInterpolator     aqueductLerper        = new SpiralLinearInterpolator(aqueductStart, aqueductEnd);
		UnitVectorLinearInterpolator unitVectorLerper      = new UnitVectorLinearInterpolator();
		DomeLinearInterpolater       domeLerper            = new DomeLinearInterpolater();
		double                       averageRadius         = (aqueductStart.radius() + aqueductEnd.radius()) / 2;
		double                       arcLength             = 2 * Math.PI * averageRadius;
		double                       aqueductsPerArcLength = arcLength / aqueductWidth;
		int                          numAqueducts          = (int) Math.round(aqueductsPerArcLength);
		int                          numSamples            = (int) Math.round(aqueductWidth) * 200;
		double                       startPercent          = 0;
		for (int a = 1; a <= numAqueducts; a++)
		{
			double endLength = arcLength / (numAqueducts / (double) a);
			double endPercent = SpiralUtil.getPercentThroughSpiralAtLength(aqueductStart.radius(), aqueductEnd.radius(),
				endLength);
			DoubleLinearInterpolator percentLerper = new DoubleLinearInterpolator(startPercent, endPercent);
			double pathMaxHeight = Math.max(currentSpiralLerper.interpolate(startPercent).height(),
				currentSpiralLerper.interpolate(endPercent).height());
			double baseMaxHeight = Math.max(aqueductLerper.interpolate(startPercent).height(),
				aqueductLerper.interpolate(endPercent).height());
			double stacks        = 1;
			double currentHeight = baseMaxHeight + aqueductHeight;
			while (currentHeight + aqueductHeight < pathMaxHeight)
			{
				currentHeight += aqueductHeight * 2;
				stacks++;
			}
			SpiralSample startBaseSample = aqueductLerper.interpolate(startPercent);
			Vector startBaseVector = unitVectorLerper.interpolate(startPercent).clone()
													 .multiply(startBaseSample.radius()).setY(startBaseSample.height());
			Vector startOfArchVector = startBaseVector.clone().add(new Vector(0, aqueductArchStart, 0));
			for (int stack = 0; stack < stacks; stack++)
			{
				double addedHeight = stack * aqueductHeight * 2;
				SpiralUtil.drawLine(cuboidByteWorld, startBaseVector.clone().add(new Vector(0, addedHeight, 0)),
					startOfArchVector.clone().add(new Vector(0, addedHeight, 0)), CommandCarve.aqueductStart,
					CommandCarve.aqueductEnd, false);
			}
			Vector lastSampleVector = startOfArchVector;
			for (int s = 1; s <= numSamples; s++)
			{
				double       samplePercentage     = (1.0D * s / numSamples);
				double       percentThroughSpiral = percentLerper.interpolate(samplePercentage);
				SpiralSample baseSample           = aqueductLerper.interpolate(percentThroughSpiral);
				double archHeight = baseSample.height() + aqueductArchStart +
									domeLerper.interpolate(samplePercentage) * (aqueductWidth / 2);
				Vector unitVector   = unitVectorLerper.interpolate(percentThroughSpiral);
				Vector sampleVector = unitVector.clone().multiply(baseSample.radius()).setY(archHeight);
				for (int stack = 0; stack < stacks; stack++)
				{
					double addedHeight = stack * aqueductHeight * 2;
					SpiralUtil.drawLine(cuboidByteWorld, lastSampleVector.clone().add(new Vector(0, addedHeight, 0)),
						sampleVector.clone().add(new Vector(0, addedHeight, 0)), CommandCarve.aqueductStart,
						CommandCarve.aqueductEnd, false);
				}
				lastSampleVector = sampleVector;
			}
			SpiralSample endBaseSample = aqueductLerper.interpolate(endPercent);
			Vector endBaseVector = unitVectorLerper.interpolate(endPercent).clone().multiply(endBaseSample.radius())
												   .setY(endBaseSample.height());
			for (int stack = 0; stack < stacks; stack++)
			{
				double addedHeight = stack * aqueductHeight * 2;
				SpiralUtil.drawLine(cuboidByteWorld, lastSampleVector.clone().add(new Vector(0, addedHeight, 0)),
					endBaseVector.clone().add(new Vector(0, addedHeight, 0)), CommandCarve.aqueductStart,
					CommandCarve.aqueductEnd, false);
			}
			startPercent = endPercent;
		}
	}

	private static
	void drawOffsetAqueducts(CuboidByteWorld cuboidByteWorld, double aqueductWidth, double aqueductHeight,
							 SpiralSample aqueductStart, SpiralSample aqueductEnd,
							 SpiralLinearInterpolator currentSpiralLerper)
	{
		final double                 aqueductArchStart     = (aqueductHeight - (aqueductWidth / 2));
		SpiralLinearInterpolator     aqueductLerper        = new SpiralLinearInterpolator(aqueductStart, aqueductEnd);
		UnitVectorLinearInterpolator unitVectorLerper      = new UnitVectorLinearInterpolator();
		DomeLinearInterpolater       domeLerper            = new DomeLinearInterpolater();
		double                       averageRadius         = (aqueductStart.radius() + aqueductEnd.radius()) / 2;
		double                       arcLength             = 2 * Math.PI * averageRadius;
		double                       aqueductsPerArcLength = arcLength / aqueductWidth;
		int                          numAqueducts          = (int) Math.round(aqueductsPerArcLength);
		int                          numSamples            = (int) Math.round(aqueductWidth) * 200;
		double                       startPercent          = 0;
		for (int a = 1; a <= numAqueducts; a++)
		{
			double endLength = arcLength / (numAqueducts / (double) a);
			double endPercent = SpiralUtil.getPercentThroughSpiralAtLength(aqueductStart.radius(), aqueductEnd.radius(),
				endLength);
			DoubleLinearInterpolator percentLerper   = new DoubleLinearInterpolator(startPercent, endPercent);
			double                   middlePercent   = percentLerper.interpolate(.5);
			SpiralSample             startBaseSample = aqueductLerper.interpolate(startPercent);
			Vector startBaseVector = unitVectorLerper.interpolate(startPercent).clone()
													 .multiply(startBaseSample.radius()).setY(startBaseSample.height());
			Vector middleOfArchVector = startBaseVector.clone()
													   .add(new Vector(0, aqueductArchStart + aqueductWidth / 2, 0));
			Vector lastSampleVector = middleOfArchVector;
			double pathMaxHeight = Math.max(currentSpiralLerper.interpolate(startPercent).height(),
				currentSpiralLerper.interpolate(middlePercent).height());
			double baseMaxHeight = Math.max(aqueductLerper.interpolate(startPercent).height(),
				aqueductLerper.interpolate(middlePercent).height());
			double stacks        = 0;
			double currentHeight = baseMaxHeight;
			while (currentHeight + aqueductHeight < pathMaxHeight)
			{
				currentHeight += aqueductHeight * 2;
				stacks++;
			}
			for (int s = 1; s <= numSamples / 2; s++)
			{
				double       samplePercentage     = (1.0D * s / numSamples);
				double       percentThroughSpiral = percentLerper.interpolate(samplePercentage);
				SpiralSample baseSample           = aqueductLerper.interpolate(percentThroughSpiral);
				double archHeight = baseSample.height() + aqueductArchStart +
									domeLerper.interpolate(.5 + samplePercentage) * (aqueductWidth / 2);
				Vector unitVector   = unitVectorLerper.interpolate(percentThroughSpiral);
				Vector sampleVector = unitVector.clone().multiply(baseSample.radius()).setY(archHeight);
				for (int stack = 0; stack < stacks; stack++)
				{
					double addedHeight = aqueductHeight + stack * aqueductHeight * 2;
					SpiralUtil.drawLine(cuboidByteWorld, lastSampleVector.clone().add(new Vector(0, addedHeight, 0)),
						sampleVector.clone().add(new Vector(0, addedHeight, 0)), CommandCarve.aqueductStart,
						CommandCarve.aqueductEnd, false);
				}
				lastSampleVector = sampleVector;
			}
			SpiralSample middleBaseSample = aqueductLerper.interpolate(middlePercent);
			Vector middleBaseVector = unitVectorLerper.interpolate(middlePercent).clone()
													  .multiply(middleBaseSample.radius())
													  .setY(middleBaseSample.height());
			for (int stack = 0; stack < stacks; stack++)
			{
				double addedHeight = aqueductHeight + stack * aqueductHeight * 2;
				SpiralUtil.drawLine(cuboidByteWorld, lastSampleVector.clone().add(new Vector(0, addedHeight, 0)),
					middleBaseVector.clone().add(new Vector(0, addedHeight, 0)), CommandCarve.aqueductStart,
					CommandCarve.aqueductEnd, false);
			}

			pathMaxHeight = Math.max(currentSpiralLerper.interpolate(middlePercent).height(),
				currentSpiralLerper.interpolate(endPercent).height());
			baseMaxHeight = Math.max(aqueductLerper.interpolate(middlePercent).height(),
				aqueductLerper.interpolate(endPercent).height());
			stacks        = 0;
			currentHeight = baseMaxHeight;
			while (currentHeight + aqueductHeight < pathMaxHeight)
			{
				currentHeight += aqueductHeight * 2;
				stacks++;
			}
			for (int s = numSamples / 2 + 1; s <= numSamples; s++)
			{
				double       samplePercentage     = (1.0D * s / numSamples);
				double       percentThroughSpiral = percentLerper.interpolate(samplePercentage);
				SpiralSample baseSample           = aqueductLerper.interpolate(percentThroughSpiral);
				double archHeight = baseSample.height() + aqueductArchStart +
									domeLerper.interpolate(samplePercentage - .5) * (aqueductWidth / 2);
				Vector unitVector   = unitVectorLerper.interpolate(percentThroughSpiral);
				Vector sampleVector = unitVector.clone().multiply(baseSample.radius()).setY(archHeight);
				for (int stack = 0; stack < stacks; stack++)
				{
					double addedHeight = aqueductHeight + stack * aqueductHeight * 2;
					SpiralUtil.drawLine(cuboidByteWorld, lastSampleVector.clone().add(new Vector(0, addedHeight, 0)),
						sampleVector.clone().add(new Vector(0, addedHeight, 0)), CommandCarve.aqueductStart,
						CommandCarve.aqueductEnd, false);
				}
				lastSampleVector = sampleVector;
			}

			startPercent = endPercent;
		}
	}

	private static
	double getLengthThroughSpiralAtPercent(double startRadius, double endRadius, double percent)
	{
		return 2 * Math.PI * ((2 * startRadius + percent * (endRadius - startRadius)) / 2) * percent;
	}

	private static
	double getPercentThroughSpiralAtLength(double startRadius, double endRadius, double length)
	{
		double deltaRadius = endRadius - startRadius;
		return (
				   -Math.PI * startRadius +
				   Math.sqrt(Math.PI * (Math.PI * (startRadius * startRadius) + length * deltaRadius))
			   ) / (Math.PI * deltaRadius);
	}

	private static
	void drawLine(CuboidByteWorld cuboidByteWorld, Vector start, Vector finish, byte randStart, byte randEnd,
				  boolean step)
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
						cuboidByteWorld.put((byte) SpiralUtil.random.nextInt(randStart, randEnd + 1), x, y, z);
						if (!step)
						{
							continue;
						}
						for (int lx = x - 1; lx <= x + 1; lx++)
						{
							for (int lz = z - 1; lz <= z + 1; lz++)
							{
								if (cuboidByteWorld.at(lx, y - 1, lz) != 0 && cuboidByteWorld.at(lx, y, lz) == 0)
								{
									cuboidByteWorld.put((byte) CommandCarve.slab, lx, y, lz);
								}
							}
						}
					}
				}
			}
		}
	}

}
