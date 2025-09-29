# Milvus Admin

为了方便查看rag系统里面的数据，写了一个简易版的Milvus管理工具

一个基于Spring Boot的Milvus向量数据库管理工具。
<img width="1507" height="943" alt="image" src="https://github.com/user-attachments/assets/8ea2fbac-e546-40f5-ba9b-e0fe1ea96dce" />

## 项目结构

```
milvus-admin/
├── src/main/java/com/example/milvus/
│   ├── MilvusSpringBootApplication.java    # Spring Boot启动类
│   ├── config/
│   │   └── MilvusConfig.java               # Milvus客户端配置
│   ├── controller/
│   │   ├── MilvusController.java          # Milvus API控制器
│   │   └── ViewController.java          # 视图控制器
│   ├── service/
│   │   └── MilvusService.java             # Milvus业务逻辑服务
│   ├── model/
│   │   └── CollectionInfo.java            # 集合信息模型
│   └── util/
│       └── ResponseUtil.java              # 响应工具类
├── src/main/resources/
│   ├── application.properties             # 应用配置文件
│   └── static/                           # 前端静态资源
└── pom.xml                               # Maven依赖配置
```

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

## 修复的问题

1. **导入错误**: 修复了`FieldInfo`导入错误，使用正确的`FieldSchema`类
2. **方法调用错误**: 修复了`getFields()`方法调用，使用正确的`getFieldsList()`方法
3. **数据获取逻辑**: 优化了集合数据获取，从搜索接口改为查询接口
4. **代码结构**: 重构了业务逻辑，将重复代码提取到服务类中
5. **响应处理**: 添加了统一的响应工具类简化API响应

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
