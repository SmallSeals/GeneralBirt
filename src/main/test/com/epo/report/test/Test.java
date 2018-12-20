package com.epo.report.test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

public class Test {

    public static void main(String[] args) throws Exception {
//        HttpHost target = new HttpHost("10.12.203.6", 8011, "http");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("Moco_ErpPos01", "soaosb01"));
       
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build(); 
        
        
        
        
        try {

//            AuthCache authCache = new BasicAuthCache();
//            BasicScheme basicAuth = new BasicScheme();
//            authCache.put(target, basicAuth);
//            HttpClientContext localContext = HttpClientContext.create();
//            localContext.setAuthCache(authCache);

            HttpPost   httpPost  = new HttpPost ("http://10.12.203.6:8011/CrmSB/Sell/Order/ProxyServices/CrmOrderCreateRestfulProxy");
            
            JSONObject jsonEsbInfo = new JSONObject();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime time = LocalDateTime.now();
            jsonEsbInfo.put("instId", "string");
            jsonEsbInfo.put("requestTime",df.format(time));
            jsonEsbInfo.put("attr1", "string");
            jsonEsbInfo.put("attr2", "string");
            jsonEsbInfo.put("attr3", "string");
            
            JSONObject jsonFinal = new JSONObject();
            jsonFinal.put("esbInfo", jsonEsbInfo);
            
            String jsonString = "{\"orderstatus\":\"30\",\"iffullrefund\":\"True\",\"remark\":\"测试测试\",\"/salesorderid@salesorderpayment\":[{\"amount\":\"-999\",\"salesorderid\":\"GZ20100101-SA008502543-1\",\"currency\":\"CNY\",\"paydetailid\":\"8743223\",\"paytype\":\"现金(CASH)\"}],\"amountafterdiscount\":\"-999\",\"posno\":\"\",\"sourcesystemid\":\"10\",\"employeeid\":\"\",\"/salesorderid@salesorderdetail\":[{\"linediscount\":\"1\",\"originalorderitemid\":\"\",\"productcode\":\"MA181PAT107R35M\",\"quantity\":\"-1\",\"colorcode\":\"R35\",\"amountafterdiscount\":\"-999\",\"sizecode\":\"M\",\"unitprice\":\"999\",\"originalamount\":\"-999\",\"unit\":\"\",\"discountamount\":\"0\",\"price\":\"999\",\"orderdetailno\":\"21211495\",\"salesorderid\":\"GZ20100101-SA008502543-1\",\"orderdtadddate\":\"2018-07-18 14:02:54\",\"currency\":\"CNY\",\"pluicode\":\"MA181PAT107\",\"coupondiscount\":\"0\",\"actualpay\":\"-999\"}],\"ordersubmitdate\":\"2018-07-18 14:03:09\",\"shippingfee\":\"0\",\"discountamount\":\"0\",\"brandid\":\"10\",\"nopoint\":\"False\",\"currency\":\"CNY\",\"salesguideid\":\"\",\"originalsalesorderno\":\"GZ20100101-SA008502543\",\"actualpay\":\"-999\",\"couponamt\":\"0\",\"quantity\":\"-1\",\"orderadddate\":\"2018-07-18 14:02:54\",\"originalsalesorderid\":\"8147978\",\"ifstaff\":\"False\",\"ordertypeid\":\"20\",\"salesorderno\":\"GZ20100101-SA008502543-1\",\"orderstoreid\":\"A002\",\"membercardid\":\"\",\"ordertime\":\"2018-07-18 00:00:00\",\"ifsviporder\":\"False\",\"originalamount\":\"-999\",\"ifonline\":\"False\",\"orderdiscount\":\"1\",\"deliverytime\":\"2018-07-18 00:00:00\",\"salesorderid\":\"8147979\",\"memberid\":\"\"}";
            JSONObject jsonParam = JSONObject.parseObject(jsonString);
            
            jsonFinal.put("requestInfo", jsonParam);
            
            httpPost.setEntity(new StringEntity(jsonFinal.toJSONString(),"UTF-8"));
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse  res =  httpclient.execute(httpPost);  
            String content = EntityUtils.toString(res.getEntity(),"UTF-8");  
            
            System.out.println(content);
          
        } finally {
             httpclient.close();
        }
    }

}
