package com.ssssssss.milvus.service;

import com.ssssssss.milvus.model.CollectionInfo;
import io.milvus.client.MilvusClient;
import io.milvus.grpc.*;
import io.milvus.param.R;
import io.milvus.param.collection.DescribeCollectionParam;
import io.milvus.param.collection.GetCollectionStatisticsParam;
import io.milvus.param.collection.GetLoadStateParam;
import io.milvus.param.collection.ShowCollectionsParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Milvus服务类，提供对Milvus数据库的操作方法
 *
 * @author 冰点
 */
@Service
@Slf4j
public class MilvusService {

    @Autowired
    private MilvusClient milvusClient;

    /**
     * 获取集合的向量维度
     */
    public int getVectorDimension(String collectionName) {
        try {
            DescribeCollectionParam describeParam = DescribeCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();
            R<DescribeCollectionResponse> response = milvusClient.describeCollection(describeParam);
            
            if (response.getStatus() == R.Status.Success.getCode()) {
                for (FieldSchema field : response.getData().getSchema().getFieldsList()) {
                    if (field.getDataType().name().equals("FloatVector")) {
                        return field.getTypeParamsList().stream()
                                .filter(param -> param.getKey().equals("dim"))
                                .map(param -> Integer.parseInt(param.getValue()))
                                .findFirst().orElse(128);
                    }
                }
            }
            return 128; // 默认维度
        } catch (Exception e) {
            log.error("获取向量维度失败", e);
            return 128; // 默认维度
        }
    }

    /**
     * 生成随机向量
     */
    public java.util.List<Float> generateRandomVector(int dimension) {
        java.util.List<Float> vector = new java.util.ArrayList<>(dimension);
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < dimension; i++) {
            vector.add(random.nextFloat());
        }
        return vector;
    }

    /**
     * 检查集合是否存在
     */
    public boolean collectionExists(String collectionName) {
        try {
            io.milvus.param.collection.HasCollectionParam hasParam = io.milvus.param.collection.HasCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();
            R<Boolean> hasResult = milvusClient.hasCollection(hasParam);
            return hasResult.getData();
        } catch (Exception e) {
            log.error("检查集合存在性失败", e);
            return false;
        }
    }

    /**
     * 获取所有集合信息
     */
    public List<CollectionInfo> getAllCollections() {
        try {
            ShowCollectionsParam showParam = ShowCollectionsParam.newBuilder().build();
            R<ShowCollectionsResponse> showResponse = milvusClient.showCollections(showParam);
            
            if (showResponse.getStatus() != R.Status.Success.getCode()) {
                log.error("获取集合列表失败: {}", showResponse.getMessage());
                return Collections.emptyList();
            }
            
            List<CollectionInfo> collections = new ArrayList<>();
            ShowCollectionsResponse response = showResponse.getData();
            
            for (int i = 0; i < response.getCollectionNamesCount(); i++) {
                String name = response.getCollectionNames(i);
                String collectionId = String.valueOf(response.getCollectionIds(i));
                
                // 获取集合统计信息
                GetCollectionStatisticsParam statsParam = GetCollectionStatisticsParam.newBuilder()
                        .withCollectionName(name)
                        .build();
                R<GetCollectionStatisticsResponse> statsResponse = milvusClient.getCollectionStatistics(statsParam);
                
                long rowCount = 0;
                if (statsResponse.getStatus() == R.Status.Success.getCode()) {
                    for (KeyValuePair pair : statsResponse.getData().getStatsList()) {
                        if (pair.getKey().equals("row_count")) {
                            rowCount = Long.parseLong(pair.getValue());
                            break;
                        }
                    }
                }
                
                // 获取加载状态
                String state = "not_loaded";
                GetLoadStateParam loadStateParam = GetLoadStateParam.newBuilder()
                        .withCollectionName(name)
                        .build();
                R<GetLoadStateResponse> loadStateResponse = milvusClient.getLoadState(loadStateParam);
                
                if (loadStateResponse.getStatus() == R.Status.Success.getCode() &&
                    loadStateResponse.getData().getState() == LoadState.LoadStateLoaded) {
                    state = "loaded";
                }
                
                // 获取向量维度
                int dimension = getVectorDimension(name);
                
                CollectionInfo collection = new CollectionInfo(name, "", dimension, rowCount, state);
                collections.add(collection);
            }
            
            return collections;
        } catch (Exception e) {
            log.error("获取集合列表失败", e);
            return Collections.emptyList();
        }
    }
}