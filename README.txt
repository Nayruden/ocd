OChestDump v0.9
Copyright (c) 2011 Nayruden <http://omnivr.net>

OChestDump allows you to dump the contents of your inventory to a chest, or to
dump the contents of a chest into your inventory, or to swap the contents of a
chest with your inventory, or to sort a chest.

To use, type "/ocd swap", "/ocd loot", "/ocd stash", or "/ocd sort" in chat
while looking at a chest. Typing "/ocd" in chat will bring up the help for the
command.

Loot fits as many items from the chest into your inventory as possible.
Stash fits as many items from your inventory into the chest as possible.
Swap simply swaps the items from the chest and your inventory as best as it can.
You can add an item name or ID after swap or loot to exchange only the specified
item.
Sort allows you to sort a chest by name (default) or total amount of items.

All commands combine stacks where possible to save space!

You can enable a basic compatibility with chest protection by requiring users to
open a chest before they're allowed to use OCD on it. Enable this behavior in
the configuration file.

To build this plugin from source, you'll need OLib, found at
http://github.com/Nayruden/olib

Changelog:
v0.9 - 2/07/2011
    * [ADD] Support for durability/colors throughout OCD, including sorting.
    * [ADD] Check for Bukkit bug, warns user when the chest is bugged.
    * [FIX] A bug with compacting air due to Bukkit changes.
    * [CHANGE] Converted command implementation to new Bukkit command model.

v0.8 - 1/30/2011
    * [ADD] New sort: sort by item id.
    * [ADD] A basic compatibility with chest protection mods: you must open the
        chest to prove ownership (Enable this behavior in config).

v0.7 - 1/29/2011
    * [ADD] Sort command.
    * [ADD] All commands will now combine stacks as much as possible.
    * [FIX] Putting items in the wrong starting row with large chests.

v0.6 - 1/27/2011
    * [ADD] Ability to stash and loot only a specific item.

v0.5 - 1/25/2011
    * Initial release.

License:
Except where otherwise stated, this work is licensed under the Creative Commons
Attribution-NonCommercial-ShareAlike 3.0 Unported License. To view a copy of
this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ or send a
letter to Creative Commons, 171 Second Street, Suite 300, San Francisco,
California, 94105, USA.
