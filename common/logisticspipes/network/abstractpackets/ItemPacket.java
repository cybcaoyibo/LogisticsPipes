package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.item.ItemStack;

@Accessors(chain = true)
public abstract class ItemPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private ItemStack stack;

	public ItemPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		if (getStack() != null) {
			data.writeInt(getStack().itemID);
			data.writeInt(getStack().stackSize);
			data.writeInt(getStack().getItemDamage());
			data.writeNBTTagCompound(getStack().getTagCompound());
		} else {
			data.writeInt(0);
		}
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);

		final int itemID = data.readInt();
		if (itemID != 0) {
			int stackSize = data.readInt();
			int damage = data.readInt();
			setStack(new ItemStack(itemID, stackSize, damage));
			getStack().setTagCompound(data.readNBTTagCompound());
		} else {
			setStack(null);
		}
	}
}
