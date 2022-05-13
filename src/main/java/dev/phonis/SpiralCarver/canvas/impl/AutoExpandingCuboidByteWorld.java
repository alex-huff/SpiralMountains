package dev.phonis.SpiralCarver.canvas.impl;

import dev.phonis.SpiralCarver.canvas.BoundingBox3D;
import dev.phonis.SpiralCarver.canvas.CuboidByteWorld;

public class AutoExpandingCuboidByteWorld implements CuboidByteWorld
{

    private final BoundingBox3D logicalBounds;
    private final BoundingBox3D realBounds;
    private       byte[][][]    world;

    public AutoExpandingCuboidByteWorld(int startSize)
    {
        this.logicalBounds = new BoundingBox3D(0, 0, 0, 0, 0, 0);
        this.realBounds    = new BoundingBox3D(-startSize, startSize,
                                               -startSize, startSize,
                                               -startSize, startSize
        );
        world              = new byte[this.getRealXAxisSize()][this.getRealYAxisSize()][this.getRealZAxisSize()];
    }

    @Override
    public int getMinX()
    {
        return this.logicalBounds.getMinX();
    }

    @Override
    public int getMaxX()
    {
        return this.logicalBounds.getMaxX();
    }

    @Override
    public int getMinY()
    {
        return this.logicalBounds.getMinY();
    }

    @Override
    public int getMaxY()
    {
        return this.logicalBounds.getMaxY();
    }

    @Override
    public int getMinZ()
    {
        return this.logicalBounds.getMinZ();
    }

    @Override
    public int getMaxZ()
    {
        return this.logicalBounds.getMaxZ();
    }

    @Override
    public byte at(int x, int y, int z)
    {
        if (this.inBounds(x, y, z))
        {
            return this.world[this.getRealIndexOfX(x)][this.getRealIndexOfY(y)][this.getRealIndexOfZ(z)];
        }

        return 0;
    }

    @Override
    public void put(byte b, int x, int y, int z)
    {
        this.expandToFit(x, y, z);
        this.world[this.getRealIndexOfX(x)][this.getRealIndexOfY(y)][this.getRealIndexOfZ(z)] = b;
    }

    private void expandToFit(int x, int y, int z)
    {
        this.expandX(x);
        this.expandY(y);
        this.expandZ(z);
    }

    private void expandX(int x)
    {
        if (this.numInBounds(x, this.logicalBounds.getMinX(), this.logicalBounds.getMaxX())) return;

        int newLogicalMinX  = Math.min(x, this.logicalBounds.getMinX());
        int newLogicalMaxX  = Math.max(x, this.logicalBounds.getMaxX());
        if (this.numInBounds(x, this.realBounds.getMinX(), this.realBounds.getMaxX()))
        {
            this.logicalBounds.setMinX(newLogicalMinX);
            this.logicalBounds.setMaxX(newLogicalMaxX);
            return;
        }

        int extraOnEachSide = (int) Math.ceil(.15D * this.getBlocksBetween(newLogicalMaxX, newLogicalMinX));
        int newRealMinX     = newLogicalMinX - extraOnEachSide;
        int newRealMaxX     = newLogicalMaxX + extraOnEachSide;
        byte[][][] newArray =
            new byte[this.getBlocksBetween(newRealMaxX, newRealMinX)][this.getRealYAxisSize()][this.getRealZAxisSize()];
        System.arraycopy(this.world, this.getRealIndexOfX(this.logicalBounds.getMinX()), newArray,
                         this.logicalBounds.getMinX() - newRealMinX, this.getLogicalXAxisSize()
        );
        this.world = newArray;
        this.logicalBounds.setMinX(newLogicalMinX);
        this.logicalBounds.setMaxX(newLogicalMaxX);
        this.realBounds.setMinX(newRealMinX);
        this.realBounds.setMaxX(newRealMaxX);
    }

