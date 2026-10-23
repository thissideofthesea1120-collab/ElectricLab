# F-Droid 上架材料

这个目录不是 Android 工程的一部分，而是准备提交到
[fdroiddata](https://gitlab.com/fdroid/fdroiddata) 的元数据模板。

## 前置条件

1. GitHub 仓库公开，并已推送 tag `v2.1.13`。
2. 仓库根目录包含 `LICENSE`（GPL-3.0-or-later）、`gradlew`、`gradlew.bat` 和 `gradle/wrapper/`。
3. F-Droid 构建时不需要签名密钥；release 未配置签名时构建为未签名 APK，由 F-Droid 重新签名。
4. 仓库中不能提交 `keystore.properties`、`keystore/` 或 `local.properties`。

## 提交步骤

1. 在 GitLab 注册账号，fork [fdroiddata](https://gitlab.com/fdroid/fdroiddata)。
2. 把 `com.physlab.electric.yml` 放到 fork 的 `metadata/` 目录。
3. 在 fork 中创建分支，提交 MR 到 `fdroid/fdroiddata`。
4. 在 MR 描述中说明应用是离线教学工具、GPL-3.0-or-later、无网络权限、无广告。
5. 等待 F-Droid 审核；审核通过后会进入 F-Droid 官方仓库。

## 版本更新

每次发布新版本时：

1. 更新 `versionName` / `versionCode`；
2. 推送新 tag；
3. 在 fdroiddata 中追加新的 `Builds` 条目，并更新 `CurrentVersion` / `CurrentVersionCode`。

更推荐使用 `AutoUpdateMode: Version` + `UpdateCheckMode: Tags`，让 F-Droid 自动检测 tag。
