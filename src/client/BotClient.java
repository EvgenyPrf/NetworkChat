package client;

import server.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class BotClient extends Client {
    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() throws IOException, ClassNotFoundException {
        return String.format("date_bot_%d", 1 + ((int) (Math.random() * 99)));
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            BotClient.this.sendTextMessage("Привет чатику.\n Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
                String[] nameAndMessage = message.split(": ");
                if (nameAndMessage.length != 2) {return;}
                String userName = nameAndMessage[0];
                String userMessage = nameAndMessage[1].trim();
                String format = null;
                switch (userMessage) {
                    case "дата":
                        format = "d.MM.YYYY";
                        break;
                    case "день":
                        format = "d";
                        break;
                    case "месяц":
                       format = "MMMM";
                        break;
                    case "год":
                        format = "YYYY";
                        break;
                    case "время":
                       format = "H:mm:ss";
                        break;
                    case "час":
                        format = "H";
                        break;
                    case "минуты":
                       format = "m";
                        break;
                    case "секунды":
                        format = "s";
                        break;
                }
                if(format != null) {
                    BotClient.this.sendTextMessage(String.format("Информация для %s: %s",
                            userName, new SimpleDateFormat(format).format(Calendar.getInstance().getTime())));
                }
            }
    }
}
