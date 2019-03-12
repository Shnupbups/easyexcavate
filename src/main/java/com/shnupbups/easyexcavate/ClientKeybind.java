package com.shnupbups.easyexcavate;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class ClientKeybind implements ClientModInitializer {
	public static FabricKeyBinding keybind;

	@Override
	public void onInitializeClient() {
		KeyBindingRegistry.INSTANCE.addCategory("easyexcavate.category");
		keybind = FabricKeyBinding.Builder.create(
					new Identifier("easyexcavate:excavate"),
					InputUtil.Type.KEY_KEYBOARD,
					96,
					"easyexcavate.category"
				).build();
		KeyBindingRegistry.INSTANCE.register(keybind);
	}
}
