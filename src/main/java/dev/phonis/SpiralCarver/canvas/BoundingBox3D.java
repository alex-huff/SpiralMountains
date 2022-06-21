package dev.phonis.SpiralCarver.canvas;

public
class BoundingBox3D
{

	private int minX;
	private int maxX;
	private int minY;
	private int maxY;
	private int minZ;
	private int maxZ;

	public
	BoundingBox3D(int minX, int maxX, int minY, int maxY, int minZ, int maxZ)
	{
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.minZ = minZ;
		this.maxZ = maxZ;
	}

	public
	int getMinX()
	{
		return this.minX;
	}

	public
	int getMaxX()
	{
		return this.maxX;
	}

	public
	int getMinY()
	{
		return this.minY;
	}

	public
	int getMaxY()
	{
		return this.maxY;
	}

	public
	int getMinZ()
	{
		return this.minZ;
	}

	public
	int getMaxZ()
	{
		return this.maxZ;
	}

	public
	void setMinX(int minX)
	{
		this.minX = minX;
	}

	public
	void setMaxX(int maxX)
	{
		this.maxX = maxX;
	}

	public
	void setMinY(int minY)
	{
		this.minY = minY;
	}

	public
	void setMaxY(int maxY)
	{
		this.maxY = maxY;
	}

	public
	void setMinZ(int minZ)
	{
		this.minZ = minZ;
	}

	public
	void setMaxZ(int maxZ)
	{
		this.maxZ = maxZ;
	}

}
