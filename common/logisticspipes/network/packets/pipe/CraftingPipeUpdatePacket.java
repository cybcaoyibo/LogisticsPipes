package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.items.ItemUpgrade;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain = true)
public class CraftingPipeUpdatePacket extends CoordinatesPacket {

	@Getter
	@Setter
	private int[] amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];

	@Getter
	@Setter
	private int[] liquidSatelliteIdArray = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];

	@Getter
	@Setter
	private int liquidSatelliteId = 0;

	@Getter
	@Setter
	private int satelliteId = 0;

	@Getter
	@Setter
	private int[] advancedSatelliteIdArray = new int[9];

	@Getter
	@Setter
	private int[] fuzzyCraftingFlagArray = new int[9];

	@Getter
	@Setter
	private int priority = 0;

	public CraftingPipeUpdatePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) return;
		if (!(pipe.pipe instanceof PipeItemsCraftingLogistics)) return;
		((PipeItemsCraftingLogistics) pipe.pipe).handleCraftingUpdatePacket(this);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeIntegerArray(amount);
		data.writeIntegerArray(liquidSatelliteIdArray);
		data.writeInt(liquidSatelliteId);
		data.writeInt(satelliteId);
		data.writeIntegerArray(advancedSatelliteIdArray);
		data.writeIntegerArray(fuzzyCraftingFlagArray);
		data.writeInt(priority);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		amount = data.readIntegerArray();
		liquidSatelliteIdArray = data.readIntegerArray();
		liquidSatelliteId = data.readInt();
		satelliteId = data.readInt();
		advancedSatelliteIdArray = data.readIntegerArray();
		fuzzyCraftingFlagArray = data.readIntegerArray();
		priority = data.readInt();
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipeUpdatePacket(getId());
	}
}
