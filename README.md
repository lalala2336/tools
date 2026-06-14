# Email MCP Server

基于 Spring Boot 的 MCP (Model Context Protocol) 服务，让 AI 助手（如 Claude）直接发送邮件。

**Stdio 传输模式** — Claude Desktop/Claude Code 直接启动并管理进程生命周期，无需手动启停服务。通信通过 stdin/stdout，不占用 HTTP 端口。

## 架构

```
┌─────────────────┐                  ┌──────────────────┐     SMTP      ┌──────────┐
│  Claude Desktop  │   stdin/stdout   │  Spring Boot JAR  │ ◄──────────► │  SMTP    │
│  (launches JAR)  │ ◄──────────────► │  - sendEmail tool │              │  Server  │
│                  │   MCP Protocol   │  - Spring Mail    │              └──────────┘
└─────────────────┘                  └──────────────────┘
```

## 功能

| 工具 | 描述 |
|------|------|
| `sendEmail` | 发送邮件，支持纯文本和 HTML，可选 CC/BCC |

### sendEmail 参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| `to` | string | ✓ | 收件人邮箱 |
| `subject` | string | ✓ | 邮件主题 |
| `body` | string | ✓ | 邮件正文（纯文本或 HTML） |
| `cc` | string | | 抄送，逗号分隔 |
| `bcc` | string | | 密送，逗号分隔 |
| `html` | boolean | | body 是否为 HTML（默认 false） |

## 快速开始

### 前置要求

- Java 17+
- Maven 3.8+（仅首次构建需要，之后只需 JRE 即可运行）

### 1. 构建 JAR

```bash
# 克隆项目后，构建可执行 JAR
mvn package -DskipTests
```

生成的 JAR 位于 `target/tools-1.0.0.jar`。

### 2. 配置 Claude Desktop

编辑 Claude Desktop 的 `claude_desktop_config.json`：

```json
{
  "mcpServers": {
    "email": {
      "command": "java",
      "args": ["-Dfile.encoding=UTF-8", "-jar", "D:\\xm\\tools\\target\\tools-1.0.0.jar"],
      "env": {
        "SMTP_HOST": "smtp.qq.com",
        "SMTP_PORT": "587",
        "SMTP_USERNAME": "your-email@qq.com",
        "SMTP_PASSWORD": "your-auth-code"
      }
    }
  }
}
```

> **配置后重启 Claude Desktop 即可。** Claude 会自动启动 JAR 进程，你不需要手动运行任何命令。退出 Claude Desktop 时进程自动结束。

## 各邮箱 SMTP 配置

| 邮箱 | SMTP_HOST | SMTP_PORT | 说明 |
|------|-----------|-----------|------|
| QQ邮箱 | smtp.qq.com | 587 | 需开启 POP3/SMTP 服务获取授权码 |
| 163邮箱 | smtp.163.com | 465 | 需开启 SMTP 服务获取授权码 |
| Gmail | smtp.gmail.com | 587 | 需开启两步验证后生成应用密码 |
| Outlook | smtp.office365.com | 587 | 使用账号密码 |

> **注意：** 国内邮箱（QQ、163）需在邮箱设置中手动开启 SMTP 服务，使用「授权码」而非登录密码。

## 使用方式

配置完成、重启 Claude Desktop 后，直接对话即可：

> **你:** 帮我发一封邮件给 zhangsan@example.com，主题是"会议通知"，内容是"明天下午 3 点会议室开会"

Claude 会自动调用 `sendEmail` 工具完成发送。

## 手动测试（可选）

如果想在不启动 Claude Desktop 的情况下测试 JAR 能否正常工作：

```bash
# 通过环境变量传入 SMTP 配置
SMTP_HOST=smtp.qq.com \
SMTP_PORT=587 \
SMTP_USERNAME=your-email@qq.com \
SMTP_PASSWORD=your-auth-code \
java -Dfile.encoding=UTF-8 -jar target/tools-1.0.0.jar

# 启动后等待 stdin 输入 MCP 协议消息（Ctrl+C 退出即可）
```

## 项目结构

```
tools/
├── pom.xml
├── .gitignore
├── README.md
├── src/main/java/com/xm/tools/
│   ├── ToolsApplication.java          # Spring Boot 启动类
│   └── service/
│       └── EmailMcpTools.java         # MCP @Tool 定义 + 邮件发送实现
└── src/main/resources/
    ├── application.yml                # 默认配置（环境变量占位）
    └── application-example.yml        # 各邮箱配置示例
```

## 技术栈

- Spring Boot 3.3.x
- Spring AI MCP Server (stdio transport)
- Spring Boot Mail (JavaMailSender)
- Java 17
