/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.gui;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import fi.dy.masa.malilib.render.RenderUtils;
import io.github.darkkronicle.advancedchatcore.util.Colors;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class WidgetConfigListEntry<TYPE> extends WidgetListEntryBase<TYPE> {

    private final boolean odd;
    private final List<String> hoverLines;

    @Setter
    @Getter
    private int buttonStartX;

    public WidgetConfigListEntry(
            int x, int y, int width, int height, boolean isOdd, TYPE entry, int listIndex) {
        this(x, y, width, height, isOdd, entry, listIndex, null);
    }

    public WidgetConfigListEntry(
            int x,
            int y,
            int width,
            int height,
            boolean isOdd,
            TYPE entry,
            int listIndex,
            List<String> hoverLines) {
        super(x, y, width, height, entry, listIndex);
        this.odd = isOdd;
        this.hoverLines = hoverLines;
        this.buttonStartX = x + width;
    }

    /**
     * Get's the name to render for the entry.
     */
    public String getName() {
        return "";
    }

    public List<TextFieldWrapper<GuiTextFieldGeneric>> getTextFields() {
        return null;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, boolean selected) {
        // Draw a lighter background for the hovered and the selected entry
        if (selected || this.isMouseOver(mouseX, mouseY)) {
            RenderUtils.drawRect(
                    this.x,
                    this.y,
                    this.width,
                    this.height,
                    Colors.getInstance().getColorOrWhite("white").withAlpha(150).color());
        } else if (this.odd) {
            RenderUtils.drawRect(
                    this.x,
                    this.y,
                    this.width,
                    this.height,
                    Colors.getInstance().getColorOrWhite("white").withAlpha(70).color());
        } else {
            RenderUtils.drawRect(
                    this.x,
                    this.y,
                    this.width,
                    this.height,
                    Colors.getInstance().getColorOrWhite("white").withAlpha(50).color());
        }

        renderEntry(mouseX, mouseY, selected, drawContext);

        this.drawTextFields(mouseX, mouseY, drawContext);

        super.render(drawContext, mouseX, mouseY, selected);
    }

    /**
     * Render's in the middle of the rendering cycle. After the background, but before it goes to
     * super.
     */
    public void renderEntry(int mouseX, int mouseY, boolean selected, DrawContext drawContext) {
        String name = getName();
        this.drawString(drawContext,
                this.x + 4,
                this.y + 7,
                Colors.getInstance().getColorOrWhite("white").color(),
                name);
    }

    @Override
    public void postRenderHovered(DrawContext drawContext, int mouseX, int mouseY, boolean selected) {
        super.postRenderHovered(drawContext, mouseX, mouseY, selected);

        if (hoverLines == null) {
            return;
        }

        if (mouseX >= this.x
                && mouseX < this.buttonStartX
                && mouseY >= this.y
                && mouseY <= this.y + this.height) {
            RenderUtils.drawHoverText(drawContext, mouseX, mouseY, this.hoverLines);
        }
    }

    @Override
    protected boolean onKeyTypedImpl(KeyInput keyInput) {
        if (getTextFields() == null) {
            return false;
        }
        for (TextFieldWrapper<GuiTextFieldGeneric> field : getTextFields()) {
            if (field != null && field.isFocused()) {
                return field.onKeyTyped(keyInput);
            }
        }
        return false;
    }

    @Override
    protected boolean onCharTypedImpl(CharInput charInput) {
        if (getTextFields() != null) {
            for (TextFieldWrapper<GuiTextFieldGeneric> field : getTextFields()) {
                if (field != null && field.onCharTyped(charInput)) {
                    return true;
                }
            }
        }

        return super.onCharTypedImpl(charInput);
    }

    @Override
    protected boolean onMouseClickedImpl(Click click, boolean propagated) {
        if (super.onMouseClickedImpl(click, propagated)) {
            return true;
        }

        boolean ret = false;
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();

        if (getTextFields() != null) {
            for (TextFieldWrapper<GuiTextFieldGeneric> field : getTextFields()) {
                if (field != null) {
                    ret = field.getTextField().mouseClicked(click, propagated);
                }
            }
        }

        if (!this.subWidgets.isEmpty()) {
            for (WidgetBase widget : this.subWidgets) {
                ret |=
                        widget.isMouseOver(mouseX, mouseY)
                                && widget.onMouseClicked(click, propagated);
            }
        }

        return ret;
    }

    protected void drawTextFields(int mouseX, int mouseY, DrawContext drawContext) {
        if (getTextFields() == null) {
            return;
        }
        for (TextFieldWrapper<GuiTextFieldGeneric> field : getTextFields()) {
            field.getTextField().render(drawContext, mouseX, mouseY, 0f);
        }
    }
}
