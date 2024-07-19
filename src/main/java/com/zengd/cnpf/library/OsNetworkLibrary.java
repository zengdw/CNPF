package com.zengd.cnpf.library;

import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.utils.NativeUtil;
import com.zengd.cnpf.vo.OsType;

/**
 * @author zengd
 * @date 2024/7/18 下午4:04
 */
public sealed interface OsNetworkLibrary permits WindowsNetworkLibrary, LinuxNetworkLibrary, MacOSNetworkLibrary {
    OsNetworkLibrary CURRENT = switch (NativeUtil.ostype()) {
        case OsType.Windows -> new WindowsNetworkLibrary();
        case OsType.Linux -> new LinuxNetworkLibrary();
        case OsType.MacOS -> new MacOSNetworkLibrary();
        default -> throw new FrameworkException(ExceptionType.NETWORK, "Unsupported operating system");
    };
}
