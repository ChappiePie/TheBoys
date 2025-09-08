package chappie.theboys.mixin.client;

import chappie.theboys.util.interfaces.IItemRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin implements IItemRenderState {

    @Shadow private ItemStackRenderState.LayerRenderState[] layers;

    @Override
    public ItemStackRenderState theBoys$getRenderState() {
        if (this.layers[0] instanceof IItemRenderState state) {
            return state.theBoys$getRenderState();
        }
        return null;
    }
}
