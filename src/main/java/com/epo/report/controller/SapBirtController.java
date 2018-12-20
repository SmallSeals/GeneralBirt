package com.epo.report.controller;

import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.epo.report.service.ReportCommonService;

@RestController
@RequestMapping("/sap_birt")
public class SapBirtController {
    
    
    private  static final Logger logger = LoggerFactory.getLogger(SapBirtController.class);
    
    @Autowired
    private ReportCommonService reportCommonService;
    
    
    /**
     * 显示birt 报表
     * @param json
     * @param reportPath
     * @param response
     * @param request
     * @throws Exception 
     */
    @RequestMapping("/show")
    public void show(@RequestParam("format") String format, @RequestParam(value = "json", required = true) String json,
            @RequestParam("reportPath") String reportPath,HttpServletResponse response,HttpServletRequest request) throws Exception {
//        System.setProperty("sun.jnu.encoding", "GBK");
        logger.info("report json :{} ",json);
        JSONObject jsonObject = JSONObject.parseObject(json);
        if(jsonObject.containsKey("method_name")){
            jsonObject.remove("method_name");
        }
        if(jsonObject.containsKey("proc_type")){
            jsonObject.remove("proc_type");
        }
        if(jsonObject.containsKey("user_code")){
            jsonObject.remove("user_code");
        }
        if(jsonObject.containsKey("user_pwd")){
            jsonObject.remove("user_pwd");
        }
        List<Map<String, List<Map<String,Object>>>> resultList = new ArrayList<Map<String, List<Map<String,Object>>>>();
        Map<String,Object> reportParamsMap = new HashMap<String, Object>();
        if(!jsonObject.isEmpty()){
            for(Entry<String, Object> entry: jsonObject.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                JSONArray entryValue = null;
                if(value instanceof JSONArray){
                    entryValue = (JSONArray)entry.getValue();
                }else{
                    reportParamsMap.put(key, value);
                }
                
                if(entryValue != null){
                    List<Map<String, Object>> list = JSON.parseObject(entryValue.toJSONString(), new TypeReference<List<Map<String,Object>>>(){});
//                    System.out.println(list);
                    Map<String,List<Map<String,Object>>> dataMap = new HashMap<String, List<Map<String,Object>>>();
                    dataMap.put(key, list);
                    resultList.add(dataMap);
                }
                
            }
        }
        reportCommonService.generateReport(format,resultList,reportParamsMap,reportPath,response,request);

    }
    
    @RequestMapping("/customerFunds")
    public void  customerFunds(@RequestParam("format") String format, @RequestParam(value = "customerCode", required = true) String customerCode,
            @RequestParam(value = "datefrom", required = true) String datefrom,
            @RequestParam(value = "dateto", required = true) String dateto,
            @RequestParam(value = "bukrs", required = true) String bukrs,
            HttpServletResponse response,HttpServletRequest request) throws Exception {
        Properties properties =  new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("SapUserPassword.properties"));
        HttpHost target = new HttpHost(properties.getProperty("customerFunds.sap.hostname"), Integer.parseInt(properties.getProperty("customerFunds.sap.port")), "http");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(properties.getProperty("customerFunds.sap.username"), properties.getProperty("customerFunds.sap.password")));
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build(); 
        
        
        try {

            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(target, basicAuth);

            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            HttpPost httpPost = new HttpPost(properties.getProperty("customerFunds.sap.url"));
            
            JSONObject jsonObject = new JSONObject();
            
            jsonObject.put("BUKRS",bukrs);
            jsonObject.put("KUNNR", customerCode);
            jsonObject.put("DATAB", datefrom);
            jsonObject.put("DATBI", dateto);
            
            httpPost.setEntity(new StringEntity(jsonObject.toJSONString(),"UTF-8"));
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse res =  httpclient.execute(target,httpPost,localContext);  
            String content = EntityUtils.toString(res.getEntity(),"UTF-8");  
            
            logger.debug("客户资金账报表返回的数据:{}", content);
            
            JSONObject restltJson = JSONObject.parseObject(content);
            if(!restltJson.getJSONObject("return").isEmpty()){
             JSONObject returnJsonObject =    restltJson.getJSONObject("return");
             if("S".equals(returnJsonObject.get("msgty"))){
                 HashMap<String, Object> headerMap = new HashMap<String, Object>();
                 JSONObject headerJsoNobjecJsonObject  = restltJson.getJSONObject("header");
                 
                 headerMap.putAll(headerJsoNobjecJsonObject);
//                 headerMap.put("userName", "张三");
                
                
                 
                 List<Map<String, List<Map<String,Object>>>> resultList = new ArrayList<Map<String, List<Map<String,Object>>>>();
                 
                 JSONArray  detail1JsonArray = restltJson.getJSONArray("detail1");
                 JSONArray  detail2JsonArray = restltJson.getJSONArray("detail2");
                 JSONArray  detail3JsonArray = restltJson.getJSONArray("detail3");
                 JSONArray  detail4JsonArray = restltJson.getJSONArray("detail4");
                 
                 List<Map<String, Object>> detail1List = JSON.parseObject(detail1JsonArray.toJSONString(), new TypeReference<List<Map<String,Object>>>(){});
                 List<Map<String, Object>> detail2List = JSON.parseObject(detail2JsonArray.toJSONString(), new TypeReference<List<Map<String,Object>>>(){});
                 List<Map<String, Object>> detail3List = JSON.parseObject(detail3JsonArray.toJSONString(), new TypeReference<List<Map<String,Object>>>(){});
                 List<Map<String, Object>> detail4List = JSON.parseObject(detail4JsonArray.toJSONString(), new TypeReference<List<Map<String,Object>>>(){});
                 Map<String,List<Map<String,Object>>> dataMap1 = new HashMap<String, List<Map<String,Object>>>();
                 dataMap1.put("detail1", detail1List);
                 resultList.add(dataMap1);
                 Map<String,List<Map<String,Object>>> dataMap2 = new HashMap<String, List<Map<String,Object>>>();
                 dataMap2.put("detail2", detail2List);
                 resultList.add(dataMap2);
                 Map<String,List<Map<String,Object>>> dataMap3 = new HashMap<String, List<Map<String,Object>>>();
                 dataMap3.put("detail3", detail3List);
                 resultList.add(dataMap3);
                 Map<String,List<Map<String,Object>>> dataMap4 = new HashMap<String, List<Map<String,Object>>>();
                 dataMap4.put("detail4", detail4List);
                 resultList.add(dataMap4);
                 
//                 System.out.println(resultList);
                 
                 this.reportCommonService.generateReport(format, resultList, headerMap, "customerMonthly1.rptdesign", response, request);
             }else{
                 logger.error("客户资金账报表返回了数据,但是状态是失败的:{}" ,returnJsonObject.get("msgtx").toString());
             }
            }
            
        } finally {
            httpclient.close();
        }
//        String result = HttpUtil.sendPost("http://CNHQ_PHD:1234567890@erpdev01.mo-co.org:8000/zcustom_service/zfi_statement_c", "", "UTF8");
        
        
    } 
}
