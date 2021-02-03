package com.kingsley.client.handlers;

/**
 * UserInteractionHandler:
 *      It handles communication with users
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

public class UserInteractionHandler {
    private final ClientProcessHandler instance;
    private final DataOutputStream dos;

    public UserInteractionHandler(OutputStream dos, ClientProcessHandler instance) {
        this.dos = new DataOutputStream(dos);
        this.instance = instance;
    }

    public void start() {
       new Thread(this::run).start();
    }

    private void run() {
        boolean run = ClientProcessHandler.getInstance().isRun();

        while (run) {
            // commands
            switch (ClientProcessHandler.getInstance().getCommand()) {

                case "transfer":
                    ClientProcessHandler.getInstance().createTransferEvent();
                    break;

                case "print":
                    ClientProcessHandler.getInstance().printEventList();
                    break;

                case "print1":
                    ClientProcessHandler.getInstance().printBalance();
                    break;
                case "print2":
                    ClientProcessHandler.getInstance().printBlockChain();
                    break;

                case "done":
                    // let main process know, this pro wants disconnect.
                    System.out.println("Disconnecting with NetworkProcess.");
                    try {
                        dos.writeUTF("done");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    run = false;
                    break;
                default:
                    System.out.println("Incorrect command, try again.");
                    break;
            }
        }
    }
}
