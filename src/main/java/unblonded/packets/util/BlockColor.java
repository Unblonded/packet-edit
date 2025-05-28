package unblonded.packets.util;

import net.minecraft.block.Block;

public class BlockColor {
    private Block block;
    private Color color;
    private boolean enabled = true; // default to enabled

    public BlockColor(Block block, Color color) {
        this.block = block;
        this.color = color;
    }

    public BlockColor(Block block, Color color, boolean enabled) {
        this.block = block;
        this.color = color;
        this.enabled = enabled;
    }

    public Block getBlock() {
        return block;
    }

    public Color getColor() {
        return color;
    }

    public float[] getColorF() {
        return new float[] {color.R(), color.G(), color.B(), color.A()};
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
