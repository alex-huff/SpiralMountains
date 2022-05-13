package dev.phonis.SpiralCarver.canvas;

public interface CuboidByteWorld
{

    int getMinX();

    int getMaxX();

    int getMinY();

    int getMaxY();

    int getMinZ();

    int getMaxZ();

    byte at(int x, int y, int z);

    void put(byte b, int x, int y, int z);

    default int getLogicalXAxisSize()
    {
        return this.getBlocksBetween(this.getMaxX(), this.getMinX());
    }

    default int getLogicalYAxisSize()
    {
        return this.getBlocksBetween(this.getMaxY(), this.getMinY());
    }

    default int getLogicalZAxisSize()
    {
        return this.getBlocksBetween(this.getMaxZ(), this.getMinZ());
    }

    default int getBlocksBetween(int one, int two)
    {
        return Math.abs(one - two) + 1;
    }

}
