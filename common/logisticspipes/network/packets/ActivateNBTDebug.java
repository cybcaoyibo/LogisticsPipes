package logisticspipes.network.packets;

import java.io.IOException;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.nei.LoadingHelper;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;

public class ActivateNBTDebug extends ModernPacket {

	public ActivateNBTDebug(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {}

	@Override
	public void processPacket(EntityPlayer player) {
		try {
			Class.forName("codechicken.nei.forge.GuiContainerManager");
			Configs.TOOLTIP_INFO = true;
			LoadingHelper.loadNeiNBTDebugHelper();
		} catch (ClassNotFoundException e) {

		} catch (Exception e1) {
			if (LogisticsPipes.DEBUG) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {}

	@Override
	public ModernPacket template() {
		return new ActivateNBTDebug(getId());
	}
}
