package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.materials.ArmorMaterials;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class GogglesItem extends ArmorItem {
	public GogglesItem(Item.Settings settings) {
		super(ArmorMaterials.GOGGLE, EquipmentSlot.HEAD, settings);
	}

	@Override
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
		return super.getAttributeModifiers(EquipmentSlot.MAINHAND); // not HEAD
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(itemStack);
		ItemStack itemStack2 = user.getEquippedStack(equipmentSlot);

		if (itemStack2.isEmpty()) {
			user.equipStack(equipmentSlot, itemStack.copy());
			itemStack.setCount(0);
			itemStack = new ItemStack(Items.AIR);

//			Packet<?> packet = ((NetworkSyncedItem)itemStack.getItem()).createSyncPacket(itemStack, world, user);
//			ServerSidePacketRegistry.INSTANCE.sendToPlayer(user, packet);

			return TypedActionResult.method_29237(itemStack, world.isClient());
		} else {
			return TypedActionResult.fail(itemStack);
		}
	}

	public static void setValue(ItemStack stack, String key, Number value) {
		switch (key) {
			case "band":
				setBand(stack, value.intValue());
				break;
			case "channel":
				setChannel(stack, value.intValue());
				break;
			default:
				break;
		}
	}

	public static int getValue(ItemStack stack, String key) {
		switch (key) {
			case "band":
				return getBand(stack);
			case "channel":
				return getChannel(stack);
			default:
				return 0; // unknown key, default value
		}
	}

	public static void setBand(ItemStack itemStack, int band) {
		itemStack.getOrCreateSubTag("frequency").putInt("band", band);
	}

	public static int getBand(ItemStack itemStack) {
		if(itemStack.getSubTag("frequency") != null)
			return itemStack.getSubTag("frequency").getInt("band");
		return 0;
	}

	public static void setChannel(ItemStack itemStack, int channel) {
		itemStack.getOrCreateSubTag("frequency").putInt("channel", channel);
	}

	public static int getChannel(ItemStack itemStack) {
		if(itemStack.getSubTag("frequency") != null)
			return itemStack.getSubTag("frequency").getInt("channel");
		return 0;
	}

	public static void setOn(ItemStack itemStack, boolean on, PlayerEntity player, String[] keys) {
		if(itemStack.getSubTag("misc") != null && itemStack.getSubTag("misc").contains("on")) {
			if(on && !itemStack.getSubTag("misc").getBoolean("on")) {

				String subString = keys[0] + " or " + keys[1];

				player.sendMessage(new LiteralText("Press " + subString + " power off goggles"), true);
			}
		}
		itemStack.getOrCreateSubTag("misc").putBoolean("on", on);
	}

	public static boolean isOn(PlayerEntity player) {
		if(GogglesItem.isWearingGoggles(player)) {
			ItemStack hat = player.inventory.armor.get(3);
			if (hat.getSubTag("misc") != null && hat.getSubTag("misc").contains("on"))
				return hat.getSubTag("misc").getBoolean("on");
		}
		return false;
	}

	public static boolean isWearingGoggles(PlayerEntity player) {
		return player.inventory.armor.get(3).getItem() instanceof GogglesItem;
	}

	public static boolean isOnRightChannel(DroneEntity drone, PlayerEntity player) {
		if (GogglesItem.isWearingGoggles(player)) {
			ItemStack hat = player.inventory.armor.get(3);
			if(GogglesItem.isOn(player)) {
				return drone.getBand() == GogglesItem.getBand(hat) && drone.getChannel() == GogglesItem.getChannel(hat);
			}
		}
		return false;
	}
}
