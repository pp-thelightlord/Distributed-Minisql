package Distributed_Minisql;

import java.io.IOException;

public class MasterManager {
    public ZookeeperManager zookeeperManager;
    public TableManager tableManager;
    private SocketManager socketManager;

    private final int PORT = 12345;

    public MasterManager() throws IOException, InterruptedException {
        tableManager = new TableManager();
        zookeeperManager = new ZookeeperManager(tableManager);
        socketManager = new SocketManager(PORT, tableManager);
    }

    public void startUp() throws IOException, InterruptedException{
        // 用于实时监控zookeeper下的znode信息的线程
        Thread monitor = new Thread(zookeeperManager);
        monitor.start();
        // 负责和从节点通信的线程
        socketManager.startSocketManager();
    }
}
