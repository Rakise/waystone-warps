# Version and menu changes in 1.1.2

## How updates work

The plugin does not download compatibility updates at runtime. Gradle compiles against Paper's API
and bundles InventoryFramework into the plugin JAR. Changing the server version does not update
the copy of InventoryFramework inside an already built JAR.

The old build used Paper 1.21.4 and InventoryFramework 0.12.0. InventoryFramework maps all 26.x
versions to its `v26_1` adapter. Its released 0.12.1 anvil adapter still reads `Slot.slot` directly,
which matches the supplied `IllegalAccessError` on Paper 26.2 and Cardboard. Merely changing
`api-version` in plugin.yml cannot fix that access violation.

All six text-entry menus now use [Paper's dialog API](https://docs.papermc.io/paper/dev/dialogs/):
warp creation, warp renaming, warp search, player search, group creation, and group renaming.
They have Confirm and Back buttons. Failed validation preserves the entered text and shows the
error in the dialog. Callbacks are single use, check the player and active menu, and schedule game
actions on the server thread. Creating a warp replaces the creation prompt in the navigation stack.
Input is limited to 50 characters, matching the former anvil field.

InventoryFramework is pinned to 0.12.0 for the remaining inventory menus. The 1.1.1 test build's
upgrade to 0.12.1 introduced a constructor crash: its annotation scanner reads parameter zero
even for methods without parameters. A constructor regression test reproduces that exact error
with 0.12.1 and checks chest, hopper, and furnace construction with the pinned dependency.
The anvil adapter is still unused, so this pin does not restore the original anvil crash.
Command-opened warp and group menus now enter the navigation stack, so search/back navigation
also works when starting from a command.

Native dialogs raise
the plugin's minimum Paper version to 1.21.8. Cardboard requires separate in-game verification:
compiling against Paper does not establish compatibility with a Fabric Bukkit/Paper bridge.

## Inventory fixes

- Full warp pages previously applied `(1, 2)` twice: once for the grid in the chest, then again for
  a page inside that grid. That clipped entries outside the 7-by-3 page. Full and partial pages now
  use explicit local slots, and empty lists have a valid empty first page.
- Returning from another menu restores the selected page. If results shrink, the page is clamped.
  This also applies to player and group lists. Searches and filter changes reset the warp page.
- Search preserves separate warps with identical names and includes results beyond the first 21.
- Invalid, air, and non-item icon materials fall back to a lodestone. Empty custom-model-data
  components are omitted; actual saved custom icon metadata is retained.
- Browsing lists cancel top-inventory dragging and double-click collection from player inventory,
  in addition to the existing click and shift-click restrictions.

The reported 10/11-entry behavior cannot be confirmed without the affected server and data.
Regression tests cover those counts as well as empty, full, and multiple pages. No database
migration or deletion of discovered warps is needed for these changes.

## Automated checks

JDK 25 is used for builds. The default artifact targets Java 21; builds against 26.x APIs target
Java 25 because those API artifacts require it.

| Paper API | Purpose |
| --- | --- |
| 1.21.8-R0.1-SNAPSHOT | Minimum API and distributable build |
| 26.1.2.build.74-stable | Existing 26.1 line |
| 26.2.build.121-stable | Exact 26.2 API from the reported server |
| 26.3.build.135-beta | 26.3 prerelease compatibility |

Run `./gradlew build -PpaperApiVersion=<version>` for each matrix entry. CI performs the same checks.
The tests exercise actual InventoryFramework page rendering at 0, 1, 3, 7, 10, 11, 20, 21, 22, 42,
43, and 100 entries, filtered lists, restored pages, duplicate-name searches, and navigation lifecycle.
These are compilation and automated logic checks, not a claim of live-server verification.

## In-game verification before release

1. On each target server, create a waystone, submit a blank name, then an existing name, then a valid
   name. Verify the error can be corrected, creation happens once, and Back/Escape behave normally.
2. Rename a warp without changing its name, then change it. Search warps and players; create and
   rename groups. Verify permissions, validation, and return navigation.
3. Discover 10, 11, 21, 22, and 43 usable warps. Verify every icon appears exactly once, pagination
   reaches every entry, right-click opens the correct warp, and returning preserves the page.
4. Search for a name shared by different owners and for a query matching more than 21 entries.
   Clear the query, toggle favourites/owned/usable filters, and exercise an empty result list.
5. Use a persisted AIR/invalid icon and a custom resource-pack icon. The invalid icon should render
   as a lodestone; the valid custom icon should retain its appearance.
6. Try shift-clicks, number keys, dragging, and double-click collection with items in the player
   inventory. Menu items must stay in the GUI and player items must remain intact.
7. Repeat creation/search on Cardboard separately. Its Paper API bridge and client protocol support
   determine whether native dialogs function there. Do not infer this from the Paper API matrix.
