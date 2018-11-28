package com.qc.my3DComputer.socket;


import com.qc.my3DComputer.domain.Position;
import com.qc.my3DComputer.mapper.PositionMapper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketHandle extends Thread {
    private Socket socket;

    private PositionMapper positionMapper;


    ExecutorService pool = new ThreadPoolExecutor(8, 8, 200L,
            TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(8), new ThreadPoolExecutor.AbortPolicy());


    public SocketHandle(Socket socket, PositionMapper positionMapper) {
        this.socket = socket;
        this.positionMapper = positionMapper;
    }

    private boolean isValid(Position position) {
        if (position.getX() > -100 && position.getX() < 1000) {
            if (position.getY() > 0 && position.getY() < 1000) {
                if (position.getZ() > 0 && position.getZ() < 400) {
                    return true;
                }
            }
        }
        return false;
    }

    public void writeToDB(Position position) {
        /**
         * @Description: 将计算得到的坐标写入数据库
         * @Param: [bytes]
         * @return: void
         * @Author: Chao Qian
         * @Date: 2018-11-02
         */
        if (isValid(position)) {
            if (positionMapper.insert(position) == 1) {
                System.out.println("已写入数据库 坐标：(" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")");
            }
        }
    }

    @Override
    public void run() {
        DataInputStream dis;
        try {
            dis = new DataInputStream(socket.getInputStream());
            String str;
            byte[] bytes = new byte[19];
            int len = 0;
            boolean flag = true;
            while ((len = dis.read(bytes)) != -1) {
                /*获得定位坐标（注释部分）*/
                int[] d = new int[4];
                d[0] += ((bytes[2] & 0xff) << 24) + ((bytes[3] & 0xff) << 16) +
                        ((bytes[4] & 0xff) << 8) + (bytes[5] & 0xff);
                d[1] += ((bytes[6] & 0xff) << 24) + ((bytes[7] & 0xff) << 16) +
                        ((bytes[8] & 0xff) << 8) + (bytes[9] & 0xff);
                d[2] += ((bytes[10] & 0xff) << 24) + ((bytes[11] & 0xff) << 16) +
                        ((bytes[12] & 0xff) << 8) + (bytes[13] & 0xff);
                d[3] += ((bytes[14] & 0xff) << 24) + ((bytes[15] & 0xff) << 16) +
                        ((bytes[16] & 0xff) << 8) + (bytes[17] & 0xff);

                System.out.println("d1 = " + String.valueOf(d[0]) + " d2 = " + String.valueOf(d[1]) + " d3 = "
                        + String.valueOf(d[2]) + " d4 = " + String.valueOf(d[3]));

                /************开启线程连接python写的程序计算坐标并存入数据库***********/

                pool.execute(() -> {
                    /**
                     * @Description: 连接python获得坐标，存入数据库
                     * @Param: []
                     * @return: void
                     * @Author: Chao Qian
                     * @Date: 2018-11-02
                     */
                    try {
                        String[] pythonArgs = new String[]{"python", "E:\\pyproject\\test2.py",
                                String.valueOf(d[0]), String.valueOf(d[1]), String.valueOf(d[2]), String.valueOf(d[3])};
                        // 执行py文件
                        Process proc = Runtime.getRuntime().exec(pythonArgs);
                        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        proc.waitFor();
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            Position position = new Position();
                            String[] strs = line.split("\\s+");
                            position.setState("Y");
                            position.setX(Integer.parseInt(strs[0]) / 10);
                            position.setY(Integer.parseInt(strs[1]) / 10);
                            position.setZ(Integer.parseInt(strs[2]) / 10);
                            writeToDB(position);
                        }
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
