# Paper 26.3 / Floodgate mobile interface

## Setup

- Use Paper 26.3 with Java 25 and a Geyser version supporting its Java protocol.
- Install Floodgate on the **Paper backend**, including when Geyser is on Velocity/BungeeCord.
- On a proxy, set `send-floodgate-data: true` in Floodgate and share the same `key.pem` with the backend.
- Use Floodgate authentication in Geyser. Detection uses `isFloodgatePlayer(UUID)`; name prefixes are never guessed.
- Floodgate and Cumulus are provided dependencies; the plugin does not shade a second copy.
- Without Floodgate, Java inventory menus and native Paper text dialogs continue to work.

## Touch interface

The compass, a normal tap on an existing waystone, and `/warpmenu` open the complete destination
list for Floodgate players. Destinations appear first with built-in Bedrock texture icons.
There are no filters, search controls, page numbers, previous/next buttons or extra home screen
in this travel list. Scroll normally and tap a destination once to teleport. Any configured
travel cost is shown on its button; existing access, world, cooldown and payment checks still apply.
Known locked or other-world destinations are not silently hidden by a usable-only filter.

The list includes discovered, global and owned waystones, deduplicated by identity. Equal names
remain separate, with the owner shown to disambiguate them. Discovering a waystone by tapping
it happens before the list is displayed. Compass taps on a lodestone work on Bedrock too.
Server configuration still controls whether compass/waystone menus are enabled; private
waystone protections and explicit sneaking interactions are preserved.

Advanced functionality remains available through `/warpmenu advanced`, which opens the tools
home screen. Its Search and filter waystones button retains the full filter/search/options UI.
`/warpgroups` and existing creation/administration commands remain available. Every existing
menu still has a form path; no anvil or Java dialog is sent to a detected Floodgate player.
Java inventory presentation is unchanged. No custom resource pack is required for the icons;
unmapped Java material/model icons use the native lodestone fallback.

| Function | Bedrock interface |
| --- | --- |
| Browse / teleport | Complete scrollable list with icons; tap once to travel |
| Warp search | Advanced tools: CustomForm input, clear-search control |
| Filters | Advanced tools only: discovered / favourites / owned; usable-only toggle |
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
| Back / pagination / empty lists | Empty-state guidance in travel; advanced screens retain navigation |

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
responses, transport failure, 127 visible icon buttons and empty lists. Travel-list tests cover
0–300 destinations, duplicate names/identities, explicit groups and native icon fallbacks. Existing tests cover pagination, search and navigation.
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
