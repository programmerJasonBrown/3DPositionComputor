package com.qc.my3DComputer.socket;

import com.qc.my3DComputer.mapper.PositionMapper;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListener extends Thread {
    @Override
    public void run() {
        PositionMapper positionMapper = (PositionMapper) new ClassPathXmlApplicationContext(
                "classpath:spring/applicationContext.xml").getBean("positionMapper");
        try {
            ServerSocket serverSocket = new ServerSocket(3000);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("有新的连接");

                SocketHandle socketHandle = new SocketHandle(socket, positionMapper);

                socketHandle.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
