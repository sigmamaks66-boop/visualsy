package pl.neonvisuals.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.neonvisuals.NeonConfig;
import pl.neonvisuals.NeonHud;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    /** Rysuje nasz HUD po całym oryginalnym HUD-zie. */
    @Inject(method = "render", at = @At("TAIL"))
    private void neon$render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        NeonHud.render(context);
    }

    /** Chowa zwykły celownik, gdy włączony jest neonowy. */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void neon$hideCrosshair(CallbackInfo ci) {
        if (NeonConfig.crosshair) {
            ci.cancel();
        }
    }
}
