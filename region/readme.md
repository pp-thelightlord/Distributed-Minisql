# region接口说明



特别注意：向region写入后需要新开一行并带上end结尾；region的返回值同理！！！

```
例:
error:    detect:alive end

correct:
detect:alive
end
```



### 已完成调试

###### detectAlive

```
向region发送String类型字符串，"detect:alive"
region返回:
ok
end
```





###### excuteSql

```
向region发送String类型字符串，"excute:"并在之后带上sql语句
region返回值分情况

修改语句：
成功则返回minisql的成功语句，失败返回sql语句错误原因（注意end）

查询语句：
失败返回sql语句错误原因，成功返回查询结果，数据结构如下的一个String,数组的每一项代表一行查询结果
[{"name":"John","age":18}, {"name":"Jane","age":19}, {"name":"Jack","age":20}]
//解析方式：
System.out.println("解析并遍历:");
JSONArray jsonArray = new JSONArray(info);
for(int i=0;i<jsonArray.length();i++){
    Iterator iterator = jsonArray.getJSONObject(i).keys();
    while(iterator.hasNext()){
    	String key = (String)iterator.next();
    	System.out.println(key+":"+jsonArray.getJSONObject(i).get(key));
    }
}

//输出为
解析并遍历:
name:John
age:18
name:Jane
age:19
name:Jack
age:20
```





### 待调试

###### moveTable

```
向region发送String类型字符串，"move:tablename:regionIp"
迁移tablename表到regionIp
成功返回
success
end
```



###### backup

```
region内部使用，对一条sql语句进行backup
```



### 未实现

###### regionIp

```
给region分配副regionIp，region:tablename:region1:region2
```



###### reconnectZookeeper

```
died:master，重连zookeeper
```

