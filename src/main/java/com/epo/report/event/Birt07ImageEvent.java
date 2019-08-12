package com.epo.report.event;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.ImageEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IImageInstance;
import org.eclipse.birt.report.engine.extension.IRowSet;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Birt07ImageEvent extends ImageEventAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Birt07ImageEvent.class);

    @Override
    public void onCreate(IImageInstance imageInstance, IReportContext reportContext) throws ScriptException {
        imageInstance.setAltText("");
        Object pciUrlObject = imageInstance.getRowData().getColumnValue("pic_url");
        if (pciUrlObject != null && !"".equals(pciUrlObject)) {
            try {
               
//                @SuppressWarnings("serial")
                ArrayList<String> imgTypeList = new ArrayList<String>(){{
                    add("png");
                    add("PNG");
//                    add("jpg");
//                    add("JPG");
//                    add("jpeg");
//                    add("bmp");
                }};
                String imgPath = pciUrlObject.toString().substring(0,pciUrlObject.toString().lastIndexOf(".") );
                String imgSuffix = pciUrlObject.toString().substring(pciUrlObject.toString().lastIndexOf(".")+1 );
//                
                InputStream inputStream = null;
                
                URL url = new URL(pciUrlObject.toString());
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                
                int responseCode = urlConnection.getResponseCode();
                if(responseCode == 404 || responseCode == 500 ){
                    if(imgTypeList.indexOf(imgSuffix)>0){
                        imgTypeList.remove(imgSuffix);
                    }
                    for(int i=0;i < imgTypeList.size();i++){
                       
                        String newImgPath  =  imgPath + "." + imgTypeList.get(i);
                        URL urlImg = new URL(newImgPath);
                        HttpURLConnection imgUrlConnection = (HttpURLConnection)urlImg.openConnection();
                        int imgResponseCode = imgUrlConnection.getResponseCode();
                        if(imgResponseCode == 404 || imgResponseCode == 500 ){
                            imgTypeList.remove(i);
                            i--;
                        }else {
                            imageInstance.setURL(newImgPath);
                            inputStream = imgUrlConnection.getInputStream();
                            break;
                        }
                    }
                }else {
                    inputStream = urlConnection.getInputStream();
                }
               
                
                    
                    
                int imgHeight = 0;
                int imgWidth = 0;
                
                if(inputStream != null){

                    BufferedImage bufferedImage =   ImageIO.read(inputStream);
//                    ColorModel color = bufferedImage.getColorModel();  
//                    logger.info("图片深度：{}",color.getPixelSize());
                    imgHeight = bufferedImage.getHeight();
                    imgWidth =  bufferedImage.getWidth();
                    
                    String reportImageWidth = imageInstance.getWidth(); // 模板上面设置的宽度和单位
                    String reportImageHeight = imageInstance.getHeight(); // 模板上面设置的高度和单位
                    String unitWidth = "";  // 模板上面设置的宽度的单位
                    String unitHeight = "";  // 模板上面设置的高度的单位
                    Integer w = 0;
                    Integer h = 0;
                    
                    String unitFind = "(%|cm|em|ex|in|mm|pc|pt|px)$";
                    Pattern unitPattern = Pattern.compile(unitFind);
                    Matcher unitWMatch = unitPattern.matcher(reportImageWidth);
                    if (unitWMatch.find()) {
                        unitWidth = unitWMatch.group();
                    }
                    
                    Matcher unitHMatch = unitPattern.matcher(reportImageHeight);
                    if (unitHMatch.find()) {
                        unitHeight = unitHMatch.group();
                    }
                    
                    
                    
                    Double   dpi  = (float) 96/25.4; // 分辨率
                    Double _w = Double.parseDouble(reportImageWidth.replace(unitWidth, ""));// 模板设置图片的宽度
                    Double _h = Double.parseDouble(reportImageHeight.replace(unitHeight, ""));// 模板设置图片的高度
                    
                    w = new BigDecimal(_w).multiply(new BigDecimal(dpi)).setScale(0, BigDecimal.ROUND_DOWN).intValue();  // 模板设置图片的宽度mm 转换成像素
                    h = new BigDecimal(_h).multiply(new BigDecimal(dpi)).setScale(0, BigDecimal.ROUND_DOWN).intValue(); // 模板设置图片的高度 mm 转换成像素
                    //Math.round(Double.parseDouble(reportImageHeight.replace(unitHeight, "")) * dpi );
                        
                    logger.info("模板的图片宽度：{};高度{}",w,h);
                    
//                    BufferedImage small = Scalr.resize(bufferedImage,
//                            Method.ULTRA_QUALITY,
//                            Mode.AUTOMATIC,
//                            w, h,
//                            Scalr.OP_ANTIALIAS);
                   
                    BufferedImage small = this.resize(bufferedImage, w, h);
                    logger.info("缩放后的图片宽度：{};高度{}",small.getWidth(),small.getHeight());
                    
//                    if(imgHeight< imgWidth * 300/300){
//                        imageInstance.setWidth(300+"px");
//                        imageInstance.setHeight((imgHeight*300/imgWidth)+"px");
//                    }else{
//                        imageInstance.setHeight(300+"px");
//                        imageInstance.setWidth(imgWidth*300/h+"px");
//                    }
                    
                    
                    
                    imageInstance.setWidth(small.getWidth()+"px");
                    imageInstance.setHeight(small.getHeight()+"px");
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("图片地址：{}在有问题，无法访问！", pciUrlObject.toString());
            }
        }
    }
    
    
    private BufferedImage resize(BufferedImage image, int width, int height) {
        Mode translationMode = Mode.AUTOMATIC;
        if (image.getWidth() < width && image.getHeight() < height) {
            return image;
        } else if (image.getWidth() < width) {
            translationMode = Mode.FIT_TO_HEIGHT;
        } else if (image.getHeight() < height) {
            translationMode = Mode.FIT_TO_WIDTH;
        } else {
            float wRatio = ((float)width / (float)image.getWidth());
            float hRatio = ((float)height / (float)image.getHeight());
            
//            translationMode = hRatio < wRatio ?    Mode.FIT_TO_WIDTH : Mode.FIT_TO_HEIGHT  ;
            translationMode = width > width ?    Mode.FIT_TO_WIDTH : Mode.FIT_TO_HEIGHT  ;
        }
        BufferedImage bufferedImage = Scalr.resize(image, Scalr.Method.SPEED, translationMode,width, height,Scalr.OP_ANTIALIAS);
        
        return bufferedImage;
    }
    
}
