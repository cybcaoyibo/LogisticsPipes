package logisticspipes.items;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.pipes.upgrades.AdvancedSatelliteUpgrade;
import logisticspipes.pipes.upgrades.CCRemoteControlUpgrade;
import logisticspipes.pipes.upgrades.CombinedSneakyUpgrade;
import logisticspipes.pipes.upgrades.CraftingByproductUpgrade;
import logisticspipes.pipes.upgrades.CraftingMonitoringUpgrade;
import logisticspipes.pipes.upgrades.FluidCraftingUpgrade;
import logisticspipes.pipes.upgrades.FuzzyCraftingUpgrade;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.pipes.upgrades.OpaqueUpgrade;
import logisticspipes.pipes.upgrades.PatternUpgrade;
import logisticspipes.pipes.upgrades.PowerTransportationUpgrade;
import logisticspipes.pipes.upgrades.SpeedUpgrade;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeDOWN;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeEAST;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeNORTH;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeSOUTH;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeUP;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeWEST;
import logisticspipes.pipes.upgrades.power.BCPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2EVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2HVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2LVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2MVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.RFPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeDOWN;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeEAST;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeNORTH;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeSOUTH;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeUP;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeWEST;
import logisticspipes.utils.string.StringUtil;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemUpgrade extends LogisticsItem {

	//Sneaky Upgrades
	public static final int SNEAKY_UP = 0;
	public static final int SNEAKY_DOWN = 1;
	public static final int SNEAKY_NORTH = 2;
	public static final int SNEAKY_SOUTH = 3;
	public static final int SNEAKY_EAST = 4;
	public static final int SNEAKY_WEST = 5;
	public static final int SNEAKY_COMBINATION = 6;

	//Connection Upgrades
	public static final int CONNECTION_UP = 10;
	public static final int CONNECTION_DOWN = 11;
	public static final int CONNECTION_NORTH = 12;
	public static final int CONNECTION_SOUTH = 13;
	public static final int CONNECTION_EAST = 14;
	public static final int CONNECTION_WEST = 15;

	//Speed Upgrade
	public static final int SPEED = 20;

	//Crafting Upgrades
	public static final int ADVANCED_SAT_CRAFTINGPIPE = 21;
	public static final int LIQUID_CRAFTING = 22;
	public static final int CRAFTING_BYPRODUCT_EXTRACTOR = 23;
	public static final int SUPPLIER_PATTERN = 24;
	public static final int FUZZY_CRAFTING = 25;

	//Power Upgrades
	public static final int POWER_TRANSPORTATION = 30;
	public static final int POWER_BC_SUPPLIER = 31;
	public static final int POWER_RF_SUPPLIER = 32;
	public static final int POWER_IC2_LV_SUPPLIER = 33;
	public static final int POWER_IC2_MV_SUPPLIER = 34;
	public static final int POWER_IC2_HV_SUPPLIER = 35;
	public static final int POWER_IC2_EV_SUPPLIER = 36;

	//Various
	public static final int CC_REMOTE_CONTROL = 40;
	public static final int CRAFTING_MONITORING = 41;
	public static final int OPAQUE_UPGRADE = 42;

	//Values
	public static final int MAX_LIQUID_CRAFTER = 3;

	List<Upgrade> upgrades = new ArrayList<Upgrade>();
	private Icon[] icons;

	private final class Upgrade {

		private int id;
		private Class<? extends IPipeUpgrade> upgradeClass;
		private int textureIndex = -1;

		private Upgrade(int id, Class<? extends IPipeUpgrade> moduleClass, int textureIndex) {
			this.id = id;
			this.upgradeClass = moduleClass;
			this.textureIndex = textureIndex;
		}

		private IPipeUpgrade getIPipeUpgrade() {
			if (upgradeClass == null) return null;
			try {
				return upgradeClass.getConstructor(new Class[] {}).newInstance(new Object[] {});
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
			return null;
		}

		private Class<? extends IPipeUpgrade> getIPipeUpgradeClass() {
			return upgradeClass;
		}

		private int getId() {
			return id;
		}

		private int getTextureIndex() {
			return textureIndex;
		}
	}

	public ItemUpgrade(int i) {
		super(i);
		this.hasSubtypes = true;
	}

	public void loadUpgrades() {
		registerUpgrade(SNEAKY_UP, SneakyUpgradeUP.class, 0);
		registerUpgrade(SNEAKY_DOWN, SneakyUpgradeDOWN.class, 1);
		registerUpgrade(SNEAKY_NORTH, SneakyUpgradeNORTH.class, 2);
		registerUpgrade(SNEAKY_SOUTH, SneakyUpgradeSOUTH.class, 3);
		registerUpgrade(SNEAKY_EAST, SneakyUpgradeEAST.class, 4);
		registerUpgrade(SNEAKY_WEST, SneakyUpgradeWEST.class, 5);
		registerUpgrade(SNEAKY_COMBINATION, CombinedSneakyUpgrade.class, 6);
		registerUpgrade(SPEED, SpeedUpgrade.class, 7);
		registerUpgrade(CONNECTION_UP, ConnectionUpgradeUP.class, 8);
		registerUpgrade(CONNECTION_DOWN, ConnectionUpgradeDOWN.class, 9);
		registerUpgrade(CONNECTION_NORTH, ConnectionUpgradeNORTH.class, 10);
		registerUpgrade(CONNECTION_SOUTH, ConnectionUpgradeSOUTH.class, 11);
		registerUpgrade(CONNECTION_EAST, ConnectionUpgradeEAST.class, 12);
		registerUpgrade(CONNECTION_WEST, ConnectionUpgradeWEST.class, 13);

		registerUpgrade(ADVANCED_SAT_CRAFTINGPIPE, AdvancedSatelliteUpgrade.class, 14);
		registerUpgrade(LIQUID_CRAFTING, FluidCraftingUpgrade.class, 15);
		registerUpgrade(CRAFTING_BYPRODUCT_EXTRACTOR, CraftingByproductUpgrade.class, 16);
		registerUpgrade(SUPPLIER_PATTERN, PatternUpgrade.class, 17);
		registerUpgrade(FUZZY_CRAFTING, FuzzyCraftingUpgrade.class, 18);
		registerUpgrade(POWER_TRANSPORTATION, PowerTransportationUpgrade.class, 19);
		registerUpgrade(POWER_BC_SUPPLIER, BCPowerSupplierUpgrade.class, 20);
		registerUpgrade(POWER_RF_SUPPLIER, RFPowerSupplierUpgrade.class, 21);
		registerUpgrade(POWER_IC2_LV_SUPPLIER, IC2LVPowerSupplierUpgrade.class, 22);
		registerUpgrade(POWER_IC2_MV_SUPPLIER, IC2MVPowerSupplierUpgrade.class, 23);
		registerUpgrade(POWER_IC2_HV_SUPPLIER, IC2HVPowerSupplierUpgrade.class, 24);
		registerUpgrade(POWER_IC2_EV_SUPPLIER, IC2EVPowerSupplierUpgrade.class, 25);
		registerUpgrade(CC_REMOTE_CONTROL, CCRemoteControlUpgrade.class, 26);
		registerUpgrade(CRAFTING_MONITORING, CraftingMonitoringUpgrade.class, 27);
		registerUpgrade(OPAQUE_UPGRADE, OpaqueUpgrade.class, 28);
	}

	public void registerUpgrade(int id, Class<? extends IPipeUpgrade> moduleClass, int textureId) {
		boolean flag = true;
		for (Upgrade upgrade : upgrades) {
			if (upgrade.getId() == id) {
				flag = false;
			}
		}
		if (flag) {
			upgrades.add(new Upgrade(id, moduleClass, textureId));
		} else if (!flag) {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Upgrade. (Id " + id + " already in use)");
		} else {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Upgrade. (No name given)");
		}
	}

	public int[] getRegisteredUpgradeIDs() {
		int[] array = new int[upgrades.size()];
		int i = 0;
		for (Upgrade upgrade : upgrades) {
			array[i++] = upgrade.getId();
		}
		return array;
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.tabRedstone;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		for (Upgrade upgrade : upgrades) {
			par3List.add(new ItemStack(this, 1, upgrade.getId()));
		}
	}

	public IPipeUpgrade getUpgradeForItem(ItemStack itemStack, IPipeUpgrade currentUpgrade) {
		if (itemStack == null) return null;
		if (itemStack.itemID != this.itemID) return null;
		for (Upgrade upgrade : upgrades) {
			if (itemStack.getItemDamage() == upgrade.getId()) {
				if (upgrade.getIPipeUpgradeClass() == null) return null;
				if (currentUpgrade != null) {
					if (upgrade.getIPipeUpgradeClass().equals(currentUpgrade.getClass())) return currentUpgrade;
				}
				IPipeUpgrade newupgrade = upgrade.getIPipeUpgrade();
				if (newupgrade == null) return null;
				return newupgrade;
			}
		}
		return null;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		for (Upgrade upgrade : upgrades) {
			if (itemstack.getItemDamage() == upgrade.getId()) {
				return "item." + upgrade.getIPipeUpgradeClass().getSimpleName();
			}
		}
		return null;
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return StringUtil.translate(getUnlocalizedName(itemstack));
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		icons = new Icon[29];
		icons[0] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyUP");
		icons[1] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyDOWN");
		icons[2] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyNORTH");
		icons[3] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakySOUTH");
		icons[4] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyEAST");
		icons[5] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyWEST");
		icons[6] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyCombination");

		icons[7] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/Speed");

		icons[8] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisUP");
		icons[9] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisDOWN");
		icons[10] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisNORTH");
		icons[11] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisSOUTH");
		icons[12] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisEAST");
		icons[13] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisWEST");

		icons[14] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/Satelite");
		icons[15] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/FluidCrafting");
		icons[16] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/CraftingByproduct");
		icons[17] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/PlacementRules");
		icons[18] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/FuzzyCrafting");
		icons[19] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransport");
		icons[20] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportBC");
		icons[21] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportTE");
		icons[22] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportIC2-LV");
		icons[23] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportIC2-MV");
		icons[24] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportIC2-HV");
		icons[25] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportIC2-EV");
		icons[26] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/CCRemoteControl");
		icons[27] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/CraftingMonitoring");
		icons[28] = par1IconRegister.registerIcon("logisticspipes:itemUpgrade/OpaqueUpgrade");
	}

	@Override
	public Icon getIconFromDamage(int i) {

		for (Upgrade upgrade : upgrades) {
			if (upgrade.getId() == i) {
				if (upgrade.getTextureIndex() != -1) {
					return icons[upgrade.getTextureIndex()];
				}
			}
		}
		return icons[0];
	}
}
