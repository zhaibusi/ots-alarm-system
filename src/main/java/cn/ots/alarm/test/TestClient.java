package cn.ots.alarm.test;

import cn.ots.alarm.utils.ByteUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * 测试client
 *
 * @author
 * @since 2020/12/5 12:34
 */
public class TestClient {

    public static void main(String[] args) throws Exception {
        try {
            //1.建立客户端socket连接，指定服务器位置及端口
            Socket socket = new Socket("localhost", 5555);
            //2.得到socket读写流
            OutputStream os = socket.getOutputStream();
            //输入流
            InputStream is = socket.getInputStream();
            //labdam方式
            new Thread(() -> {
                clientMonitor(is);
            }).start();
            //3.利用流按照一定的操作，对socket进行读写操作
            System.out.println("请输入一个指令（例如:握手命令:AA,询问告警:FA,故障确认:7E,握手断开:7A）:");
            while (true) {
                Scanner sc = new Scanner(System.in);
                String comd = sc.nextLine();
                if (StringUtils.isBlank(comd)) {
                    continue;
                }
                String[] comdArr = Lists.newArrayList(comd, "02").toArray(new String[2]);
                byte[] bytes = ByteUtils.hesStringArrTobyteArr(comdArr);
                byte[] bytes1 = new byte[ArrayUtils.getLength(bytes)];
                for (int i = 0; i < ArrayUtils.getLength(bytes); i++) {
                    bytes1[i] = bytes[i];
                }
                //byte[] bytes = {(byte) 0xAA};
                os.write(bytes1);
                os.flush();
            }

            //4.关闭资源
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监控服务的响应
     *
     * @param is
     */
    public static void clientMonitor(InputStream is) {
        try {
            //接收服务器的相应
            byte[] arr = new byte[64];
            int len;
            while (true) {
                //将数据读到字节数组中
                if ((len = is.read(arr)) != -1) {
                    byte[] resArr = new byte[len];
                    String[] resStrArr = new String[len];
                    for (int i = 0; i < len; i++) {
                        resArr[i] = arr[i];
                        resStrArr[i] = ByteUtils.byteToHex(arr[i]);

                    }
                    System.out.println();
                    System.out.println("收到服务响应:" + LocalDateTime.now());
                    System.out.println("字节数组是:" + ArrayUtils.toString(resArr));
                    System.out.println("转换告警字节数组信息是:" + ArrayUtils.toString(resStrArr));
                    System.out.println();
                    System.out.println("请输入一个指令（例如:握手命令:AA,询问告警:FA,故障确认:7E,握手断开:7A）:");

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
