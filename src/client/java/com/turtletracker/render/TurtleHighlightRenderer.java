package com.turtletracker.render;

import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Handles rendering visual effects for turtle tracking using basic OpenGL calls
 * This version uses minimal APIs to avoid compatibility issues with 1.21.5 changes
 */
public class TurtleHighlightRenderer {
    
    // Rendering constants
    private static final float HIGHLIGHT_SIZE = 1.3f;  
    private static final double MAX_LINE_DISTANCE = 48.0; 

    /**
     * Main render method called during world rendering
     * Only renders effects for turtles that are visible (not behind blocks)
     */
    public void render(WorldRenderContext context, List<Turtle> visibleTurtles, List<Turtle> allTurtles) {
        // Only render for visible turtles (those that pass line-of-sight check)
        if (visibleTurtles.isEmpty()) {
            return;
        }
        
        // Get camera information for rendering calculations
        Camera camera = context.camera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = context.matrixStack();
        
        try {
            // Set up basic OpenGL state
            setupOpenGLState();
            
            // Render effects for visible turtles only
            renderTurtleEffects(poseStack, visibleTurtles, cameraPos);
            
        } finally {
            // Restore OpenGL state
            restoreOpenGLState();
        }
    }
    
    /**
     * Set up basic OpenGL state using direct GL calls
     */
    private void setupOpenGLState() {
        // Save current state
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        
        // Enable blending for transparency
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        // Disable depth testing so effects render on top
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        
        // Set line width
        GL11.glLineWidth(3.0f);
        
        // Disable texturing
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }
    
    /**
     * Restore OpenGL state
     */
    private void restoreOpenGLState() {
        // Restore previous state
        GL11.glPopAttrib();
    }
    
    /**
     * Render both highlight boxes and tracer lines for visible turtles
     */
    private void renderTurtleEffects(PoseStack poseStack, List<Turtle> visibleTurtles, Vec3 cameraPos) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        
        // Get player position (where lines start from)
        Vec3 playerPos = client.player.position().add(0, client.player.getEyeHeight(), 0);
        
        // Set up the pose stack
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        
        // Get the transformation matrix
        Matrix4f matrix = poseStack.last().pose();
        
        // Apply the matrix transformation
        GL11.glPushMatrix();
        GL11.glMultMatrixf(matrixToFloatArray(matrix));
        
        try {
            // Render highlight boxes around turtles
            renderHighlightBoxes(visibleTurtles);
            
            // Render tracer lines from player to turtles
            renderTracerLines(visibleTurtles, playerPos);
            
        } finally {
            GL11.glPopMatrix();
        }
        
        poseStack.popPose();
    }
    
    /**
     * Render semi-transparent highlight boxes around turtles using immediate mode
     */
    private void renderHighlightBoxes(List<Turtle> visibleTurtles) {
        // Set color for highlights (green with transparency)
        GL11.glColor4f(0.0f, 1.0f, 0.0f, 0.4f);
        
        for (Turtle turtle : visibleTurtles) {
            Vec3 turtlePos = turtle.position();
            float width = turtle.getBbWidth() * HIGHLIGHT_SIZE;
            float height = turtle.getBbHeight() * HIGHLIGHT_SIZE;
            
            // Draw a wireframe box around the turtle
            drawWireframeBox(turtlePos, width, height);
        }
    }
    
    /**
     * Render tracer lines from player to turtles
     */
    private void renderTracerLines(List<Turtle> visibleTurtles, Vec3 playerPos) {
        // Set color for lines (bright yellow)
        GL11.glColor4f(1.0f, 1.0f, 0.0f, 0.8f);
        
        GL11.glBegin(GL11.GL_LINES);
        
        for (Turtle turtle : visibleTurtles) {
            Vec3 turtlePos = turtle.position().add(0, turtle.getBbHeight() / 2, 0);
            double distance = playerPos.distanceTo(turtlePos);
            
            if (distance <= MAX_LINE_DISTANCE) {
                // Calculate alpha based on distance
                float alpha = (float) Math.max(0.4, 1.0 - (distance / MAX_LINE_DISTANCE));
                GL11.glColor4f(1.0f, 1.0f, 0.0f, alpha);
                
                // Draw line from player to turtle center
                GL11.glVertex3d(playerPos.x, playerPos.y, playerPos.z);
                GL11.glVertex3d(turtlePos.x, turtlePos.y, turtlePos.z);
            }
        }
        
        GL11.glEnd();
    }
    
    /**
     * Draw a wireframe box using immediate mode OpenGL
     */
    private void drawWireframeBox(Vec3 pos, float width, float height) {
        float halfWidth = width / 2.0f;
        float x = (float) pos.x;
        float y = (float) pos.y;
        float z = (float) pos.z;
        
        GL11.glBegin(GL11.GL_LINES);
        
        // Bottom face edges
        GL11.glVertex3f(x - halfWidth, y, z - halfWidth);
        GL11.glVertex3f(x + halfWidth, y, z - halfWidth);
        
        GL11.glVertex3f(x + halfWidth, y, z - halfWidth);
        GL11.glVertex3f(x + halfWidth, y, z + halfWidth);
        
        GL11.glVertex3f(x + halfWidth, y, z + halfWidth);
        GL11.glVertex3f(x - halfWidth, y, z + halfWidth);
        
        GL11.glVertex3f(x - halfWidth, y, z + halfWidth);
        GL11.glVertex3f(x - halfWidth, y, z - halfWidth);
        
        // Top face edges
        float topY = y + height;
        GL11.glVertex3f(x - halfWidth, topY, z - halfWidth);
        GL11.glVertex3f(x + halfWidth, topY, z - halfWidth);
        
        GL11.glVertex3f(x + halfWidth, topY, z - halfWidth);
        GL11.glVertex3f(x + halfWidth, topY, z + halfWidth);
        
        GL11.glVertex3f(x + halfWidth, topY, z + halfWidth);
        GL11.glVertex3f(x - halfWidth, topY, z + halfWidth);
        
        GL11.glVertex3f(x - halfWidth, topY, z + halfWidth);
        GL11.glVertex3f(x - halfWidth, topY, z - halfWidth);
        
        // Vertical edges
        GL11.glVertex3f(x - halfWidth, y, z - halfWidth);
        GL11.glVertex3f(x - halfWidth, topY, z - halfWidth);
        
        GL11.glVertex3f(x + halfWidth, y, z - halfWidth);
        GL11.glVertex3f(x + halfWidth, topY, z - halfWidth);
        
        GL11.glVertex3f(x + halfWidth, y, z + halfWidth);
        GL11.glVertex3f(x + halfWidth, topY, z + halfWidth);
        
        GL11.glVertex3f(x - halfWidth, y, z + halfWidth);
        GL11.glVertex3f(x - halfWidth, topY, z + halfWidth);
        
        GL11.glEnd();
    }
    
    /**
     * Convert Matrix4f to float array for OpenGL
     */
    private float[] matrixToFloatArray(Matrix4f matrix) {
        float[] result = new float[16];
        matrix.get(result);
        return result;
    }
}