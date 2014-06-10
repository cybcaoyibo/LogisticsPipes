package logisticspipes.proxy.ic2;

import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import logisticspipes.proxy.interfaces.IGenericProgressProvider;
import net.minecraft.tileentity.TileEntity;

public class IC2ProgressProvider implements IGenericProgressProvider {

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof TileEntityStandardMachine;
	}

	@Override
	public byte getProgress(TileEntity tile) {
		return (byte) Math.max(0, Math.min(((TileEntityStandardMachine) tile).getProgress() * 100, 100));
	}
}
