package unblonded.packets.util;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class PosColor extends Color {
    public BlockPos pos;

    public PosColor(BlockPos pos, Color color) {
        super(color);
        this.pos = pos;
    }

    public Color getColor() {
        return new Color(R(), G(), B(), A());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        PosColor posColor = (PosColor) obj;
        return Objects.equals(pos, posColor.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pos);
    }

    @Override
    public String toString() {
        return "PosColor{pos=" + pos + ", color=Color[r=" + R() + ",g=" + G() + ",b=" + B() + ",a=" + A() + "]}";
    }
}