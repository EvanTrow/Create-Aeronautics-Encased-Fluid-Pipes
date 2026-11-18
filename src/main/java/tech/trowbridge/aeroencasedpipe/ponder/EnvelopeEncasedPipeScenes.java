package tech.trowbridge.aeroencasedpipe.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import tech.trowbridge.aeroencasedpipe.content.EnvelopeEncasedPipeBlock;
import tech.trowbridge.aeroencasedpipe.index.AddonBlocks;

public class EnvelopeEncasedPipeScenes {

    public static void sealingPipes(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("envelope_encased_pipe", "Sealing Fluid Pipes with Envelopes");
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(0.75f);
        scene.setSceneOffsetY(-1.5f);

        BlockPos lowerTank = util.grid().at(4, 2, 4);
        BlockPos pump = util.grid().at(4, 3, 4);
        BlockPos roofPipe = util.grid().at(4, 6, 4);
        BlockPos upperTank = util.grid().at(4, 7, 4);
        Selection pumpDrive = util.select().fromTo(5, 2, 4, 5, 3, 4);
        Vec3 roofPipeTop = util.vector().topOf(roofPipe);

        BlockState plainPipe = withVerticalConnections(AllBlocks.FLUID_PIPE.getDefaultState());
        BlockState whiteEncasedPipe = withVerticalConnections(
                AddonBlocks.getEncasedPipe(DyeColor.WHITE).defaultBlockState());
        BlockState blueEncasedPipe = withVerticalConnections(
                AddonBlocks.getEncasedPipe(DyeColor.BLUE).defaultBlockState());

        // Air inside the balloon (x/z 3..5, y 4..5). Hot air fills it from the roof downwards.
        AABB hotAirTop = new AABB(3, 6, 3, 6, 6, 6);
        AABB hotAirHalf = new AABB(3, 5, 3, 6, 6, 6);
        AABB hotAirFull = new AABB(3, 4, 3, 6, 6, 6);
        AABB leakingAir = new AABB(4.25, 6, 4.25, 4.75, 7.5, 4.75);

        scene.world().modifyBlockEntity(lowerTank, FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(new FluidStack(Fluids.WATER, 8000), IFluidHandler.FluidAction.EXECUTE));

        // Build the airship: deck, gondola, balloon walls, then the roof with the pipe crossing it.
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(0, 2, 0, 8, 3, 8), Direction.DOWN);
        scene.idle(5);
        for (int y = 4; y <= 5; y++) {
            scene.world().showSection(util.select().layer(y), Direction.DOWN);
            scene.idle(4);
        }
        scene.world().showSection(util.select().layer(6).substract(util.select().position(roofPipe)), Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(util.select().position(roofPipe), Direction.DOWN);
        scene.idle(15);

        Object hotAir = new Object();
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, hotAir, hotAirTop, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, hotAir, hotAirHalf, 20);
        scene.idle(8);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, hotAir, hotAirFull, 80);
        scene.idle(5);
        scene.overlay().showText(70)
                .text("Balloons need an airtight Envelope to hold Hot Air")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(util.grid().at(2, 5, 4), Direction.WEST))
                .placeNearTarget();
        scene.idle(80);

        // A plain pipe through the roof is a hole: the hot air squeezes out through it.
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, hotAir, hotAirFull, 15);
        scene.overlay().showOutline(PonderPalette.RED, roofPipe, util.select().position(roofPipe), 80);
        scene.idle(10);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, hotAir, leakingAir, 20);
        scene.effects().emitParticles(roofPipeTop,
                scene.effects().simpleParticleEmitter(ParticleTypes.CLOUD, new Vec3(0, 0.05, 0)), 0.5f, 40);
        scene.idle(5);
        scene.overlay().showText(80)
                .text("Fluid Pipes are not airtight, so Hot Air escapes wherever one passes through the Envelope")
                .colored(PonderPalette.RED)
                .attachKeyFrame()
                .pointAt(roofPipeTop)
                .placeNearTarget();
        scene.idle(95);

        // Encase it with an Envelope Block.
        scene.overlay().showControls(roofPipeTop, Pointing.DOWN, 30)
                .rightClick()
                .withItem(envelopeItem(DyeColor.WHITE));
        scene.idle(7);
        scene.world().setBlock(roofPipe, whiteEncasedPipe, true);
        scene.idle(15);
        scene.overlay().showText(70)
                .text("Fluid Pipes can be encased with Envelope Blocks to make them airtight")
                .attachKeyFrame()
                .pointAt(roofPipeTop)
                .placeNearTarget();
        scene.idle(10);

        Object sealedAir = new Object();
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, sealedAir, hotAirTop, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, sealedAir, hotAirHalf, 20);
        scene.idle(8);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, sealedAir, hotAirFull, 60);
        scene.idle(70);

        // Dyeing.
        scene.overlay().showControls(roofPipeTop, Pointing.DOWN, 25)
                .rightClick()
                .withItem(new ItemStack(Items.BLUE_DYE));
        scene.idle(7);
        scene.world().setBlock(roofPipe, blueEncasedPipe, false);
        scene.idle(10);
        scene.overlay().showText(50)
                .text("Right-Click with Dye to paint them")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .pointAt(roofPipeTop)
                .placeNearTarget();
        scene.idle(65);

        // Removing the casing, then putting the returned envelope straight back on.
        scene.overlay().showControls(roofPipeTop, Pointing.DOWN, 30)
                .rightClick()
                .withItem(AllItems.WRENCH.asStack())
                .whileSneaking();
        scene.idle(7);
        scene.world().setBlock(roofPipe, plainPipe, true);
        scene.idle(10);
        scene.overlay().showText(70)
                .text("Sneak + Wrench removes the Envelope again, returning it to your inventory")
                .attachKeyFrame()
                .pointAt(roofPipeTop)
                .placeNearTarget();
        scene.idle(85);
        scene.world().setBlock(roofPipe, whiteEncasedPipe, true);
        scene.idle(20);

        // Fluid keeps flowing through the encased section.
        scene.world().showSection(util.select().position(upperTank), Direction.DOWN);
        scene.idle(15);
        scene.world().setKineticSpeed(pumpDrive, 64);
        scene.world().setKineticSpeed(util.select().position(pump), -64);
        scene.world().propagatePipeChange(pump);
        scene.idle(25);
        scene.overlay().showText(80)
                .text("Fluids still flow through Envelope Encased Pipes, just like through regular ones")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(upperTank, Direction.WEST))
                .placeNearTarget();
        scene.idle(100);
    }

    private static ItemStack envelopeItem(DyeColor color) {
        return new ItemStack(EnvelopeEncasedPipeBlock.lookupEnvelopeBlock(color));
    }

    /**
     * Opens the up and down faces of a six-way pipe state and closes the rest.
     */
    private static BlockState withVerticalConnections(BlockState state) {
        for (Direction direction : Direction.values()) {
            BooleanProperty property = PipeBlock.PROPERTY_BY_DIRECTION.get(direction);
            if (state.hasProperty(property)) {
                state = state.setValue(property, direction.getAxis() == Direction.Axis.Y);
            }
        }
        return state;
    }
}
