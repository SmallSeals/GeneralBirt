package com.epo.report.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.epo.report.service.ReportCommonService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RestController
@RequestMapping("/srm_birt/")
public class SrmPrintController {

	private static final Logger logger = LoggerFactory.getLogger(SrmPrintController.class);

	@Autowired
	private ReportCommonService reportCommonService;

	private final OkHttpClient client = new OkHttpClient();

	/**
	 * 物料清单打印
	 * 
	 * @throws Exception
	 */
	@RequestMapping("billOfMaterials")
	public void billOfMaterials(@RequestParam(value = "pur_header_id", required = true) Long pur_header_id,
			 HttpServletResponse response,HttpServletRequest request
			)
			throws Exception {
		Properties properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
		String srm_address = properties.getProperty("srm.address");
		String material_header_url = srm_address + properties.getProperty("srm.material_header_url") + "?pur_header_id="
				+ pur_header_id.toString();
		String material_size_url = srm_address + properties.getProperty("srm.material_size_url") + "?pur_header_id="
				+ pur_header_id.toString();
		String material_items_url = srm_address + properties.getProperty("srm.material_items_url") + "?pur_header_id="
				+ pur_header_id.toString();

		// Request.Builder reqBuild = new Request.Builder();
		// HttpUrl.Builder urlBuilder =
		// HttpUrl.parse(material_header_url).newBuilder();
		// urlBuilder.addQueryParameter("pur_header_id",
		// pur_header_id.toString());
		//
		// Builder url = reqBuild.url(urlBuilder.build());
		String material_header_str = null;
		String material_size_str = null;
		String material_items_str = null;
		Request request_header = new Request.Builder().url(material_header_url).build();
		Response response_header = client.newCall(request_header).execute();
		if (response_header.isSuccessful()) {
			material_header_str = response_header.body().string();
			logger.info("\n RM 物料清单打印header json: \n {}", material_header_str.toString());

			Request request_szie = new Request.Builder().url(material_size_url).build();
			Response response_size = client.newCall(request_szie).execute();
			if (response_size.isSuccessful()) {
				material_size_str = response_size.body().string();
				logger.info("\n RM 物料清单打印size json: \n {}", material_size_str.toString());

				Request request_items = new Request.Builder().url(material_items_url).build();
				Response response_items = client.newCall(request_items).execute();
				if (response_items.isSuccessful()) {
					material_items_str = response_items.body().string();
					logger.info(" \n RM 物料清单打印items json: \n {}", material_items_str.toString());

				} else {
					throw new IOException("Unexpected code  " + response_size);
				}

			} else {
				throw new IOException("Unexpected code  " + response_size);
			}

		} else {
			throw new IOException("Unexpected code   " + response_header);
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (material_header_str != null && !"".equals(material_header_str)) {
			JSONObject materialHeaderJsonObject = JSONObject.parseObject(material_header_str);
			
			Object success = materialHeaderJsonObject.get("success");
			if (Boolean.TRUE.equals(success)) {
				JSONObject resultJsonObject = materialHeaderJsonObject.getJSONObject("result");
				JSONObject recordJsonObject = resultJsonObject.getJSONObject("record");
				resultMap.put("header", recordJsonObject);
			} else {
				throw new Exception("获取物料清单header数据错误");
			}
		}
		
		
		if (material_size_str != null && !"".equals(material_size_str)) {
			JSONObject materialSizeJsonObject = JSONObject.parseObject(material_size_str);
			Object success = materialSizeJsonObject.get("success");
			if (Boolean.TRUE.equals(success)) {
				JSONObject resultJsonObject = materialSizeJsonObject.getJSONObject("result");
				JSONArray recordJsonArray = resultJsonObject.getJSONArray("record");
				resultMap.put("szie", recordJsonArray);
			} else {
				throw new Exception("获取物料清单size数据错误");
			}
		}
		
		if (material_items_str != null && !"".equals(material_items_str)) {
			JSONObject materialItemsJsonObject = JSONObject.parseObject(material_items_str);
			Object success = materialItemsJsonObject.get("success");
			if (Boolean.TRUE.equals(success)) {
				JSONObject resultJsonObject = materialItemsJsonObject.getJSONObject("result");
				JSONArray recordJsonArray = resultJsonObject.getJSONArray("record");
				resultMap.put("items", recordJsonArray);
			} else {
				throw new Exception("获取物料清单items数据错误");
			}
		}
		
		HashMap<String,Object> headerMap = new HashMap<String, Object>();
		this.reportCommonService.innerJsonGenerateReport("pdf", resultMap, headerMap, "srm_material.rptdesign", response, request);
		
	}
}
