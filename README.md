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
    - 将NFC标签格式化为空的小米碰碰贴2
    - 将NDEF标签置为空

## NTAG选择

NFC写入界面有写入字节大小的提示，请测试后根据自己的需求选择不同大小的NTAG。

目前至少使用NTAG213可以支持部分功能，如果希望通用建议使用NTAG215。

## 注意事项

本应用不保证小米一碰妙享NFC标签触发稳定性，如遇到任何问题可以尝试重启设备，重启小米电脑助手或升级互联互通等相关服务。

所有NFC标签的解析建立在对Xiaomi HyperOS中提取出的APP上，并在小米电脑管家`4.0.0.533`上通过测试。

| 名称       | 包名                              | 版本                       |
|----------|---------------------------------|--------------------------|
| 小米互联通信服务 | `com.xiaomi.mi_connect_service` | 3.1.453.10               |
| 投屏       | `com.milink.service`            | 15.0.5.0.ceaac61.2919843 |
| 米家       | `com.xiaomi.smarthome`          | 9.1.501                  |
| NFC服务    | `com.android.nfc`               | 14                       |

## 相关项目

- [XiaomiNDEF](https://github.com/XFY9326/XiaomiNDEF)  
  使用Python自定义小米NDEF信息

## 开源协议

```text
MIT License

Copyright (c) 2023 XFY9326

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
