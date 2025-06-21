# TO DO

## Done

-   [x] Chat Fonts: Enable fonts in messages
-   [x] Module: AFK Kick
-   [x] Auto Totem + (Random Delay to Prevent Anti-Cheat)
-   [x] Chat Bot: Add Config File for Triggers + Outputs
-   [x] enderport locator (per enderpearls)
-   [x] doubble commands (use "&&" do do mulitible comands: "/kit && /rtp")
    -   [ ] add meteor commands
-   [x] No Item Destroy Module (CPvP)
    -   [ ] limet crytal speed on ping
    -   [ ] maby crystal block over item finding
-   [x] Module: Better Macros (Cursor Position)
-   [x] /info Command Module (Armor Info)

## Todo

-   [ ] Maybe Coordinates Logger Module
-   [ ] Anti Chat Spam Module (Auto Anti-Spam)
-   [ ] Slot Swapper
-   [ ] Maybe Chat Emojis
-   [ ] Maybe Starstrike Support
-   [ ] ESP Module That Shows Landing Spot of Other Players' Pearls (Prediction)
-   [ ] Hold Key Module
-   [ ] Walk Slow Module
-   [ ] McTires befor name module (https://mctiers.com/api/search_profile/{player})
-   [ ] Full block hitbox place (stairs, slabs, fence)

## To Fix

-   [x] Afk log start afk timer after first enabling the module
-   [x] Auto gg, better kill identifier (kill effect module)
-   [ ] Auto tot plus, still detectable
-   [x] Chat fonts, dose not apply if there is a space after the $f
-   [ ] multi command: multiple /msg don't work
-   [ ] multi command: delay delays main thread (game freezes)
-   [ ] multi command: add chat messages (without /)
-   [ ] multi command: make Combatible with chat fonts
-   [x] Fix kill detection on no item destroy (kill effect module)

**Auto Totem Debung Client send packeges**

```
[Meteor] Packet outgoing ClickSlot025Packet [0, 127, 13, 40 SWAP, O minecraft:air, (13=>0 minecraft:air, 45=>1 minecraft.totem_of_undying)]

(What got me banned:)
[Meteor] Packet outgoing ClickSlotC2SPacket [0, 146, 11, 0, PICKUP, 1 minecraft:totem_of_undying, {11=>0 minecraft:air}]
[Meteor] Packet outgoing ClickSlotC2SPacket [0, 146, 45, 0, PICKUP, 0 minecraft:air, {45=>1 minecraft:totem_of_undying}]
```
