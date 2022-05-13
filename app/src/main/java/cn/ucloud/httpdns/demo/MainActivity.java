package cn.ucloud.httpdns.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.ucloud.httpdns.sdk.HttpDns;
import cn.ucloud.httpdns.sdk.HttpDnsConfig;
import cn.ucloud.httpdns.sdk.HttpDnsService;
import cn.ucloud.httpdns.sdk.ParseHostCallback;
import cn.ucloud.httpdns.sdk.RegisterCallback;
import cn.ucloud.httpdns.sdk.RepositoryType;
import cn.ucloud.httpdns.sdk.data.Host;
import cn.ucloud.httpdns.sdk.data.IpType;
import cn.ucloud.httpdns.sdk.data.ParseStatus;

public class MainActivity extends AppCompatActivity implements RegisterCallback, ParseHostCallback,
    View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String appKey = "a0bdb499d0ea11ecb7c20242ac110016";
    /**
     * 也可以在manifest文件的<application>标签内加入
     * <meta-data
     * android:name="httpdns-secret"
     * android:value="您的appSecret" />
     */
    private static final String appSecret = "您的AppSecret";

    private HttpDnsService service;
    private HttpDnsConfig config;
    private Boolean isRegistered = false;
    private Set<String> domains = new ArraySet<>();

    private RecyclerView recycler_records;
    private Button btn_register;
    private EditText edit_domains;
    private Button btn_pre_parse;
    private Button btn_parse_v4;
    private Button btn_parse_v6;
    private Switch switch_auto_parse_network_changed;
    private Switch switch_cache_enable;
    private Switch switch_auto_clear_cache_after_load;
    private Switch switch_load_expired_ip;
    private Button btn_clear_record;
    private Button btn_clear_cache;

    private SharedPreferences sharedPreferences;
    private List<Record> records = new ArrayList<>();
    private RecordAdapter adapter;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 24) {
                adapter.notifyDataSetChanged();
                recycler_records.smoothScrollToPosition(0);
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(String.format("Version: %s (%d)", HttpDns.VERSION_NAME, HttpDns.VERSION_CODE));
        }

        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        ArraySet defaultDomainSet = new ArraySet<>();
        defaultDomainSet.add("ucloud.cn");
        defaultDomainSet.add("baidu.com");
        domains = sharedPreferences.getStringSet("domains", defaultDomainSet);
        config = new HttpDnsConfig(RepositoryType.REPO_SP)
            .setEnableAutoCleanCacheAfterLoad(sharedPreferences.getBoolean("auto_clear_cache_after_load", false))
            .setEnableCachedResult(sharedPreferences.getBoolean("cache_enable", false))
            .setEnableParseWhenNetworkChanged(sharedPreferences.getBoolean("auto_parse_network_changed", false))
            .setEnableExpiredResult(sharedPreferences.getBoolean("load_expired_ip", false));

        initView();
    }

    private void initView() {
        recycler_records = findViewById(R.id.recycler_records);
        btn_register = findViewById(R.id.btn_register);
        edit_domains = findViewById(R.id.edit_domains);
        btn_pre_parse = findViewById(R.id.btn_pre_parse);
        btn_parse_v4 = findViewById(R.id.btn_parse_v4);
        btn_parse_v6 = findViewById(R.id.btn_parse_v6);
        switch_auto_parse_network_changed = findViewById(R.id.switch_auto_parse_network_changed);
        switch_cache_enable = findViewById(R.id.switch_cache_enable);
        switch_auto_clear_cache_after_load = findViewById(R.id.switch_auto_clear_cache_after_load);
        switch_load_expired_ip = findViewById(R.id.switch_load_expired_ip);
        btn_clear_record = findViewById(R.id.btn_clear_record);
        btn_clear_cache = findViewById(R.id.btn_clear_cache);

        adapter = new RecordAdapter(this, records);
        recycler_records.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycler_records.setAdapter(adapter);

        if (domains.isEmpty()) {
            edit_domains.setText("");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String domain : domains) {
                sb.append(domain).append("\n");
            }
            edit_domains.setText(sb.toString());
        }

        btn_register.setOnClickListener(this);
        btn_pre_parse.setOnClickListener(this);
        btn_parse_v4.setOnClickListener(this);
        btn_parse_v6.setOnClickListener(this);
        btn_clear_record.setOnClickListener(this);
        btn_clear_cache.setOnClickListener(this);

        switch_auto_parse_network_changed.setOnCheckedChangeListener(this);
        switch_cache_enable.setOnCheckedChangeListener(this);
        switch_auto_clear_cache_after_load.setOnCheckedChangeListener(this);
        switch_load_expired_ip.setOnCheckedChangeListener(this);

        switch_auto_parse_network_changed.setChecked(sharedPreferences.getBoolean("auto_parse_network_changed", false));
        switch_cache_enable.setChecked(sharedPreferences.getBoolean("cache_enable", false));
        switch_auto_clear_cache_after_load.setChecked(sharedPreferences.getBoolean("auto_clear_cache_after_load", false));
        switch_load_expired_ip.setChecked(sharedPreferences.getBoolean("load_expired_ip", false));
    }

    @Override
    public void onParseStateChanged(ParseStatus status, String host, IpType type) {
        records.add(0, new Record(String.format("[解析状态]: %s:%s -> %s", host, type.type(), status.toString())));
        handler.sendEmptyMessage(24);
    }

    @Override
    public void onFailed(String host, IpType type, Exception exception) {
        String error = exception.getMessage() == null ? exception.getCause().getMessage() : exception.getMessage();
        records.add(0, new Record(Record.RecordType.ERROR,
            String.format("[解析失败]: %s:%s -> %s", host, type.type(), error)));
        handler.sendEmptyMessage(24);
    }

    @Override
    public void onParsed(String host, IpType type, String[] ips) {
        StringBuilder sb = new StringBuilder();
        if (ips == null) {
            sb.append("null");
        } else {
            for (int i = 0, len = ips.length; i < len; i++) {
                sb.append(ips[i]);
                if (i < len - 1)
                    sb.append(", ");
            }
        }
        records.add(0, new Record(String.format("[解析完成]: %s:%s -> %s", host, type.type(), sb.toString())));
        handler.sendEmptyMessage(24);
    }

    @Override
    public void onRegister(boolean success, String message) {
        synchronized (isRegistered) {
            isRegistered = success;
        }
        records.add(0, success ? new Record("[注册成功]: " + message) : new Record(Record.RecordType.ERROR, "[注册失败]: " + message));
        handler.sendEmptyMessage(24);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear_record: {
                records.clear();
                adapter.notifyDataSetChanged();

                break;
            }
            case R.id.btn_clear_cache: {
                synchronized (isRegistered) {
                    if (!isRegistered) {
                        Toast.makeText(this, "请先注册！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                service.clearCache();
                records.add(0, new Record("[清空缓存数据]"));
                handler.sendEmptyMessage(24);
                break;
            }
            case R.id.btn_register: {
                if (appKey == null || appKey.isEmpty()) {
                    Toast.makeText(this, "请填写有效的appKey", Toast.LENGTH_SHORT).show();
                    return;
                }

                service = HttpDns.getService(getApplicationContext(), appKey);
                /**
                 * 若appSecret使用<meta-data>标签，则使用一下方式初始化
                 * service = HttpDns.getService(getApplicationContext(), appKey);
                 */
                service.addParseHostCallback(this);
                service.register(config, this::onRegister);
                break;
            }
            case R.id.btn_pre_parse: {
                synchronized (isRegistered) {
                    if (!isRegistered) {
                        Toast.makeText(this, "请先注册！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                String content = edit_domains.getText().toString();
                if (content == null || content.isEmpty()) {
                    Toast.makeText(this, "请输入有效的待解析域名，一行一个", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] arrDomains = content.split("\n");
                domains.clear();
                for (String domain : arrDomains) {
                    domains.add(domain);
                }
                sharedPreferences.edit().putStringSet("domains", domains).apply();

                List<Host> hosts = new ArrayList<>();
                for (String domain : domains) {
                    hosts.add(new Host(domain));
                }
                service.setPreParseHosts(hosts);

                break;
            }
            case R.id.btn_parse_v4: {
                synchronized (isRegistered) {
                    if (!isRegistered) {
                        Toast.makeText(this, "请先注册！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                String content = edit_domains.getText().toString();
                if (content == null || content.isEmpty()) {
                    Toast.makeText(this, "请输入有效的待解析域名，一行一个", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] arrDomains = content.split("\n");
                domains.clear();
                for (String domain : arrDomains) {
                    domains.add(domain);
                }
                sharedPreferences.edit().putStringSet("domains", domains).apply();

                for (String domain : domains) {
                    String[] ips = service.parseIpsAsync(domain);
                    if (ips == null) {
                        records.add(0, new Record(String.format("[缓存/降级]: %s:%s -> 没有缓存或降级数据", domain, "v4")));
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0, len = ips.length; i < len; i++) {
                            sb.append(ips[i]);
                            if (i < len - 1)
                                sb.append(", ");
                        }
                        records.add(0, new Record(String.format("[缓存/降级]: %s:%s -> %s", domain, "v4", sb.toString())));
                    }
                    handler.sendEmptyMessage(24);
                }
                break;
            }
            case R.id.btn_parse_v6: {
                synchronized (isRegistered) {
                    if (!isRegistered) {
                        Toast.makeText(this, "请先注册！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                String content = edit_domains.getText().toString();
                if (content == null || content.isEmpty()) {
                    Toast.makeText(this, "请输入有效的待解析域名，一行一个", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] arrDomains = content.split("\n");
                domains.clear();
                for (String domain : arrDomains) {
                    domains.add(domain);
                }
                sharedPreferences.edit().putStringSet("domains", domains).apply();

                for (String domain : domains) {
                    String[] ips = service.parseIPv6sAsync(domain);
                    if (ips == null) {
                        records.add(0, new Record(String.format("[缓存/降级]: %s:%s -> 没有缓存或降级数据", domain, "v6")));
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0, len = ips.length; i < len; i++) {
                            sb.append(ips[i]);
                            if (i < len - 1)
                                sb.append(", ");
                        }
                        records.add(0, new Record(String.format("[缓存/降级]: %s:%s -> %s", domain, "v6", sb.toString())));
                    }
                    handler.sendEmptyMessage(24);
                }
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_auto_clear_cache_after_load: {
                synchronized (isRegistered) {
                    if (isRegistered) {
                        service.setCachedResultEnabled(switch_cache_enable.isChecked(), isChecked);
                    } else {
                        config.setEnableAutoCleanCacheAfterLoad(isChecked);
                    }
                }
                sharedPreferences.edit().putBoolean("auto_clear_cache_after_load", isChecked).apply();
                break;
            }
            case R.id.switch_auto_parse_network_changed: {
                synchronized (isRegistered) {
                    if (isRegistered) {
                        service.setPreParseAfterNetworkChanged(isChecked);
                    } else {
                        config.setEnableParseWhenNetworkChanged(isChecked);
                    }
                }
                sharedPreferences.edit().putBoolean("auto_parse_network_changed", isChecked).apply();
                break;
            }
            case R.id.switch_cache_enable: {
                synchronized (isRegistered) {
                    if (isRegistered) {
                        service.setCachedResultEnabled(isChecked);
                    } else {
                        config.setEnableCachedResult(isChecked);
                    }
                }
                sharedPreferences.edit().putBoolean("cache_enable", isChecked).apply();
                break;
            }
            case R.id.switch_load_expired_ip: {
                synchronized (isRegistered) {
                    if (isRegistered) {
                        service.setExpiredResultEnabled(isChecked);
                    } else {
                        config.setEnableExpiredResult(isChecked);
                    }
                }
                sharedPreferences.edit().putBoolean("load_expired_ip", isChecked).apply();
                break;
            }
        }
    }
}
