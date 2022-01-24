package model;

import java.io.Serializable;

public class Message implements Serializable {

    private int type;
    private Object content;

    public Message(int type, Object content) {
        this.type = type;
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content.toString();
    }

}
