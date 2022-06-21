package dev.phonis.SpiralCarver.canvas.impl;

import dev.phonis.SpiralCarver.canvas.BoundingBox3D;
import dev.phonis.SpiralCarver.canvas.CuboidByteWorld;

import java.util.HashMap;
import java.util.Map;

public
class ChunkBasedAutoExpandingCuboidByteWorld implements CuboidByteWorld
{

	private static final int CHUNK_SIZE  = 64;
	private static final int CHUNK_SHIFT = 6;

	private
	record ChunkPos(int x, int y, int z)
	{

	}

	private final BoundingBox3D             logicalBounds;
	private final Map<ChunkPos, byte[][][]> world = new HashMap<>();

	public
	ChunkBasedAutoExpandingCuboidByteWorld()
	{
		this.logicalBounds = new BoundingBox3D(0, 0, 0, 0, 0, 0);
	}

	@Override
	public
	int getMinX()
	{
		return this.logicalBounds.getMinX();
	}

	@Override
	public
	int getMaxX()
	{
		return this.logicalBounds.getMaxX();
	}

	@Override
	public
	int getMinY()
	{
		return this.logicalBounds.getMinY();
	}

	@Override
	public
	int getMaxY()
	{
		return this.logicalBounds.getMaxY();
	}

	@Override
	public
	int getMinZ()
	{
		return this.logicalBounds.getMinZ();
	}

	@Override
	public
	int getMaxZ()
	{
		return this.logicalBounds.getMaxZ();
	}

	@Override
	public
	byte at(int x, int y, int z)
	{
		ChunkPos chunkPos = this.getChunkPosFor(x, y, z);
		int      xOff     = this.getOffsetFor(x);
		int      yOff     = this.getOffsetFor(y);
		int      zOff     = this.getOffsetFor(z);
		if (this.world.containsKey(chunkPos))
		{
			return this.world.get(chunkPos)[xOff][yOff][zOff];
		}
		return 0;
	}

	@Override
	public
	void put(byte b, int x, int y, int z)
	{
		this.expandLogicalBounds(x, y, z);
		ChunkPos chunkPos = this.getChunkPosFor(x, y, z);
		int      xOff     = this.getOffsetFor(x);
		int      yOff     = this.getOffsetFor(y);
		int      zOff     = this.getOffsetFor(z);
		this.getOrCreateChunkFor(chunkPos)[xOff][yOff][zOff] = b;
	}

	private
	int getOffsetFor(int pos)
	{
		return Math.floorMod(pos, ChunkBasedAutoExpandingCuboidByteWorld.CHUNK_SIZE);
	}

	private
	ChunkPos getChunkPosFor(int x, int y, int z)
	{
		return new ChunkPos(
			x >> ChunkBasedAutoExpandingCuboidByteWorld.CHUNK_SHIFT,
			y >> ChunkBasedAutoExpandingCuboidByteWorld.CHUNK_SHIFT,
			z >> ChunkBasedAutoExpandingCuboidByteWorld.CHUNK_SHIFT
		);
	}

	private
	byte[][][] getOrCreateChunkFor(ChunkPos chunkPos)
	{
		return this.world.computeIfAbsent(
			chunkPos,
			k -> new byte[ChunkBasedAutoExpandingCuboidByteWorld.CHUNK_SIZE][ChunkBasedAutoExpandingCuboidByteWorld.CHUNK_SIZE][ChunkBasedAutoExpandingCuboidByteWorld.CHUNK_SIZE]
		);
	}

	private
	void expandLogicalBounds(int x, int y, int z)
	{
		int newLogicalMinX = Math.min(x, this.logicalBounds.getMinX());
		int newLogicalMaxX = Math.max(x, this.logicalBounds.getMaxX());
		int newLogicalMinY = Math.min(y, this.logicalBounds.getMinY());
		int newLogicalMaxY = Math.max(y, this.logicalBounds.getMaxY());
		int newLogicalMinZ = Math.min(z, this.logicalBounds.getMinZ());
		int newLogicalMaxZ = Math.max(z, this.logicalBounds.getMaxZ());
		this.logicalBounds.setMinX(newLogicalMinX);
		this.logicalBounds.setMaxX(newLogicalMaxX);
		this.logicalBounds.setMinY(newLogicalMinY);
		this.logicalBounds.setMaxY(newLogicalMaxY);
		this.logicalBounds.setMinZ(newLogicalMinZ);
		this.logicalBounds.setMaxZ(newLogicalMaxZ);
	}

}
