package com.mactso.hardercheating.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@Mixin(ServerCommonPacketListenerImpl.class)
public class RecipePacketCounterMixin {

    private int recipePacketCount = 0;

    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD")
    )
//    @Inject(at = @At("HEAD"), method = "loadLevel")
    private void onSend(Packet<?> packet, CallbackInfo ci) {
    	int x = 1;
    	
    	
    	if (packet instanceof ClientboundPlaceGhostRecipePacket) {
//        	System.out.println (packet.getClass().getName());
            recipePacketCount++;
//          System.out.println("[HarderCheating] Ghost recipe packets sent: " + recipePacketCount);
        }
    	
        else if (packet instanceof ClientboundContainerSetSlotPacket content) {
//        	System.out.println (packet.getClass().getName());
//            System.out.println("[HarderCheating] Real recipe placement (SetContent). " + content.getContainerId() + " Total: " + recipePacketCount);
            if (content.getContainerId() == 0 ) {
                recipePacketCount++;
//                System.out.println("[HarderCheating] Real recipe placement (SetContent). Total: " + recipePacketCount);
            }
        }
    }
}
