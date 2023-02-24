# Modification Updater

A simple standalone Kotlin/JVM application to generate or update Ignition project resource signatures.

## Usage

First - for now, you'll have to download the .jar file and run it manually. Future streamlining TBD.
Once you have the self-contained `modification-updater.jar` (and a Java 17 runtime), simply invoke as a jar:
`java -jar modification-updater.jar ARGS`

Where `ARGS` is any of the following:

- Any number of Ignition project resource _paths_. You must provide the directory containing the `resource.json` and
  other files, _not_ any of the specific file paths within.
- `-s` to output just the resource signature
- `-nr, --no-replace` to explicitly avoid updating the resource manifest
- `-c, --console` to print the updated manifest to stdout
- `-a, --actor` to update the 'actor' for the provided resources. Defaults to the current user.
