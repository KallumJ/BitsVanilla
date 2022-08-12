
# Bits Vanilla

A mod to add Bits and QoL related functionality for the Bits Minecraft servers.




## Features

- /beam [player] - Requests to teleport to the provided player
- /bed - Teleports the player back to the bed they last slept in
- /rtp - Teleports the player to a random location on the map
- /warp [warp] - Teleports the player to the specified warp. Only used for community areas, such as Spawn, Nether Hub, etc.
- /nick [nickname] - Gives you the specified nickname
- /challenges - Lots of challenges for cool rewards!
- /freecam - Enters the player into freecam.
- Dragons have a 30% chance to drop an Elytra. No more endless end city searching!
- Mobs can be silenced by naming then "Silence Me".
- Item frames can be made invisible by splashing it with an invisibility potion.
- Leaves decay instantly!
- Mobs have a rare chance to drop their heads.


## Running Locally


In order to use this plugin, the `server.properties` file of your server must include a `server-name` property.

Additionally, the `resources/database.properties` should contain credentials pointing to a valid database in the following format.
```
db.driver_class=
db.address=
db.port=
db.username=
db.password=
db.name=
```

## Acknowledgements

- [Vanilla Tweaks](https://vanillatweaks.net/) - for mob head data

