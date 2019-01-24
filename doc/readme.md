1. 定制版（企业版和专业版），有关闭的时候有toast;
专业版本的和企业版本是定制版本， <action android:name="cn.wps.moffice.service.OfficeService"/>对应的service才开放

2. bindServer有时候不成功 。不清楚具体是什么原因。
只是实现加密和不落地打开的基础，这个问题不稳定，不确定解决方案，后面的功能基本都不用考虑了。

Found that If can't get the ResolveInfo of Service, it will false.

3. 需要使用aidl打开方式，才能实现加解密(这种方式wps才会调用agent、client），才能实现不落地打开.

5。需要自诩商务的问题：
5.1)applicationId必须是"cn.wps.moffice.demo"，否则提示"连接不到第三方应用，加解密功能将失效""
   关于这个问题如何处理，需要联系商务。
5.2)如何判断专业版，是否已经注册

5.3)demo的加密算法加密后打不开

5.4)mService.openDocument带密码到该接口之后，仍然需要输入密码。
而且关闭后，直接关闭了demo的activity

