package ai.platon.pulsar.persist.metadata;

/**
 * Created by Vincent on 17-3-19.
 * Copyright @ 2013-2023 Platon AI. All rights reserved
 */
public enum Name {
    UNKNOWN(""),

    HREF("F_HREF"),
    LOCATION("F_LOCATION"),
    FETCH_MODE("F_MD"),
    FETCH_TIME_HISTORY("F_FTH"),
    FETCH_MAX_RETRY("F_MR"),
    RESPONSE_TIME("F_RT"),

    ORIGINAL_CONTENT_LENGTH("POCL"),

    ORIGINAL_EXPORT_PATH("S_OEP");

    private final String text;

    Name(String name) {
        this.text = name;
    }

    public String text() {
        return this.text;
    }
}
