package com.turtletracker;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class for Turtle Tracker mod
 * This class handles the server-side initialization of the mod
 */
public class TurtleTrackerMod implements ModInitializer {
    // Define the mod ID as a constant for consistency
    public static final String MOD_ID = "turtle_tracker";
    
    // Create a logger for this mod using SLF4J (Simple Logging Facade for Java)
    // This allows us to output debug information and errors to the console
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * This method is called when the mod is initialized
     * It runs on both client and server sides
     * We use this to set up any shared functionality
     */
    @Override
    public void onInitialize() {
        // Log that our mod has been initialized
        LOGGER.info("Turtle Tracker mod initialized!");
        
        // Since our turtle tracking functionality is primarily client-side,
        // we don't need to register much here. The main logic will be in
        // the client initializer class.
        
        // If we needed to register items, blocks, or server-side functionality,
        // we would do it here. For now, this serves as the entry point
        // and logging confirmation that the mod loaded correctly.
    }
}
