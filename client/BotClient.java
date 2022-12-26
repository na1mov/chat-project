package client;

import server.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BotClient extends Client {
    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            String[] nameAndText = message.split(":");
            if (nameAndText.length != 2)
                return;
            String name = nameAndText[0].trim();
            String text = nameAndText[1].trim();
            Calendar time = new GregorianCalendar();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            switch (text) {
                case "дата":
                    simpleDateFormat.applyPattern("d.MM.yyyy");
                    break;
                case "день":
                    simpleDateFormat.applyPattern("d");
                    break;
                case "месяц":
                    simpleDateFormat.applyPattern("MMMM");
                    break;
                case "год":
                    simpleDateFormat.applyPattern("yyyy");
                    break;
                case "время":
                    simpleDateFormat.applyPattern("H:mm:ss");
                    break;
                case "час":
                    simpleDateFormat.applyPattern("H");
                    break;
                case "минуты":
                    simpleDateFormat.applyPattern("m");
                    break;
                case "секунды":
                    simpleDateFormat.applyPattern("s");
                    break;
                default:
                    return;
            }
            simpleDateFormat.setCalendar(time);
            BotClient.this.sendTextMessage(String.format(
                    "Информация для %s: %s", name, simpleDateFormat.format(time.getTime())));
        }
    }
}
