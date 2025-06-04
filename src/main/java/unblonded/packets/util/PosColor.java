package unblonded.packets.util;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class PosColor {
    public BlockPos pos;
    public Color color;

    public PosColor(BlockPos pos, Color color) {
        this.pos = pos;
        this.color = color;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PosColor posColor = (PosColor) obj;
        return Objects.equals(pos, posColor.pos) && Objects.equals(color, posColor.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, color);
    }

    @Override
    public String toString() {
        return "PosColor{pos=" + pos + ", color=" + color + '}';
    }
}