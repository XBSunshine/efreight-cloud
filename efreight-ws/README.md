#项目结构说明
- 访问地址：http://127.0.0.1:8085/ws/v1.0/af/AEOrderWS?wsdl
- efreight-ws与efreigh其它模块无任何关联关系
- 根据业务模块来分包存在JAVA文件，包结构如下：
```
-----afbse：业务模块包
        |--contant：常量包
        |--entity：只存放对应表结构的JAVA文件
        |--mapper：操作数据库表的Mapper文件
        |--pojo：传输所需要的JAVABean
        |--service：webService服务业务接口与实现类
        |--ws：存信webservice服务类     
-----common：公用模块
        |--annotation：自定义注解包
        |--config：配置相关包
        |--contant：公用常量包
        |--interceptor：拦截器包
        |--pojo：公用JAVA类
        |--util：公用工具类
```
- 授权实现与权限验证概述(不赞成的实现方式)
```aidl
    涉有的类：AuthInIntercepter，EFWSAuthorize
    通过AuthInIntercepter拦截进行消息的拦截，获取<soap:header>标签中的<ws-auth>标签内容，
    再获取到访问服务与之对应方法上的@EFWSAuthorized注解配置的权限名称来进行数据的查询，
    如果查询到数据则认为有访问权限，否则无访问权限
    @EFWSAuthorize 需要添加到服务提供者实现类的方法上
```
- 项目整体有全局异常处理（暂未实现）

#开发规范
- 代码存放位置说明
```aidl
    1，对外服务分业务封装到不同的接口类中，并存放在ws包中
    2，service包存放服务实现业务，比如说一个对外提供的Order相关服务，则有一个WSOrderService业务类，其通用服务也可以放入此包
    3，service包中的类，以mapper形势进行注入，除公用Service外。
    4，mapper包中的类可以写服务相关的其它表的查询语句，但是不可以写其它表的插入与修改语句，其它表的插入与修改必须使用与之对应的mapper类
      进行相关操作，并将其与之表的相关查询存放到与之对应的mapper文件中。(前期这么开发)
    5,pojo包根据业务来进行分包管理，比如订单服务，需要在pojo下建立order包存储相关JAVA类，如果订单服务下又分创建，修改，查询，也可以根据其
     动作建立相关包，比如订单创建，则建立的包名为pojo.order.create。 
```
- 服务定义说明
```aidl
    1，@WebService 标注为对外服务类，其它targetNamespace属性标识为域，其它值根据业务进行划分。
    2，默认以方法名为服务名，使用注解@WebMethod标注
    3，请求参数如果大于两个以上封装为XXXXXRequest类进行数据的接收，使用@WebParam中的name属性指定，建议与类名相同，首字段小写
    4，响应参数都封装为WSXXXXXXXResponse类进行数据的返回，需要继承WSResponse类，使用@WebResult中的name属性指定,建议与类名相同且WS小写即可
    5，个人建议请求与响应JAVA类，不进行公用.
```
- 代码实现
```aidl
    1, 所有业务验证均抛出WSException异常，标注好业务异常代码，与异常信息，异常信息定义模板：服务名：异常信息
    2，所有字段的非空验证均使用注解进行验证（暂无实现）
```