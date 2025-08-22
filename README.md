# _vuis's BlockFront mappings

A project that aims to turn BlockFront's obfuscated java names into clear and understandable ones.

## Usage

See the "Modding guide" wiki page for instructions on how to set up a BlockFront modding environment.

## Contributing

Modifying the mappings should be as easy as running the `enigma` task in the gradle project (run the command `./gradlew enigma`). If you have encounter any issues with the new build script, please open an issue.

## Notes

### Naming conventions

There are some naming conventions that are loosely followed:

- Classes that you would usually give generic names (e.g. `ModItems`) should be prefixed with BF (so `BFItems`)
- Client specific classes are in the `client` package, server specific classes are in the `server` package, and common classes are in the `common` package
  - ...except for packges at the root level, like `assets`, `cloud`, `game`, `registry`, and `util`.
- Classes that are not obfuscated in the original jar (e.g. `RustCorpsePhysics`) should **not** be changed.

### Project structure

Each branch in this repo contains the mappings for its respective BlockFront version. Versions which had patches released later follow the naming scheme `BaseVersion+X`, where `X` is the patch number.

### Intermediary / named jar

The jar file produced by `intermediaryJar` will run as normal. However, the jar file produced by the `namedJar` will not work correctly due to package-private classes being mislocated.

## License

`vbm` is licensed under the GNU General Public License, version 3. See `LICENSE.txt`.
