package Distributed_Minisql;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;
import java.util.List;

public class ZookeeperManager implements Runnable{
    private TableManager tableManager;

    ZookeeperManager(TableManager t){
        tableManager = t;
    }

    @Override
    public void run() {
        try {
            ZookeeperStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
        while(true){

        }
    }

    public void ZookeeperStart() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        CuratorFramework client = CuratorFrameworkFactory.newClient("10.162.22.76:2181", retryPolicy);
        client.start();
//        client.create().withMode(CreateMode.EPHEMERAL).forPath("/Distributed_Minisql");

        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/Distributed_Minisql", true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
//                System.out.println("子节点变化了");
                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                if(type.equals(PathChildrenCacheEvent.Type.CHILD_ADDED)){
                    byte[] data = pathChildrenCacheEvent.getData().getData();
                    String ip_address = new String(data);
                    System.out.println(ip_address);
                    ManageRegionAdded(ip_address);
                }
                if(type.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
                    byte[] data = pathChildrenCacheEvent.getData().getData();
                    String ip_address = new String(data);
                    System.out.println(ip_address);
                    ManageRegionRemoved(ip_address);
                }
            }
        });
        pathChildrenCache.start();
    }

    public void ManageRegionAdded(String ip_address){
        System.out.println("新节点加入"+ip_address);
        tableManager.addServer(ip_address);
    }

    public void ManageRegionRemoved(String ip_address){
        List<String> AllTable = tableManager.ipToTables.get(ip_address);
        tableManager.ipToTables.remove(ip_address);
        tableManager.serverList.remove(ip_address);
        tableManager.ipToSocket.remove(ip_address);
        for(String table: AllTable){
            SocketThread BestServerSocket;
            String BestServer;
            if(tableManager.tableToIp.get(table).equals(ip_address)){
                BestServer = tableManager.getBestServer(tableManager.getBestServer(tableManager.getIpAddressCopy(table)));
                BestServerSocket = tableManager.ipToSocket.get(BestServer);
                tableManager.tableToIp.put(table, BestServer);
            }
            else if(tableManager.tableToCopyIp.get(table).equals(ip_address)){
                BestServer = tableManager.getBestServer(tableManager.getBestServer(tableManager.getIpAddressMain(table)));
                BestServerSocket = tableManager.ipToSocket.get(BestServer);
                tableManager.tableToCopyIp.put(table, BestServer);
            }
            else {
                System.out.println("table不存在IP，错误！");
                return;
            }
            /* 向新选中的主机传送复制该table的消息 */
            // 新选中的主机：BestServer String table
            try {
                BestServerSocket.output.write("move:" + table + ":" + BestServer);
                BestServerSocket.output.newLine();
                BestServerSocket.output.write("end");
                BestServerSocket.output.newLine();
                BestServerSocket.output.flush();
                String in = BestServerSocket.input.readLine();
                BestServerSocket.input.readLine();
                if (in.contains("suc")){
                    System.out.println("成功转移备份");
                }
                else {
                    System.out.println("备份错误");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Bestserver 发送消息错误");
            }
        }

    }
}
