package com.kingsley.client.handlers;

/**
 * ClientProcessHandler:
 *      The core logic
 *      It handles all the private valuables from client implementation
 * Reason:
 *      Singleton pattern works nicely on multiple threads
 *      Other classes dont need ot know how logic works under the hood
 *      Multi thread debugging relatively easier in this manner
 */

import com.kingsley.client.models.Event;
import com.kingsley.client.models.EventType;
import com.kingsley.client.models.Transaction;
import javafx.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static com.kingsley.client.models.EventType.*;

public class ClientProcessHandler {
    private static Logger logger = Logger.getLogger(ClientProcessHandler.class.getSimpleName());

    public static ClientProcessHandler instance;

    public static void init(int myID) {
        if (instance != null) {
            throw new RuntimeException("you can only init onece");
        }
        instance = new ClientProcessHandler(myID);
    }

    public static ClientProcessHandler getInstance() {
        if (instance == null) {
            throw new RuntimeException("instance is not init yet");
        }

        return instance;
    }

    private ClientProcessHandler(int myID) {
        this.myID = myID;
    }

    private final int myID;
    private final List<Event> eventList = Collections.synchronizedList(new ArrayList<>());

    private final ArrayBlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(100);
    private final LinkedList<Pair<Integer, Integer>> csRequestList = new LinkedList<>();

