# Modification Updater

A simple standalone Kotlin/JVM application to generate or update Ignition project resource signatures.

## Usage

First - for now, you'll have to download the .jar file and run it manually. Future streamlining TBD.
Once you have the self-contained `modification-updater.jar` (and a Java 17 runtime), simply invoke as a jar:
`java -jar modification-updater.jar ARGS`

Where `ARGS` is any of the following:

- Any number of Ignition project resource _paths_. You must provide the directory containing the `resource.json` and
  other files, _not_ any of the specific file paths within.
- `-s` to switch output mode to be resource signatures
- `-a, --actor` to update the 'actor' for the provided resources
- `-t, --timestamp` to override the timestamp used. Otherwise, the current time will be provided.

If the `-s` flag is not provided, a full `resource.json` file be returned to STDOUT - you are responsible for piping it
into the actual file, overwriting if necessary.
