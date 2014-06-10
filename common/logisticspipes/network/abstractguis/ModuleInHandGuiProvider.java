package logisticspipes.network.abstractguis;

import java.io.IOException;

import logisticspipes.LogisticsPipes;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@Accessors(chain = true)
public abstract class ModuleInHandGuiProvider extends GuiProvider {

	public ModuleInHandGuiProvider(int id) {
		super(id);
	}

	@Getter
	@Setter
	private int invSlot;

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(invSlot);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		invSlot = data.readInt();
	}

	public final LogisticsModule getLogisticsModule(EntityPlayer player) {
		ItemStack item = player.inventory.mainInventory[invSlot];
		if (item == null) return null;
		LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
		module.registerSlot(-1 - invSlot);
		ItemModuleInformationManager.readInformation(item, module);
		return module;
	}
}
