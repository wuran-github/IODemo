import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author: wu
 * @date: 2020/3/26
 **/
public class Client {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 12345;
        Socket socket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            socket = new Socket(host,port);
            //用于读取
            reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream(),"UTF-8"));
            //用于写入
            writer = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream(), "UTF-8"
            ));
            String msg = "";
            Scanner in = new Scanner(System.in);
            //开始写入
            while(true){
                msg = in.nextLine();
                if(msg.equals("exit")){
                    break;
                }
                writer.println(msg);
                writer.flush();
                System.out.println(reader.readLine());
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
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
