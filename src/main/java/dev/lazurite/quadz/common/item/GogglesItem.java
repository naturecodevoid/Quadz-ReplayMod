package dev.lazurite.quadz.common.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class GogglesItem extends ArmorItem implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public GogglesItem(Item.Properties properties) {
        super(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, properties.tab(CreativeModeTab.TAB_COMBAT));
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "goggles_controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
