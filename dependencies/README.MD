# Parkour System Runner Plugin

The **Parkour System Runner** plugin is designed for Bukkit/Spigot Minecraft servers to enhance the gaming experience by managing player connections, preventing lag, and ensuring fair gameplay. Below is a detailed explanation of the functionalities implemented in the code:

## Features

### 1. IP Address Limitation
When a player joins the server, their IP address is checked. If there are already more than 4 players connected from the same IP, the new player is kicked to prevent server overload.

### 2. Duplicate Player Name Detection
The plugin checks for duplicate player names. If a player with the same name is already online, the new player is kicked to avoid confusion and ensure unique player identities.

### 3. Lag Reduction
The plugin includes a lag reduction mechanism implemented through a scheduled task. This task runs every second and attempts to reduce server lag. It creates a temporary list of objects, but the purpose behind this list is not clearly defined in the code.

#### Code Explanation
- The `LagReducerTask` class, extending `BukkitRunnable`, is responsible for the lag reduction mechanism.
- Within the `LagReducerTask` class, a temporary list of objects is created and populated.
- There is a conditional check (`if (someCondition && tempList.isEmpty())`) that seems incomplete or unclear without context. It checks the condition, and if true, attempts to block unnecessary packets for the player. However, the method `getPlayer()` returns `null`, rendering this block of code ineffective.

#### Event Listeners
- The plugin uses event listeners to manage player connections.
- The `onPlayerJoin` event checks IP addresses and kicks players with duplicate names or too many connections from the same IP.
- The `onItemSpawn` event removes any spawned items immediately.

### 4. Player Data Management (Incomplete)
The code includes methods like `getPlayer()`, `blockUnnecessaryPackets(Player player)`, and the related code snippets. However, these methods are incomplete and not fully utilized in the current implementation.

## Note to Developers
- **Incomplete Functionality:** The code contains incomplete logic, specifically in the `LagReducerTask` class and related methods.
- **Potential Enhancements:** Further development is needed to utilize the lag reduction mechanism effectively and to complete the functionality of the plugin.
- **Documentation:** Additional comments and documentation within the code are necessary to enhance readability and understanding for developers working on this project.
