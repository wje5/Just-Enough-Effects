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
		Minecraft.getMinecraft().getTextureManager().bindTexture(INV);
		Potion potion = effect.getPotion();
		if (potion.hasStatusIcon()) {
			int i1 = potion.getStatusIconIndex();
			Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18, 256, 256);
		}
		potion.renderInventoryEffect(effect, screen, x, y, 100.0F);
		if (potion.shouldRenderInvText(effect)) {
			String s1 = I18n.format(potion.getName());

			if (effect.getAmplifier() == 1) {
				s1 = s1 + " " + I18n.format("enchantment.level.2");
			} else if (effect.getAmplifier() == 2) {
				s1 = s1 + " " + I18n.format("enchantment.level.3");
			} else if (effect.getAmplifier() == 3) {
				s1 = s1 + " " + I18n.format("enchantment.level.4");
			}
			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			fr.drawStringWithShadow(s1, x + 28, y + 6, 0xFFFFFF);
			String s = Potion.getPotionDurationString(effect, 1.0F);
			fr.drawStringWithShadow(s, x + 28, y + 6 + 10, 0x7F7F7F);
		}
		GlStateManager.popMatrix();
	}
}
