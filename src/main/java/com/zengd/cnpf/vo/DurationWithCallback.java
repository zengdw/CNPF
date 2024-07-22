package com.zengd.cnpf.vo;

import java.time.Duration;

/**
 * @author zengd
 */
public record DurationWithCallback(Duration duration, Runnable callback) {
}
