package com.turtletracker.tracker;

import com.turtletracker.TurtleTrackerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main turtle tracking class that handles:
 * - Finding all turtles within a specified radius
 * - Determining which turtles are visible to the player
 * - Maintaining lists of both all turtles and visible turtles
 */
public class TurtleTracker {
    
    // Maximum distance to search for turtles (in blocks)
    private static final double SEARCH_RADIUS = 64.0;
    
    // List of all turtles within range (including those behind walls)
    private final List<Turtle> allTurtles = new ArrayList<>();
    
    // List of turtles that are visible to the player (not blocked by walls)
    private final List<Turtle> visibleTurtles = new ArrayList<>();
    
    // Set to track turtle IDs we've already processed this tick (prevents duplicates)
    private final Set<Integer> processedTurtles = new HashSet<>();

    /**
     * Main update method called every client tick
     * This scans for turtles around the player and updates our tracking lists
     * 
     * @param client The Minecraft client instance
     */
    public void updateTurtleTracking(Minecraft client) {
        // Clear previous tick's data
        clearTurtleLists();
        
        // Get the current player - if null, we can't track anything
        Player player = client.player;
        if (player == null || client.level == null) {
            return;
        }
        
        // Get player's current position for distance calculations
        Vec3 playerPosition = player.position();
        
        // Create a bounding box around the player to search for entities
        // This creates a cube extending SEARCH_RADIUS blocks in each direction
        AABB searchArea = new AABB(
            playerPosition.x - SEARCH_RADIUS, 
            playerPosition.y - SEARCH_RADIUS, 
            playerPosition.z - SEARCH_RADIUS,
            playerPosition.x + SEARCH_RADIUS, 
            playerPosition.y + SEARCH_RADIUS, 
            playerPosition.z + SEARCH_RADIUS
        );
        
        // Get all entities within the search area
        List<Entity> nearbyEntities = client.level.getEntities(player, searchArea);
        
        // Process each entity to find turtles
        for (Entity entity : nearbyEntities) {
            // Check if this entity is a turtle and we haven't processed it yet
            if (entity instanceof Turtle turtle && !processedTurtles.contains(entity.getId())) {
                processTurtle(turtle, playerPosition, client);
                processedTurtles.add(entity.getId());
            }
        }
        
        // Log turtle count for debugging (only if we found any)
        if (!allTurtles.isEmpty()) {
            TurtleTrackerMod.LOGGER.debug("Found {} turtles ({} visible)", 
                                        allTurtles.size(), visibleTurtles.size());
        }
    }
    
    /**
     * Process a single turtle to determine if it should be tracked
     * Adds it to appropriate lists and checks visibility
     * 
     * @param turtle The turtle entity to process
     * @param playerPosition The player's current position
     * @param client The Minecraft client instance
     */
    private void processTurtle(Turtle turtle, Vec3 playerPosition, Minecraft client) {
        // Calculate distance to the turtle
        double distance = turtle.position().distanceTo(playerPosition);
        
        // Only track turtles within our search radius
        if (distance <= SEARCH_RADIUS) {
            // Add to all turtles list (this includes turtles behind walls)
            allTurtles.add(turtle);
            
            // Check if the turtle is visible (not blocked by walls)
            if (isTurtleVisible(turtle, playerPosition, client)) {
                visibleTurtles.add(turtle);
            }
        }
    }
    
    /**
     * Check if a turtle is visible to the player using raycasting
     * This performs a line-of-sight check to see if there are blocks between
     * the player and the turtle
     * 
     * @param turtle The turtle to check visibility for
     * @param playerPosition The player's current position
     * @param client The Minecraft client instance
     * @return true if the turtle is visible, false if blocked by blocks
     */
    private boolean isTurtleVisible(Turtle turtle, Vec3 playerPosition, Minecraft client) {
        // Get the turtle's position
        Vec3 turtlePosition = turtle.position();
        
        // Adjust the start position to be at the player's eye level
        // This prevents false negatives when the player is standing on the ground
        Vec3 eyePosition = new Vec3(playerPosition.x, 
                                   playerPosition.y + client.player.getEyeHeight(), 
                                   playerPosition.z);
        
        // Adjust turtle position to be at the turtle's center/eye level
        Vec3 turtleEyePosition = new Vec3(turtlePosition.x, 
                                         turtlePosition.y + turtle.getEyeHeight(), 
                                         turtlePosition.z);
        
        // Perform a raycast from player's eyes to turtle's center
        // ClipContext.Block.COLLIDER means we check for collision with solid blocks
        // ClipContext.Fluid.NONE means we ignore fluids (water, lava)
        ClipContext clipContext = new ClipContext(
            eyePosition,
            turtleEyePosition,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            client.player
        );
        
        // Perform the actual raycast
        HitResult hitResult = client.level.clip(clipContext);
        
        // If the raycast didn't hit anything (HitResult.Type.MISS), 
        // then there's a clear line of sight to the turtle
        return hitResult.getType() == HitResult.Type.MISS;
    }
    
    /**
     * Clear all turtle tracking lists and reset for the next tick
     */
    private void clearTurtleLists() {
        allTurtles.clear();
        visibleTurtles.clear();
        processedTurtles.clear();
    }
    
    /**
     * Get the total count of all tracked turtles (including those behind walls)
     * @return The number of turtles within range
     */
    public int getTurtleCount() {
        return allTurtles.size();
    }
    
    /**
     * Get the list of all turtles within range
     * @return List of all tracked turtles
     */
    public List<Turtle> getAllTurtles() {
        return new ArrayList<>(allTurtles);
    }
    
    /**
     * Get the list of visible turtles (not blocked by walls)
     * @return List of visible turtles
     */
    public List<Turtle> getVisibleTurtles() {
        return new ArrayList<>(visibleTurtles);
    }
    
    /**
     * Get the current search radius
     * @return The search radius in blocks
     */
    public static double getSearchRadius() {
        return SEARCH_RADIUS;
    }
}
