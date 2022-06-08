# BitsVanilla [![Build Status](https://jenkins.bits.team/buildStatus/icon?job=Bits%2FBitsVanilla%2Fmaster)](https://jenkins.bits.team/job/Bits/job/BitsVanilla/job/master/)

A fabric reimplementation of the Bits Vanilla plugin

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

To be used in conjunction with:

- [Spark](https://spark.lucko.me/download)
- [Lithium](https://modrinth.com/mod/lithium)
- [Ferrite Core](https://modrinth.com/mod/ferrite-core)
- [Ledger](https://modrinth.com/mod/ledger)
- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)

Credits:
Vanilla Tweaks by Xisumavoid: https://www.xisumavoid.com/vanillatweaks - For mob head data