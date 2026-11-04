# Paper 26.3 / Floodgate mobile interface

## Setup

- Use Paper 26.3 with Java 25 and a Geyser version supporting its Java protocol.
- Install Floodgate on the **Paper backend**, including when Geyser is on Velocity/BungeeCord.
- On a proxy, set `send-floodgate-data: true` in Floodgate and share the same `key.pem` with the backend.
- Use Floodgate authentication in Geyser. Detection uses `isFloodgatePlayer(UUID)`; name prefixes are never guessed.
- Floodgate and Cumulus are provided dependencies; the plugin does not shade a second copy.
- Without Floodgate, Java inventory menus and native Paper text dialogs continue to work.

## Touch interface

`/warpmenu` opens the mobile home menu for Floodgate players. Compass and waystone interactions
route to the same mobile renderer. Every existing menu has a form path; no anvil or Java dialog
is sent to a detected Floodgate player.

| Function | Bedrock interface |
| --- | --- |
| Browse / teleport | Paged list, warp details, Teleport button |
| Warp search | CustomForm input, clear-search control |
| Filters | Discovered / favourites / owned; usable-only toggle |
| Options | Explicit Warp options button instead of right click |
| Favourite / forget / compass | Options form, confirmation for forgetting |
| Creation / rename | Input form with server validation and editable errors |
| Access modes | Existing public / private / global action and permissions |
| Whitelist / discovered access | Player list and search; toggle whitelist / revoke buttons |
| Icons | Inventory item selector and confirmation; all existing icon metadata copied |
| Skins | Existing preview plus inventory block selector in Structure, skin and removal |
| Relocation | Form gives the existing placement tool and instructions |
| Removal | Nearby confirmed normal break steps, preserving protection events and drops |
| Groups | Browse, create, rename, delete with confirmation, assign / unassign |
| Invalid worlds | Paged worlds list and confirmed per-world / all removal with command permissions |
| Back / pagination / empty lists | Touch controls and preserved page state |

Form callbacks run once on the server thread. Reopening a menu, navigating away or opening a new
session invalidates earlier responses. Offline players and changed effective permissions cannot
apply an old response. Icon selection rechecks the item and permission; skin selection rechecks
ownership, item and distance. Closing or invalid form responses never confirm destructive actions.

Skins use the same item cost as Java interaction. Relocation still requires placement in the world;
it honours the existing placement protections. Breaking requires repeated normal break steps and
respects the existing counter, ownership checks and protection listeners. Physical discovery,
construction and compass targeting remain normal Minecraft interactions on both editions.

The new mobile labels include Portuguese (Brazil) and English fallback. Existing plugin labels
retain their configured language. No resource pack is required for forms; custom Java model visuals
still depend on the server's Bedrock resource-pack mappings.

## Automated verification

Only `.github/workflows/ci-release.yml` compiles or runs the server. It builds/tests with Java 25
against pinned Paper API `26.3.build.230-stable`, then starts real Paper both without Floodgate and
with Floodgate/Geyser. Form response tests cover selection, Unicode input, cancellation, malformed
responses and transport failure. Existing tests cover pagination, search and navigation.
Releases require every job to succeed, and include the tested JAR and checksum.

Startup and response tests do not simulate a logged-in Bedrock client. In-game acceptance should
exercise the table above on Android/iOS and Java, with private/global permissions, economy,
world groups, linked accounts, inventory-full states, disconnects and simultaneous menu sessions.

## Official references

- [Floodgate API and backend requirements](https://geysermc.org/wiki/floodgate/api/)
- [Floodgate dependency setup](https://geysermc.org/wiki/geyser/getting-started-with-the-api/)
- [Current forms and Cumulus guide](https://geysermc.org/wiki/geyser/forms/)
- [Cumulus source](https://github.com/GeyserMC/Cumulus)
- [Legacy Cumulus wiki (outdated warning)](https://github.com/GeyserMC/Cumulus/wiki)
- [Paper project setup / Java 25](https://docs.papermc.io/paper/dev/project-setup/)

This document supersedes the historical version matrix in menu-compatibility.md for this fork.
