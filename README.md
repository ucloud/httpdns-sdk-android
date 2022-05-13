# httpdns-sdk-android

HttpDns SDK for Android

## Version

> Version: 1.0.0 (code:1)

## 兼容性

> android 6.0(api23) 及以上

## QuickStart

### 注册使用

* 请前往[**应用管理**](https://console.ucloud.cn/uhttpdns/app_manage)页面，创建您的应用。并将创建成功后对应的***AppKey***和***AppSecret***记录下。
* 在[**域名管理**](https://console.ucloud.cn/uhttpdns/domain_manage)页面，编辑您需要授权解析的域名列表，**不在域名管理中的域名，sdk将无法解析**。

### Dependencies

- 将httpdns-sdk-android.aar包放置于android studio的app项目目录里的libs目录下
- 编辑app/build.gradle配置文件，按照如下方式配置依赖项：

    ``` java
    dependencies {
        // Your other dependencies
        implementation files('libs/httpdns-sdk-android.aar')
        implementation 'com.google.code.gson:gson:2.8.8'
    }
    ```

<br/>

### Usage

``` xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />

<application
    // your application setting
    android:networkSecurityConfig="@xml/network_security_config">
    <meta-data
        android:name="httpdns-secret"
        android:value="平台分配的AppSecret" />
</application>
```

``` java
HttpDnsService service = HttpDns.getService(getApplicationContext(), "平台分配的AppKey");
service.addParseHostCallback(new ParseHostCallback(){
    @Override
    public void onParseStateChanged(ParseStatus status, String host, IpType type) {
        
    }

    @Override
    public void onFailed(String host, IpType type, Exception exception) {

    }

    @Override
    public void onParsed(String host, IpType type, String[] ips) {

    }
});

service.register(new HttpDnsConfig(), new RegisterCallback() {
            @Override
            public void onRegister(boolean success, String message) {
                if (success) {
                    List<Host> hosts = new ArrayList<>();
                    hosts.add(new Host("需要解析的域名"));
                    // 预解析
                    service.setPreParseHosts(hosts);
                    // 手动触发解析
                    String[] results = service.parseIpsAsync("需要解析的域名");
                }
            }
        });
```

<br/>

## API

### HttpDns

> SDK版本信息

``` java
static final int VERSION_CODE        // 版本数值编号（递增）
static final String VERSION_NAME     // 版本号名称
```

> 获取HttpDnsService

``` java
/**
 * 获取HttpDnsService实例
 * 以该方法获取HttpDnsService时，AppSecret需要配置在manifest.xml <application>中的<meta-data>里
 *
 * @param applicationContext
 * @param appKey             平台分配的appKey
 * @return
 */
static HttpDnsService getService(@NonNull Context applicationContext, @NonNull String appKey)
```

> 获取HttpDnsService

``` java
/**
 * 获取HttpDnsService实例
 *
 * @param applicationContext
 * @param appKey             平台分配的appKey
 * @param appSecret          平台分配的appSecret
 * @return
 */
static HttpDnsService getService(@NonNull Context applicationContext, @NonNull String appKey, @NonNull String appSecret)
```

<br/>

### HttpDnsService

> 注册HttpDnsService

``` java
/**
 * 注册HttpDnsService
 *
 * @param config           HttpDnsConfig配置项
 * @param serviceUrl       管理平台服务接口地址
 * @param registerCallback 注册结果回调
 */
void register(HttpDnsConfig config, @NonNull String serviceUrl, @NonNull RegisterCallback registerCallback);
```

<br>

> 注册私有化部署HttpDnsService

``` java
/**
 * 注册HttpDnsService
 *
 * @param config           HttpDnsConfig配置项
 * @param serviceUrl       管理平台服务接口地址
 * @param registerCallback 注册结果回调
 */
void register(HttpDnsConfig config, @NonNull String serviceUrl, @NonNull RegisterCallback registerCallback);
```

<br>

> 添加解析回调

``` java
/**
 * 添加解析回调
 * 添加多个解析回调后，每个回调都返回解析结果
 *
 * @param callback
 */
void addParseHostCallback(ParseHostCallback callback);
```

<br>

> 移除解析回调

``` java
/**
 * 移除解析回调
 *
 * @param callback
 */
void removeParseHostCallback(ParseHostCallback callback);
```

<br>

> 设置预解析域名列表

**【注意！】:若不启用缓存，则该接口不执行任何操作！**

``` java
/**
 * 设置预解析域名列表
 * 设置完成后将会后台自动开始解析，解析完成后通过{@link ParseHostCallback}返回结果。
 * 若启用缓存，则后续手动解析域名时，优先检查缓存
 *【注意！】:若不启用缓存，则该接口不执行任何操作！
 *
 * @param hosts 需预解析的域名列表
 */
void setPreParseHosts(List<Host> hosts);
``` 

<br>

> 异步解析域名，获取IPv4解析结果列表

``` java
/**
 * 异步解析域名，获取IPv4解析结果列表
 * 若启用缓存，先查询缓存，若存在则返回，若不存在则返回null，并进行异步解析，解析完成后通过{@link ParseHostCallback}返回结果
 *
 * @param host 需解析的域名
 * @return 返回IP的String数组, 如果没得到解析结果, 则数组的长度为0
 */
String[] parseIpsAsync(String host);
``` 

<br>

> 异步解析域名，获取IPv6解析结果列表

``` java
/**
 * 异步解析域名，获取IPv6解析结果列表
 * 若启用缓存，先查询缓存，若存在则返回，若不存在则返回null，并进行异步解析，解析完成后通过{@link ParseHostCallback}返回结果
 *
 * @param host 需解析的域名
 * @return 返回IPv6的String数组, 如果没得到解析结果, 则数组的长度为0
 */
String[] parseIPv6sAsync(String host);
``` 

<br>

> 设置是否允许返回超时的解析结果

``` java
/**
 * 设置是否允许返回超时的解析结果
 *
 * @param enable
 */
void setExpiredResultEnabled(boolean enable);
``` 

<br>

> 设置是否允许启用缓存

``` java
/**
 * 设置是否允许启用缓存
 *
 * @param enable
 */
void setCachedResultEnabled(boolean enable);
``` 

<br>

> 设置是否允许启用缓存，以及是否在缓存加载过后自动清除

``` java
/**
 * 设置是否允许启用缓存，以及是否在缓存加载过后自动清除
 *
 * @param enable
 * @param autoCleanCacheAfterLoad 缓存加载后是否立即清除
 */
void setCachedResultEnabled(boolean enable, boolean autoCleanCacheAfterLoad);
``` 

<br>

> 设置降级策略

``` java
/**
 * 设置降级策略
 * 用户可定制规则降级为原生DNS解析方式
 *
 * @param filter 降级规则回调 {@link DegradationFilter}
 */
void setDegradationFilter(DegradationFilter filter);
``` 

<br>

> 设置网络切换时是否自动刷新所有域名解析结果

``` java
/**
 * 设置网络切换时是否自动刷新所有域名解析结果
 *
 * @param enable
 */
void setPreParseAfterNetworkChanged(boolean enable);
``` 

<br>

> 设置请求超时时间

``` java
/**
 * 设置请求超时时间
 * default 30_000 ms
 *
 * @param timeout
 */
void setRequestTimeout(int timeout);
```

<br>

### DegradationFilter

> 是否需要降级解析

``` java
/**
 * 是否需要降级解析
 * 如果降级，则域名将被直接返回作为解析结果
 *
 * @param host 域名
 * @return true: 降级解析 false: 进行HttpDns解析
 */
boolean shouldDegradeHttpDns(String host);
```

<br>

``` java
/**
 * 清空缓存数据
 */
void clearCache();
```

<br/>

### HttpDnsConfig

``` java
public class HttpDnsConfig {
    /**
     * 构造方法
     * 可配置本地持久化方案
     *
     * @param repoType 本地持久化方案
     */
    public HttpDnsConfig(RepositoryType repoType) {
        ...
    }

    /**
     * 构造方法
     * 默认不启用本地持久化
     */
    public HttpDnsConfig() {
        ...
    }

    public HttpDnsConfig setEnableParseWhenNetworkChanged(boolean enableParseWhenNetworkChanged) {
        ...
    }

    public HttpDnsConfig setEnableExpiredResult(boolean enableExpiredResult) {
        ...
    }

    public HttpDnsConfig setEnableCachedResult(boolean enableCachedResult) {
        ...
    }

    public HttpDnsConfig setEnableAutoCleanCacheAfterLoad(boolean enableAutoCleanCacheAfterLoad) {
        ...
    }

    public HttpDnsConfig setRequestTimeout(int requestTimeout) {
        ...
    }
}
```

<br/>

### RepositoryType

``` java
public enum RepositoryType {
    /**
     * 不启用本地持久化
     */
    REPO_UNABLE, 
    /**
     * 使用SharedPerferences作为本地持久化
     */
    REPO_SP
}
```