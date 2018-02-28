package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by ichimukuren on 25.02.2018.
 */
public class Client {
   protected Connection connection;
   private volatile boolean clientConnected = false;

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage("Пользователь  " + userName + " присоеденился к чату.");
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage("Пользователь " + userName + " покинул чат.");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
            Client.this.clientConnected = clientConnected;
            Client.this.notify();
            }
        }
        protected void clientHandshake() throws IOException, ClassNotFoundException {

            Message message;
            while (!clientConnected) {

                    message = connection.receive();
                    if (message.getType() == MessageType.NAME_REQUEST) {
                        connection.send(new Message(MessageType.USER_NAME, getUserName()));
                    } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                        notifyConnectionStatusChanged(true);
                    } else throw new IOException("Unexpected MessageType");

            }
        }
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            Message message;

            while (true) {
                message = connection.receive();

                if (message.getType() == MessageType.TEXT) processIncomingMessage(message.getData());
                else {
                    if (message.getType() == MessageType.USER_ADDED) informAboutAddingNewUser(message.getData());
                    else {
                        if (message.getType() == MessageType.USER_REMOVED) informAboutDeletingNewUser(message.getData());
                        else break;
                    }
                }

            }
            throw new IOException("Unexpected MessageType");

        }

        @Override
        public void run() {
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();
            try {
                Socket socket = new Socket(serverAddress,serverPort);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            }
            catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);

            }
        }
    }
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    protected String getServerAddress() {
        String serverAddress;
        ConsoleHelper.writeMessage("Введите адрес сервера или localhost:");
        serverAddress = ConsoleHelper.readString();
        return serverAddress;
    }

    protected int getServerPort() {
        int serverPort;
        ConsoleHelper.writeMessage("Введите порт сервера:");
        serverPort = ConsoleHelper.readInt();
        return serverPort;

    }
    protected String getUserName() {
        String userName;
        ConsoleHelper.writeMessage("Введите имя пользователя:");
        userName = ConsoleHelper.readString();
        return userName;
    }
    protected boolean shouldSendTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
        connection.send(new Message(MessageType.TEXT,text));}
        catch (IOException e) {
            ConsoleHelper.writeMessage("Произошла ошибка!");
            clientConnected=false;
        }
    }

    public void run() {

            SocketThread socketThread = getSocketThread();
            socketThread.setDaemon(true);
            socketThread.start();
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Ошибка потока...");
            System.exit(1);
        }
        if(clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
            while (clientConnected) {
                String msg = ConsoleHelper.readString();
                if (msg.equals("exit")) {break;}
                else {
                    if(shouldSendTextFromConsole()) {
                        sendTextMessage(msg);
                    }
                }
            }

        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

        }
}
