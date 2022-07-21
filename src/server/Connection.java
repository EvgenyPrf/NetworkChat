package server;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

/*класс соединения между клиентом и сервером, который умеет сериализовать
и десериализовать объекты типа server.Message в сокет
*/

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    //Создать объект класса ObjectOutputStream нужно до того, как будет создаваться объект класса ObjectInputStream,
    //иначе может возникнуть взаимная блокировка потоков, которые хотят установить соединение через класс server.Connection.
    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    //сериализация
    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    //десериализация
    public Message receive() throws IOException, ClassNotFoundException {
        synchronized (in) {
            return (Message) in.readObject();
        }
    }

    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }


    @Override
    public void close() throws IOException {
        socket.close();
        out.close();
        in.close();
    }
}
