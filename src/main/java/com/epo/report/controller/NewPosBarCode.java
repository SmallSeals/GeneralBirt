package com.epo.report.controller;

import com.epo.report.service.ReportCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class NewPosBarCode {

    private  final  static Logger  logger = LoggerFactory.getLogger(NewPosBarCode.class);


    @Autowired
    private ReportCommonService reportCommonService;


    @RequestMapping("/generateBarcode/index")
    public ModelAndView index(){
        ModelAndView modelAndView = new ModelAndView("newPosBarcode/index");
        return modelAndView;
    }

    @RequestMapping("/showBarCodePage")
    public  Map<String,Object> showBarCodePage(@RequestParam(value = "barcode",required = true) String barcode,
                                                                        @RequestParam(value = "count",required = true) Long  count,
                                                                        HttpServletResponse response, HttpServletRequest request) throws  Exception{


        if(count<=0){
            throw new Exception("次数必须大于是0整数");
        }
        int  rowSize = 0;
        if(count==1){
            rowSize = 1;
            Map<String,Object> map = new HashMap<>();
            List<Map<String,Object>> rowData = new ArrayList<>();
            HashMap<String,Object> barCodeData = new HashMap<>(2);
            barCodeData.put("barcode1",barcode);
            barCodeData.put("barcode2",null);
            rowData.add(barCodeData);
            map.put("data",rowData);
            return map;
        }else{
            rowSize = (int)(count/2);
            if(count%2 != 0 ){
                rowSize ++;
            }
            String preBarcode  = barcode.substring(0,2);
            Long subBarcode = Long.parseLong(barcode.substring(2));
            String subBarcodeStr = subBarcode.toString();
            int barCodeLength = barcode.length()-2;
            String zeroStr = "";
            for(int x=0;x<barCodeLength-subBarcodeStr.length();x++){
                zeroStr+='0';
            }
            preBarcode+=zeroStr;
            int j = 0;
            Map<String,Object> map = new HashMap<>();
            List<Map<String,Object>> rowData = new ArrayList<>();
            for(int i=0;i< rowSize;i++){
                HashMap<String,Object> barCodeData = new HashMap<>(2);
                if(j>count){
                    break;
                }

                barCodeData.put("barcode1",preBarcode + (subBarcode+j));
                j++;
                barCodeData.put("barcode2",generBarCode(j,count,preBarcode,subBarcode));
                rowData.add(barCodeData);
                j++;
        }
            map.put("data",rowData);
            return map;
        }

//        reportCommonService.generateReport("pdf",resultList,dataMap,"demo.rptdesign",response,request);
    }

    private static String  generBarCode(int j,Long count,String preBarcode,Long subBarcode){
        String  newBarCode = null;
        if(j<=count){
            newBarCode = preBarcode + (subBarcode+j);
        }
        return newBarCode;
    }
}
