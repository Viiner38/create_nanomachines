package net.viiner.nanomachines.client;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.viiner.nanomachines.Nanomachines;

public class BloomeryPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return Nanomachines.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(new ResourceLocation(Nanomachines.MOD_ID, "bloomery"))
                .addStoryBoard("bloomery/sizes", BloomeryScenes::sizes)
                .addStoryBoard("bloomery/usage", BloomeryScenes::usage);
    }
}