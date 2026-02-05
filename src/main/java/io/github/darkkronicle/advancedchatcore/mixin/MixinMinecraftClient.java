/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.mixin;

import io.github.darkkronicle.advancedchatcore.chat.AdvancedChatScreen;
import io.github.darkkronicle.advancedchatcore.chat.AdvancedSleepingChatScreen;
import io.github.darkkronicle.advancedchatcore.chat.ChatHistory;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "disconnect(Lnet/minecraft/text/Text;)V", at = @At("RETURN"))
    public void disconnect(CallbackInfo ci) {
        if (ConfigStorage.General.CLEAR_ON_DISCONNECT.config.getBooleanValue()) {
            ChatHistory.getInstance().clearAll();
        }
    }

    @Inject(method = "openChatScreen(Lnet/minecraft/client/gui/hud/ChatHud$ChatMethod;)V",
            at = @At(value = "HEAD"), cancellable = true)
    public void openChatScreen(ChatHud.ChatMethod chatMethod, CallbackInfo ci) {
        String text = chatMethod.getReplacement();
        MinecraftClient.getInstance().setScreen(new AdvancedChatScreen(text != null ? text : ""));
        ci.cancel();
    }

    @ModifyArg(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"),
            require = 0)
    public Screen openSleepingChatScreen(@Nullable Screen screen) {
        if (screen instanceof net.minecraft.client.gui.screen.SleepingChatScreen) {
            return new AdvancedSleepingChatScreen();
        }
        return screen;
    }
}
