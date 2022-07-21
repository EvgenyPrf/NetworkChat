package client;


import server.Connection;
import server.ConsoleHelper;
import server.Message;
import server.MessageType;

import java.io.IOException;
import java.net.Socket;

/*Клиент, в начале своей работы, должен запросить у пользователя адрес и
порт сервера, подсоединиться к указанному адресу, получить запрос имени от
сервера, спросить имя у пользователя, отправить имя пользователя серверу, дождаться принятия имени сервером.
После этого клиент может обмениваться текстовыми сообщениями с сервером.
Обмен сообщениями будет происходить в двух параллельно работающих потоках.
Один будет заниматься чтением из консоли и отправкой прочитанного серверу,
а второй поток будет получать данные от сервера и выводить их в консоль.
* */
public class Client {
    protected Connection connection;
    //если клиент подключился к серверу, поле меняется на true
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Ошибка соединения с сервером. Завершение программы.");
                return;
            }
            clientConnected = true;
            if (clientConnected) {
                ConsoleHelper.writeMessage("Соединение установлено.");
            } else {
                ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
                return;
            }
            while (clientConnected) {
                String text = ConsoleHelper.readString();
                if (text.equals("exit")) {
                    break;
                }
                if (shouldSendTextFromConsole()) {
                    sendTextMessage(text);
                }
            }

        }
    }

    protected String getServerAddress() throws IOException, ClassNotFoundException {
        ConsoleHelper.writeMessage("Введите адрес сервера");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() throws IOException, ClassNotFoundException {
        ConsoleHelper.writeMessage("Введите порт сервера");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() throws IOException, ClassNotFoundException {
        ConsoleHelper.writeMessage("Введите имя пользователя");
        return ConsoleHelper.readString();
    }

    //всегда отправляем текст введенный в консоль
    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка отправки сообщения...");
            clientConnected = false;
        }
    }


    //поток, устанавливающий сокетное соединение и читающий сообщения сервера
    public class SocketThread extends Thread {
        public void run(){
            try {
                Socket socket = new Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }

        }

        //главный цикл обработки сообщений сервера
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if(message.getType() != null) {
                    switch (message.getType()) {
                        case TEXT:
                            processIncomingMessage(message.getData());
                            break;
                        case USER_ADDED:
                            informAboutAddingNewUser(message.getData());
                            break;
                        case USER_REMOVED:
                            informAboutDeletingNewUser(message.getData());
                            break;
                        default:
                            throw new IOException("Unexpected server.MessageType");
                    }
                }
                else {
                    throw new IOException("Unexpected server.MessageType");
                }
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
        }

        //метод, который представляет пользователя серверу
        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() != null) {
                    switch (message.getType()) {
                        case NAME_REQUEST:
                            //если сервер запросил имя, отправляем ему имя
                            connection.send(new Message(MessageType.USER_NAME, getUserName()));
                            break;
                        case NAME_ACCEPTED:
                            //если имя принято, уведомляем главный поток о том, что соединение установлено
                            notifyConnectionStatusChanged(true);
                            return;
                        default:
                            throw new IOException("Unexpected server.MessageType");
                    }
                } else {
                    throw new IOException("Unexpected server.MessageType");
                }
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }

        }

    }
}
