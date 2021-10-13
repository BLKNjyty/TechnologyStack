package com.kuang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kuang.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class KuangEsApiApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    // 测试创建索引
    @Test
    void testCreateIndex() throws IOException {
        // 创建索引库！ 创建规则！
        CreateIndexRequest request = new CreateIndexRequest("kuang_index");
        CreateIndexResponse createIndexResponse
                =restHighLevelClient.indices().create(request,RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    // 测试获取索引
    @Test
    void testExistsIndex() throws IOException {
        // 获取索引请求！
        GetIndexRequest request = new GetIndexRequest("kuang_index");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 测试删除索引
    @Test
    void testDeleteIndexRequest() throws IOException {
        // 删除索引请求！
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("kuang_index");
        AcknowledgedResponse response
                = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

    // 测试添加文档！
    @Test
    void testAddDocument() throws IOException {
        // 创建对象
        User user = new User("狂神说", 3);
        // 创建请求
        IndexRequest request = new IndexRequest("kuang_index");
        // 规则
        request.id("3");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");
        request.source(JSON.toJSONString(user), XContentType.JSON);
        // 发送请求
        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
        RestStatus Status = indexResponse.status();
        System.out.println(Status == RestStatus.OK || Status == RestStatus.CREATED);
    }

    // 判断此id是否存在这个索引库中
    @Test
    void testIsExists() throws IOException {
        GetRequest getRequest = new GetRequest("kuang_index","1");
        // 不获取_source上下文  storedFields
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        // 判断此id是否存在！
        boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 获得文档记录
    @Test
    void testGetDocument() throws IOException {
        GetRequest getRequest = new GetRequest("kuang_index","3");
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString()); // 打印文档内容
        System.out.println(getResponse);
    }

    // 更新文档记录
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("kuang_index","1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");

        User user = new User("狂神说", 18);
        request.doc(JSON.toJSONString(user), XContentType.JSON);

        UpdateResponse updateResponse = restHighLevelClient.update(
                request, RequestOptions.DEFAULT);

        System.out.println(updateResponse.status() == RestStatus.OK);
    }

    // 删除文档测试
    @Test
    void testDelete() throws IOException {
        DeleteRequest request = new DeleteRequest("kuang_index","3");
        //timeout
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");

        DeleteResponse deleteResponse = restHighLevelClient.delete(
                request, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status() == RestStatus.OK);
    }

    // 批量添加数据
    @Test
    void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        //timeout
        bulkRequest.timeout(TimeValue.timeValueMinutes(2));
        bulkRequest.timeout("2m");

        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("kuangshen1",3));
        userList.add(new User("kuangshen2",3));
        userList.add(new User("kuangshen3",3));
        userList.add(new User("qinjiang1",3));
        userList.add(new User("qinjiang2",3));
        userList.add(new User("qinjiang3",3));

        for (int i =0;i<userList.size();i++){
            bulkRequest
                    .add(new IndexRequest("kuang_index")
                            .id(""+(i+1))
                            .source(JSON.toJSONString(userList.get(i)),XContentType.JSON));
        }
        // bulk
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest,RequestOptions.DEFAULT);

        System.out.println(!bulkResponse.hasFailures());
    }

    // 查询测试
    /**
     * 使用QueryBuilder
     * termQuery("key", obj) 完全匹配
     * termsQuery("key", obj1, obj2..)   一次匹配多个值
     * matchQuery("key", Obj) 单个匹配, field不支持通配符, 前缀具高级特性
     * multiMatchQuery("text", "field1", "field2"..);  匹配多个字段, field有通配符忒行
     * matchAllQuery();         匹配所有文件
     */
    @Test
    void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("jd_goods");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "qinjiang1");
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        sourceBuilder.query(matchAllQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // 分页
        sourceBuilder.from(0);
        sourceBuilder.size(20);
        searchRequest.source(sourceBuilder);

        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(JSON.toJSONString(response.getHits()));
        System.out.println("================SearchHit==================");
        for (SearchHit documentFields : response.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }
    }


}
