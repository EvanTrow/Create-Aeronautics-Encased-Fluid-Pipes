package tech.trowbridge.aeroencasedpipe.index;

import tech.trowbridge.aeroencasedpipe.AeroEncasedPipe;
import tech.trowbridge.aeroencasedpipe.content.EnvelopeEncasedPipeBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public class AddonBlocks {

    private static final SoundType ENVELOPE_PIPE_SOUND = new SoundType(1.0f, 1.0f, null, null, null, null, null) {
        private SoundEvent cachedBreak, cachedPlace, cachedHit;

        private SoundEvent aero(String path, SoundEvent fallback) {
            return BuiltInRegistries.SOUND_EVENT
                    .getOptional(ResourceLocation.fromNamespaceAndPath("aeronautics", path))
                    .orElse(fallback);
        }

        @Override public SoundEvent getBreakSound() {
            if (cachedBreak == null) cachedBreak = aero("block.envelope.break", SoundType.WOOL.getBreakSound());
            return cachedBreak;
        }
        @Override public SoundEvent getStepSound()  { return SoundType.WOOL.getStepSound(); }
        @Override public SoundEvent getPlaceSound() {
            if (cachedPlace == null) cachedPlace = aero("block.envelope.place", SoundType.WOOL.getPlaceSound());
            return cachedPlace;
        }
        @Override public SoundEvent getHitSound() {
            if (cachedHit == null) cachedHit = aero("block.envelope.hit", SoundType.WOOL.getHitSound());
            return cachedHit;
        }
        @Override public SoundEvent getFallSound()  { return SoundType.WOOL.getFallSound(); }
    };

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, AeroEncasedPipe.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, AeroEncasedPipe.MOD_ID);

    private static final Map<DyeColor, DeferredHolder<Block, EnvelopeEncasedPipeBlock>> ENVELOPE_ENCASED_PIPES =
            new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            DeferredHolder<Block, EnvelopeEncasedPipeBlock> holder = BLOCKS.register(
                    color.getSerializedName() + "_envelope_encased_pipe",
                    () -> new EnvelopeEncasedPipeBlock(
                            BlockBehaviour.Properties.of()
                                    .noOcclusion()
                                    .sound(ENVELOPE_PIPE_SOUND)
                                    .mapColor(color),
                            color
                    )
            );
            ENVELOPE_ENCASED_PIPES.put(color, holder);

            final DeferredHolder<Block, EnvelopeEncasedPipeBlock> blockHolder = holder;
            ITEMS.register(
                    color.getSerializedName() + "_envelope_encased_pipe",
                    () -> new BlockItem(blockHolder.get(), new Item.Properties())
            );
        }
    }

    public static EnvelopeEncasedPipeBlock getEncasedPipe(DyeColor color) {
        return ENVELOPE_ENCASED_PIPES.get(color).get();
    }

    public static Block[] getAllEncasedPipes() {
        return ENVELOPE_ENCASED_PIPES.values().stream()
                .map(DeferredHolder::get)
                .toArray(Block[]::new);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
