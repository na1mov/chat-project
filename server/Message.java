package server;

import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType type;
    private final String data;

    // конструкторы
    public Message(MessageType type) {
        this.type = type;
        this.data = null;
    }

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    // геттеры и сеттеры
    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
