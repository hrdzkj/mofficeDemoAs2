1. 定制版（企业版和专业版）
专业版本的和企业版本是定制版本， <action android:name="cn.wps.moffice.service.OfficeService"/>对应的service才开放

2. 
2.1)bindServer有时候不成功 。不清楚具体是什么原因。
只是实现加密和不落地打开的基础，这个问题不稳定，不确定解决方案，后面的功能基本都不用考虑了。
场景：1）启动第三方的白名单问题（wps不启动，则bindservice=false），测试机"开启自启动权限"后则bindservice仍然不成功,设置关联启动。
日志显示显示原因如下(不在白名单中)：
01-24 10:17:01.607 3567-3578/? D/HwPFWLogger: AutoStartupDataMgr:isUnderControll third party not in whitelist: com.kingsoft.moffice_pro
01-24 10:17:01.607 3567-3578/? D/HwPFWLogger: AutoStartupDataMgr:retrieveStartupSettings type 1 of com.kingsoft.moffice_pro is 0

Found that If can't get the ResolveInfo of Service, it will false.

2.2)OfficeEventListener不调用的问题。这个是实现加密与不落地打开的关键。

3. 需要使用aidl打开方式，才能实现加解密(这种方式wps才会调用agent、client），才能实现不落地打开.

5。需要自诩商务的问题：
5.1)applicationId必须是"cn.wps.moffice.demo"，否则提示"连接不到第三方应用，加解密功能将失效""
   关于这个问题如何处理，需要联系商务。
5.2)如何判断专业版，是否已经注册

5.3)demo的加密算法加密后打不开

5.4)mService.openDocument带密码到该接口之后，仍然需要输入密码。


5.5)OfficeClientEventListener方法不调用，难道是因为之前用第三方方式启动OfficeService，之后binder成功，也不调用OfficeClientEventListener？
打开文档的过程，不知道为何FloatServiceTest.onDestroy发生了调用




