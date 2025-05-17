# _vuis's BlockFront mappings

A project that aims to turn BlockFront's obfuscated names into clear and understandable ones.

## Setting up a development environment

This process is currently pretty manual, and there will be an automated system in place soon&#x2122;.

- Download the latest **mapped** BF version and save it somewhere
- Clone [bf-intermediary](https://github.com/forteus19/bf-intermediary) for the intermediary mappings
- Clone [Tiny Remapper](https://github.com/FabricMC/tiny-remapper) and use it to create an intermediary jar with the intermediary mappings
- Clone [Enigma](https://github.com/FabricMC/Enigma) and apply the patch in this repo (`enigma.patch`)
- Open the intermediary jar in Enigma
- Open the mappings directory in Enigma as an Enigma directory

And now you're off to the races.

## Contribution notes

There are some naming conventions that are loosely followed:

- Classes that you would usually give generic names (e.g. `ModItems`) should be prefixed with BF (so `BFItems`)
- Client specific classes are in the `client` package, server specific classes are in the `server` package, and common classes are in the `common` package
  - ...except for packges at the root level, like `assets`, `cloud`, `game`, `registry`, and `util`.
- Classes that are not obfuscated in the release jar (e.g. `RustCorpsePhysics`) should **not** be changed.

## Project structure

Each branch in this repo contains the mappings for its respective BlockFront version. Verions which had patches released later follow the naming scheme `BaseVersion+X`, where `X` is the patch number.
