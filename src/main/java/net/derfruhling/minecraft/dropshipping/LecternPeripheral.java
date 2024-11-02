package net.derfruhling.minecraft.dropshipping;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.CapabilityUtil;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record LecternPeripheral(LecternBlockEntity block) implements IPeripheral {
    @Override
    public String getType() {
        return "lectern";
    }

    @Nullable
    @Override
    public Object getTarget() {
        return IPeripheral.super.getTarget();
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return false;
    }

    @LuaFunction
    public boolean hasBook() {
        return block.hasBook();
    }

    // from CC: Tweaked: InventoryMethods.java
    @Nullable
    private static IItemHandler extractHandler(IPeripheral peripheral) {
        var object = peripheral.getTarget();
        var direction = peripheral instanceof dan200.computercraft.shared.peripheral.generic.GenericPeripheral sided ? sided.side() : null;

        if (object instanceof BlockEntity blockEntity && blockEntity.isRemoved()) return null;

        if (object instanceof ICapabilityProvider provider) {
            var cap = CapabilityUtil.getCapability(provider, ForgeCapabilities.ITEM_HANDLER, direction);
            if (cap.isPresent()) return cap.orElseThrow(NullPointerException::new);
        }

        if (object instanceof IItemHandler handler) return handler;
        if (object instanceof Container container) return new InvWrapper(container);
        return null;
    }

    @LuaFunction(mainThread = true)
    public void pullBook(IComputerAccess access, String fromInv, int fromSlot) throws LuaException {
        var srcPeripheral = access.getAvailablePeripheral(fromInv);
        if (srcPeripheral == null) throw new LuaException("source peripheral not found");
        var src = extractHandler(srcPeripheral);
        if(src == null) throw new LuaException("source peripheral does not provide an inventory");

        var itemStack = src.extractItem(fromSlot, 1, true);
        if(itemStack.isEmpty()) {
            throw new LuaException("no item in that slot");
        }

        if(!itemStack.is(Items.WRITABLE_BOOK) && !itemStack.is(Items.WRITTEN_BOOK)) {
            throw new LuaException("item in source slot must be a writable or written book");
        }

        if(!LecternBlock.tryPlaceBook(null, Objects.requireNonNull(block.getLevel()), block.getBlockPos(), block.getBlockState(), itemStack)) {
            throw new LuaException("failed to place book in lectern");
        }

        src.extractItem(fromSlot, 1, false);
    }

    @LuaFunction(mainThread = true)
    public void pushBook(IComputerAccess access, String targetInv, int targetSlot) throws LuaException {
        var tgtPeripheral = access.getAvailablePeripheral(targetInv);
        if (tgtPeripheral == null) throw new LuaException("target peripheral not found");
        var tgt = extractHandler(tgtPeripheral);
        if(tgt == null) throw new LuaException("target peripheral does not provide an inventory");

        var itemStack = tgt.extractItem(targetSlot, 1, true);
        if(!itemStack.isEmpty()) {
            throw new LuaException("item already in that slot / slot inaccessible");
        }

        tgt.insertItem(targetSlot, block.getBook(), false);
        LecternBlock.resetBookState(null, Objects.requireNonNull(block.getLevel()), block.getBlockPos(), block.getBlockState(), false);
    }

    @LuaFunction(mainThread = true)
    public int getPageCount() throws LuaException {
        if(block.hasBook()) return WrittenBookItem.getPageCount(block.getBook());
        else throw new LuaException("no book present");
    }

    @LuaFunction(mainThread = true)
    public int getPage() throws LuaException {
        if(block.hasBook()) return block.getPage() + 1;
        else throw new LuaException("no book present");
    }

    @LuaFunction(mainThread = true)
    public void setPage(int page) throws LuaException {
        if(block.hasBook()) block.setPage(page - 1);
        else throw new LuaException("no book present");
    }

    @LuaFunction(mainThread = true)
    public String getPageText() throws LuaException {
        if(block.hasBook()) return Objects.requireNonNull(block.getBook().getTag()).getList(WrittenBookItem.TAG_PAGES, Tag.TAG_STRING).getString(block.getPage());
        else throw new LuaException("no book present");
    }
}
