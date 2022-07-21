package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//Основной класс сервера
/*
- Сервер создает серверное сокетное соединение.
- В цикле ожидает, когда какой-то клиент подключится к сокету.
- Создает новый поток обработчик Handler, в котором будет происходить обмен сообщениями с клиентом.
- Ожидает следующее соединение.
* */
public class Server {
    //список всех подключений для отправки сообщения всем клиентам
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage("Введите порт сервера");
        ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt());
        try {
            System.out.println("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }

    //метод, который отправляет сообщение всем соединениям из connectionMap
    public static void sendBroadcastMessage(Message message) {
        connectionMap.forEach((name, connection) -> {
            try {
                connection.send(message);
            } catch (IOException e) {
                System.out.println("Возникла ошибка отправки сообщения");
            }
        });
    }

    //Поток-обработчик, в котором происходит обмен сообщениями сервера с клиентом
    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }
        public void run(){
            ConsoleHelper.writeMessage("Установлено новое соединение с удалённым адресом " + socket.getRemoteSocketAddress());
            String userName = null;
            try (Connection connection = new Connection(socket)){
                //знакомимся с пользователем и получаем его имя
                 userName = serverHandshake(connection);
                //уведомляем всех пользователей, что новый пользователь добавлен в чат
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                //уведомляем нового пользователя об участниках чата
                notifyUsers(connection, userName);
                //запускаем обработчик сообщений
                serverMainLoop(connection, userName);
                //перед завершением run, удаляем пользователя из списка соединений
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом.");
            }
            connectionMap.remove(userName);
            //уведомляем остальных клиентов, что пользователь вышел
            sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            ConsoleHelper.writeMessage("Соединение с " + socket.getRemoteSocketAddress() + " закрыто.");
        }

        //главный цикл обработки сообщений сервером
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                } else {
                    ConsoleHelper.writeMessage("Ошибка: полученное сообщение не является текстовым сообщением.");
                }
            }
        }

        //отправка клиенту (новому участнику) информации об остальных клиентах (участниках) чата
        private void notifyUsers(Connection connection, String userName) throws IOException {
            connectionMap.forEach((name, connect) -> {
                try {
                    if (!userName.equals(name)) {
                        connection.send(new Message(MessageType.USER_ADDED, name));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }


        //знакомство сервера с клиентом
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                //отправляем запрос имени пользователя
                connection.send(new Message(MessageType.NAME_REQUEST));
                //получаем ответ
                Message message = connection.receive();
                //выполняем проверки имени, если все ок, тогда добавляем его в список соединений
                if (message.getType().equals(MessageType.USER_NAME) && !message.getData().equals("") && !connectionMap.containsKey(message.getData())) {
                    connectionMap.put(message.getData(), connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    return message.getData();
                } else {
                    ConsoleHelper.writeMessage("Такой пользователь уже существует либо имя введено некорректно. Повторите попытку.");
                }
            }
        }
    }
}
