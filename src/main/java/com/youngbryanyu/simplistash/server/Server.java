package com.youngbryanyu.simplistash.server;

public interface Server {
    public static final int WRITEABLE_PORT = 3000;
    public static final int READ_ONLY_PORT = 3001;
    public void start() throws Exception;
}
