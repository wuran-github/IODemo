package nio;

import com.sun.security.ntlm.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author: wu
 * @date: 2020/3/26
 **/
public class NIOServer {
    public static void main(String[] args) {
        ServerHandle handle = new ServerHandle(12345);
        handle.run();
    }
    static class ServerHandle implements Runnable{
        Selector selector;
        ServerSocketChannel serverChannel;
        boolean started;
        Scanner in;
        public ServerHandle(int port){
            in = new Scanner(System.in);
            try{
                //创建选择器 Nio 实现多路复用
                selector = Selector.open();
                //打开监听通道
                serverChannel = ServerSocketChannel.open();

                //开启非阻塞模式
                serverChannel.configureBlocking(false);
                //绑定端口 队列长度设置为1024
                serverChannel.socket().bind(new InetSocketAddress(port), 1024);
                //监听客户端连接请求
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                started = true;
                System.out.println("server opened");

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        @Override
        public void run() {
            int i = 0;
            while (started){
                try{
                    //写上时间就代表阻塞这么多秒
//                    selector.select(1000);
                    //不带参表示一直阻塞
                    selector.select();
                    //获取成功注册的keys
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    System.out.println(i);
                    i++;
                    //遍历key
                    while(iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        //不删除还会在这里
                        iterator.remove();
                        try{

                            handleInput(key);
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            if(selector!=null){
                try{
                    selector.close();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        //处理输入事件
        private void handleInput(SelectionKey key) throws IOException {
            if(key.isValid()){
                //处理接入的消息
                if(key.isAcceptable()){
                    ServerSocketChannel channel = (ServerSocketChannel)key.channel();
                    //建立连接
                    SocketChannel socketChannel = channel.accept();

                    //设置为非阻塞，开始通信
                    socketChannel.configureBlocking(false);
                    //开启监听
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    }

                }
            //读消息
            if(key.isReadable()){
                SocketChannel channel = (SocketChannel)key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int byteNums = channel.read(buffer);
                if(byteNums > 0){
                    //设置为开始
                    buffer.flip();

                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    String res = new String(bytes,"UTF-8");
                    System.out.println("server read:"+res);

                    //返回应答消息
//                            System.out.print("input:");
//                            res = in.nextLine();
//                            if(res == "stop"){
//                                started = false;
//                            }

                    doWrite(channel, "sever:"+res);

                }
                //小于0说明通道已经释放
                else if(byteNums < 0){
                    key.cancel();
                    channel.close();
                }
            }
        }

        private void doWrite(SocketChannel channel, String request) throws IOException {
            //将消息转换为字节码
            byte[] bytes = request.getBytes();
            //根据容量创建buffer写入
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);

            writeBuffer.flip();

            channel.write(writeBuffer);

        }
    }
}
