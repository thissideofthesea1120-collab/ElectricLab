# F-Droid 提交文案

源码仓库：<https://github.com/thissideofthesea1120-collab/ElectricLab>
Tag：`v2.1.13`
应用 ID：`com.physlab.electric`

## 方式 A：向 fdroiddata 提交 Merge Request（推荐）

1. 登录 GitLab，打开 <https://gitlab.com/fdroid/fdroiddata>，点击 **Fork**。
2. 在 fork 中新建文件：

   ```text
   metadata/com.physlab.electric.yml
   ```

3. 内容使用本目录的 [com.physlab.electric.yml](com.physlab.electric.yml)。
4. Commit 到 fork 的新分支，例如 `add-electriclab`。
5. 向上游 `fdroid/fdroiddata` 创建 Merge Request。

MR 标题：

```text
New app: com.physlab.electric (ElectricLab)
```

MR 描述：

```markdown
## New app

- App ID: `com.physlab.electric`
- Name: 电学虚拟实验室 (ElectricLab)
- Source: https://github.com/thissideofthesea1120-collab/ElectricLab
- Tag: `v2.1.13` (versionCode 37)
- License: GPL-3.0-or-later
- Category: Science & Education

ElectricLab is an offline virtual electricity laboratory for middle and high
school physics. Students wire circuits themselves, read ammeters and
voltmeters, and complete 15 graded experiments.

The app has no network permission, no ads, no analytics, and no proprietary
dependencies. It is built with Kotlin and Jetpack Compose.

F-Droid signing note: the repository does not contain a release keystore.
Without `keystore.properties`, `assembleRelease` produces an unsigned APK
(`app-release-unsigned.apk`), which F-Droid signs with its own key.
```

## 方式 B：先提交 RFP issue

如果没有时间维护 fdroiddata，可以先在
<https://gitlab.com/fdroid/rfp/-/issues/new> 提交收录请求。

标题：

```text
RFP: ElectricLab — offline junior-high electricity lab
```

正文：

```markdown
App name: 电学虚拟实验室 / ElectricLab
Package: com.physlab.electric
Source: https://github.com/thissideofthesea1120-collab/ElectricLab
Tag: v2.1.13
License: GPL-3.0-or-later

An offline Android virtual electricity lab for junior-high physics. Students
connect circuits, take meter readings, and work through 13 graded tasks.
No network permission, no ads, no tracking.

F-Droid metadata is prepared in the source repository under `fdroid/`.
```

## 审核时可能被问到

- **为什么没有签名密钥？** F-Droid 使用自己的密钥签名；源码仓库只需能构建未签名 release。
- **为什么有 Google Maven 依赖？** 只使用 AndroidX / Jetpack Compose / Material 3，均为 Apache-2.0。
- **是否有联网或统计？** 没有 `INTERNET` 权限，没有广告、统计或账号系统。
- **截图？** 已放在 `fastlane/metadata/android/zh-CN/images/phoneScreenshots/`。
- **应用只有中文？** 当前界面为中文；`fastlane` 同时提供 `zh-CN` 和 `en-US` 商店文案。

## 重要：签名不同

现有 APK 使用项目自己的证书签名；F-Droid 使用 F-Droid 的证书签名。
两个版本不能直接覆盖安装。已经安装 GitHub 版的用户如果要改用 F-Droid 版，
需要先卸载（会清除本地闯关进度和快照），或继续使用 GitHub 版。
