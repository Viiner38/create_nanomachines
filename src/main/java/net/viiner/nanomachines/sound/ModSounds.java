package net.viiner.nanomachines.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.viiner.nanomachines.Nanomachines;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Nanomachines.MOD_ID);

    public static final RegistryObject<SoundEvent> PLASMA_BEAM = SOUNDS.register("plasma_beam",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Nanomachines.MOD_ID, "plasma_beam")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}