    private void expandY(int y)
    {
        if (this.numInBounds(y, this.logicalBounds.getMinY(), this.logicalBounds.getMaxY())) return;

        int newLogicalMinY  = Math.min(y, this.logicalBounds.getMinY());
        int newLogicalMaxY  = Math.max(y, this.logicalBounds.getMaxY());
        if (this.numInBounds(y, this.realBounds.getMinY(), this.realBounds.getMaxY()))
        {
            this.logicalBounds.setMinY(newLogicalMinY);
            this.logicalBounds.setMaxY(newLogicalMaxY);
            return;
        }

        int extraOnEachSide = (int) (.15D * this.getBlocksBetween(newLogicalMaxY, newLogicalMinY));
        int newRealMinY     = newLogicalMinY - extraOnEachSide;
        int newRealMaxY     = newLogicalMaxY + extraOnEachSide;
        int newYAxisSize = this.getBlocksBetween(newRealMaxY, newRealMinY);
        for (int x = this.realBounds.getMinX(); x <= this.realBounds.getMaxX(); x++)
        {
            int realXIndex = this.getRealIndexOfX(x);
            byte[][] newArray =
                new byte[newYAxisSize][this.getRealZAxisSize()];
            if (this.numInBounds(x, this.logicalBounds.getMinX(), this.logicalBounds.getMaxX()))
            {
                System.arraycopy(this.world[realXIndex], this.getRealIndexOfY(this.logicalBounds.getMinY()), newArray,
                                 this.logicalBounds.getMinY() - newRealMinY, this.getLogicalYAxisSize()
                );
            }
            this.world[realXIndex] = newArray;
        }
        this.logicalBounds.setMinY(newLogicalMinY);
        this.logicalBounds.setMaxY(newLogicalMaxY);
        this.realBounds.setMinY(newRealMinY);
        this.realBounds.setMaxY(newRealMaxY);
    }

    private void expandZ(int z)
    {
        if (this.numInBounds(z, this.logicalBounds.getMinZ(), this.logicalBounds.getMaxZ())) return;

        int newLogicalMinZ  = Math.min(z, this.logicalBounds.getMinZ());
        int newLogicalMaxZ  = Math.max(z, this.logicalBounds.getMaxZ());
        if (this.numInBounds(z, this.realBounds.getMinZ(), this.realBounds.getMaxZ()))
        {
            this.logicalBounds.setMinZ(newLogicalMinZ);
            this.logicalBounds.setMaxZ(newLogicalMaxZ);
            return;
        }

        int extraOnEachSide = (int) (.15D * this.getBlocksBetween(newLogicalMaxZ, newLogicalMinZ));
        int newRealMinZ     = newLogicalMinZ - extraOnEachSide;
        int newRealMaxZ     = newLogicalMaxZ + extraOnEachSide;
        int newZAxisSize = this.getBlocksBetween(newRealMaxZ, newRealMinZ);
        for (int x = this.realBounds.getMinX(); x <= this.realBounds.getMaxX(); x++)
        {
            int realXIndex = this.getRealIndexOfX(x);
            for (int y = this.realBounds.getMinY(); y <= this.realBounds.getMaxY(); y++)
            {
                int realYIndex = this.getRealIndexOfY(y);
                byte[] newArray =
                    new byte[newZAxisSize];
                if (this.numInBounds(x, this.logicalBounds.getMinX(), this.logicalBounds.getMaxX()) &&
                    this.numInBounds(y, this.logicalBounds.getMinY(), this.logicalBounds.getMaxY()))
                {
                    System.arraycopy(this.world[realXIndex][realYIndex],
                                     this.getRealIndexOfZ(this.logicalBounds.getMinZ()),
                                     newArray,
                                     this.logicalBounds.getMinZ() - newRealMinZ, this.getLogicalZAxisSize()
                    );
                }
                this.world[realXIndex][realYIndex] = newArray;
            }
        }
        this.logicalBounds.setMinZ(newLogicalMinZ);
        this.logicalBounds.setMaxZ(newLogicalMaxZ);
        this.realBounds.setMinZ(newRealMinZ);
        this.realBounds.setMaxZ(newRealMaxZ);
    }

    private int getRealXAxisSize()
    {
        return this.getBlocksBetween(this.realBounds.getMaxX(), this.realBounds.getMinX());
    }

    private int getRealYAxisSize()
    {
        return this.getBlocksBetween(this.realBounds.getMaxY(), this.realBounds.getMinY());
    }

    private int getRealZAxisSize()
    {
        return this.getBlocksBetween(this.realBounds.getMaxZ(), this.realBounds.getMinZ());
    }

    private int getRealIndexOfX(int x)
    {
        return x - this.realBounds.getMinX();
    }

    private int getRealIndexOfY(int y)
    {
        return y - this.realBounds.getMinY();
    }

    private int getRealIndexOfZ(int z)
    {
        return z - this.realBounds.getMinZ();
    }

    private boolean inBounds(int x, int y, int z)
    {
        return this.numInBounds(x, this.logicalBounds.getMinX(), this.logicalBounds.getMaxX()) &&
               this.numInBounds(y, this.logicalBounds.getMinY(), this.logicalBounds.getMaxY()) &&
               this.numInBounds(z, this.logicalBounds.getMinZ(), this.logicalBounds.getMaxZ());
    }

    private boolean numInBounds(int i, int min, int max)
    {
        return i >= min && i <= max;
    }

}
