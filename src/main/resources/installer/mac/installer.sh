#!/bin/bash

# Check if running as root (admin)
if [ "$EUID" -ne 0 ]
then
  echo "Please run as root"
  exec sudo "$0" "$@"
  exit
fi

# Variables
EXT="clr"
FILE_TYPE="ClrFileType"
ICON_PATH="$HOME/Clarity/logo.icns"

# Check if the icon file exists
if [ ! -f "$ICON_PATH" ]; then
  echo "Icon file not found at $ICON_PATH"
  exit 1
fi

# Create or modify a plist file for the file association
PLIST_PATH="$HOME/Library/LaunchAgents/com.user.$FILE_TYPE.plist"

cat << EOF > "$PLIST_PATH"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple Computer//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleTypeExtensions</key>
    <array>
        <string>$EXT</string>
    </array>
    <key>CFBundleTypeIconFile</key>
    <string>logo.icns</string>
    <key>CFBundleTypeName</key>
    <string>Clarity File Type</string>
    <key>CFBundleTypeRole</key>
    <string>Editor</string>
</dict>
</plist>
EOF

# Copy the icon to a location where macOS expects it
cp "$ICON_PATH" "$HOME/Library/LaunchAgents/logo.icns"

# Refresh the Finder to apply changes (not a direct equivalent to explorer.exe restart)
killall Finder

echo "Done!"
