package com.turtletracker.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import com.turtletracker.TurtleTrackerMod;

import java.util.List;

/**
 * Turtle renderer that uses particles to create tracer effects
 * This approach should be safer than custom OpenGL rendering
 */
public class TurtleHighlightRenderer {
    
    private static final double MAX_TRACER_DISTANCE = 48.0;
    private static final int PARTICLES_PER_LINE = 8; // Number of particles along each tracer line
    private int tickCounter = 0;
    private int lastVisibleCount = -1;

    /**
     * Main render method called during world rendering
     * Creates particle tracers and glow effects for visible turtles
     * 
     * @param context World render context providing camera and matrix information
     * @param visibleTurtles List of turtles that are visible to the player (LOS check passed)
     * @param allTurtles List of all turtles (including those behind walls)
     */
    public void render(WorldRenderContext context, List<Turtle> visibleTurtles, List<Turtle> allTurtles) {
        // Only render effects for visible turtles (those that pass line-of-sight check)
        if (visibleTurtles.isEmpty()) {
            if (lastVisibleCount > 0) {
                lastVisibleCount = 0;
                TurtleTrackerMod.LOGGER.debug("No visible turtles - effects cleared");
            }
            return;
        }
        
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        
        // Increment tick counter for animation timing
        tickCounter++;
        
        // Apply glow effects to visible turtles
        applyTurtleGlow(visibleTurtles);
        
        // Create particle tracers every few ticks (not every tick to avoid spam)
        if (tickCounter % 3 == 0) { // Every 3 ticks = ~6 times per second
            createParticleTracers(visibleTurtles, client);
        }
        
        // Log visible turtle information (only when count changes)
        if (visibleTurtles.size() != lastVisibleCount) {
            lastVisibleCount = visibleTurtles.size();
            TurtleTrackerMod.LOGGER.info("Tracking {} visible turtles with glow + particle tracers", 
                                       visibleTurtles.size());
        }
    }
    
    /**
     * Apply glow effects to make visible turtles stand out
     */
    private void applyTurtleGlow(List<Turtle> visibleTurtles) {
        for (Turtle turtle : visibleTurtles) {
            // Make the turtle glow using Minecraft's built-in glow effect
            if (!turtle.hasGlowingTag()) {
                turtle.setGlowingTag(true);
            }
        }
    }
    
    /**
     * Create particle tracers from player to visible turtles
     */
    private void createParticleTracers(List<Turtle> visibleTurtles, Minecraft client) {
        // Get player eye position (where tracers start from)
        Vec3 playerPos = client.player.position().add(0, client.player.getEyeHeight(), 0);
        
        for (Turtle turtle : visibleTurtles) {
            Vec3 turtlePos = turtle.position().add(0, turtle.getBbHeight() / 2, 0);
            double distance = playerPos.distanceTo(turtlePos);
            
            if (distance <= MAX_TRACER_DISTANCE && distance > 2.0) { // Don't draw for very close turtles
                createParticleLine(client, playerPos, turtlePos, distance);
            }
        }
    }
    
    /**
     * Create a line of particles between two points
     */
    private void createParticleLine(Minecraft client, Vec3 start, Vec3 end, double distance) {
        // Calculate the vector from start to end
        Vec3 direction = end.subtract(start);
        
        // Create particles along the line
        for (int i = 1; i < PARTICLES_PER_LINE; i++) { // Skip start and end points
            double progress = (double) i / PARTICLES_PER_LINE;
            Vec3 particlePos = start.add(direction.scale(progress));
            
            // Use different particle types based on distance for visual variety
            if (distance < 16.0) {
                // Close turtles: bright yellow particles
                client.level.addParticle(ParticleTypes.END_ROD,
                    particlePos.x, particlePos.y, particlePos.z,
                    0.0, 0.0, 0.0);
            } else if (distance < 32.0) {
                // Medium distance: electric spark particles
                client.level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    particlePos.x, particlePos.y, particlePos.z,
                    0.0, 0.0, 0.0);
            } else {
                // Far turtles: enchanting glyphs
                client.level.addParticle(ParticleTypes.ENCHANT,
                    particlePos.x, particlePos.y, particlePos.z,
                    0.0, 0.1, 0.0);
            }
        }
        
        // Add a special particle at the turtle location for emphasis
        client.level.addParticle(ParticleTypes.TOTEM_OF_UNDYING,
            end.x, end.y, end.z,
            0.0, 0.0, 0.0);
    }
}