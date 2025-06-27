# 🐢 Turtle Tracker - Minecraft Fabric Mod

A Minecraft Fabric mod for version 1.21.5 that helps you locate and track sea turtles in your world. Perfect for turtle farms, conservation efforts, or just finding these elusive creatures!

## ✨ Features

- 🔍 **Real-time turtle detection** within a 64-block radius
- 👁️ **Visual highlights** around turtles you can see
- 📏 **Line-of-sight detection** - distinguishes between visible and hidden turtles
- 📊 **Live turtle counter** displayed on your screen
- 🎯 **Connecting lines** from crosshair to nearby visible turtles
- 🚀 **Performance optimized** - minimal impact on FPS

## 🖼️ Screenshots

### UI Overlay
The mod displays a turtle counter in the middle-left area of your screen:
```
🐢 Turtles: 3
```

### Visual Effects
- **Green wireframe boxes** around visible turtles
- **Yellow lines** connecting your crosshair to nearby turtles
- **Smart visibility detection** - only highlights turtles you can actually see

## 🛠️ Installation

### For Players
1. Download the latest release from the [Releases page]
2. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.5
3. Install [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
4. Place the mod jar file in your `mods` folder
5. Launch Minecraft with the Fabric profile

### For Developers (GitHub Codespaces)
1. Clone this repository
2. Open in GitHub Codespaces
3. Run the setup commands:
   ```bash
   chmod +x gradlew
   ./gradlew build
   ```
4. The compiled mod will be in `build/libs/`

## 🔧 Development Setup

### Prerequisites
- Java 21 or higher
- Gradle 9.0
- Minecraft 1.21.5
- Fabric Loader 0.16.9+
- Fabric API 0.110.5+

### Building from Source
```bash
# Clone the repository
git clone https://github.com/yourusername/turtle-tracker.git
cd turtle-tracker

# Make gradlew executable (Unix/Mac only)
chmod +x gradlew

# Build the mod
./gradlew build

# Run in development environment
./gradlew runClient
```

### Project Structure
```
src/
├── main/java/com/turtletracker/
│   └── TurtleTrackerMod.java           # Main mod class
├── client/java/com/turtletracker/
│   ├── TurtleTrackerClient.java        # Client initialization
│   ├── tracker/TurtleTracker.java      # Turtle detection logic
│   └── render/
│       ├── TurtleUIOverlay.java        # UI rendering
│       └── TurtleHighlightRenderer.java # 3D highlighting
└── main/resources/
    ├── fabric.mod.json                 # Mod metadata
    └── turtle_tracker.mixins.json      # Mixin config
```

## 🎮 How It Works

### Turtle Detection
The mod continuously scans for turtle entities within a 64-block radius of the player. It uses Minecraft's built-in entity querying system with bounding box intersection for optimal performance.

### Visibility Checking
For each detected turtle, the mod performs a raycast from the player's eye position to the turtle to determine if there are blocks obstructing the view. This ensures that only truly visible turtles are highlighted.

### Rendering System
The mod uses Minecraft's rendering pipeline to draw:
- **UI Overlay**: Rendered during the HUD render phase
- **3D Highlights**: Rendered during the world render phase after entities
- **Connecting Lines**: Dynamic lines that adjust opacity based on distance

### Performance Optimization
- Entity queries use efficient AABB (Axis-Aligned Bounding Box) intersection
- Render calls are batched to minimize OpenGL state changes
- Data structures are cleared and rebuilt each tick to prevent memory leaks
- Distance checks prevent unnecessary processing of far-away entities

## ⚙️ Configuration

Currently, the mod uses hardcoded values optimized for most use cases:
- **Search Radius**: 64 blocks
- **Maximum Line Distance**: 32 blocks  
- **Update Frequency**: Every client tick (20 times per second)

Future versions may include configuration options for these values.

## 🐛 Known Issues

- Lines may occasionally appear to go through blocks due to the crosshair position calculation
- Very fast-moving turtles might briefly disappear from tracking
- The mod currently only tracks sea turtles (not other turtle variants)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests.

### Development Guidelines
1. Follow the existing code style and documentation patterns
2. Test thoroughly in both single-player and multiplayer environments
3. Ensure compatibility with the latest Fabric API version
4. Add comments explaining complex algorithms or OpenGL operations

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Fabric](https://fabricmc.net/) - The modding platform that makes this possible
- [Minecraft Wiki](https://minecraft.wiki/) - For entity behavior documentation
- The Fabric community for excellent documentation and examples

## 📞 Support

If you encounter issues or have questions:
1. Check the [Issues page](https://github.com/yourusername/turtle-tracker/issues)
2. Join the discussion in [Discussions](https://github.com/yourusername/turtle-tracker/discussions)
3. For bugs, please include:
   - Minecraft version
   - Fabric Loader version
   - Fabric API version
   - Mod version
   - Steps to reproduce the issue

---

**Happy turtle tracking!** 🐢✨
