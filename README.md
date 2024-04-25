# MiLink NFC

**小米设备互联NFC助手**

本应用仅在Xiaomi HyperOS下测试通过，目前仅支持安卓10及以上

## 功能

- 音频接力
    - 写入NFC标签为小米碰碰贴的音频接力

- 内容流转
    - 写入NFC标签为小米内容流转
    - 手动触发模拟测试NFC内容流转标签

- 一碰妙享
    - 写入NFC标签为小米一碰妙享屏幕镜像
    - 手动触发模拟测试NFC一碰妙享标签
    - 通过通知栏磁贴触发NFC一碰妙享标签
    - 转发华为笔记本一碰传为小米一碰妙享屏幕镜像

- 工具
    - 读取并查看小米协议的NFC标签的详细信息
    - 扫描附近的蓝牙设备并复制MAC地址
    - 将NFC标签格式化为空的小米碰碰贴2
    - 将NDEF标签置为空

## 一碰妙享触发接口

为了能实现与其他App之间的相互调用，简化二次开发的难度的功能，本应用提供了快捷接口Intent：

- Action: `tool.xfy9326.milink.nfc.action.screen_mirror`
- Extra:
  注：不填写某个参数则使用默认值

  | 键        | 值类型     | 默认值    | 解释       |
  |----------|---------|--------|----------|
  | `device` | int     | `1`    | 模拟的设备类型  |
  | `action` | int     | `1`    | 触发方式     |
  | `btMac`  | String  | 空字符串   | 蓝牙MAC地址  |
  | `lyra`   | boolean | `true` | 是否启用Lyra |

  模拟设备类型的可用值：

  | 值   | 解释    |
  |-----|-------|
  | `0` | `TV`  |
  | `1` | `PC`  |
  | `2` | `CAR` |
  | `3` | `PAD` |

  触发方式的可用值

  | 值   | 解释      |
  |-----|---------|
  | `0` | 虚拟NFC标签 |
  | `1` | 互联通信服务  |

这个Intent会快速启动一个透明的一次性Activity用于模拟一碰秒享的操作

在App中使用Intent：

```kotlin
val intent = Intent("tool.xfy9326.milink.nfc.action.screen_mirror").apply {
    putExtra("device", 1)
    putExtra("action", 1)
    putExtra("btMac", "00:00:00:00:00:00")
    putExtra("lyra", true)
}
startActivity(intent)
```

使用ADB或者Shell可以用以下命令触发：

```shell
am start -a tool.xfy9326.milink.nfc.action.screen_mirror --es btMac "00:00:00:00:00:00"
```

如果完整填入所有需要的参数则可以是：

```shell
am start -a tool.xfy9326.milink.nfc.action.screen_mirror --ei device 1 --ei action 1 --es btMac "00:00:00:00:00:00" --ez lyra true 
```

注：对于beta或者debug版本，Action会有一些区别。

## NTAG选择

NFC写入界面有写入字节大小的提示，请测试后根据自己的需求选择不同大小的NTAG。

目前至少使用NTAG213可以支持部分功能，如果希望通用建议使用NTAG215。

## 常见问题

1. 如何在小米手环中写入NFC数据？  
   打开小米运动健康-设备-卡包-添加新门卡-空白门卡，然后在手环上打开刚添加空白门卡，使用本应用写入数据
2. 为什么MIUI和妙享助手无法使用这个NFC标签？  
   可以在写入标签时尝试关闭Lyra再试试
3. 为什么在控制中心连接正常，但是用NFC一直无法连上电脑？  
   请检查蓝牙MAC地址是否正确，有时扫描到电脑会提供两个蓝牙MAC地址请注意！
   在Powershell等终端中，输入`ipconfig /all`，找到描述为`Bluetooth Device`的项目的MAC地址即为真实蓝牙MAC地址
4. 其他连接问题  
   **本应用只能保证NFC标签中写入的数据与触发方式是正确的**  
   **由于不同电脑的网络、硬件、版本均存在差异，所以如果使用过程中遇到任何连接问题本应用都无法解决，只能给出建议**

## 注意事项

本应用不保证小米一碰妙享NFC标签触发稳定性，如遇到任何问题可以尝试重启设备，重启小米电脑助手或升级互联互通等相关服务。

所有NFC标签的解析建立在对Xiaomi HyperOS中提取出的APP上，并在小米电脑管家`4.2.0.1036`上通过测试。

| 名称       | 包名                              | 版本                       |
|----------|---------------------------------|--------------------------|
| 小米互联通信服务 | `com.xiaomi.mi_connect_service` | 3.1.874.10               |
| 互联互通服务   | `com.milink.service`            | 15.0.6.4.d4e3948.3002415 |
| 跨屏协同服务   | `com.xiaomi.mirror`             | 15.01.11.d               |
| 文件管理     | `com.android.fileexplorer`      | 5.0.3.2                  |
| 相册       | `com.miui.gallery`              | 3.7.1.2                  |

## 相关项目

- [XiaomiNDEF](https://github.com/XFY9326/XiaomiNDEF)  
  使用Python自定义小米NDEF信息

## 开源协议

```text
MIT License

Copyright (c) 2024 XFY9326

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
