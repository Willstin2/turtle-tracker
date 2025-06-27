package com.turtletracker.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Handles rendering visual effects for turtle tracking:
 * - Highlights around visible turtles
 * - Lines from the crosshair to visible turtles
 * - Different visual styles for different turtle states
 */
public class TurtleHighlightRenderer {
    
    // Color constants (RGBA format)
    private static final float[] HIGHLIGHT_COLOR = {0.0f, 1.0f, 0.0f, 0.5f}; // Green with transparency
    private static final float[] LINE_COLOR = {1.0f, 1.0f, 0.0f, 0.8f};      // Yellow, more opaque
    private static final float[] HIDDEN_TURTLE_COLOR = {1.0f, 0.0f, 0.0f, 0.3f}; // Red, low opacity
    
    // Rendering constants
    private static final float HIGHLIGHT_SIZE = 1.2f;  // Size multiplier for highlights
    private static final float LINE_WIDTH = 2.0f;      // Width of the lines
    private static final double MAX_LINE_DISTANCE = 32.0; // Max distance to draw lines

    /**
     * Main render method called during world rendering
     * 
     * @param context World render context providing camera and matrix information
     * @param visibleTurtles List of turtles that are visible to the player
     * @param allTurtles List of all turtles (including those behind walls)
     */
    public void render(WorldRenderContext context, List<Turtle> visibleTurtles, List<Turtle> allTurtles) {
        if (visibleTurtles.isEmpty() && allTurtles.isEmpty()) {
            return;
        }
        
        // Set up rendering state
        setupRenderState();
        
        // Get camera information for rendering calculations
        Camera camera = context.camera();
        Vec3 cameraPos = camera.getPosition();
        
        // Get the projection and model view matrices
        Matrix4f projectionMatrix = context.projectionMatrix();
        PoseStack poseStack = context.matrixStack();
        
        try {
            // Render highlights around visible turtles
            renderTurtleHighlights(poseStack, projectionMatrix, visibleTurtles, cameraPos);
            
            // Render lines from crosshair to visible turtles
            renderLinesToTurtles(poseStack, projectionMatrix, visibleTurtles, cameraPos);
            
            // Optionally render subtle indicators for hidden turtles
            // renderHiddenTurtleIndicators(poseStack, projectionMatrix, allTurtles, visibleTurtles, cameraPos);
            
        } finally {
            // Always restore render state when done
            restoreRenderState();
        }
    }
    
    /**
     * Set up OpenGL render state for our custom rendering
     * This enables transparency, disables depth testing where needed, etc.
     */
    private void setupRenderState() {
        // Enable blending for transparency effects
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // Disable depth testing so our highlights render on top
        RenderSystem.disableDepthTest();
        
        // Disable cull face so we can see highlights from any angle
        RenderSystem.disableCull();
        
        // Set line width for the connecting lines
        RenderSystem.lineWidth(LINE_WIDTH);
        
        // Use the position shader for our rendering
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }
    
