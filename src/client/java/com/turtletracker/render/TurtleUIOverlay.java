package com.turtletracker.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/**
 * UI Overlay class that renders the turtle count on the screen
 * Displays the number in the middle-left area of the screen with a background
 */
public class TurtleUIOverlay {
    
    // Colors for the UI elements (ARGB format)
    private static final int BACKGROUND_COLOR = 0x80000000; // Semi-transparent black
    private static final int TEXT_COLOR = 0xFFFFFFFF;       // White text
    private static final int BORDER_COLOR = 0xFF00FF00;     // Green border
    
    // Positioning and sizing constants
    private static final int X_OFFSET = 20;  // Distance from left edge of screen
    private static final int PADDING = 8;    // Padding inside the background box
    private static final int BORDER_WIDTH = 2; // Width of the border

    /**
     * Render the turtle count UI overlay
     * This method is called every frame by the HUD render callback
     * 
     * @param guiGraphics The graphics context for rendering
     * @param turtleCount The current number of tracked turtles
     */
    public void render(GuiGraphics guiGraphics, int turtleCount) {
        Minecraft client = Minecraft.getInstance();
        
        // Don't render if we're not in a world or if the debug screen is open
        if (client.level == null || client.options.renderDebug) {
            return;
        }
        
        // Get the font renderer for text rendering
        Font fontRenderer = client.font;
        
        // Create the text to display
        Component turtleText = Component.literal("🐢 Turtles: " + turtleCount);
        
        // Calculate text dimensions
        int textWidth = fontRenderer.width(turtleText);
        int textHeight = fontRenderer.lineHeight;
        
        // Calculate background box dimensions
        int boxWidth = textWidth + (PADDING * 2);
        int boxHeight = textHeight + (PADDING * 2);
        
        // Calculate positioning (middle-left of screen)
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int x = X_OFFSET;
        int y = (screenHeight / 2) - (boxHeight / 2);
        
        // Render the background box with border
        renderBackground(guiGraphics, x, y, boxWidth, boxHeight);
        
        // Render the text on top of the background
        renderText(guiGraphics, fontRenderer, turtleText, x + PADDING, y + PADDING);
    }
    
    /**
     * Render the background box and border for the UI element
     * 
     * @param guiGraphics The graphics context
     * @param x X position of the box
     * @param y Y position of the box
     * @param width Width of the box
     * @param height Height of the box
     */
    private void renderBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Draw the border (slightly larger rectangle)
        guiGraphics.fill(
            x - BORDER_WIDTH, 
            y - BORDER_WIDTH, 
            x + width + BORDER_WIDTH, 
            y + height + BORDER_WIDTH, 
            BORDER_COLOR
        );
        
        // Draw the main background (smaller rectangle on top)
        guiGraphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
    }
    
    /**
     * Render the text content
     * 
     * @param guiGraphics The graphics context
     * @param fontRenderer The font renderer
     * @param text The text component to render
     * @param x X position for the text
     * @param y Y position for the text
     */
    private void renderText(GuiGraphics guiGraphics, Font fontRenderer, Component text, int x, int y) {
        // Render the text with shadow for better visibility
        guiGraphics.drawString(fontRenderer, text, x, y, TEXT_COLOR, true);
    }
    
    /**
     * Alternative render method for when you want to show additional information
     * This could be extended to show distance to nearest turtle, etc.
     * 
     * @param guiGraphics The graphics context
     * @param turtleCount Total turtle count
     * @param visibleCount Count of visible turtles
     */
    public void renderDetailed(GuiGraphics guiGraphics, int turtleCount, int visibleCount) {
        Minecraft client = Minecraft.getInstance();
        
        if (client.level == null || client.options.renderDebug) {
            return;
        }
        
        Font fontRenderer = client.font;
        
        // Create multi-line text
        Component line1 = Component.literal("🐢 Total: " + turtleCount);
        Component line2 = Component.literal("👁 Visible: " + visibleCount);
        
        // Calculate dimensions for both lines
        int textWidth = Math.max(fontRenderer.width(line1), fontRenderer.width(line2));
        int textHeight = fontRenderer.lineHeight * 2 + 2; // 2 lines + small gap
        
        int boxWidth = textWidth + (PADDING * 2);
        int boxHeight = textHeight + (PADDING * 2);
        
        // Position calculation
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int x = X_OFFSET;
        int y = (screenHeight / 2) - (boxHeight / 2);
        
        // Render background
        renderBackground(guiGraphics, x, y, boxWidth, boxHeight);
        
        // Render both lines of text
        guiGraphics.drawString(fontRenderer, line1, x + PADDING, y + PADDING, TEXT_COLOR, true);
        guiGraphics.drawString(fontRenderer, line2, x + PADDING, y + PADDING + fontRenderer.lineHeight + 2, TEXT_COLOR, true);
    }
}
