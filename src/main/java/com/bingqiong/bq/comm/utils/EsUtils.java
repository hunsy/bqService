package com.bingqiong.bq.comm.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.comm.constants.EsIndexType;
import com.bingqiong.bq.comm.http.DoRequest;
import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.comm.Sensitive;
import com.bingqiong.bq.model.post.Post;
import com.bingqiong.bq.model.post.PostLike;
import com.bingqiong.bq.model.post.PostType;
import com.bingqiong.bq.model.user.User;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ShardedJedisPool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by hunsy on 2017/6/23.
 */
public class EsUtils {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static EsUtils esUtils = null;

    private Properties properties;
    private String host;
    private int port = 9300;
    private Client client;

    private static final String INDEX = "bq_cms";

    private EsUtils() {
    }

    public static EsUtils getInstance() {

        if (esUtils == null)
            esUtils = new EsUtils();

        return esUtils;
    }

    /**
     * 初始化
     *
     * @param properties
     */
    public void init(Properties properties) throws UnknownHostException {

        this.properties = properties;
        host = properties.getProperty("es.host", "localhost");
        port = Integer.valueOf(properties.getProperty("es.port", "9300"));
        String clusterName = properties.getProperty("es.clusterName", "node1");
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", clusterName).build();
        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(host, 9300));
    }


    /**
     * 创建索引
     *
     * @param id
     * @param type
     * @param content
     */
    public void createIndex(String id, String type, String content) {

        IndexResponse response = client.prepareIndex(INDEX, type, id)
                .setSource(content)
                .execute()
                .actionGet();

//        String full_port = host + "/bq_cms/" + type + "/" + id;
//        try {
//            DoRequest.putJsonPost(content, full_port);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        boolean flag = response.isCreated();
        long version = response.getVersion();
        logger.info("新增es索引->flag:{},version:{}", flag, version);
    }

    public void deleteIndex(String id, String type) {

        DeleteResponse response = client.prepareDelete(INDEX, type, id).execute().actionGet();

//        String host = properties.getProperty("es.host");
//        String full_port = host + "/bq_cms/" + type + "/" + id;
//        try {
//            DoRequest.delPost(full_port);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        logger.info("删除es索引->flag:{}", response.isFound());
    }


    public void deleteAll(String type) {

        DeleteResponse response = client.prepareDelete()
                .setIndex(INDEX)
                .setType(type).execute().actionGet();
        logger.info("删除es索引->flag:{}", response.isFound());
    }

    /**
     * 查询圈子
     *
     * @param text
     * @return
     */
    public JSONObject searchGroup(String text, int from, int size) throws IOException {

        QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery("name",text);
        SearchResponse response = client.prepareSearch("bq_cms")
                .setTypes("group")
                .setQuery(queryBuilder)
//                .setMinScore(0.5F)
                .setFrom(from)
                .setSize(size)
                .get();

        SearchHits hits = response.getHits();

        logger.info("hits:{}", JsonKit.toJson(hits));
//        logger.info("aggr:{}", JsonKit.toJson(response.getAggregations()));
//        logger.info("fact:{}", JsonKit.toJson(response.getFacets()));
//        logger.info("failed:{}", JsonKit.toJson(response.getFailedShards()));
//        logger.info("ss:{}", JsonKit.toJson(response.getSuccessfulShards()));
//        logger.info("sug:{}", JsonKit.toJson(response.getSuggest()));
//        logger.info("context:{}", JsonKit.toJson(response.getContext()));
//
//
//        logger.info("------------------------------");
//
////        String url = host + "/bq_cms/group/_search?size=" + size + "&from=" +
////                from + "&q=name:" + URLEncoder.encode(text, "UTF-8");

       /**
        String url_str = host + "/bq_cms/group/_search";

        Map<String, Object> tc = new HashMap<>();
        tc.put("name", text);

        Map<String, Map<String, Object>> term = new HashMap<>();
        term.put("term", tc);

        Map<String, Object> query = new HashMap<>();
        query.put("query", term);
        query.put("from", from);
        query.put("size", size);
//
////        JSONObject termContent = new JSONObject();
////        termContent.put("name", text);
////
////        JSONObject term = new JSONObject();
////        term.put("term",termContent);
////
////        JSONObject query = new JSONObject();
////        query.put("query", term.toJSONString());
////        query.put("from", from);
////        query.put("size", size);
        String query_str = JSON.toJSONString(tc);
//        String query_str = "{\"size\":" + size + ",\"query\":{\"term\":{\"name\":\"" + text + "\"}},\"from\":" + from + "}";
        logger.info("query->{}", query_str);
        URL url = new URL(url_str);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setUseCaches(false);
        con.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());

        IOUtils.write(query_str.getBytes(), writer, Charset.forName("utf-8"));
        InputStream is = con.getInputStream();
        byte[] bt = IOUtils.toByteArray(is);
        is.close();
        writer.close();
        String str = new String(bt, Charset.forName("utf-8"));
        logger.info("ret->:{}", str);
//        JSONObject jsonObject = DoRequest.doGet(url);
//                JSONObject jsonObject = DoRequest.doPut(url, query);
        JSONObject jsonObject = JSON.parseObject(str);
        JSONArray objects = new JSONArray();
        JSONArray array = jsonObject.getJSONObject("hits").getJSONArray("hits");
        int total = jsonObject.getJSONObject("hits").getIntValue("total");
        if (!array.isEmpty()) {
            for (int i = 0; i < array.size(); i++) {
                objects.add(array.getJSONObject(i).getJSONObject("_source"));
            }
        }**/
        long total = hits.totalHits();
        List<Map<String, Object>> list = new ArrayList<>();
        SearchHit[] searchHits = hits.hits();
        for (SearchHit hit : searchHits) {
            list.add(hit.getSource());
        }
        JSONObject object = new JSONObject();
        object.put("total", total);
        object.put("list", list);
        return object;
    }


    /**
     * 查询帖子
     *
     * @param text
     * @return
     */
    public JSONObject searchPost(String text, Object group_id, int from, int size, String user_id) throws Exception {

        SearchRequestBuilder sb = client.prepareSearch("bq_cms")
                .setTypes("post")
//                .setMinScore(0.5F)
                .setFrom(from)
                .setSize(size);

        QueryBuilder qb1 = QueryBuilders.matchPhraseQuery( "title", text);
        QueryBuilder qbx = QueryBuilders.matchPhraseQuery("content",text);
        QueryBuilder qb = QueryBuilders.boolQuery()
                .should(qbx)
                .should(qb1);
        sb.setQuery(qb1);
        if (group_id != null) {
            QueryBuilder qb2 = QueryBuilders.termsQuery("group_id", group_id);
            QueryBuilder bq = QueryBuilders.boolQuery()
                    .must(qb2)
                    .should(qb);
            sb.setQuery(bq);
        } else {
            sb.setQuery(qb);
        }
        SearchResponse response = sb.execute()
                .actionGet();
        SearchHits hits = response.getHits();
//
//        String url = host + "/bq_cms/post/_search?q=title:"
//                + URLEncoder.encode(text, "UTF-8")
//                + "&q=content:" + URLEncoder.encode(text, "UTF-8")
//                + "&size=" + size + "&from=" + from;
//        if (group_id != null) {
//            url += "&q=group_id:" + group_id;
//        }
//
//
//        JSONObject jsonObject = DoRequest.doGet(url);
//        JSONArray objects = new JSONArray();
//        JSONArray array = jsonObject.getJSONObject("hits").getJSONArray("hits");
//        int total = jsonObject.getJSONObject("hits").getIntValue("total");
//        if (!array.isEmpty()) {
//            for (int i = 0; i < array.size(); i++) {
//                objects.add(array.getJSONObject(i).getJSONObject("_source"));
//            }
//        }
//        if (CollectionUtils.isNotEmpty(objects)) {
//            for (int i = 0; i < objects.size(); i++) {
////                //获取所有可用标签
////                //因为存在精华标签待审核，所有不能获取所有的标签
////                List<Record> tags = PostTags.dao.findTagsByPost(record.getLong("id"));
////                if (CollectionUtils.isNotEmpty(tags)) {
////                    record.set("tags", tags);
////                }
//                JSONObject obj = objects.getJSONObject(i);
//
//                obj.put("title", Sensitive.dao.filterSensitive(obj.getString("title")));
//                obj.put("content", Sensitive.dao.filterSensitive(obj.getString("content")));
//                obj.put("intro", Sensitive.dao.filterSensitive(obj.getString("intro")));
//
//                //转换图片
//                obj.put("thumb_url", Post.dao.parseThumbUrl(obj.getString("thumb_url"), false));
//                //是否点赞
//                if (user_id != null) {
//                    obj.put("praised", PostLike.dao.liked(obj.getLong("id"), user_id));
//                } else {
//                    obj.put("praised", false);
//                }
//
//                Group group = Group.dao.findById(obj.getLong("group_id"));
//                obj.put("group_name", group.getStr("name"));
//                obj.put("group_icon", group.getStr("thumb_url"));
//
//                User user = User.dao.findByUserId(obj.getString("user_id"));
//                if (user != null) {
//                    obj.put("user_name", user.getStr("user_name"));
//                    obj.put("avatar_url", user.getStr("avatar_url"));
//                    obj.put("user_name", user.getStr("user_name"));
//                }
//
//                if (obj.getLong("type_id") != null) {
//                    PostType type = PostType.dao.findById(obj.getLong("type_id"));
//                    if (type != null) {
//                        obj.put("type_name", type.getStr("type_name"));
//                    }
//                }
//
//            }
//        }
        long total = hits.totalHits();
        logger.info("total:{}", total);
        List<Map<String, Object>> list = new ArrayList<>();
        SearchHit[] searchHits = hits.hits();
        for (SearchHit hit : searchHits) {
            JSONObject hitObj = JSON.parseObject(JsonKit.toJson(hit));
            JSONObject source = hitObj.getJSONObject("source");
//            logger.info("source:{}", source.toJSONString());
            //转换图片
            source.put("thumb_url", Post.dao.parseThumbUrl(source.getString("thumb_url"), false));
            //是否点赞
            if (user_id != null) {
                source.put("praised", PostLike.dao.liked(source.getLong("id"), user_id));
            } else {
                source.put("praised", false);
            }

            Group group = Group.dao.findById(source.get("group_id"));
            source.put("group_name", group.getStr("name"));
            source.put("group_icon", group.getStr("thumb_url"));


            User user = User.dao.findByUserId(source.getString("user_id"));
            if (user != null) {
                source.put("user_name", user.getStr("user_name"));
                source.put("avatar_url", user.getStr("avatar_url"));
                source.put("user_name", user.getStr("user_name"));
            }

            if (source.get("type_id") != null) {
                PostType type = PostType.dao.findById(source.get("type_id"));
                if (type != null) {
                    source.put("type_name", type.getStr("type_name"));
                }
            }
            list.add(source);
        }
        JSONObject object = new JSONObject();
        object.put("total", total);
        object.put("list", list);
        return object;
    }


}
