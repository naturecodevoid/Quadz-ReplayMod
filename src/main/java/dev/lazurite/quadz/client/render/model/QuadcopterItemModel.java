package dev.lazurite.quadz.client.render.model;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.item.QuadcopterItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class QuadcopterItemModel extends AnimatedGeoModel<QuadcopterItem> {
    @Override
    public ResourceLocation getModelLocation(QuadcopterItem object) {
        return new ResourceLocation(Quadz.MODID, "geo/quadcopter_item.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(QuadcopterItem object) {
        return new ResourceLocation(Quadz.MODID, "textures/item/quadcopter_item.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(QuadcopterItem animatable) {
        return new ResourceLocation(Quadz.MODID, "animations/quadcopter_item.animation.json");
    }
}
