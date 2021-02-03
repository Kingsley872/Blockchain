package com.kingsley.server;

/**
 * ProcessHandler:
 *      It takes incomming message from process and forward to other processes.
 */

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.StringTokenizer;

public class ProcessHandler implements Runnable {
    private int id;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private Socket s;
    private boolean connected = true;

    public ProcessHandler (Socket s, int id, InputStream dis, OutputStream dos) {
        this.s = s;
        this.id = id;
        InputStream in;
        this.dis = new DataInputStream(dis);
        this.dos = new DataOutputStream(dos);

    }

    @Override
    public void run() {

        String receive;
        StringTokenizer st;
        String message;
        String messageType;
        String requestOrNode;
        String eventClock;
        String senderID;
        String receiverID;
        String sendTo;
        String amt;
        int waitTime;
        ProcessHandler processHandler;
        SortedMap<Integer, ProcessHandler> sortedMap;

        while (true) {
            try {

                sortedMap = ProcessManager.getInstance().getSortedMap();
                receive = dis.readUTF();

                // disconnect with client process
                if (receive.equals("done")) {
                    connected = false;
                    System.out.println("====> Disconnect with P" + this.id);
                    this.dos.writeUTF("done");
                    this.s.close();
                    break;
                }

                Thread.sleep(5000);

                // parse message
                st = new StringTokenizer(receive, ",");
                messageType = st.nextToken();

                if (messageType.equals("broadcast")) {
                    requestOrNode = st.nextToken();
                    if (requestOrNode.equals("request")) {
                        eventClock = st.nextToken();
                        senderID = st.nextToken();
                        message = "request," + eventClock + "," + senderID;

                        System.out.println("====> Broadcasting request from {" + senderID + "}.");
                        Iterator iterator = sortedMap.keySet().iterator();
                        waitOneSec();
                        while (iterator.hasNext()) {
                            int key = (int) iterator.next();
                            if (key != Integer.parseInt(senderID)) {
                                processHandler = sortedMap.get(key);
                                processHandler.dos.writeUTF(message);
                            }
                        }
                    } else if (requestOrNode.equals("transaction")) {
                        eventClock = st.nextToken();
                        senderID = st.nextToken();
                        receiverID = st.nextToken();
                        amt = st.nextToken();
                        message = "broadcastTransaction," + eventClock + "," + senderID + "," + receiverID + "," + amt;
                        System.out.println("====> Broadcasting transaction from {" + senderID + "}.");
                        Iterator iterator = sortedMap.keySet().iterator();
                        waitOneSec();
                        while (iterator.hasNext()) {
                            int key = (int) iterator.next();
                            if (key != Integer.parseInt(senderID)) {
                                processHandler = sortedMap.get(key);
                                processHandler.dos.writeUTF(message);
                            }
                        }
                    }
                } else {
                    eventClock = st.nextToken();
                    receiverID = st.nextToken();
                    message = "requestReply," + eventClock + "," +this.id;
                    System.out.println("===> request reply from " + this.id + "to " + receiverID);
                    processHandler = sortedMap.get(Integer.parseInt(receiverID));
                    processHandler.dos.writeUTF(message);
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        // close up :
        try {
            this.dis.close();
            this.dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void waitOneSec () throws InterruptedException {
        Thread.sleep( 100);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
