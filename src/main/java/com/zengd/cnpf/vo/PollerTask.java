package com.zengd.cnpf.vo;

import com.zengd.cnpf.handler.Channel;

/**
 * @author zengd
 */
public record PollerTask(PollerTaskType type, Channel channel, Object msg) {
}
