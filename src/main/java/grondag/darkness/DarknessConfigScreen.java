package grondag.darkness;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class DarknessConfigScreen extends Screen {
	protected final Screen parent;

	protected CheckboxWidget blockLightOnlyWidget;
	protected CheckboxWidget ignoreMoonPhaseWidget;
	protected CheckboxWidget darkOverworldWidget;
	protected CheckboxWidget darkNetherWidget;
	protected CheckboxWidget darkEndWidget;
	protected CheckboxWidget darkDefaultWidget;
	protected CheckboxWidget darkSkylessWidget;


	public DarknessConfigScreen(Screen parent) {
		super(new TranslatableText("config.darkness.title"));
		this.parent = parent;
	}

	@Override
	public void removed() {
		Darkness.saveConfig();
	}

	@Override
	public void onClose() {
		client.openScreen(parent);
	}

	@Override
	protected void init() {
		int i = 27;
		blockLightOnlyWidget = new CheckboxWidget(width / 2 - 100, i, 200, 20, new TranslatableText("config.darkness.label.block_light_only"), Darkness.blockLightOnly) {
			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (hovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableText("config.darkness.help.block_light_only"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		ignoreMoonPhaseWidget = new CheckboxWidget(width / 2 - 100, i, 200, 20, new TranslatableText("config.darkness.label.ignore_moon_phase"), Darkness.ignoreMoonPhase) {
			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (hovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableText("config.darkness.help.ignore_moon_phase"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		darkOverworldWidget = new CheckboxWidget(width / 2 - 100, i, 200, 20, new TranslatableText("config.darkness.label.dark_overworld"), Darkness.darkOverworld) {
			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (hovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableText("config.darkness.help.dark_overworld"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		darkNetherWidget = new CheckboxWidget(width / 2 - 100, i, 200, 20, new TranslatableText("config.darkness.label.dark_nether"), Darkness.darkNether) {
			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (hovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableText("config.darkness.help.dark_nether"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		darkEndWidget = new CheckboxWidget(width / 2 - 100, i, 200, 20, new TranslatableText("config.darkness.label.dark_end"), Darkness.darkEnd) {
			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (hovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableText("config.darkness.help.dark_end"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		darkDefaultWidget = new CheckboxWidget(width / 2 - 100, i, 200, 20, new TranslatableText("config.darkness.label.dark_default"), Darkness.darkDefault) {
			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (hovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableText("config.darkness.help.dark_default"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		darkSkylessWidget = new CheckboxWidget(width / 2 - 100, i, 200, 20, new TranslatableText("config.darkness.label.dark_skyless"), Darkness.darkSkyless) {
			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				super.renderButton(matrices, mouseX, mouseY, delta);

				if (hovered) {
					DarknessConfigScreen.this.renderTooltip(matrices,  new TranslatableText("config.darkness.help.dark_skyless"), mouseX, mouseY);
				}
			}
		};

		i +=  27;

		addButton(blockLightOnlyWidget);
		addButton(ignoreMoonPhaseWidget);
		addButton(darkOverworldWidget);
		addButton(darkNetherWidget);
		addButton(darkEndWidget);
		addButton(darkDefaultWidget);
		addButton(darkSkylessWidget);

		addButton(new ButtonWidget(width / 2 - 100, height - 27, 200, 20, ScreenTexts.DONE, (buttonWidget) -> {
			Darkness.blockLightOnly = blockLightOnlyWidget.isChecked();
			Darkness.ignoreMoonPhase = ignoreMoonPhaseWidget.isChecked();
			Darkness.darkOverworld = darkOverworldWidget.isChecked();
			Darkness.darkNether = darkNetherWidget.isChecked();
			Darkness.darkEnd = darkEndWidget.isChecked();
			Darkness.darkDefault = darkDefaultWidget.isChecked();
			Darkness.darkSkyless = darkSkylessWidget.isChecked();
			Darkness.saveConfig();
			client.openScreen(parent);
		}));
	}

	@Override
	public void render(MatrixStack matrixStack, int i, int j, float f) {
		renderBackground(matrixStack, 0);
		drawCenteredText(matrixStack, textRenderer, title, width / 2, 5, 16777215);

		super.render(matrixStack, i, j, f);
	}
}
