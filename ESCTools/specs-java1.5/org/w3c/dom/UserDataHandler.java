package org.w3c.dom;

public interface UserDataHandler {
    public static final short NODE_CLONED = 1;
    public static final short NODE_IMPORTED = 2;
    public static final short NODE_DELETED = 3;
    public static final short NODE_RENAMED = 4;
    public static final short NODE_ADOPTED = 5;
    
    public void handle(short operation, String key, Object data, Node src, Node dst);
}
