## Setup

On Mac, install gum and jq
```
brew install gum jq
```

## Changelog creation
To create a changelog, run the following command from the root of the repository:
```
./add-changelog-entry.sh
```
This will prompt you to provide ticket number and title, as well as the type of change (fix/story).

When releasing, you can use the following command to create a changelog for the release:
```
./release-changelog.sh
```
This will prompt you to provide the version number for the release, and will create a changelog entry for the release based on the entries created with the `add-changelog-entry.sh` script.
The result will be prepended to the `CHANGELOG.md` file.