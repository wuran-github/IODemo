import com.sun.corba.se.spi.orbutil.fsm.Input;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author: wu
 * @date: 2020/3/26
 **/
public class Server {
    //端口
    private static int DEFAULT_PORT = 12345;
    //服务
    private static ServerSocket server;

    public static void main(String[] args) throws IOException {
        start();
    }
    public static void start() throws IOException {
        server = new ServerSocket(DEFAULT_PORT);
        System.out.println("server start.");
        try {
            Socket socket = server.accept();
            Handler handler = new Handler(socket);
            handler.run();
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    static class Handler implements Runnable{
        Socket socket;
        public Handler(Socket socket){
            this.socket = socket;
        }
        @Override
        public void run() {
            BufferedReader reader = null;
            PrintWriter writer = null;
            try{
                reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), "UTF-8"));
                writer = new PrintWriter(new OutputStreamWriter(
                        socket.getOutputStream(), "UTF-8"
                ));
                String msg = "";
                //开始读取
                while(true){
                    System.out.println("server reading...");
                    msg = reader.readLine();
                    if(msg == null || msg.equals("stop")){
                        break;
                    }
                    System.out.println(msg);
                    writer.println("server receive:"+msg);
                    writer.flush();
                }

            }

            catch (Exception ex){
                System.out.println(ex.getMessage());
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                    if (reader != null) {
                        reader.close();
                    }
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
