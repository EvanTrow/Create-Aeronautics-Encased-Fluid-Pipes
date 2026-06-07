package tech.trowbridge.aeroencasedpipe;

import tech.trowbridge.aeroencasedpipe.index.AddonBlockEntityTypes;
import tech.trowbridge.aeroencasedpipe.index.AddonBlocks;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(AeroEncasedPipe.MOD_ID)
public class AeroEncasedPipe {

    public static final String MOD_ID = "aeroencasedpipe";

    public AeroEncasedPipe(IEventBus modBus) {
        AddonBlocks.register(modBus);
        AddonBlockEntityTypes.register(modBus);
        modBus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            var optPipe = BuiltInRegistries.BLOCK.getOptional(
                    ResourceLocation.fromNamespaceAndPath("create", "fluid_pipe"));
            optPipe.ifPresent(pipe -> {
                if (pipe instanceof FluidPipeBlock fluidPipe) {
                    for (DyeColor color : DyeColor.values()) {
                        EncasingRegistry.addVariant(fluidPipe, AddonBlocks.getEncasedPipe(color));
                    }
                }
            });
        });
    }
}
