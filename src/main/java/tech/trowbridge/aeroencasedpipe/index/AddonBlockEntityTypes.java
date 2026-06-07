package tech.trowbridge.aeroencasedpipe.index;

import tech.trowbridge.aeroencasedpipe.AeroEncasedPipe;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AddonBlockEntityTypes {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AeroEncasedPipe.MOD_ID);

    private static BlockEntityType<FluidPipeBlockEntity> envelopeEncasedPipeType;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>>
            ENVELOPE_ENCASED_PIPE = BLOCK_ENTITIES.register("envelope_encased_pipe", () -> {
        envelopeEncasedPipeType = BlockEntityType.Builder.of(
                AddonBlockEntityTypes::createPipeEntity,
                AddonBlocks.getAllEncasedPipes()
        ).build(null);
        return envelopeEncasedPipeType;
    });

    private static FluidPipeBlockEntity createPipeEntity(BlockPos pos, BlockState state) {
        return new FluidPipeBlockEntity(envelopeEncasedPipeType, pos, state);
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
