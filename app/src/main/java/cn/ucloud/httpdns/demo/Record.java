package cn.ucloud.httpdns.demo;

/**
 * Created by joshua
 * On 2021/12/14 21:24
 * E-mail: joshua.yin@ucloud.cn
 * Description:
 */
public class Record {
    public enum RecordType {
        MESSAGE, ERROR
    }

    public final long timestamp;
    public final RecordType type;
    public final String content;

    public Record(RecordType type, String content) {
        this.type = type;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public Record(String content) {
        this(RecordType.MESSAGE, content);
    }

}
