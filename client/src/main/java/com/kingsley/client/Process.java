package com.kingsley.client;

/*
 * Process
 * Main thread:
 *      Creates thread handler instance and forward Event to handler to process
 */


import com.kingsley.client.handlers.ClientProcessHandler;
import com.kingsley.client.handlers.HandlerProvider;
import com.kingsley.client.handlers.SocketHandler;
import com.kingsley.client.handlers.UserInteractionHandler;
import com.kingsley.client.models.Event;

import java.io.*;

public class Process {
    final static int serverPort = 1234;
    static boolean run = true;

    public static void main(String[] args) throws IOException, InterruptedException {

        SocketHandler socketHandler = HandlerProvider.provideSocketHandler(serverPort);
        socketHandler.start();

        ClientProcessHandler instance = HandlerProvider.provideClientProcessHandler();

        UserInteractionHandler userInteractionHandler = new UserInteractionHandler(socketHandler.getOutputStream(), instance);
        userInteractionHandler.start();


        // main thread
        while (run) {
            while (instance.hasEvent()) {
                Event e = instance.nextEvent();
                String message = instance.processEvent(e.getType(), e);
                if (message != null) {
                    socketHandler.sendMessage(message);
                }
            }
        }
    }
}
