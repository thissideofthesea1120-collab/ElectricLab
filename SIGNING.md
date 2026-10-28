# 签名与发布说明

## 签名材料

| 项目 | 值 |
| --- | --- |
| Keystore | `keystore/electriclab-release.jks` |
| Key alias | `electriclab` |
| 算法 | RSA 2048 |
| 有效期 | 10000 天 |
| 签名方案 | v1 + v2 + v3 |
| 证书 DN | `CN=ElectricLab, OU=Junior Physics Lab, O=PhysLab, L=Beijing, ST=Beijing, C=CN` |
| 证书 SHA-256 | `12ffb476271873e88ba67fdfc12b92102a6698939efd98ba3b6a72e90b44f73f` |
| 证书 SHA-1 | `f74d01cd80c0a4d9b22810fcb8eabdc69ed75233` |
| 版本 | 2.5.1（versionCode 51） |
| APK SHA-256 | `4428F85813CB4668BBD4750DD12367C5B01ABF8AF8E68FBEB7C69ECB24293FF8` |
| APK 大小 | 7,124,822 字节（约 6.79 MiB） |

签名口令、别名和 keystore 路径保存在 `keystore.properties`。该文件和 `keystore/` 目录属于私密材料，**不要上传到公开仓库**，并请至少备份两份。丢失签名私钥后将无法为已安装用户提供覆盖升级。

## 重新打包

```bat
build-electric.cmd :app:assembleRelease
```

签名配置在 `app/build.gradle.kts` 中读取 `keystore.properties`，release 构建会自动签名。最终文件：

```text
app/build/outputs/apk/release/app-release.apk
```

## 校验签名

```bat
apksigner verify --verbose --print-certs 电学虚拟实验室-v2.1.14-release.apk
```

本版本校验结果：

```text
Verifies
Verified using v1 scheme (JAR signing): true
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): true
Number of signers: 1
```

## 安装

```bat
adb install -r ElectricLab-v2.1.8-release.apk
```

如果设备上安装过使用其他证书签名的同包名版本，需要先卸载：

```bat
adb uninstall com.physlab.electric
```

## 发布检查清单

- [x] `applicationId` 为 `com.physlab.electric`
- [x] versionCode 32 / versionName 2.1.8
- [x] 横屏 Activity 配置
- [x] release 使用 RSA 2048 签名，v1/v2/v3 校验通过
- [x] 无网络权限，无敏感权限
- [x] APK 可被 aapt 正常解析
- [x] 已在 640×360、915×412、1280×800 三种横屏尺寸下完成 Compose 渲染截图验证
- [x] 三接线柱仪表、1～4 节电池组、三种灯泡规格渲染验证
- [x] 导线弯折、导线库、规格面板、磁体相互作用、损坏 / 修复、实验模板渲染验证
- [x] 长按详情、导线任意位置形变、仪表放大刻度、磁体旋转、电源极性、双指平移渲染验证
- [x] 接线柱端点改接、仪表 10 小格刻度、扩展实验台、模板导线不交叉、仪器库顺序排列渲染验证
- [x] 新手引导、改接原导线半透明、仪表细长指针渲染验证
- [x] 实验快照、记录表、蜂鸣器静音、5 小格刻度、四种电阻丝、回形针、仪表翻转渲染验证
- [x] 电磁铁 / 螺线管换向、螺线管反向绕线、电阻值显示开关渲染验证
- [x] 电流表 / 电压表正负接反损坏与提示渲染验证
- [x] 电压表“正进负出”接反损坏与提示渲染验证
- [x] 显示读数时电压表导线不显示电子流方向渲染验证
- [x] 改接导线时支路断路、电流归零渲染验证
- [x] 串联双灯、超量程损坏、电动机风扇、灯泡亮度、读数验证、双指缩放渲染验证
- [x] 实验台纵向扩展、滑片优先命中、手动记录、灯泡规格与 3 倍过载阈值逻辑验证
- [x] 不同规格灯泡按统一实际功率—亮度标准渲染验证
- [x] 缩放后边缘命中修复、独立电源短路隔离、U1/U2/I1/I2 动态记录表渲染验证
- [x] 仪表负刻度与越线损坏、4.5 V 电动机/蜂鸣器、导线交叉提醒、8 步串联引导渲染验证
- [x] 任务闯关 9 关自主连线、数据表提交与串联/并联满分验证
- [x] 9 关自主连线/数据表提交、负刻度 5 小格、接反未损坏提醒渲染验证
