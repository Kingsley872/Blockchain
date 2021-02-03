package com.kingsley.client.handlers;

/**
 * SocketHandler:
 *      This is project it is thread that keep listening message from network process
 *      It also handles sending messages to server with sendMessage() function
 * Reason:
 *      Separate out the socket related actions from other threads or classes.
 *      This way we can avoid sending multiple message from different time (different orders)
 */

import com.kingsley.client.models.Event;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

import static com.kingsley.client.models.EventType.*;

public class SocketHandler {
    private final DataInputStream dis;
    private final DataOutputStream dos;

    private final Socket s;

    private boolean isRunning = false;

    SocketHandler(int port) throws IOException {
        InetAddress ip = InetAddress.getByName("localhost");
        Socket s = new Socket(ip, port);

        // set up stream
        dis = new DataInputStream(s.getInputStream());
        dos = new DataOutputStream(s.getOutputStream());

        int myID = Integer.parseInt(dis.readUTF());
        ClientProcessHandler.init(myID);
        this.s = s;

        System.out.println("This client ID: " + myID);
    }

    public DataOutputStream getOutputStream() {
        return dos;
    }

    public void start() {
        if (isRunning) {
            throw new RuntimeException("Is already processing.");
        }
        isRunning = true;
        new Thread(this::process).start();
    }

    private void process() {
        //    private Socket s;

        Event event;
        String message;
        String messageType;
        String eventClock;
        String senderID;
        String receiverID;
        StringTokenizer st;
        String amt;


        ClientProcessHandler instance = ClientProcessHandler.getInstance();
        boolean run = instance.isRun();

        while (run) {
            try {
                message = dis.readUTF();

                if (message.equals("done"))
                    break;

                st = new StringTokenizer(message, ",");
                messageType = st.nextToken();

                switch (messageType) {
                    case "request":
                        eventClock = st.nextToken();
                        senderID = st.nextToken();

                        instance.updateCSRequestQueue(Integer.parseInt(eventClock), senderID, RECEIVED_REQUEST);

                        break;

                    case "requestReply":
                        eventClock = st.nextToken();
                        senderID = st.nextToken();

                        instance.updateClock(Integer.parseInt(eventClock), senderID, REQUEST_REPLY);
                        break;

                    case "broadcastTransaction":
                        eventClock = st.nextToken();
                        senderID = st.nextToken();
                        receiverID = st.nextToken();
                        amt = st.nextToken();

                        instance.updateClock(Integer.parseInt(eventClock), senderID + "," + receiverID + "," + amt, RECEIVED_BROADCAST_TRANSACTION);

                        break;
                    default:
                        break;
                }

                run = instance.isRun();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // close everything
        try {
            dis.close();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMessage(String message) throws IOException {
        dos.writeUTF(message);
    }
}
