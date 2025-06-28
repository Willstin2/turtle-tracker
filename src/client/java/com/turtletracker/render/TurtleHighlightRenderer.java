package com.turtletracker.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import com.turtletracker.TurtleTrackerMod;

import java.util.List;

/**
 * Minimal turtle renderer that uses only guaranteed-working effects
 * Focuses on glow + simple particle tracers
 */
public class TurtleHighlightRenderer {
    
    private static final double MAX_TRACER_DISTANCE = 48.0;
    private int tickCounter = 0;
    private int lastVisibleCount = -1;

    /**
     * Main render method called during world rendering
     * Applies basic glow and particle effects to visible turtles
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
        
        // Increment tick counter
        tickCounter++;
        
        // Apply basic visual effects to make turtles stand out
        applyBasicEffects(visibleTurtles, client);
        
        // Create tracer lines every few ticks
        if (tickCounter % 4 == 0) { // Every 4 ticks = 5 times per second
            createTracerLines(visibleTurtles, client);
        }
        
        // Log visible turtle information (only when count changes)
        if (visibleTurtles.size() != lastVisibleCount) {
            lastVisibleCount = visibleTurtles.size();
            TurtleTrackerMod.LOGGER.info("Highlighting {} visible turtles", visibleTurtles.size());
        }
    }
    
    /**
     * Apply basic visual effects using only safe methods
     */
    private void applyBasicEffects(List<Turtle> visibleTurtles, Minecraft client) {
        for (Turtle turtle : visibleTurtles) {
            try {
                // Apply glow effect
                turtle.setGlowingTag(true);
                
                // Add particle effects around the turtle every 8 ticks
                if (tickCounter % 8 == 0) {
                    Vec3 turtlePos = turtle.position();
                    
                    // Create a simple particle effect at turtle location
                    client.level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        turtlePos.x, 
                        turtlePos.y + turtle.getBbHeight() + 0.3, 
                        turtlePos.z,
                        0.0, 0.1, 0.0);
                    
                    // Add particles in a cross pattern around the turtle
                    double offset = 1.2;
                    client.level.addParticle(ParticleTypes.END_ROD,
                        turtlePos.x + offset, turtlePos.y + 0.5, turtlePos.z, 0.0, 0.0, 0.0);
                    client.level.addParticle(ParticleTypes.END_ROD,
                        turtlePos.x - offset, turtlePos.y + 0.5, turtlePos.z, 0.0, 0.0, 0.0);
                    client.level.addParticle(ParticleTypes.END_ROD,
                        turtlePos.x, turtlePos.y + 0.5, turtlePos.z + offset, 0.0, 0.0, 0.0);
                    client.level.addParticle(ParticleTypes.END_ROD,
                        turtlePos.x, turtlePos.y + 0.5, turtlePos.z - offset, 0.0, 0.0, 0.0);
                }
                
            } catch (Exception e) {
                TurtleTrackerMod.LOGGER.debug("Could not apply effect to turtle: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Create tracer lines using particles
     */
    private void createTracerLines(List<Turtle> visibleTurtles, Minecraft client) {
        // Get player eye position (where tracers start from)
        Vec3 playerPos = client.player.position().add(0, client.player.getEyeHeight(), 0);
        
        for (Turtle turtle : visibleTurtles) {
            Vec3 turtlePos = turtle.position().add(0, turtle.getBbHeight() / 2, 0);
            double distance = playerPos.distanceTo(turtlePos);
            
            if (distance <= MAX_TRACER_DISTANCE && distance > 3.0) {
                createParticleTracer(client, playerPos, turtlePos);
            }
        }
    }
    
    /**
     * Create a simple tracer line with particles
     */
    private void createParticleTracer(Minecraft client, Vec3 start, Vec3 end) {
        // Calculate the vector from start to end
        Vec3 direction = end.subtract(start);
        
        // Create 4 particles along the line for a visible tracer
        for (int i = 1; i <= 4; i++) {
            double progress = (double) i / 5.0; // 1/5, 2/5, 3/5, 4/5 of the way
            Vec3 particlePos = start.add(direction.scale(progress));
            
            // Use bright particles for the tracer line
            client.level.addParticle(ParticleTypes.END_ROD,
                particlePos.x, particlePos.y, particlePos.z,
                0.0, 0.0, 0.0);
        }
    }
}