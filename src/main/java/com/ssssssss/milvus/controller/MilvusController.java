package com.ssssssss.milvus.controller;

import com.ssssssss.milvus.model.CollectionInfo;
import com.ssssssss.milvus.service.MilvusService;
import com.ssssssss.milvus.util.ResponseUtil;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Milvus控制器类，提供操作Milvus数据库的REST API接口
 *
 * @author 冰点
 */
@RestController
@RequestMapping("/api/milvus")
@Slf4j
public class MilvusController {

    @Autowired
    private MilvusClient milvusClient;

    @Autowired
    private MilvusService milvusService;

    /**
     * 测试连接
     */
    @GetMapping("/connect")
    public ResponseEntity<?> connect(
            @RequestParam String host,
            @RequestParam int port,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password) {
        try {
            ConnectParam.Builder connectParamBuilder = ConnectParam.newBuilder()
                    .withHost(host)
                    .withPort(port);

            if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
                connectParamBuilder.withAuthorization(username, password);
            }

            // 尝试创建临时客户端测试连接
            MilvusClient testClient = new MilvusServiceClient(connectParamBuilder.build());

            // 执行一个简单操作验证连接
            R<Boolean> hasCollection = testClient.hasCollection(HasCollectionParam.newBuilder()
                    .withCollectionName("test_connection")
                    .build());

            if (hasCollection.getStatus() == R.Status.Success.getCode()) {
                return ResponseEntity.ok(Collections.singletonMap("message", "连接成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "连接失败: " + hasCollection.getMessage()));
            }
        } catch (Exception e) {
            log.error("连接失败", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "连接失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有集合
     */
    @GetMapping("/collections")
    public ResponseEntity<?> getCollections() {
        try {
            List<CollectionInfo> collections = milvusService.getAllCollections();
            List<Map<String, Object>> result = new ArrayList<>();

            for (CollectionInfo collection : collections) {
                result.add(collection.toMap());
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取集合列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "获取集合列表失败: " + e.getMessage()));
        }
    }

    /**
     * 创建集合
     */
    @PostMapping("/collections")
    public ResponseEntity<?> createCollection(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam int dimension) {
        try {
            // 检查集合是否已存在
            if (milvusService.collectionExists(name)) {
                return ResponseUtil.error("集合已存在");
            }

            // 创建ID字段
            FieldType idField = FieldType.newBuilder()
                    .withName("id")
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(false)
                    .build();

            // 创建向量字段
            FieldType vectorField = FieldType.newBuilder()
                    .withName("vector")
                    .withDataType(DataType.FloatVector)
                    .withDimension(dimension)
                    .build();

            // 创建集合
            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(name)
                    .withDescription(description != null ? description : "")
                    .addFieldType(idField)
                    .addFieldType(vectorField)
                    .withShardsNum(2)
                    .build();

            R<RpcStatus> createResult = milvusClient.createCollection(createParam);

            if (createResult.getStatus() != R.Status.Success.getCode()) {
                return ResponseUtil.error(createResult.getMessage());
            }

            // 创建索引
            CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(name)
                    .withFieldName("vector")
                    .withIndexType(IndexType.IVF_FLAT)
                    .withMetricType(MetricType.L2)
                    .withExtraParam("{\"nlist\": 1024}")
                    .build();

            milvusClient.createIndex(indexParam);

            return ResponseUtil.success("集合创建成功");
        } catch (Exception e) {
            log.error("创建集合失败", e);
            return ResponseUtil.serverError("创建集合失败: " + e.getMessage());
        }
    }

    /**
     * 删除集合
     */
    @DeleteMapping("/collections/{name}")
    public ResponseEntity<?> deleteCollection(@PathVariable String name) {
        try {
            DropCollectionParam param = DropCollectionParam.newBuilder()
                    .withCollectionName(name)
                    .build();

            R<RpcStatus> response = milvusClient.dropCollection(param);

            if (response.getStatus() != R.Status.Success.getCode()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", response.getMessage()));
            }

            return ResponseEntity.ok(Collections.singletonMap("message", "集合删除成功"));
        } catch (Exception e) {
            log.error("删除集合失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "删除集合失败: " + e.getMessage()));
        }
    }

    /**
     * 加载集合
     */
    @PostMapping("/collections/{name}/load")
    public ResponseEntity<?> loadCollection(@PathVariable String name) {
        try {
            LoadCollectionParam param = LoadCollectionParam.newBuilder()
                    .withCollectionName(name)
                    .build();

            R<RpcStatus> response = milvusClient.loadCollection(param);

            if (response.getStatus() != R.Status.Success.getCode()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", response.getMessage()));
            }

            return ResponseEntity.ok(Collections.singletonMap("message", "集合加载成功"));
        } catch (Exception e) {
            log.error("加载集合失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "加载集合失败: " + e.getMessage()));
        }
    }

    /**
     * 插入数据
     */
    @PostMapping("/collections/{name}/data")
    public ResponseEntity<?> insertData(
            @PathVariable String name,
            @RequestParam Long id,
            @RequestBody List<Float> vector) {
        try {
            List<Long> ids = Collections.singletonList(id);
            List<List<Float>> vectors = Collections.singletonList(vector);

            // 创建字段列表
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("id", ids));
            fields.add(new InsertParam.Field("vector", vectors));

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(name)
                    .withFields(fields)
                    .build();

            R<MutationResult> response = milvusClient.insert(insertParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", response.getMessage()));
            }

            // 刷新集合使数据可见
            FlushParam flushParam = FlushParam.newBuilder()
                    .addCollectionName(name)
                    .build();
            milvusClient.flush(flushParam);

            return ResponseEntity.ok(Collections.singletonMap("message", "数据插入成功"));
        } catch (Exception e) {
            log.error("插入数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "插入数据失败: " + e.getMessage()));
        }
    }

    /**
     * 获取集合数据
     */
    @GetMapping("/collections/{name}/data")
    public ResponseEntity<?> getCollectionData(
            @PathVariable String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 使用查询表达式获取数据，避免全表扫描
            String expr = "id >= 0"; // 查询所有ID大于等于0的记录
            List<String> outputFields = Arrays.asList("id", "vector");

            // 使用query接口而不是search接口来获取数据
            QueryParam queryParam = QueryParam.newBuilder()
                    .withCollectionName(name)
                    .withExpr(expr)
                    .withOutFields(outputFields)
                    .withLimit((long) pageSize)
                    .withOffset((long) ((page - 1) * pageSize))
                    .build();

            R<QueryResults> response = milvusClient.query(queryParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", response.getMessage()));
            }

            QueryResultsWrapper wrapper = new QueryResultsWrapper(response.getData());
            List<Map<String, Object>> data = new ArrayList<>();

            for (int i = 0; i < wrapper.getRowCount(); i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", wrapper.getFieldWrapper("id").get(i, "id"));
                row.put("vector", wrapper.getFieldWrapper("vector").get(i, "vector"));
                data.add(row);
            }

            // 获取总数
            GetCollectionStatisticsParam statsParam = GetCollectionStatisticsParam.newBuilder()
                    .withCollectionName(name)
                    .build();
            R<GetCollectionStatisticsResponse> statsResponse = milvusClient.getCollectionStatistics(statsParam);

            long total = 0;
            if (statsResponse.getStatus() == R.Status.Success.getCode()) {
                total = Long.parseLong(String.valueOf(statsResponse.getData().getStatsCount()));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("hasMore", page * pageSize < total);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取集合数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "获取集合数据失败: " + e.getMessage()));
        }
    }

    /**
     * 删除数据
     */
    @DeleteMapping("/collections/{name}/data/{id}")
    public ResponseEntity<?> deleteData(
            @PathVariable String name,
            @PathVariable Long id) {
        try {
            // Milvus中删除数据需要使用delete接口
            DeleteParam deleteParam = DeleteParam.newBuilder()
                    .withCollectionName(name)
                    .withExpr("id == " + id)
                    .build();

            R<MutationResult> response = milvusClient.delete(deleteParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", response.getMessage()));
            }

            // 刷新集合使删除生效
            FlushParam flushParam = FlushParam.newBuilder()
                    .addCollectionName(name)
                    .build();
            milvusClient.flush(flushParam);

            return ResponseEntity.ok(Collections.singletonMap("message", "数据删除成功"));
        } catch (Exception e) {
            log.error("删除数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "删除数据失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索向量
     */
    @PostMapping("/collections/{name}/search")
    public ResponseEntity<?> searchVector(
            @PathVariable String name,
            @RequestBody List<Float> vector,
            @RequestParam(defaultValue = "10") int topK) {
        try {
            long startTime = System.currentTimeMillis();

            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(name)
                    .withMetricType(MetricType.L2)
                    .withOutFields(Collections.singletonList("id"))
                    .withTopK(topK)
                    .withVectors(Collections.singletonList(vector))
                    .withVectorFieldName("vector")
                    .withParams("{\"nprobe\": 10}")
                    .build();

            R<SearchResults> response = milvusClient.search(searchParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", response.getMessage()));
            }

            long endTime = System.currentTimeMillis();
            long searchTime = endTime - startTime;

            SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
            List<Map<String, Object>> results = new ArrayList<>();

            for (int i = 0; i < wrapper.getDynamicWrapper().getRowCount(); i++) {
                Map<String, Object> result = new HashMap<>();
                result.put("rank", i + 1);
                result.put("id", wrapper.getDynamicWrapper().get(i, "id"));
                result.put("vector",wrapper.getDynamicWrapper().get(i, "vector"));
                result.put("distance", wrapper.getIDScore(i));
                results.add(result);
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("results", results);
            responseData.put("time", searchTime);

            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            log.error("搜索向量失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "搜索向量失败: " + e.getMessage()));
        }
    }

    /**
     * 获取向量维度
     */
    private int getVectorDimension(String collectionName) {
        return milvusService.getVectorDimension(collectionName);
    }

    /**
     * 生成随机向量
     */
    private List<Float> generateRandomVector(int dimension) {
        return milvusService.generateRandomVector(dimension);
    }
}
