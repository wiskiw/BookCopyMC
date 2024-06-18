package xyz.eclipseisoffline.bookcopy;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileUtils {

    public static Path getBookSavePath() throws IOException {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(BookCopy.BOOK_SAVE_PATH);
        File directory = new File(path.toUri());
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create book save directory");
            }
        }
        return path;
    }
}
