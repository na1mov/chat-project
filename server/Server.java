package server;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            System.out.println("server.Server has been started");
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String result;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message answer = connection.receive();
                if (answer.getType() != MessageType.USER_NAME) {
                    continue;
                }
                if (answer.getData() == null || answer.getData().isEmpty()) {
                    continue;
                }
                if (connectionMap.containsKey(answer.getData())) {
                    continue;
                }
                connectionMap.put(answer.getData(), connection);
                result = answer.getData();
                connection.send(new Message(MessageType.NAME_ACCEPTED, answer.getData()));
                break;
            }
            return result;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String str : connectionMap.keySet()) {
                if (!str.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, str));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String textMessage = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, textMessage));
                } else {
                    ConsoleHelper.writeMessage("Ошибка принятия сообщения");
                }
            }
        }

        public void run() {
            String userName = "";
            ConsoleHelper.writeMessage("Установлено соединение с " + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто.");
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        try {
            for (String key : connectionMap.keySet()) {
                connectionMap.get(key).send(message);
            }
        } catch (IOException e) {
            System.out.println("Не смогли отправить сообщение.");
        }
    }
}
