package aio;

import java.awt.print.Pageable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;

/**
 * @author: wu
 * @date: 2020/3/27
 **/
public class AIOClient {
    public static void main(String[] args) throws IOException {
        AsyncClient client = new AsyncClient();
        Scanner in = new Scanner(System.in);
        while(true){
            String res = in.nextLine();
            if(res.equals("stop")){
                client.disconnect();
                break;
            }
            client.send(res);
        }
    }
    static class AsyncClient{
        AsynchronousSocketChannel channel;

        public AsyncClient() throws IOException {
            channel = AsynchronousSocketChannel.open();
            channel.connect(new InetSocketAddress("127.0.0.1",12345),channel, new ConnectHandler());
        }

        public void send(String msg){
            ByteBuffer buffer = ByteBuffer.allocate(msg.length());
            buffer.put(msg.getBytes());
            buffer.flip();
            channel.write(buffer, buffer, new WriterHandler(channel,new ReadHandler()));
        }
        public void disconnect() throws IOException {
            channel.close();
        }
    }

    /**
     * 把channel传过来
     *
     */
    static class ConnectHandler implements CompletionHandler<Void, AsynchronousSocketChannel>{
        @Override
        public void completed(Void result, AsynchronousSocketChannel attachment) {
            System.out.println("连接成功");
            //绑定read
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            attachment.read(buffer, buffer, new ReadHandler());
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
            System.out.println("连接失败");
            exc.printStackTrace();
        }
    }

    /**
     *绑定read
     */
    static class ReadHandler implements  CompletionHandler<Integer, ByteBuffer>{

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            try {
                String res = new String(bytes, "UTF-8");
                System.out.println("读取成功："+res);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {

        }
    }

    static class WriterHandler implements CompletionHandler<Integer, ByteBuffer>{
        AsynchronousSocketChannel channel;
        ReadHandler readHandler;
        public WriterHandler(AsynchronousSocketChannel channel, ReadHandler readHandler){
            this.channel = channel;
            this.readHandler = readHandler;
        }
        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            if(buffer.hasRemaining()){
                channel.write(buffer,buffer, this);
            }
            else{
                System.out.println("写入完毕");
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {

        }
    }
}
