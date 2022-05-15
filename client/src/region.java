import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
  *@类名 Chat
  *@描述 TODO Socketregion端(测试类)
  *@版本 1.0
  *@创建人 XuKang
  *@创建时间 2020/9/24 16:17
  **/
  public class region {

	private static CopyOnWriteArrayList<Channel> all =new CopyOnWriteArrayList<Channel>();

	public static void main(String[] args) throws IOException {
		System.out.println("-----Region 8080-----");
		// 1、指定端口 使用ServerSocket创建服务器
		ServerSocket server =new ServerSocket(8080);
		// 2、阻塞式等待连接 accept
		while(true) {
				Socket  client =server.accept(); 
				System.out.println("region:一个客户端建立了连接");
				Channel c =new Channel(client);
				all.add(c); //管理所有的成员
				new Thread(c).start();			
			}		
		}
		//一个客户代表一个Channel
		static class Channel implements Runnable{
			private DataInputStream dis;
			private DataOutputStream dos;
			private Socket  client;			
			private boolean isRunning;
			private String name;
			public Channel(Socket  client) {
				this.client = client;
				try {
					dis = new DataInputStream(client.getInputStream());
					dos =new DataOutputStream(client.getOutputStream());
					isRunning =true;
					
					
				} catch (IOException e) {
					System.out.println("---1------");
					release();					
				}			
			}
			//接收消息
			private String receive() {
				String msg ="";
				try {
					msg =dis.readUTF();
					System.out.println("region端已收到"+msg);
                    send("region端已收到"+msg);
				} catch (IOException e) {
					System.out.println("---2------");
					release();
				}
				return msg;
			}
			//发送消息
			private void send(String msg) {
				try {
					dos.writeUTF(msg);
					dos.flush();
				} catch (IOException e) {
					System.out.println("---3------");
					release();
				}
			}
			
			//释放资源
			private void release() {
				this.isRunning = false;
				// CloseUtils.close(dis,dos,client);
				//退出
				all.remove(this);
				
			}
			@Override
			public void run() {
				while(isRunning) {
					String msg = receive() ;
					if(!msg.equals("")) {
						//send(msg);
						
					}
				}
			}
		}
}

