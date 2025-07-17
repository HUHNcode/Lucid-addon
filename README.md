<div align="center">
	<img src="https://github.com/user-attachments/assets/e071cc52-3c46-45d5-b561-a593f777af4f" alt="Lucid" width="300px"/>
	<h1><b>Lucid</b></h1>
	<br>
	<em>A lightweight addon for the <a href="https://github.com/MeteorDevelopment/meteor-client">Meteor Client</a>.</em>
	<br>
</div>

---

## Modules

-   **[Chat Bot](docs/ChatBot.md)**

    -   Automatically detects and responds to chat messages based on configurable settings.
    -   Extracts relevant messages using **regular expressions (regex)**.
    -   Filters messages based on **required and forbidden keywords**.
    -   Uses **predefined item triggers** to generate appropriate responses.

-   **[Chat Fonts](docs/ChatFonts.md)**

    -   Allows you to change the font style of chat messages.
    -   Choose from various font styles like Block, Blackletter, Bold, Script, etc.
    -   Each font style offers a unique and customized look for your chat messages.

-   **AFK Log**

    -   Automatically logs you out after being AFK for a configured delay.
    -   Detects inactivity based on movement.
    -   Option to disable **AutoReconnect** upon logout.

-   **Auto GG +**

    -   Automatically sends a **GG message** after a kill.
    -   Supports random or predefined messages.
    -   Option to send the message privately as **/msg**.

-   **Auto Totem +**

    -   Automatically equips a **Totem of Undying** when one is consumed.
    -   **Random delay** to bypass anti-cheat systems.

-   **Anti Item Destroy**

    -   Blocks crystal and anchor use for a short time after a player dies nearby, to prevent item destruction.
    -   **delay** changes the interaction block delay (in seconds).

-   **Better Macros**

    -   Opens the chat input screen with predefined text and cursor position via keybind.
    -   Configure macros directly in the module settings.
    -   Format: `key_name::message_with_$c_for_cursor` (e.g., `k::/say hello $c world`).
    -   `$c` marks the desired cursor position.

-   **Multi Command**

    -   Allows executing multiple commands sequentially.
    -   Use a delimiter (default: `&&`) to separate commands (e.g., `/home && /kit daily`).

-   **Stronghold Finder**

    -   Assists in locating strongholds by triangulating positions.
    -   Calculates stronghold location based on two eye of ender throws (position and angle).
    -   Displays the estimated coordinates in chat.

-   **Packet Logger**

    -   Logs network packets for debugging or analysis.
    -   Option to log incoming (S2C), outgoing (C2S), or both types of packets.
    -   Can filter packets by name.

-   **Kill Tracker**

    -   Notifies you in chat when a player you recently hit dies.
    -   A death is considered your kill if it occurs within a configurable time window after your last hit.
    -   Provides the kill detection foundation for other modules like **Auto GG**.

---

## Installation Guide

1. Install [Minecraft](https://www.minecraft.net)
2. Install [Fabric](https://fabricmc.net) and [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) for your Minecraft version
3. Download [Meteor Client](https://meteorclient.com) for your Minecraft version
4. Download [Lucid](https://github.com/HUHNcode/Lucid/releases) for your Minecraft version
5. Place both the **Meteor Client** and **Lucid** in your `mods` folder

---

## Contributing

I am always trying to offer the best possible experience.
Feel free to open issues or submit pull requests if you have suggestions or improvements!

---

## License

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.
