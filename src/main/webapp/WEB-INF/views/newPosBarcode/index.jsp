<%--
  Created by IntelliJ IDEA.
  User: denghao.peng
  Date: 2018/1/18
  Time: 14:34
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>新POS生成条码</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap-theme.min.css">
</head>
<body>
    <div class="container">
        <div class="row">
            <div class="panel panel-primary" >
                <div class="panel-heading">
                    <div class="panel-title">
                        <h4>查询条件</h4>
                    </div>
                </div>
                <div class="panel-body">
                    <form>
                        <div class="row">
                            <div class="col-lg-4 form-group">
                                <label for="code" class="control-label">条码</label>
                                <input type="text" class="form-control" name="code" id="code" autocomplete="off"/>
                            </div>
                            <div class="col-lg-4 form-group">
                                <label for="count" class="control-label">次数</label>
                                <input type="text" class="form-control" name="count" id="count" autocomplete="off"/>
                            </div>

                        </div>
                        <div class="row">
                            <div  class="col-lg-12" >
                                <div class="btn btn-group " role="group" aria-label="...">
                                    <button type="button" class="btn btn-default " id= "generateBarcodeId"><i class="glyphicon glyphicon-barcode"></i>生成条码</button>
                                    <button type="button" class="btn btn-default " id="printBntId"><i class="glyphicon glyphicon-print"></i>打印</button>
                                </div>

                            </div>
                        </div>
                    </form>

                </div>
            </div>
        </div>
        <div class="row" >
            <div class="panel panel-default"  >
                <div class="panel-body "  id="pnlId">
                </div>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/jquery.min.js" type="text/javascript" ></script>
    <script src="${pageContext.request.contextPath}/js/bootstrap.min.js" type="text/javascript" ></script>
    <script src="${pageContext.request.contextPath}/js/JsBarcode.code128.min.js" type="text/javascript" ></script>
    <script src="${pageContext.request.contextPath}/js/jquery.jqprint-0.3.js" type="text/javascript" ></script>
    <script src="http://www.jq22.com/jquery/jquery-migrate-1.2.1.min.js"></script>
    <script src="http://ajax.microsoft.com/ajax/jquery.templates/beta1/jquery.tmpl.min.js"></script>
    <script id="barcodeTmplId" type="text/x-jquery-tmpl">
        <div  id="barcodeRowlId">
            {{each(i,item) data}}
                <div class="row  " style="margin-bottom: 105px;">
                    {{if barcode1}}
                        <div class="col-md-6 col-xs-6 text-center">
                            <div class="row  ">

                                <b>
                                     Carton No.: &nbsp; &nbsp; <input type="text" style="text-align:center;border-left: 0px;border-top: 0px;border-right: 0px;width: 80px;border-bottom-color: #0f0f0f;" >  &nbsp; &nbsp;    Qty:  &nbsp; &nbsp;<input type="text" style="text-align:center;border-left: 0px;border-top: 0px;border-right: 0px;width: 30px;border-bottom-color: #0f0f0f;" >
                                                                     </b>
                            </div>
                            <svg id=b_{{= item.barcode1}} ></svg>
                        </div>
                    {{/if}}
                    {{if item.barcode2}}
                        <div class="col-md-6 col-xs-6  text-center">
                           <div class="row  ">
                                <b>
                                 Carton No.: &nbsp; &nbsp;<input type="text" style="text-align:center;border-left: 0px;border-top: 0px;border-right: 0px;width: 80px;border-bottom-color: #0f0f0f;"  >  &nbsp; &nbsp; Qty:  &nbsp; &nbsp;<input type="text" style="text-align:center;border-left: 0px;border-top: 0px;border-right: 0px;width: 30px;border-bottom-color: #0f0f0f;"  >

                                </b>
                            </div>
                            <svg id=b_{{= item.barcode2}} ></svg>
                        </div>
                    {{/if}}
                </div>
            {{/each}}
        </div>
    </script>

    <script type="text/javascript">
        $(function(){
            $("#generateBarcodeId").click(function(){
               var code =  $("#code").val();
               var count =  parseInt($("#count").val());
               if(isNaN(count)){
                   alert("输入不是数字！");
                   return ;
               }
               if(count<=0 ){
                   alert("次数必须大于0");
                   return ;
               }

               if(!code){
                   alert("条码不能为空");
                   return ;
               }
               var  codeSub = parseInt(code.substr(2))
                if(isNaN(codeSub) && count>1){
                    alert("条码不是没有流水号，次数不能大于1");
                    return ;
                }
               $.ajax({
                   type:'post',
                   url:'${pageContext.request.contextPath}/showBarCodePage',
                   data:{barcode:code,count:count},
                   dataType:"json",
                   success:function (result) {
                       console.dir(result);
                       var data = result.data;
                       $("#pnlId").html("");
                       $("#barcodeTmplId").tmpl(result).appendTo('#pnlId');
                       for(var i=0;i<data.length;i++){
                           for(var jsonObjectKey in data[i]){
                               if(data[i][jsonObjectKey]){
                                   JsBarcode("#b_"+data[i][jsonObjectKey], data[i][jsonObjectKey], {
                                       format: "CODE128",
                                       ean128: true,
                                       height:104
                                   });
                               }
                           }
                       }
                       var inputArray = $("#barcodeRowlId :input[type='text']");

                       inputArray.each(function (index,element) {
                           $(this).on("blur",function (event) {
                               $(this).attr({'value': $(this).val()});
                           })
                       })
                   }
               })
            });

            $("#printBntId").click(function(){
                var   barcodeDivObject =  $("#barcodeRowlId");
                barcodeDivObject.jqprint();
            });
        })


    </script>
</body>
</html>
