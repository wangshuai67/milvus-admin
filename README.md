# Milvus Admin

为了方便查看rag系统里面的数据，写了一个简易版的Milvus管理工具

一个基于Spring Boot的Milvus向量数据库管理工具。
<img width="1507" height="943" alt="image" src="https://github.com/user-attachments/assets/8ea2fbac-e546-40f5-ba9b-e0fe1ea96dce" />

## 项目结构


## 主要功能

- 连接Milvus数据库
- 集合管理（创建、删除、加载）
- 数据插入和查询
- 向量搜索
- 集合统计信息

## API端点

- `GET /api/milvus/test` - 测试连接
- `GET /api/milvus/collections` - 获取所有集合
- `POST /api/milvus/collections` - 创建集合
- `DELETE /api/milvus/collections/{name}` - 删除集合
- `POST /api/milvus/collections/{name}/load` - 加载集合
- `POST /api/milvus/collections/{name}/data` - 插入数据
- `GET /api/milvus/collections/{name}/data` - 获取集合数据
- `DELETE /api/milvus/collections/{name}/data/{id}` - 删除数据
- `POST /api/milvus/collections/{name}/search` - 向量搜索


## 配置

在`application.properties`中配置Milvus连接信息：

```properties
milvus.host=localhost
milvus.port=19530
milvus.username=
milvus.password=
```

## 运行

```bash
mvn spring-boot:run
```

访问 http://localhost:8080 查看Web界面。
