package xyz.eclipseisoffline.bookcopy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.eclpseisoffline.bookcopy.ClientCommandListener;

import java.nio.file.Path;

public class BookCopy implements ClientModInitializer {

    public static final String MOD_ID = "bookcopy";
    public static final Path BOOK_SAVE_PATH = Path.of(MOD_ID);
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(new ClientCommandListener());
    }
}
