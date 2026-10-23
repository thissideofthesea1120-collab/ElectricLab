# 自动发布公共版

当前自动发布链路只处理 **公共版**：

```text
推送 tag v* → GitHub Actions 构建签名 APK → 创建 GitHub Release
                                      ↓
                         F-Droid 检测 tag（收录后自动更新）
```

个人版仍然在本地构建，不会上传。

## 一、GitHub Actions 首次配置

仓库已经包含：

```text
.github/workflows/public-release.yml
```

需要在 GitHub 仓库的 **Settings → Secrets and variables → Actions** 添加 4 个 Repository secrets：

| Secret | 内容 |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | 发布 keystore 的 Base64 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore 密码 |
| `ANDROID_KEY_ALIAS` | 密钥别名 |
| `ANDROID_KEY_PASSWORD` | 密钥密码 |

在 Windows 上生成 keystore 的 Base64：

```powershell
[Convert]::ToBase64String(
  [IO.File]::ReadAllBytes("C:\Users\TLMZX\Documents\deepseek-harness\default-workspace\ElectricLab\keystore\electriclab-release.jks")
) | Set-Clipboard
```

密码和别名以本地 `keystore.properties` 为准：

```text
storePassword=...
keyAlias=electriclab
keyPassword=...
```

不要把 keystore、密码或 `keystore.properties` 提交到仓库。

## 二、发布新版本

1. 修改版本号：

```kotlin
versionCode = 40
versionName = "2.1.16"
```

2. 本地构建并验证公共版。
3. 提交并推送 `main`。
4. 创建并推送 tag：

```bash
git tag v2.1.16
git push origin v2.1.16
```

5. GitHub Actions 自动：

- 校验 tag 与 `versionName` 一致
- 构建签名 release APK
- 验证签名
- 创建 GitHub Release
- 上传 `ElectricLab-v2.1.16-public.apk`

如果 Actions 失败，先看 **Actions → Public release** 日志；常见原因是 secrets 没配置或 tag 与版本号不一致。

## 三、F-Droid 自动更新

当前 `fdroiddata` 元数据已经设置：

```yaml
AutoUpdateMode: Version
UpdateCheckMode: Tags
```

含义：

- **应用被 F-Droid 正式收录后**，F-Droid 的 checkupdates bot 会检测新的 `v*` tag，并自动创建/更新 fdroiddata MR。
- 新版本必须：
  - `versionCode` / `versionName` 已更新
  - tag 已推送
  - tag 的源码 commit 能构建
- 首次收录前，MR 里的版本仍需手动更新；收录后才会自动。

F-Droid 不直接使用 GitHub Release 的 APK，而是从 tag 对应的源码重新构建，并使用 F-Droid 自己的签名。

## 四、个人版

个人版不会进入 GitHub/F-Droid。每次改动后本地构建：

```text
ElectricLab-v<version>-personal.apk
```

公共版文件：

```text
ElectricLab-v<version>-public.apk
```

两版功能相同，只有关于页署名不同。

## 五、当前限制

- GitHub Actions 使用仓库 secrets，不要把签名材料写进 workflow。
- 当前 F-Droid 仍在首次审核阶段；审核通过前，不要期望 checkupdates bot 自动收录新版本。
- 首次审核通过后，仍需观察一次自动更新是否成功，再完全依赖自动化。
