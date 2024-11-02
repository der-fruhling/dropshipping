package net.derfruhling.minecraft.dropshipping;

import com.chaosthedude.endermail.block.PackageBlock;
import com.chaosthedude.endermail.block.entity.PackageBlockEntity;
import com.chaosthedude.endermail.entity.EnderMailmanEntity;
import com.chaosthedude.endermail.registry.EnderMailBlocks;
import com.chaosthedude.endermail.registry.EnderMailEntities;
import com.chaosthedude.endermail.registry.EnderMailItems;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public record PackagePeripheral(PackageBlockEntity block) implements IPeripheral {
    @Override
    public String getType() {
        return "ender_package";
    }

    @Override
    public Set<String> getAdditionalTypes() {
        return IPeripheral.super.getAdditionalTypes();
    }

    @Override
    public void attach(IComputerAccess computer) {
        computer.queueEvent("ender_package_ready");
    }

    @Nullable
    @Override
    public Object getTarget() {
        return block;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return equals((Object)other);
    }

    @LuaFunction
    public void ship() {
        PackageBlock.setState(true, Objects.requireNonNull(block.getLevel()), block.getBlockPos());

        var level = block.getLevel();

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            var realBlock = EnderMailBlocks.PACKAGE.get();
            var state = block.getBlockState();
            if (realBlock.isStamped(state)) {
                BlockPos deliveryPos = block.getDeliveryPos();
                String lockerID = block.getLockerID();
                EnderMailmanEntity enderMailman = new EnderMailmanEntity(EnderMailEntities.ENDER_MAILMAN.get(), level, block.getBlockPos(), deliveryPos, lockerID, new ItemStack(EnderMailItems.PACKAGE_CONTROLLER.get(), 1));
                level.addFreshEntity(enderMailman);
            }
        }
    }

    @LuaFunction
    public void setLockerId(String lockerId) {
        block.setLockerID(lockerId);
    }

    @LuaFunction
    public void setTargetPosition(int x, int y, int z) {
        block.setDeliveryPos(new BlockPos(x, y, z), true);
    }
}
