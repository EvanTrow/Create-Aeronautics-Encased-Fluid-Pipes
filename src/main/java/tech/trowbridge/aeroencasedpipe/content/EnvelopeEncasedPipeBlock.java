package tech.trowbridge.aeroencasedpipe.content;

import tech.trowbridge.aeroencasedpipe.index.AddonBlockEntityTypes;
import tech.trowbridge.aeroencasedpipe.index.AddonBlocks;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.hot_air.envelope.Envelope;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class EnvelopeEncasedPipeBlock extends EncasedPipeBlock implements Envelope {

    private final DyeColor color;

    public EnvelopeEncasedPipeBlock(BlockBehaviour.Properties props, DyeColor color) {
        super(props, () -> lookupEnvelopeBlock(color));
        this.color = color;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        // Convert to plain fluid pipe (preserving connections and fluid flows), then return the envelope.
        // We call onWrenched() rather than super.onSneakWrenched() because the IWrenchable default for
        // sneak-wrench removes the entire block; we only want to strip the casing.
        InteractionResult result = onWrenched(state, context);
        Level world = context.getLevel();
        if (world instanceof ServerLevel) {
            Player player = context.getPlayer();
            if (player != null && !player.hasInfiniteMaterials()) {
                Block envelope = lookupEnvelopeBlock(color);
                if (envelope != Blocks.AIR) {
                    player.getInventory().placeItemBackInInventory(envelope.asItem().getDefaultInstance());
                }
            }
        }
        return result;
    }

    static Block lookupEnvelopeBlock(DyeColor color) {
        return BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath("aeronautics", color.getSerializedName() + "_envelope"))
                .orElse(Blocks.AIR);
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return AddonBlockEntityTypes.ENVELOPE_ENCASED_PIPE.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level,
            BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (itemStack.getItem() instanceof DyeItem dyeItem) {
            DyeColor newColor = dyeItem.getDyeColor();
            if (newColor != this.color) {
                EnvelopeEncasedPipeBlock targetBlock = AddonBlocks.getEncasedPipe(newColor);
                if (!level.isClientSide()) {
                    level.playSound(null, blockPos, SoundEvents.DYE_USE, SoundSource.BLOCKS,
                            1.0f, 1.1f - level.random.nextFloat() * 0.2f);
                    BlockState newState = EncasedPipeBlock.transferSixWayProperties(
                            blockState, targetBlock.defaultBlockState());
                    level.setBlockAndUpdate(blockPos, newState);
                    if (!player.hasInfiniteMaterials()) {
                        itemStack.shrink(1);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    public void handleEncasing(BlockState state, Level level, BlockPos pos, ItemStack heldItem,
            Player player, InteractionHand hand, BlockHitResult ray) {
        super.handleEncasing(state, level, pos, heldItem, player, hand, ray);
        if (!player.hasInfiniteMaterials()) {
            player.getItemInHand(hand).shrink(1);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(level, state, pos, entity, fallDistance);
        } else {
            entity.causeFallDamage(fallDistance, 0.5F, level.damageSources().fall());
        }
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(level, entity);
        } else {
            bounceUp(entity);
        }
    }

    private void bounceUp(Entity entity) {
        Vec3 movement = entity.getDeltaMovement();
        if (movement.y < 0.0D) {
            double scale = entity instanceof LivingEntity ? 0.5D : 0.25D;
            entity.setDeltaMovement(movement.x, -movement.y * scale, movement.z);
        }
    }
}
