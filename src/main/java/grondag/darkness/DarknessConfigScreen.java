package grondag.darkness;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

public class DarknessConfigScreen extends Screen {
	protected final Screen parent;

	protected Checkbox blockLightOnlyWidget;
	protected Checkbox ignoreMoonPhaseWidget;
	protected Checkbox darkOverworldWidget;
	protected Checkbox darkNetherWidget;
	protected Checkbox darkEndWidget;
	protected Checkbox darkDefaultWidget;
	protected Checkbox darkSkylessWidget;


	public DarknessConfigScreen(Screen parent) {
		super(new TranslatableComponent("config.darkness.title"));
		this.parent = parent;
	}

	@Override
	public void removed() {
		Darkness.saveConfig();
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	@Override
	protected void init() {
		int i = 27;
		blockLightOnlyWidget = new Checkbox(width / 2 - 100, i, 200, 20, new TranslatableComponent("config.darkness.label.block_light_only"), Darkness.blockLightOnly) {
			@Override
			public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (isHovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableComponent("config.darkness.help.block_light_only"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		ignoreMoonPhaseWidget = new Checkbox(width / 2 - 100, i, 200, 20, new TranslatableComponent("config.darkness.label.ignore_moon_phase"), Darkness.ignoreMoonPhase) {
			@Override
			public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (isHovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableComponent("config.darkness.help.ignore_moon_phase"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		darkOverworldWidget = new Checkbox(width / 2 - 100, i, 200, 20, new TranslatableComponent("config.darkness.label.dark_overworld"), Darkness.darkOverworld) {
			@Override
			public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (isHovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableComponent("config.darkness.help.dark_overworld"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		darkNetherWidget = new Checkbox(width / 2 - 100, i, 200, 20, new TranslatableComponent("config.darkness.label.dark_nether"), Darkness.darkNether) {
			@Override
			public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (isHovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableComponent("config.darkness.help.dark_nether"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		darkEndWidget = new Checkbox(width / 2 - 100, i, 200, 20, new TranslatableComponent("config.darkness.label.dark_end"), Darkness.darkEnd) {
			@Override
			public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (isHovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableComponent("config.darkness.help.dark_end"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		darkDefaultWidget = new Checkbox(width / 2 - 100, i, 200, 20, new TranslatableComponent("config.darkness.label.dark_default"), Darkness.darkDefault) {
			@Override
			public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (isHovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableComponent("config.darkness.help.dark_default"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		darkSkylessWidget = new Checkbox(width / 2 - 100, i, 200, 20, new TranslatableComponent("config.darkness.label.dark_skyless"), Darkness.darkSkyless) {
			@Override
			public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (isHovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableComponent("config.darkness.help.dark_skyless"), mouseX, mouseY);
				}
			}
		};

		i +=  27;


		addRenderableWidget(blockLightOnlyWidget);
		addRenderableWidget(ignoreMoonPhaseWidget);
		addRenderableWidget(darkOverworldWidget);
		addRenderableWidget(darkNetherWidget);
		addRenderableWidget(darkEndWidget);
		addRenderableWidget(darkDefaultWidget);
		addRenderableWidget(darkSkylessWidget);

		addRenderableWidget(new Button(width / 2 - 100, height - 27, 200, 20, CommonComponents.GUI_DONE, (buttonWidget) -> {
			Darkness.blockLightOnly = blockLightOnlyWidget.selected();
			Darkness.ignoreMoonPhase = ignoreMoonPhaseWidget.selected();
			Darkness.darkOverworld = darkOverworldWidget.selected();
			Darkness.darkNether = darkNetherWidget.selected();
			Darkness.darkEnd = darkEndWidget.selected();
			Darkness.darkDefault = darkDefaultWidget.selected();
			Darkness.darkSkyless = darkSkylessWidget.selected();
			Darkness.saveConfig();
			minecraft.setScreen(parent);
		}));
	}

	@Override
	public void render(PoseStack matrixStack, int i, int j, float f) {
		renderBackground(matrixStack, 0);
		drawCenteredString(matrixStack, font, title, width / 2, 5, 16777215);

		super.render(matrixStack, i, j, f);
	}
}
