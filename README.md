# common

[![CodeFactor](https://www.codefactor.io/repository/github/plutoproject/common/badge)](https://www.codefactor.io/repository/github/plutoproject/common)

✨ 星社 Project 服务器的通用组件。

## 📦 模块说明

- `dependency-loader`: 用于加载依赖库。
- `member`: 成员管理框架。
- `misc`: 杂项功能。
- `rpc`: 跨端通信辅助，基于 gRPC。
- `utils`: 工具类。
- `hypervisor`: 服务端监测、调控工具。
- `exchange`: 兑换系统。
- `visual`: 视觉相关 API。
- `bedrock-adaptive`: 基岩版适配逻辑。
- `teleport`: 支持跨服的传送系统。

## 🔧 构建

> [!NOTE]
>
> 此处以 Linux 系统上的步骤举例。
>
> 如果您使用的是 Windows，可能需要修改部分命令。
>

1. 将本项目拉取到你的设备：`git clone https://github.com/PlutoProject/common.git`
2. 进入项目目录：`cd ./common`
3. 打包构建：`./gradlew shadowJar`

## 👨‍💻 贡献

目前我们还没有制定明确的贡献指南。

如果你是社区中的一位玩家，你可以直接提交 Pull Request，前提是你认为你的修改是有意义且正确的。

## 📄️ 许可

[PlutoProject/common](https://github.com/PlutoProject/common)
在 [GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.html) 下许可。

![license](lgpl-v3.png)
