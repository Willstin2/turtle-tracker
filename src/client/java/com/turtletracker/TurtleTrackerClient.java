package com.turtletracker;

import com.turtletracker.render.TurtleHighlightRenderer;
import com.turtletracker.render.TurtleUIOverlay;
import com.turtletracker.tracker.TurtleTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

/**
 * Client-side initialization for the Turtle Tracker mod
 * This class sets up all the client-only functionality including:
 * - Turtle detection and tracking
 * - Rendering highlights and lines
 * - UI overlay display
 */
public class TurtleTrackerClient implements ClientModInitializer {
    
    // Instance of our turtle tracker that handles entity detection
    private static TurtleTracker turtleTracker;
    
    // Instance of our UI overlay renderer
    private static TurtleUIOverlay uiOverlay;
    
    // Instance of our highlight renderer for drawing lines and highlights
    private static TurtleHighlightRenderer highlightRenderer;

    /**
     * Client-side initialization method
     * This is called only on the client side when the game starts up
     */
    @Override
    public void onInitializeClient() {
        TurtleTrackerMod.LOGGER.info("Initializing Turtle Tracker client-side features...");
        
        // Initialize our main components
        turtleTracker = new TurtleTracker();
        uiOverlay = new TurtleUIOverlay();
        highlightRenderer = new TurtleHighlightRenderer();
        
        // Register event handlers
        registerEventHandlers();
        
        TurtleTrackerMod.LOGGER.info("Turtle Tracker client-side initialization complete!");
    }
    
    /**
     * Register all the event handlers needed for our mod to function
     * This includes tick events for updating turtle positions and render events
     */
    @SuppressWarnings("deprecation")
    private void registerEventHandlers() {
        // Register a client tick event to continuously update turtle tracking
        // This runs every game tick (20 times per second) on the client
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Only update if we're in a world and the game isn't paused
            if (client.level != null && client.player != null && !client.isPaused()) {
                turtleTracker.updateTurtleTracking(client);
            }
        });
        
        // Register the HUD render callback to draw our UI overlay
        // This renders the turtle count on the screen
        // Note: HudRenderCallback is deprecated in favor of HudLayerRegistrationCallback
        // but we'll use it for simplicity in this version
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            uiOverlay.render(guiGraphics, turtleTracker.getTurtleCount());
        });
        
        // Register world render events for drawing highlights and lines to turtles
        // AFTER_ENTITIES ensures we render on top of entities but before UI elements
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            highlightRenderer.render(context, turtleTracker.getVisibleTurtles(), 
                                   turtleTracker.getAllTurtles());
        });
    }
    
    /**
     * Getter for the turtle tracker instance
     * @return The turtle tracker instance
     */
    public static TurtleTracker getTurtleTracker() {
        return turtleTracker;
    }
    
    /**
     * Getter for the UI overlay instance
     * @return The UI overlay instance
     */
    public static TurtleUIOverlay getUIOverlay() {
        return uiOverlay;
    }
    
    /**
     * Getter for the highlight renderer instance
     * @return The highlight renderer instance
     */
    public static TurtleHighlightRenderer getHighlightRenderer() {
        return highlightRenderer;
    }
}