package tech.trowbridge.aeroencasedpipe.ponder;

import java.util.ArrayList;
import java.util.List;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import tech.trowbridge.aeroencasedpipe.AeroEncasedPipe;

public class AddonPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return AeroEncasedPipe.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // Encased pipes are only made by encasing a pipe, so the scene is attached to Aeronautics envelope blocks, right after the "Constructing Balloons" scene.
        List<ResourceLocation> components = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            components.add(ResourceLocation.fromNamespaceAndPath(
                    AeroEncasedPipe.MOD_ID, color.getSerializedName() + "_envelope_encased_pipe"));
            components.add(ResourceLocation.fromNamespaceAndPath(
                    "aeronautics", color.getSerializedName() + "_envelope"));
        }

        helper.forComponents(components)
                .addStoryBoard("envelope_encased_pipe", EnvelopeEncasedPipeScenes::sealingPipes,
                        entry -> entry.orderAfter("aeronautics", "envelope"));
    }
}
