package unblonded.packets.util;

import net.minecraft.block.Block;

public class BlockColor {
    private Block block;
    private Color color;

    public BlockColor(Block block, Color color) {
        this.block = block;
        this.color = color;
    }

    public Block getBlock() {
        return block;
    }

    public Color getColor() {
        return color;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
