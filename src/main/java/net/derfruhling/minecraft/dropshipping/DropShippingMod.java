package net.derfruhling.minecraft.dropshipping;

import com.chaosthedude.endermail.block.entity.PackageBlockEntity;
import com.chaosthedude.endermail.registry.EnderMailBlocks;
import com.mojang.logging.LogUtils;
import dan200.computercraft.impl.Peripherals;
import dan200.computercraft.shared.Capabilities;
import dan200.computercraft.shared.util.CapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DropShippingMod.MODID)
public class DropShippingMod {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "dropshipping";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public DropShippingMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if(event.getObject().getBlockState().is(EnderMailBlocks.PACKAGE.get())) {
            CapabilityProvider.attach(event, new ResourceLocation(MODID, "ender_package"), Capabilities.CAPABILITY_PERIPHERAL, () -> new PackagePeripheral((PackageBlockEntity) event.getObject()));
        } else if(event.getObject().getBlockState().is(Blocks.LECTERN)) {
            CapabilityProvider.attach(event, new ResourceLocation(MODID, "lectern"), Capabilities.CAPABILITY_PERIPHERAL, () -> new LecternPeripheral((LecternBlockEntity) event.getObject()));
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
