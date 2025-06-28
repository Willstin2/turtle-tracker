package com.turtletracker.render;

import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import com.turtletracker.TurtleTrackerMod;

import java.util.List;

/**
 * Working turtle renderer for Minecraft 1.21.5 using the new RenderLayer system
 * Creates highlighted boxes and tracer lines for visible turtles
 */
public class TurtleHighlightRenderer {
    
    private static final double MAX_TRACER_DISTANCE = 48.0;
    private static final float HIGHLIGHT_EXPANSION = 0.3f; // How much to expand the highlight box
    private int lastVisibleCount = -1;

    /**
     * Main render method using the new 1.21.5 rendering system
     */
    public void render(WorldRenderContext context, List<Turtle> visibleTurtles, List<Turtle> allTurtles) {
        if (visibleTurtles.isEmpty()) {
            if (lastVisibleCount > 0) {
                lastVisibleCount = 0;
                TurtleTrackerMod.LOGGER.debug("No visible turtles");
            }
            return;
        }
        
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        
        Camera camera = context.camera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = context.matrixStack();
        
        // Get the buffer source from context (new 1.21.5 way)
        MultiBufferSource.BufferSource bufferSource = client.renderBuffers().bufferSource();
        
        try {
            // Set up matrix transformations
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            
            // Render highlight boxes around visible turtles
            renderTurtleHighlights(poseStack, bufferSource, visibleTurtles);
            
            // Render tracer lines from player to visible turtles
            renderTracerLines(poseStack, bufferSource, visibleTurtles, client);
            
            // Finish all rendering
            bufferSource.endBatch();
            
        } catch (Exception e) {
            TurtleTrackerMod.LOGGER.warn("Error rendering turtle effects: {}", e.getMessage());
        } finally {
            poseStack.popPose();
        }
        
        // Log count changes
        if (visibleTurtles.size() != lastVisibleCount) {
            lastVisibleCount = visibleTurtles.size();
            TurtleTrackerMod.LOGGER.info("Rendering {} visible turtles with highlights and lines", 
                                       visibleTurtles.size());
        }
    }
    
    /**
     * Render highlight boxes using Minecraft's built-in line rendering
     */
    private void renderTurtleHighlights(PoseStack poseStack, MultiBufferSource bufferSource, List<Turtle> visibleTurtles) {
        // Use Minecraft's built-in LINES render type
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();
        
        for (Turtle turtle : visibleTurtles) {
            // Get turtle's bounding box and expand it slightly
            AABB boundingBox = turtle.getBoundingBox();
            AABB expandedBox = boundingBox.inflate(HIGHLIGHT_EXPANSION);
            
            // Convert to camera-relative coordinates (already handled by matrix translation)
            drawHighlightBox(buffer, matrix, expandedBox, 0.0f, 1.0f, 0.0f, 0.8f); // Green color
        }
    }
    
    /**
     * Draw a wireframe box using the new vertex system
     */
    private void drawHighlightBox(VertexConsumer buffer, Matrix4f matrix, AABB box, float r, float g, float b, float a) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;
        
        // Bottom face edges (4 lines)
        addLine(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        addLine(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        addLine(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        addLine(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);
        
        // Top face edges (4 lines)
        addLine(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        addLine(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
        
        // Vertical edges (4 lines)
        addLine(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        addLine(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }
    
    /**
     * Add a line to the vertex buffer using the new 1.21.5 format
     */
    private void addLine(VertexConsumer buffer, Matrix4f matrix, 
                        float x1, float y1, float z1, float x2, float y2, float z2, 
                        float r, float g, float b, float a) {
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(1.0f, 0.0f, 0.0f);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(1.0f, 0.0f, 0.0f);
    }
    
    /**
     * Render tracer lines from crosshair to visible turtles
     */
    private void renderTracerLines(PoseStack poseStack, MultiBufferSource bufferSource, List<Turtle> visibleTurtles, Minecraft client) {
        // Use Minecraft's built-in LINES render type
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();
        
        // Get crosshair position in world coordinates
        Vec3 crosshairPos = getCrosshairWorldPosition(client);
        
        for (Turtle turtle : visibleTurtles) {
            Vec3 turtlePos = turtle.position().add(0, turtle.getBbHeight() / 2, 0);
            double distance = crosshairPos.distanceTo(turtlePos);
            
            if (distance <= MAX_TRACER_DISTANCE && distance > 2.0) { // Don't draw for very close turtles
                // Calculate alpha based on distance (closer = more opaque)
                float alpha = (float) Math.max(0.4, 1.0 - (distance / MAX_TRACER_DISTANCE));
                
                // Draw yellow tracer line from crosshair to turtle
                addLine(buffer, matrix, 
                       (float)crosshairPos.x, (float)crosshairPos.y, (float)crosshairPos.z,
                       (float)turtlePos.x, (float)turtlePos.y, (float)turtlePos.z,
                       1.0f, 1.0f, 0.0f, alpha); // Yellow color
            }
        }
    }
    
    /**
     * Calculate the world position where the crosshair is pointing
     * This creates a point in front of the player in the direction they're looking
     */
    private Vec3 getCrosshairWorldPosition(Minecraft client) {
        // Get player eye position and look direction
        Vec3 eyePos = client.player.position().add(0, client.player.getEyeHeight(), 0);
        Vec3 lookDirection = client.player.getLookAngle();
        
        // Project the crosshair position a short distance in front of the player
        // This makes the lines appear to come from the crosshair area
        double projectionDistance = 1.5; // Distance in front of player
        
        return eyePos.add(lookDirection.scale(projectionDistance));
    }
}