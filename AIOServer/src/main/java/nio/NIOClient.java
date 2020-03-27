package nio;

import java.awt.print.Pageable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: wu
 * @date: 2020/3/27
 **/
public class NIOClient {
    public static void main(String[] args) throws IOException {
        ExecutorService service = Executors.newCachedThreadPool();
        ClientHandle client = new ClientHandle("127.0.0.1",12345);
        client.connect();
        Scanner in  = new Scanner(System.in);
        service.submit(client);
        while(true) {
            String res = in.nextLine();
            if(res.equals("stop")){
                client.disconnect();
                break;
            }
            client.send(res);
        }
    }
    static class ClientHandle implements  Runnable{
        SocketChannel channel;
        Selector selector;
        boolean started;
        String address;
        int port;
        public ClientHandle(String address, int port) throws IOException {
            this.address = address;
            this.port = port;
            //创建selector
            selector = Selector.open();
            //打开通道，可以这时候连接，也可以放到另外的地方去连接
//            channel = SocketChannel.open(new InetSocketAddress(address,port));
            channel = SocketChannel.open();

            //设置非阻塞模式 必须设置了非阻塞才可以用selector监视，否则就是BIO那种模式
           channel.configureBlocking(false);
            started = true;
        }
        @Override
        public void run() {
            System.out.println("run");
            while(started){

                    try {
                        selector.select();
                        //读取值

                        Set<SelectionKey> keys = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = keys.iterator();
                        //不能用遍历，一个key用完之后得删除掉
//                        for(SelectionKey key:keys){
//                            handleInput(key);
//                        }
                        while (iterator.hasNext()){
                            SelectionKey key = iterator.next();
                            iterator.remove();
                            handleInput(key);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

        }
        public void handleInput(SelectionKey key) throws IOException {
            System.out.println("handleInput");
            if(key.isValid() ){
                //如果是连接，这时候
                if(key.isConnectable()){
                    SocketChannel channel = (SocketChannel) key.channel();
                    //需要调用finishConnect来完成连接
                    channel.finishConnect();

                }
                if(key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int num = channel.read(buffer);
                    if (num > 0) {
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.flip();
                        buffer.get(bytes);
                        String res = new String(bytes, "UTF-8");
                        System.out.println("client:" + res);
                    }
                }
            }
        }
        public void connect() throws IOException {
            System.out.println("connect");
            if(channel.connect(new InetSocketAddress(address, port))){

            }else {
                //注册事件
                channel.register(selector, SelectionKey.OP_CONNECT);
            }
        }
        public void disconnect() throws IOException {
            channel.close();
        }
        public void send(String msg) throws IOException {
            System.out.println("send");

                byte[] bytes = msg.getBytes();
                //设置一个buffer
                ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                channel.write(buffer);


        }
    }
}
