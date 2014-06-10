package logisticspipes.routing.pathfinder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeInformaitonManager {

	private Map<Class<?> /*TileEntity*/, Class<? extends IPipeInformationProvider>> infoProvider = new HashMap<Class<?>, Class<? extends IPipeInformationProvider>>();

	public IPipeInformationProvider getInformationProviderFor(TileEntity tile) {
		if (tile instanceof IPipeInformationProvider) {
			return (IPipeInformationProvider) tile;
		} else for (Class<?> type : infoProvider.keySet()) {
			if (type.isAssignableFrom(tile.getClass())) {
				try {
					IPipeInformationProvider provider = infoProvider.get(type).getDeclaredConstructor(type).newInstance(type.cast(tile));
					if (provider.isCorrect()) {
						return provider;
					}
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public void registerProvider(Class<?> source, Class<? extends IPipeInformationProvider> provider) {
		try {
			provider.getDeclaredConstructor(source);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		infoProvider.put(source, provider);
	}

	public boolean canConnect(IPipeInformationProvider startPipe, IPipeInformationProvider provider, ForgeDirection direction, boolean flag) {
		return startPipe.canConnect(provider, direction, flag) && provider.canConnect(startPipe, direction.getOpposite(), flag);
	}
}
