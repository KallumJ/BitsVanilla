package team.bits.vanilla.fabric.mixin.challenges;

import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.vehicle.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.server.network.*;
import net.minecraft.util.*;
import net.minecraft.util.collection.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

@Mixin(ChestBoatEntity.class)
public class CargoShipMixin {

    @Shadow
    private DefaultedList<ItemStack> inventory;

    private ServerPlayerEntity lastPlayerInteracted;

    @Inject(
            method = "interact",
            at = @At("RETURN")
    )
    public void test(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        this.lastPlayerInteracted = (ServerPlayerEntity) player;
    }

    @Inject(
            method = "setStack",
            at = @At("RETURN")
    )
    public void onInsertItem(int _slot, ItemStack _stack, CallbackInfo ci) {
        if (this.isInventoryFullOfCobblestone()) {
            ((ExtendedPlayerEntity) lastPlayerInteracted).markChallengeCompleted(Challenges.CARGO_SHIP);
        }
    }

    private boolean isInventoryFullOfCobblestone() {
        int stacks = 0;
        for (ItemStack parentStack : this.inventory) {
            if (isShulkerBox(parentStack)) {
                Collection<ItemStack> contents = getShulkerBoxContents(parentStack);
                for (ItemStack childStack : contents) {
                    if (childStack.getItem().equals(Items.COBBLESTONE) && childStack.getCount() == 64) {
                        stacks++;
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return stacks == (27 * 27);
    }

    private static boolean isShulkerBox(@NotNull ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    private static @NotNull Collection<ItemStack> getShulkerBoxContents(@NotNull ItemStack shulkerStack) {
        // code based on ShulkerBoxBlock#appendTooltip()
        NbtCompound nbtCompound = BlockItem.getBlockEntityNbt(shulkerStack);
        if (nbtCompound != null) {
            if (nbtCompound.contains("Items", NbtElement.LIST_TYPE)) {
                DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
                Inventories.readNbt(nbtCompound, defaultedList);
                return defaultedList;
            }
        }
        return Collections.emptyList();
    }

}
