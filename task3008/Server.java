package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ichimukuren on 25.02.2018.
 */
public class Server {
private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {

            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message answer = connection.receive();

                if (answer.getType() == MessageType.USER_NAME) {

                    if (!answer.getData().isEmpty()) {
                        if(!connectionMap.containsKey(answer.getData())) {
                            connectionMap.put(answer.getData(),connection);
                            connection.send(new Message(MessageType.NAME_ACCEPTED));
                            return answer.getData();
                        }
                    }
                }
            }
        }
        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                if (!entry.getKey().equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
                }
            }
        }
       private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {

            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                }
                else {
                    ConsoleHelper.writeMessage("Ошибка!");
                }
            }

       }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Установлено сооеденение с новым адресом" + socket.getRemoteSocketAddress());
            String userName = null;
           try (Connection connection = new Connection(socket)) {
               userName = serverHandshake(connection);
               sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
               sendListOfUsers(connection,userName);
               serverMainLoop(connection,userName);
           }
           catch (IOException | ClassNotFoundException e)  {
               ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным сервером.");
           }
           finally {
               if (userName != null) {
                   connectionMap.remove(userName);
                   sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
               }
               ConsoleHelper.writeMessage("Соеденение с удаленным адресом закрыто." + socket.getRemoteSocketAddress()); // + socketAddress);
           }
        }
    }


    public static void sendBroadcastMessage(Message message) {
       for (Connection connection : connectionMap.values()) {
           try {
               connection.send(message);
           }
           catch (IOException e) {
               ConsoleHelper.writeMessage("Ошибка отправки сообщения!");
           }
       }
    }

    public static void main(String[] args) {

        int port = 5050;

        if (args.length==0) {
            ConsoleHelper.writeMessage("Введите порт: ");
            port = ConsoleHelper.readInt();
        }
        else port = Integer.parseInt(args[0]);


        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Сервер запущен на port: " + port);
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Что то пошло не так..");
        }

    }

        
    }

