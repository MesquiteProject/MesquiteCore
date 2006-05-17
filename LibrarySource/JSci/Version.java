package JSci;

import java.io.*;
import java.net.*;
import java.util.ResourceBundle;

/**
* The Version class contains information about the current and latest release.
* @version 1.3
* @author Mark Hale
*/
public final class Version extends Object implements Serializable {
        /**
        * Major version number.
        */
        public final int major;
        /**
        * Minor version number.
        */
        public final int minor;
        /**
        * Java platform required.
        */
        public final String platform;
        /**
        * The URL for the home of this version.
        */
        public final String home;
        /**
        * Gets the current version.
        */
        public static Version getCurrent() {
                ResourceBundle bundle = ResourceBundle.getBundle("JSci.Bundle");
                int major = Integer.parseInt(bundle.getString("version.major"));
                int minor = Integer.parseInt(bundle.getString("version.minor"));
                String platform = bundle.getString("version.platform");
                String home = bundle.getString("version.home");
                return new Version(major, minor, home, platform);
        }
        /**
        * Retrieves the latest version from the home URL.
        */
        public static Version getLatest() throws IOException {
                Version latest = null;
                try {
                        URL serurl = new URL(getCurrent().home+"version118.ser");
                        ObjectInputStream in = new ObjectInputStream(serurl.openStream());
                        latest = (Version) in.readObject();
                        in.close();
                } catch(MalformedURLException murle) {
                } catch(ClassNotFoundException cnfe) {}
                return latest;
        }
        /**
        * Constructs a version object.
        */
        private Version(int major, int minor, String home, String platform) {
                this.major = major;
                this.minor = minor;
                this.home = home;
                this.platform = platform;
        }
        /**
        * Compares two versions for equality.
        */
        public boolean equals(Object o) {
                if(!(o instanceof Version))
                        return false;
                Version ver = (Version) o;
                return (major == ver.major) && (minor == ver.minor) && platform.equals(ver.platform);
        }
        /**
        * Returns the version number as a string.
        */
        public String toString() {
                return new StringBuffer().append(major).append('.').append(minor).toString();
        }
        /**
        * Returns true if this is later than another version.
        */
        public boolean isLater(Version ver) {
                return (major>ver.major) ||
                        (major == ver.major && minor>ver.minor);
        }
}