    /**
     * Restore OpenGL render state to default values
     */
    private void restoreRenderState() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }
    
    /**
     * Render highlight boxes around visible turtles
     * 
     * @param poseStack Matrix stack for transformations
     * @param projectionMatrix Projection matrix for rendering
     * @param visibleTurtles List of visible turtles to highlight
     * @param cameraPos Camera position for offset calculations
     */
    private void renderTurtleHighlights(PoseStack poseStack, Matrix4f projectionMatrix, 
                                       List<Turtle> visibleTurtles, Vec3 cameraPos) {
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        // Begin drawing quads for the highlight boxes
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        for (Turtle turtle : visibleTurtles) {
            // Calculate turtle position relative to camera
            Vec3 turtlePos = turtle.position().subtract(cameraPos);
            
            // Get turtle's bounding box dimensions
            float width = turtle.getBbWidth() * HIGHLIGHT_SIZE;
            float height = turtle.getBbHeight() * HIGHLIGHT_SIZE;
            
            // Draw a highlight box around the turtle
            drawHighlightBox(buffer, turtlePos, width, height, HIGHLIGHT_COLOR);
        }
        
        // Finish drawing
        tesselator.end();
    }
    
    /**
     * Draw a highlight box around a specific position
     * 
     * @param buffer Vertex buffer to write to
     * @param pos Position to draw the box at
     * @param width Width of the box
     * @param height Height of the box
     * @param color Color array [r, g, b, a]
     */
    private void drawHighlightBox(BufferBuilder buffer, Vec3 pos, float width, float height, float[] color) {
        float halfWidth = width / 2.0f;
        float x = (float) pos.x;
        float y = (float) pos.y;
        float z = (float) pos.z;
        
        // Draw the highlight as a wireframe box
        // Bottom face
        addQuad(buffer, 
               x - halfWidth, y, z - halfWidth,
               x + halfWidth, y, z - halfWidth,
               x + halfWidth, y, z + halfWidth,
               x - halfWidth, y, z + halfWidth,
               color);
        
        // Top face
        addQuad(buffer,
               x - halfWidth, y + height, z - halfWidth,
               x - halfWidth, y + height, z + halfWidth,
               x + halfWidth, y + height, z + halfWidth,
               x + halfWidth, y + height, z - halfWidth,
               color);
        
        // Draw vertical edges by creating thin rectangles
        drawVerticalEdge(buffer, x - halfWidth, y, z - halfWidth, height, color);
        drawVerticalEdge(buffer, x + halfWidth, y, z - halfWidth, height, color);
        drawVerticalEdge(buffer, x + halfWidth, y, z + halfWidth, height, color);
        drawVerticalEdge(buffer, x - halfWidth, y, z + halfWidth, height, color);
    }
    
    /**
     * Add a quad (4-vertex rectangle) to the buffer
     */
    private void addQuad(BufferBuilder buffer, 
                        float x1, float y1, float z1,
                        float x2, float y2, float z2,
                        float x3, float y3, float z3,
                        float x4, float y4, float z4,
                        float[] color) {
        buffer.vertex(x1, y1, z1).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.vertex(x2, y2, z2).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.vertex(x3, y3, z3).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.vertex(x4, y4, z4).color(color[0], color[1], color[2], color[3]).endVertex();
    }
    
    /**
     * Draw a vertical edge of the highlight box
     */
    private void drawVerticalEdge(BufferBuilder buffer, float x, float y, float z, float height, float[] color) {
        float edgeWidth = 0.02f; // Very thin edge
        addQuad(buffer,
               x - edgeWidth, y, z - edgeWidth,
               x + edgeWidth, y, z - edgeWidth,
               x + edgeWidth, y + height, z - edgeWidth,
               x - edgeWidth, y + height, z - edgeWidth,
               color);
    }
    
    /**
     * Render lines from the crosshair to visible turtles
     * 
     * @param poseStack Matrix stack for transformations
     * @param projectionMatrix Projection matrix for rendering
     * @param visibleTurtles List of visible turtles to draw lines to
     * @param cameraPos Camera position for offset calculations
     */
    private void renderLinesToTurtles(PoseStack poseStack, Matrix4f projectionMatrix,
                                     List<Turtle> visibleTurtles, Vec3 cameraPos) {
        
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        
        // Get crosshair position (center of screen in world coordinates)
        Vec3 crosshairPos = getCrosshairWorldPosition(client, cameraPos);
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        // Begin drawing lines
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        
        for (Turtle turtle : visibleTurtles) {
            Vec3 turtlePos = turtle.position().subtract(cameraPos);
            double distance = turtlePos.length();
            
            // Only draw lines to turtles within reasonable distance
            if (distance <= MAX_LINE_DISTANCE) {
                // Calculate line opacity based on distance (closer = more opaque)
                float alpha = (float) Math.max(0.3, 1.0 - (distance / MAX_LINE_DISTANCE));
                float[] lineColor = {LINE_COLOR[0], LINE_COLOR[1], LINE_COLOR[2], alpha};
                
                drawLine(buffer, crosshairPos, turtlePos, lineColor);
            }
        }
        
        tesselator.end();
    }
    
    /**
     * Get the world position that corresponds to the crosshair (center of screen)
     * 
     * @param client Minecraft client instance
     * @param cameraPos Camera position offset
     * @return World position of crosshair
     */
    private Vec3 getCrosshairWorldPosition(Minecraft client, Vec3 cameraPos) {
        // For now, we'll use the camera position as the line start point
        // In a more advanced implementation, you could raycast from the camera
        // to find the exact crosshair world position
        return Vec3.ZERO; // Camera-relative position
    }
    
    /**
     * Draw a line between two points
     * 
     * @param buffer Vertex buffer to write to
     * @param start Start position of the line
     * @param end End position of the line
     * @param color Color array [r, g, b, a]
     */
    private void drawLine(BufferBuilder buffer, Vec3 start, Vec3 end, float[] color) {
        // Add start vertex
        buffer.vertex(start.x, start.y, start.z)
              .color(color[0], color[1], color[2], color[3])
              .endVertex();
        
        // Add end vertex  
        buffer.vertex(end.x, end.y, end.z)
              .color(color[0], color[1], color[2], color[3])
              .endVertex();
    }
    
    /**
     * Optional: Render subtle indicators for turtles that are hidden behind walls
     * This could show a faint outline or different colored indicator
     * 
     * @param poseStack Matrix stack for transformations
     * @param projectionMatrix Projection matrix for rendering
     * @param allTurtles All turtles in range
     * @param visibleTurtles Visible turtles (to exclude from hidden list)
     * @param cameraPos Camera position for offset calculations
     */
    private void renderHiddenTurtleIndicators(PoseStack poseStack, Matrix4f projectionMatrix,
                                            List<Turtle> allTurtles, List<Turtle> visibleTurtles,
                                            Vec3 cameraPos) {
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        for (Turtle turtle : allTurtles) {
            // Skip if this turtle is already visible
            if (visibleTurtles.contains(turtle)) {
                continue;
            }
            
            // Render a subtle indicator for hidden turtles
            Vec3 turtlePos = turtle.position().subtract(cameraPos);
            float width = turtle.getBbWidth() * 0.8f; // Slightly smaller than visible highlights
            float height = turtle.getBbHeight() * 0.8f;
            
            drawHighlightBox(buffer, turtlePos, width, height, HIDDEN_TURTLE_COLOR);
        }
        
        tesselator.end();
    }
}
