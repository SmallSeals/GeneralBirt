package com.epo.report.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class TestController {
    
    @RequestMapping(value="/index",method=RequestMethod.GET)
    public Map<String,Object> index(String name){
      Map<String, Object> map = new HashMap<String, Object>();
      Properties properties =  System.getProperties();
      for(Entry<Object, Object>  x : properties.entrySet()){
          map.put(x.getKey().toString(), x.getValue().toString());
      }
      return map;
    }
       
    
    @RequestMapping(value="/index3",method=RequestMethod.GET)
    public String index3(String name){
      return "Hello world!" + name;  
    }
    
    @RequestMapping(value="/index2",method=RequestMethod.POST)
    public void index2(@RequestParam(name="type",required=true) String type){
    }


}
