# Modification Updater

A simple standalone Kotlin/JVM application to generate or update Ignition project resource signatures.

## Usage

First - for now, you'll have to download the .jar file and run it manually. Future streamlining TBD.
Once you have the self-contained `modification-updater.jar` (and a Java 17 runtime), simply invoke as a jar:
`java -jar modification-updater.jar`

The command line is broken into three subcommands:

- `verify [resourcePaths...]` - exits successfully if all the provided resource files have valid signatures. Raises an
  error code if any have invalid codes. `stderr` can be examined to determine the issue.
- `signatures [resourcePaths...]` - Echoes to `stdout` the calculated signature for the input resource files, one per
  line.
- `update [-a, --actor actor] [-s, --dry-run] [resourcePaths...]` - Updates the provided resource paths, in place, with
  the provided actor, automatically updating the resource signature. 
  - If the `actor` flag is not specified, it defaults
    to the OS user name that invoked the process.
  - If the dry-run flag is set, instead echoes the updated resource
    manifests to `stdout`.  
