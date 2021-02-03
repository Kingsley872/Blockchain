package com.kingsley.server;

/*
 * NetworkProcess:
 *      It handles multiple connection between clients (processes)
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NetWorkProcess {

    public static void main(String[] args) throws Exception {

        System.out.println("====> Main com.kingsley.proj.Process starts");
        ServerSocket ss = new ServerSocket(1234);
        Socket s;
        DataInputStream dis;
        DataOutputStream dos;
        AtomicInteger atomicInteger;
        ProcessHandler processHandler;
        SortedMap<Integer, ProcessHandler> sortedMap;
        Thread thread;
        boolean isRunning = ProcessManager.getInstance().isRunning();

        // loop control
        while (isRunning) {
            s = ss.accept();
            System.out.println("====> New com.kingsley.proj.Process is connected.");
            System.out.println("====> New Thread for new process, and add it into pList");

            // setup
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            atomicInteger = ProcessManager.getInstance().getAtomicInteger();
            sortedMap = ProcessManager.getInstance().getSortedMap();

            dos.writeUTF(String.valueOf(atomicInteger.get()));
            // creating a new thread
            processHandler = new ProcessHandler(s, atomicInteger.getAndAdd(1), s.getInputStream(), dos);
            thread = new Thread(processHandler);

            sortedMap.put(processHandler.getId(), processHandler);

            thread.start();

        }
    }
}
