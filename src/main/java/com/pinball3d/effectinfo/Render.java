package com.pinball3d.effectinfo;

import java.lang.reflect.Field;
import java.util.Collection;

import com.google.common.collect.Ordering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class Render {
	public static ResourceLocation TEXTURE = new ResourceLocation("effectinfo:textures/gui/texture.png");
	public static ResourceLocation INV = new ResourceLocation("textures/gui/container/inventory.png");

	@SubscribeEvent
	public static void onRenderScreen(GuiScreenEvent.DrawScreenEvent.Pre event) {
		if (event.getGui() instanceof InventoryEffectRenderer) {
			InventoryEffectRenderer screen = (InventoryEffectRenderer) event.getGui();
			Collection<PotionEffect> collection = screen.mc.player.getActivePotionEffects();
			if (collection.isEmpty()) {
				return;
			}
			try {
				if (screen instanceof GuiInventory) {
					GuiInventory inv = (GuiInventory) screen;
					Field field = GuiInventory.class.getDeclaredField("recipeBookGui");
					field.setAccessible(true);
					GuiRecipeBook book = (GuiRecipeBook) field.get(inv);
					field = GuiInventory.class.getDeclaredField("widthTooNarrow");
					field.setAccessible(true);
					Boolean flag = (Boolean) field.get(inv);
					if (book.isVisible() && flag) {
						return;
					}
				} else if (!(screen instanceof GuiContainerCreative)) {
					return;
				}
				Field field = InventoryEffectRenderer.class.getDeclaredField("hasActivePotionEffects");
				field.setAccessible(true);
				Boolean flag = (Boolean) field.get(screen);
				if (!flag) {
					return;
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				return;
			}
			int x = screen.getGuiLeft() - 124;
			int y = screen.getGuiTop();
			if (event.getMouseX() - x < 0 || event.getMouseX() - x > 120) {
				return;
			}
			int l = 33;
			if (collection.size() > 5) {
				l = 132 / (collection.size() - 1);
			}
			for (PotionEffect e : Ordering.natural().sortedCopy(collection)) {
				Potion potion = e.getPotion();
				if (!potion.shouldRender(e)) {
					continue;
				}
				if (event.getMouseY() - y > 0 && event.getMouseY() - y < l) {
					render(e, x, y, screen);
					return;
				}
				y += l;
			}
		}
	}

	public static void render(PotionEffect effect, int x, int y, InventoryEffectRenderer screen) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.translate(0, 0, 100F);
		Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 120, 120, 256, 256);
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		int amp = effect.getAmplifier();
		String[] desc = new String[5];
		int max = 0;
		for (int i = 0; i < 5; i++) {
			String key = effect.getEffectName() + "." + i + ".desc";
			if (I18n.hasKey(key)) {
				desc[i] = I18n.format(key);
				max = i;
			}
		}
		int yOffset = y + 30;
		int yOffsetTemp = 0;
		for (int i = 0; i < 5; i++) {
			String s = desc[i];
			if (s != null) {
				int yPos = yOffsetTemp == 0 ? yOffset : yOffsetTemp;
				fr.drawSplitString(s, x + 30, yPos, 85, 0xFFFFFF);
				if (yPos != y + 30) {
					Gui.drawRect(x + 7, yPos - 5, x + 115, yPos - 4, 0xFF7F7F7F);
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				}
				yOffsetTemp = 0;
			} else if (yOffsetTemp == 0) {
				yOffsetTemp = yOffset;
			}
			if (i <= max) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
				Gui.drawModalRectWithCustomSizedTexture(x + 10, yOffset, 120, i * 16, 18, 16, 256, 256);
			}
			yOffset += s == null ? 20 : 25;
		}
		Potion potion = effect.getPotion();
		if (potion.hasStatusIcon()) {
			int i1 = potion.getStatusIconIndex();
			Minecraft.getMinecraft().getTextureManager().bindTexture(INV);
			Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18, 256, 256);
		}
		potion.renderInventoryEffect(effect, screen, x, y, 100.0F);
		if (potion.shouldRenderInvText(effect)) {
			String s1 = I18n.format(potion.getName());
			if (amp == 1) {
				s1 = s1 + " " + I18n.format("enchantment.level.2");
			} else if (amp == 2) {
				s1 = s1 + " " + I18n.format("enchantment.level.3");
			} else if (amp == 3) {
				s1 = s1 + " " + I18n.format("enchantment.level.4");
			}
			fr.drawStringWithShadow(s1, x + 28, y + 6, 0xFFFFFF);
			String s = Potion.getPotionDurationString(effect, 1.0F);
			fr.drawStringWithShadow(s, x + 28, y + 6 + 10, 0x7F7F7F);
		}
//		String s = I18n.format("enchantment.level.1");
//		fr.drawStringWithShadow(s, x + 15 - fr.getStringWidth(s) / 2, y + 20, 0xFFFFFF);
//		s = I18n.format("enchantment.level.2");
//		fr.drawStringWithShadow(s, x + 15 - fr.getStringWidth(s) / 2, y + 27, 0xFFFFFF);
//		s = I18n.format("enchantment.level.3");
//		fr.drawStringWithShadow(s, x + 15 - fr.getStringWidth(s) / 2, y + 34, 0xFFFFFF);
//		s = I18n.format("enchantment.level.4");
//		fr.drawStringWithShadow(s, x + 15 - fr.getStringWidth(s) / 2, y + 41, 0xFFFFFF);
		GlStateManager.popMatrix();
	}
}
