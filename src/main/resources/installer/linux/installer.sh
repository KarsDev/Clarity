#!/bin/bash

# Check if the script is running as root (administrator)
if [ "$EUID" -ne 0 ]; then
  echo "This script requires administrative privileges. Please run it as root."
  sudo "$0" "$@"
  exit
fi

# Define variables
ext=".clr"
iconPath="$HOME/.local/share/icons/clarity.png"  # Linux typically uses .png for icons
desktopFileName="clarity.desktop"
mimeType="application/x-clarity"

# Create MIME type and associate with the .clr extension
echo "Creating MIME type and file association..."

# Add MIME type
if ! grep -q "<mime-type type=\"$mimeType\">" /usr/share/mime/packages/clarity.xml 2>/dev/null; then
  sudo mkdir -p /usr/share/mime/packages
  echo '<?xml version="1.0" encoding="UTF-8"?>' | sudo tee /usr/share/mime/packages/clarity.xml
  echo '<mime-info xmlns="http://www.freedesktop.org/standards/shared-mime-info">' | sudo tee -a /usr/share/mime/packages/clarity.xml
  echo "  <mime-type type=\"$mimeType\">" | sudo tee -a /usr/share/mime/packages/clarity.xml
  echo "    <comment>Clarity File Type</comment>" | sudo tee -a /usr/share/mime/packages/clarity.xml
  echo "    <glob pattern=\"*${ext}\"/>" | sudo tee -a /usr/share/mime/packages/clarity.xml
  echo "  </mime-type>" | sudo tee -a /usr/share/mime/packages/clarity.xml
  echo '</mime-info>' | sudo tee -a /usr/share/mime/packages/clarity.xml
  sudo update-mime-database /usr/share/mime
fi

# Create .desktop file for the application
echo "Creating desktop entry..."

desktopFilePath="$HOME/.local/share/applications/$desktopFileName"

cat <<EOL >"$desktopFilePath"
[Desktop Entry]
Name=Clarity
Exec=java -jar "$(dirname "$0")/clarity.jar" %f
Icon=$iconPath
Type=Application
MimeType=$mimeType;
EOL

# Set the icon for the file type
echo "Setting icon..."
cp /path/to/your/logo.png "$iconPath"

# Update the desktop database
echo "Updating desktop database..."
sudo update-desktop-database "$HOME/.local/share/applications"

echo "Done! Please restart your file manager or log out and log back in for changes to take effect."