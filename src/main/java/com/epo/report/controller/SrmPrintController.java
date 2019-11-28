package com.epo.report.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
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
			HttpServletResponse response, HttpServletRequest request) throws Exception {
		Properties properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
		String srm_address = properties.getProperty("srm.address");
		String material_header_url = srm_address + properties.getProperty("srm.material_header_url") + "?pur_header_id="
				+ pur_header_id.toString();
		String material_size_url = srm_address + properties.getProperty("srm.material_size_url") + "?pur_header_id="
				+ pur_header_id.toString()+"&_fetchall=true";
		String material_items_url = srm_address + properties.getProperty("srm.material_items_url") + "?pur_header_id="
				+ pur_header_id.toString()+"&_fetchall=true";

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
				Object object = resultJsonObject.get("record");
				JSONArray recordJsonArray = new JSONArray();
				if (object == null) {
					resultMap.put("szie", recordJsonArray);
				} else {
					if (object instanceof JSONObject) {
						recordJsonArray.add(object);
					} else if (object instanceof JSONArray) {
						recordJsonArray.addAll((JSONArray) object);
					} else {
						throw new Exception("获取物料清单size  record 数据 类型错误");
					}
					// JSONArray recordJsonArray =
					// resultJsonObject.getJSONArray("record");
					resultMap.put("szie", recordJsonArray);
				}
			} else {
				throw new Exception("获取物料清单size数据错误");
			}
		}

		if (material_items_str != null && !"".equals(material_items_str)) {
			JSONObject materialItemsJsonObject = JSONObject.parseObject(material_items_str);
			Object success = materialItemsJsonObject.get("success");
			if (Boolean.TRUE.equals(success)) {
				JSONObject resultJsonObject = materialItemsJsonObject.getJSONObject("result");
				Object object = resultJsonObject.get("record");
				JSONArray recordJsonArray = new JSONArray();
				if (object == null) {
					resultMap.put("items", recordJsonArray);
				} else {

					if (object instanceof JSONObject) {
						recordJsonArray.add(object);
					} else if (object instanceof JSONArray) {
						recordJsonArray.addAll((JSONArray) object);
					} else {
						throw new Exception("获取物料清单items  record 数据 类型错误");
					}

					// JSONArray recordJsonArray =
					// resultJsonObject.getJSONArray("record");
					resultMap.put("items", recordJsonArray);
				}
			} else {
				throw new Exception("获取物料清单items数据错误");
			}
		}

		HashMap<String, Object> headerMap = new HashMap<String, Object>();
		this.reportCommonService.innerJsonGenerateReport("pdf", resultMap, headerMap, "srm_material.rptdesign",
				response, request);

	}
	
	@RequestMapping("gttPrint")
	public void barcode2(@RequestParam(value = "moco_gtt_check_header_id", required = true) Long moco_gtt_check_header_id,
			HttpServletResponse response, HttpServletRequest request) throws Exception {
		Properties properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
		String srm_address = properties.getProperty("srm.address");
		String  gtt_print_header_url = srm_address + properties.getProperty("srm.gtt_print_header_url") + "?moco_gtt_check_header_id="
				+ moco_gtt_check_header_id.toString();
		String gtt_print_items_url = srm_address + properties.getProperty("srm.gtt_print_items_url") + "?moco_gtt_check_header_id="
				+ moco_gtt_check_header_id.toString();
		
		String gtt_print_header_str = null;
		String gtt_print_items_str = null;
		Request request_header = new Request.Builder().url(gtt_print_header_url).build();
		Response response_header = client.newCall(request_header).execute();
		if (response_header.isSuccessful()) {
			gtt_print_header_str = response_header.body().string();
			logger.info("\n SRM GTT  打印header json: \n {}", gtt_print_header_str);
			Request request_items = new Request.Builder().url(gtt_print_items_url).build();
			Response response_items = client.newCall(request_items).execute();
			if (response_items.isSuccessful()) {
				gtt_print_items_str = response_items.body().string();
				logger.info("\n SRM GTT 打印items json: \n {}", gtt_print_items_str);
			} else {
				throw new IOException("Unexpected code  " + response_items);
			}
			
		}else{
			throw new IOException("Unexpected code   " + response_header);
		}
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		
		if (gtt_print_header_str != null && !"".equals(gtt_print_header_str)) {
			JSONObject gttHeaderJsonObject = JSONObject.parseObject(gtt_print_header_str);

			Object success = gttHeaderJsonObject.get("success");
			if (Boolean.TRUE.equals(success)) {
				JSONObject resultJsonObject = gttHeaderJsonObject.getJSONObject("result");
				JSONObject recordJsonObject = resultJsonObject.getJSONObject("record");
				if(recordJsonObject !=null  &&  !recordJsonObject.isEmpty() ){
					String contextPath = request.getContextPath();
				    int  port =  request.getServerPort();
				    String  serverName =  request.getServerName();
				  //TODO  需要根据返回值判断条码根据条码编号访问本地  generateBarcode() 方法
					recordJsonObject.put("port", port);
					recordJsonObject.put("host", serverName);
					recordJsonObject.put("contextPath", contextPath);
					resultMap.put("header", recordJsonObject);
				}else{
					throw new Exception("获取SRM GTT header数据错误");
				}
			} else {
				throw new Exception("获取SRM GTT header数据错误");
			}
		}
		
		if (gtt_print_items_str != null && !"".equals(gtt_print_items_str)) {
			JSONObject gttItemsJsonObject = JSONObject.parseObject(gtt_print_items_str);

			Object success = gttItemsJsonObject.get("success");
			if (Boolean.TRUE.equals(success)) {
				JSONObject resultJsonObject = gttItemsJsonObject.getJSONObject("result");
				Map<String,Object> itmesMap = null;
				JSONArray itmesMapJsonArray = new JSONArray();
				JSONArray recordJsonArray = resultJsonObject.getJSONArray("record");
				if(recordJsonArray != null ){
					for(int i=1;i<=recordJsonArray.size();i++ ){
						int m = i%3;
						int x =  (m==0 ? 3:m);
						if(m ==1 ){
							itmesMap =    new HashMap<String,Object>();
						}
						JSONObject  checkProjectDescObject =  recordJsonArray.getJSONObject(i-1);
						itmesMap.put("check_project_desc_"+x , "√"+checkProjectDescObject.getString("check_project_desc") );
						if(m==0 || i == recordJsonArray.size()){
							Map<String,Object> dataMap = new HashMap<String,Object>();
							dataMap.putAll(itmesMap);
							itmesMapJsonArray.add(dataMap);
						}
					}
				}
				
				if(itmesMapJsonArray.isEmpty()){
					Map<String,Object> dataMap = new HashMap<String,Object>();
					if(itmesMap == null ){
						itmesMap =    new HashMap<String,Object>();
					}
					dataMap.putAll(itmesMap);
					itmesMapJsonArray.add(dataMap);
				}
				resultMap.put("items", itmesMapJsonArray);
			} else {
				throw new Exception("获取SRM GTT items数据错误");
			}
		}
		
		
		HashMap<String, Object> headerMap = new HashMap<String, Object>();
		
		HashMap<String, Object> data = new HashMap<String, Object>();
//		data.put("barcode", "http://localhost:8383/report/srm_birt/generateBarcode?barcode="+ barcode);
//		data.put("barcode", "https://barcode.tec-it.com/barcode.ashx?data="+barcode+"&code=Code39&dpi=96&dataseparator=");
//	    JSONObject  headerJsonObject = JSONObject.parseObject(JSON.toJSON(data).toString());
//	    resultMap.put("header",headerJsonObject);
		this.reportCommonService.innerJsonGenerateReport("pdf", resultMap, headerMap, "demo.rptdesign",response, request);
		
	}
	
	
	@RequestMapping("generateBarcode")
	public void barcode(@RequestParam(value = "barcode", required = true) String barcode,
			HttpServletResponse response, HttpServletRequest request) throws Exception {
		Code39Bean		 barcode39Bean = new Code39Bean();

//		barcode39Bean.setCodeset(Code39Constants.CODESET_B);
		final int dpi = 150;

		// Configure the barcode generator
		// adjust barcode width here
		barcode39Bean.setModuleWidth(UnitConv.in2mm(1.8f / dpi));
		barcode39Bean.setWideFactor(3);
		barcode39Bean.doQuietZone(false);

		// Open output file
		// File outputFile = new File("G:/barcode.png");
		OutputStream outputStream = response.getOutputStream();
		try {
			BitmapCanvasProvider canvasProvider = new BitmapCanvasProvider(outputStream, "image/png", dpi,
					BufferedImage.TYPE_BYTE_BINARY, true, 0);

			barcode39Bean.generateBarcode(canvasProvider, barcode);

			canvasProvider.finish();
		} finally {
			outputStream.close();
		}

		
//		Map<EncodeHintType, Object> hints = new HashMap<>();
//        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//        // 生成二维码矩阵
//        BitMatrix bitMatrix = new MultiFormatWriter().encode(barcode, BarcodeFormat.CODE_39, 150, 100, hints);
//        MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);
//        outputStream.close();
		
	}
	
	
	@RequestMapping("128generateBarcode")
	public void generateBarcode_128(@RequestParam(value = "barcode", required = true) String barcode,
			HttpServletResponse response, HttpServletRequest request) throws Exception {
		Code128Bean		 code128Bean = new Code128Bean();

//		barcode39Bean.setCodeset(Code39Constants.CODESET_B);
		final int dpi = 150;

		// Configure the barcode generator
		// adjust barcode width here
		code128Bean.setModuleWidth(UnitConv.in2mm(1.8f / dpi));
		code128Bean.doQuietZone(false);
		code128Bean.setQuietZone(2);

		// Open output file
		// File outputFile = new File("G:/barcode.png");
		OutputStream outputStream = response.getOutputStream();
		try {
			BitmapCanvasProvider canvasProvider = new BitmapCanvasProvider(outputStream, "image/png", dpi,
					BufferedImage.TYPE_BYTE_BINARY, true, 0);

			code128Bean.generateBarcode(canvasProvider, barcode);

			canvasProvider.finish();
		} finally {
			outputStream.close();
		}

		
//		Map<EncodeHintType, Object> hints = new HashMap<>();
//        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//        // 生成二维码矩阵
//        BitMatrix bitMatrix = new MultiFormatWriter().encode(barcode, BarcodeFormat.CODE_39, 150, 100, hints);
//        MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);
//        outputStream.close();
		
	}
	
	
	
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
        logger.info("report json :{} ",json);
        JSONObject jsonObject = JSONObject.parseObject(json);
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
    
}
