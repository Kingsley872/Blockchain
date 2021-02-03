package com.kingsley.client;

import com.google.gson.Gson;
import com.kingsley.client.models.Message;

public class GsonTest {
    public static void main(String[] args) {
        Message message = new Message();
        message.setMessageType("type");
        message.setReceiver(1);
        message.setSender("fdnskfds");

        Gson gson = new Gson();
        System.out.println(gson.toJson(message));

        String json = "{\"messageType\":\"type\",\"sender\":\"\",\"receiver\":\"1\",\"amount\":0}";
        Message decoded = gson.fromJson(json, Message.class);

        System.out.println(decoded.getMessageType());
        System.out.println(decoded.getSender());
        System.out.println(decoded.getReceiver());
        System.out.println(decoded.getAmount());
    }
}
