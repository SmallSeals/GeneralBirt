package com.epo.report.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IExcelRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import uk.co.spudsoft.birt.emitters.excel.ExcelEmitter;

@Service
public class ReportCommonService implements ApplicationContextAware  {
    
    private  static final Logger logger = LoggerFactory.getLogger(ReportCommonService.class);
            
    @Autowired
    private ServletContext servletContext;
    
    private ApplicationContext context;
    
    private IReportEngine birtEngine;
    
   
    
    
    private static final String IMAGE_FOLDER = "/images";
    private static final String REPORTS_FOLDER = "/reports";
    
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void initialize() throws BirtException {
        EngineConfig config = new EngineConfig();
        config.getAppContext().put("spring", this.context);
        Platform.startup(config);
        IReportEngineFactory factory = (IReportEngineFactory) Platform
                .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        birtEngine = factory.createReportEngine(config);
    }

    
    /**
     * 根据参数生成birt 报表
     * @param format  生成报表类型  thml,pdf,xls
     * @param resultList sap 传递过来的数据
     * @param reportParamsMap  
     * @param reportPath 报表路径
     * @param response   
     * @param request
     * @throws EngineException 
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public void generateReport(String  format, List<Map<String, List<Map<String, Object>>>> resultList, Map<String, Object> reportParamsMap, String reportPath, HttpServletResponse response,
            HttpServletRequest request) throws Exception   {
        response.setCharacterEncoding("UTF-8");
        IRunAndRenderTask runAndRenderTask = null ;
        try {
        reportPath =  servletContext.getRealPath(REPORTS_FOLDER)+"/" + reportPath ;
        reportPath = reportPath.replace("/", System.getProperty("file.separator")).replace("\\", System.getProperty("file.separator"));
        
        IReportRunnable report  = birtEngine.openReportDesign(reportPath);
        
        runAndRenderTask = birtEngine.createRunAndRenderTask(report);
        
        IRenderOption options = new RenderOption();
        HTMLRenderOption htmlOptions = null;
        EXCELRenderOption excelRenderOption = null;
        switch (format) {
        case "html":
            response.setContentType(birtEngine.getMIMEType("html"));
             htmlOptions = new HTMLRenderOption(options);
            htmlOptions.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);
            break;
        case "xls":
            response.setContentType(" application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "inline; filename=epo.xlsx");
            
              excelRenderOption = new EXCELRenderOption(); 
             excelRenderOption.setOutputFormat("xlsx");
             excelRenderOption.setOption( IExcelRenderOption.OFFICE_VERSION, "office2007");
            excelRenderOption.setOption(ExcelEmitter.SINGLE_SHEET_PAGE_BREAKS ,"true");
            options.setOption(ExcelEmitter.DISPLAYROWCOLHEADINGS_PROP, false);
             excelRenderOption.setOption(IRenderOption.EMITTER_ID,"uk.co.spudsoft.birt.emitters.excel.XlsxEmitter");

            break;
        default:
            logger.info(" pdf contentType : {} ",birtEngine.getMIMEType("pdf"));
            response.setContentType(birtEngine.getMIMEType("pdf")+";charset=UTF-8");
             htmlOptions = new HTMLRenderOption(options);
            htmlOptions.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_PDF);
            break;
        }
        
        
        if("html".equals(format) ||  "pdf".equals(format)){
            htmlOptions.setImageHandler(new HTMLServerImageHandler());
            htmlOptions.setBaseImageURL(request.getContextPath() + IMAGE_FOLDER);
            htmlOptions.setImageDirectory(servletContext.getRealPath(IMAGE_FOLDER));
            runAndRenderTask.setRenderOption(htmlOptions);
        }else if("xls".equals(format)){
            runAndRenderTask.setRenderOption(excelRenderOption);
        }
        
       
        if (resultList != null && !resultList.isEmpty()) {
            for(Map<String, List<Map<String, Object>>> map : resultList){
                for( Entry<String, List<Map<String, Object>>> dataMap : map.entrySet()){
//                    System.out.println(dataMap.getKey());
//                    System.out.println(dataMap.getValue().toString());
                    request.setAttribute(dataMap.getKey(), dataMap.getValue());
                }
            }
        }
        
        
        runAndRenderTask.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST, request);
        
        
        if(!reportParamsMap.isEmpty()){
            runAndRenderTask.setParameterValues(reportParamsMap);
        }
       
        
       
        if("xls".equals(format)){
            excelRenderOption.setOutputStream(response.getOutputStream());
        }else {
            htmlOptions.setOutputStream(response.getOutputStream());
        }
        
        runAndRenderTask.run();
        }catch (Exception e) {
           e.printStackTrace();
           throw  new Exception(e.getMessage(),e);
        } finally {
            runAndRenderTask.close();
        }
    }
    
    
    
    /**
     * 根据参数生成birt 报表  此方法是把json 数据放到birt 模板内部进行解析
     * @param format  生成报表类型  thml,pdf,xls
     * @param resultMap SRM 传递过来的数据
     * @param reportParamsMap  
     * @param reportPath 报表路径
     * @param response   
     * @param request
     * @throws EngineException 
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public void innerJsonGenerateReport(String  format, Map<String, Object> resultMap, Map<String, Object> reportParamsMap, String reportPath, HttpServletResponse response,
            HttpServletRequest request) throws Exception   {
        response.setCharacterEncoding("UTF-8");
        IRunAndRenderTask runAndRenderTask = null ;
        try {
        reportPath =  servletContext.getRealPath(REPORTS_FOLDER)+"/" + reportPath ;
        reportPath = reportPath.replace("/", System.getProperty("file.separator")).replace("\\", System.getProperty("file.separator"));
        
        IReportRunnable report  = birtEngine.openReportDesign(reportPath);
        
        runAndRenderTask = birtEngine.createRunAndRenderTask(report);
        
        IRenderOption options = new RenderOption();
        HTMLRenderOption htmlOptions = null;
        EXCELRenderOption excelRenderOption = null;
        switch (format) {
        case "html":
            response.setContentType(birtEngine.getMIMEType("html"));
             htmlOptions = new HTMLRenderOption(options);
            htmlOptions.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);
            break;
        case "xls":
            response.setContentType(" application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "inline; filename=epo.xlsx");
            
              excelRenderOption = new EXCELRenderOption(); 
             excelRenderOption.setOutputFormat("xlsx");
             excelRenderOption.setOption( IExcelRenderOption.OFFICE_VERSION, "office2007");
            excelRenderOption.setOption(ExcelEmitter.SINGLE_SHEET_PAGE_BREAKS ,"true");
            options.setOption(ExcelEmitter.DISPLAYROWCOLHEADINGS_PROP, false);
             excelRenderOption.setOption(IRenderOption.EMITTER_ID,"uk.co.spudsoft.birt.emitters.excel.XlsxEmitter");

            break;
        default:
            logger.info(" pdf contentType : {} ",birtEngine.getMIMEType("pdf"));
            response.setContentType(birtEngine.getMIMEType("pdf")+";charset=UTF-8");
             htmlOptions = new HTMLRenderOption(options);
            htmlOptions.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_PDF);
            break;
        }
        
        
        if("html".equals(format) ||  "pdf".equals(format)){
            htmlOptions.setImageHandler(new HTMLServerImageHandler());
            htmlOptions.setBaseImageURL(request.getContextPath() + IMAGE_FOLDER);
            htmlOptions.setImageDirectory(servletContext.getRealPath(IMAGE_FOLDER));
            runAndRenderTask.setRenderOption(htmlOptions);
        }else if("xls".equals(format)){
            runAndRenderTask.setRenderOption(excelRenderOption);
        }
        
       
        if (resultMap != null && !resultMap.isEmpty()) {
            for( Entry<String,  Object> dataMap : resultMap.entrySet()){
                    System.out.println(dataMap.getKey());
                    System.out.println(dataMap.getValue().toString());
            	request.setAttribute(dataMap.getKey(), dataMap.getValue());
            }
            
        }
        
        
        runAndRenderTask.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST, request);
        
        
        if(!reportParamsMap.isEmpty()){
            runAndRenderTask.setParameterValues(reportParamsMap);
        }
       
        
       
        if("xls".equals(format)){
            excelRenderOption.setOutputStream(response.getOutputStream());
        }else {
            htmlOptions.setOutputStream(response.getOutputStream());
        }
        
        runAndRenderTask.run();
        }catch (Exception e) {
           e.printStackTrace();
           throw  new Exception(e.getMessage(),e);
        } finally {
            runAndRenderTask.close();
        }
    }
}