    private final PriorityQueue<Event> pq = new PriorityQueue<>(new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
            return 0;
        }
    });

    private final AtomicInteger clock = new AtomicInteger(0);
    private final List<Transaction> pendingTransactions = new LinkedList<>();

    private final List<Transaction> blockChain = new LinkedList<>();
    private final Map<Integer, Boolean> replyMap = new HashMap<>();

    private int balance = 10;
    private boolean readyToRun = true;
    private Scanner scanner = new Scanner(System.in);

    public boolean isRun() {
        return readyToRun;
    }

    public boolean hasEvent() {
        return !eventQueue.isEmpty();
    }

    public Event nextEvent() {
        return eventQueue.poll();
    }

    public synchronized void updateClock(int eventClock, String senderID, EventType eventType) throws InterruptedException {
        int updateClock = Math.max(clock.get(), eventClock);
        clock.set(updateClock + 1);
        eventQueue.put(new Event(String.valueOf(senderID), new Pair(clock.get(), myID), eventType));
    }

    public synchronized void updateCSRequestQueue(int eventClock, String senderID, EventType eventType) throws InterruptedException {
        System.out.println("==> update cs request queue ");

        csRequestList.add(new Pair(eventClock, Integer.parseInt(senderID)));
        Comparator<Pair<Integer, Integer>> comparator = Comparator.comparing(Pair::getKey);
        comparator = comparator.thenComparing((Pair::getValue));
        csRequestList.sort(comparator);
        updateClock(eventClock, senderID, eventType);
    }

    public synchronized void createTransferEvent() {
        System.out.print("Enter event transition detail ($amt, receiver_id): ");
        String message = scanner.nextLine();
        System.out.println("====> Create a new local event transfer.");
        eventQueue.add(new Event(message, new Pair(clock.addAndGet(1), myID), TRANSFER));

        StringTokenizer st = new StringTokenizer(message, ",");
        int amt = Integer.parseInt(st.nextToken());
        int receiverID = Integer.parseInt(st.nextToken());
        Transaction transaction = new Transaction(myID, receiverID, amt);
        pendingTransactions.add(transaction);
    }

    public synchronized String processEvent(EventType eventType, Event event) throws IOException, InterruptedException {
        switch (eventType) {
            case TRANSFER:
                return processTransferEvent(event);
            case REQUEST:
                break;
            case RECEIVED_REQUEST:
                return processReceivedRequestEvent(event);
            case REQUEST_REPLY:
                return processRequestReplyEvent(event);
            case BROADCAST_TRANSACTION:
                break;
            case RECEIVED_BROADCAST_TRANSACTION:
                return processReceivedBroadcastTransactionEvent(event);
            default:
                return null;
        }
        return null;
    }

    public synchronized String processTransferEvent(Event e) throws IOException {
        System.out.println("==> processTransferEvent ");

        eventList.add(e);

        StringTokenizer st = new StringTokenizer(e.getName(), ",");
        String amt = st.nextToken();
        if (Integer.parseInt(amt) > balance) {
            System.out.println("====> Transaction id denied because of the low balance. Try again!");
            pendingTransactions.remove(pendingTransactions.size() - 1);
        } else {
            Event newEvent = new Event("request", new Pair(clock.addAndGet(1), myID), REQUEST);
            eventList.add(newEvent);

            csRequestList.add(new Pair(newEvent.getClock().getKey(), myID));

            replyMap.put(1, false);
            replyMap.put(2, false);
            replyMap.put(3, false);
            replyMap.replace(myID, true);

            String message = "broadcast,request," + newEvent.getClock().getKey() + "," + myID;
//                  System.out.println(message);
            logger.info(message);
//            dos.writeUTF(message);
            return message;
        }
        return null;
    }

    public synchronized String processReceivedRequestEvent(Event e) throws IOException {
        System.out.println("==> processReceivedRequestEvent ");

        eventList.add(e);
        if (csRequestList.getFirst() == null) {
            // TODO:
            throw new RuntimeException("TODO://dfjdskafnldosafdas");
        }
        if (csRequestList.getFirst().getValue() != myID) {
            System.out.println("sending reply to " + csRequestList.getFirst().getValue());

            Event newEvent = new Event("request_reply", new Pair(clock.addAndGet(1), myID), REQUEST_REPLY);
            eventList.add(newEvent);

            String message = "requestReply," + newEvent.getClock().getKey() + "," + csRequestList.getFirst().getValue();
            return message;
        }
        return null;
    }

    public synchronized String processRequestReplyEvent(Event e) throws IOException, InterruptedException {
        System.out.println("==> processRequestReplyEvent");

        eventList.add(e);

        int requestReplySender = Integer.parseInt(e.getName());
        replyMap.replace(requestReplySender, true);
        System.out.println(requestReplySender);
        System.out.println(replyMap.get(1));
        System.out.println(replyMap.get(2));
        System.out.println(replyMap.get(3));

        if (!replyMap.containsValue(false)) {
            // reset
            replyMap.replace(1, false);
            replyMap.replace(2, false);
            replyMap.replace(3, false);

            csRequestList.poll();
            // done pending
            Transaction transaction = pendingTransactions.get(0);
            pendingTransactions.remove(0);
            blockChain.add(transaction);
            balance -= transaction.getAmt();

            // broadcast the transaction
            String message = transaction.getSenderID() + "," + transaction.getReceiverID() + "," + transaction.getAmt();
            Event newEvent = new Event(message, new Pair(clock.addAndGet(1), myID), BROADCAST_TRANSACTION);
            eventList.add(newEvent);

            // reply on next
            if (!csRequestList.isEmpty()) {
                System.out.println("reply on next " + csRequestList.getFirst().getValue());
                eventQueue.put(new Event(String.valueOf(csRequestList.getFirst().getValue()), new Pair(csRequestList.getFirst().getKey(), myID), RECEIVED_REQUEST));
            }


            return "broadcast,transaction," + newEvent.getClock().getKey() + "," + message;
        }
        return null;
    }

    public synchronized String processReceivedBroadcastTransactionEvent(Event e) {
        System.out.println("==> processReceivedBroadcastTransactionEvent");

        eventList.add(e);
        StringTokenizer st = new StringTokenizer(e.getName(), ",");
        String senderID = st.nextToken();
        String amtReceiverID = st.nextToken();
        String amt = st.nextToken();
        Transaction transaction = new Transaction(Integer.parseInt(senderID), Integer.parseInt(amtReceiverID), Integer.parseInt(amt));
        blockChain.add(transaction);

        if (myID == Integer.parseInt(amtReceiverID)) {
            System.out.println("Received &" + transaction.getAmt() + " from " + Integer.parseInt(amtReceiverID));
            logger.info("Received: " + transaction);
            balance += transaction.getAmt();
        }

        csRequestList.pollFirst();

        if (!csRequestList.isEmpty() && csRequestList.peek().getValue() != myID) {
            Event newEvent = new Event("request_reply", new Pair(clock.addAndGet(1), myID), REQUEST_REPLY);
            eventList.add(newEvent);

            System.out.println("sending reply to " + csRequestList.getFirst().getValue());

            String message = "requestReply," + newEvent.getClock().getKey() + "," + csRequestList.getFirst().getValue();
//                    System.out.println(message);
//            dos.writeUTF(message);
            return message;
        }
        return null;
    }

    public String getCommand() {
        return scanner.nextLine().trim();
    }

    public void printBalance() {
        System.out.println("Print Balance: $" + balance);
    }

    public void printEventList() {
        System.out.println("Print event list : ");
        for (Event ev : eventList) {
            System.out.print("[" + ev.getClock().getKey() + "," + ev.getClock().getValue() + "] ");
        }
        System.out.println();
    }

    public void printBlockChain() {
        System.out.println("Print BlockChain: ");
        System.out.print("[");
        for (Transaction t : blockChain) {
            System.out.print("(P" + t.getSenderID() + ", P" + t.getReceiverID() + ", $" + t.getAmt() + "), ");
        }
        System.out.println("]");
    }
}

