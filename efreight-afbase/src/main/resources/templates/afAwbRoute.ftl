<?xml version="1.0" encoding="UTF-8"?>
<ResultCode>200</ResultCode>
<ResultMessage>调用成功</ResultMessage>
<ResultData>
<AfAwbRouteList>
<#list list as route>
<AfAwbRoute>
<AwbNumber>${route.awbNumber}</AwbNumber>
</AfAwbRoute>
</#list>
</AfAwbRouteList>
</ResultData>
