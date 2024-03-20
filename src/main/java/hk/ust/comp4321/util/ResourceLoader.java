package hk.ust.comp4321.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Utility class to load resources.
 */
public class ResourceLoader {
    private ResourceLoader(){
        throw new AssertionError("ResourceLoader cannot be instantiated!");
    }

    /**
     * Loads a resource from the specified Path if it exists, or the executable if it does not.
     * @param path The Path to load the resource from
     * @return The InputStream to read the resource
     * @throws FileNotFoundException If the resource does not exist in both locations
     */
    public static InputStream loadResource(Path path) throws FileNotFoundException {
        if (!path.toFile().exists()) {
            String fileName = path.toFile().getName();
            return ResourceLoader.class.getResourceAsStream("/" + fileName);
        } else {
            return new FileInputStream(path.toFile());
        }
    }
}
