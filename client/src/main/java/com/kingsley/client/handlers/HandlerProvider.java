package com.kingsley.client.handlers;
/**
 * HandlerProvider:
 *      This class provides socket handler to main process
 * Reason:
 *      Separation between main thread and socket
 */

import java.io.IOException;

public final class HandlerProvider {

    public static ClientProcessHandler provideClientProcessHandler() {
        return ClientProcessHandler.getInstance();
    }

    public static SocketHandler provideSocketHandler(int port) throws IOException {
        return new SocketHandler(port);
    }

    public static UserInteractionHandler provideUserInteractionHandler() {
        return new UserInteractionHandler(null, provideClientProcessHandler());
    }

    private HandlerProvider() {
    }
}
