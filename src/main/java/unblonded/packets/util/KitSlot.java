package unblonded.packets.util;

import net.minecraft.item.Item;

public class KitSlot {
    public final int slotIndex;
    public final Item item;

    public KitSlot(int slotIndex, Item item) {
        this.slotIndex = slotIndex;
        this.item = item;
    }
}
