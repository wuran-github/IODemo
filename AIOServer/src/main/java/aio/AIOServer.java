package aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.Selector;
import java.util.Scanner;

/**
 * @author: wu
 * @date: 2020/3/27
 **/
public class AIOServer {
    public static void main(String[] args) {
        AsyncServerHandler serverHandler = new AsyncServerHandler();
        while(true){
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
        }
    }
    static int clientCount = 0;
    static class AsyncServerHandler implements Runnable{
        AsynchronousServerSocketChannel socketChannel;
        int port = 12345;
        public AsyncServerHandler(){
            try {
                socketChannel = AsynchronousServerSocketChannel.open();

                //绑定端口
                socketChannel.bind(new InetSocketAddress(port));
                System.out.println("server opened");
                //直接绑定回调对象
                //第二个参数是accept要调用的对象
                //第一个参数是就是要附加的对象
                socketChannel.accept(this,new AcceptHandler());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {

        }

    }

    /**
     * accept处理类
     * V代表I/O操作的结果 这里是SocketChannel，因为监听的是accept请求
     * A代表附加到I/O操作中的对象
     */
    static class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncServerHandler>{

        @Override
        public void completed(AsynchronousSocketChannel channel, AsyncServerHandler serverHandler) {
            clientCount++;
            System.out.println("第"+clientCount+"个连接的客户端");
            //继续监听accept 使用new acceptHandler
            serverHandler.socketChannel.accept(serverHandler, this);
            //通过channel传进来了，开始读取
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer, buffer, new ReadHandler(channel));
            //直接绑定写事件
            //因为服务器和客户端是1对多，所以不能事先绑定write，只能连接之后看看想写啥东西了

        }

        @Override
        public void failed(Throwable exc, AsyncServerHandler attachment) {
            exc.printStackTrace();
        }
    }

    /**
     * V代表I/O操作的结果 read是返回的数据的长度Integer
     * A代表附加到I/O操作中的对象 在这里把ByteBuffer附加过来
     * channel通过构造函数传过来
     */
    static class ReadHandler implements CompletionHandler<Integer,ByteBuffer>{
        AsynchronousSocketChannel channel;
        public ReadHandler(AsynchronousSocketChannel channel){
            this.channel = channel;
        }


        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            //重置指针到0
            attachment.flip();
            byte[] bytes = new byte[attachment.remaining()];
            attachment.get(bytes);
            try {
                String res = new String(bytes,"UTF-8");
                System.out.println("recv:"+res);
                //
                byte[] wb =res.getBytes();
                ByteBuffer writer = ByteBuffer.allocate(res.length());
                writer.put(wb);
                writer.flip();
                //读到之后返回收到
                channel.write(writer, writer, new Writer(this,channel));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            exc.printStackTrace();
        }
    }
    static class Writer implements CompletionHandler<Integer, ByteBuffer>{
        ReadHandler readHandler;
        AsynchronousSocketChannel channel;
        public Writer(ReadHandler readHandler, AsynchronousSocketChannel channel){
            this.readHandler = readHandler;
            this.channel = channel;
        }
        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            //这里不需要做啥
            System.out.println("writer");
            //每次都得重新绑定
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer, buffer, readHandler);
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {

        }
    }
}
