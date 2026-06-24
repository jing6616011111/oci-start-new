# OCI Start

OCI Start 是一个基于 Spring Boot 2.7.6 的 Oracle Cloud Infrastructure（OCI）实例管理面板。

## 功能

- 多租户 OCI API 密钥管理
- 跨可用性域创建 OCI 实例（AMD / ARM）
- 实例生命周期管理（启动、停止、销毁）
- 公网 IP 刷新和 IPv6 支持
- 自动配置 VCN、子网和安全组
- 基于 Thymeleaf 的 Web 管理界面
- 内置 H2 本地数据库，无需外部数据库
- 异步开机任务处理

## 技术栈

| 组件 | 版本 |
|---|---|
| Java | 8+ |
| Spring Boot | 2.7.6 |
| Spring Data JPA | 2.7.6 |
| H2 Database | 内嵌 |
| OCI Java SDK | 3.88.0 |
| Thymeleaf | 2.7.6 |
| Caffeine Cache | 2.9.2 |

## 快速启动

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar oci-server/target/oci-server-1.0.0-SNAPSHOT.jar
```

然后打开 http://localhost:9856

默认账号：

- 用户名：`admin`
- 密码：`admin123`

## Linux 一键安装包

远程服务器一键安装：

```bash
curl -fLsS https://raw.githubusercontent.com/jing6616011111/oci-start-new/main/install-mirror.sh | bash
```

在本机编译并生成 Linux 安装包：

```bash
scripts/build-linux-package.sh
```

构建完成后会生成：

```text
dist/oci-start-linux.tar.gz
```

把该压缩包上传到 Linux 服务器后执行：

```bash
tar -xzf oci-start-linux.tar.gz
cd oci-start-linux
sudo ./install.sh
```

安装后常用命令：

```bash
sudo systemctl status oci-start
sudo systemctl restart oci-start
sudo systemctl stop oci-start
```

默认安装目录：

- 程序目录：`/opt/oci-start`
- 数据目录：`/var/lib/oci-start`
- 日志目录：`/var/log/oci-start`
- 服务端口：`9856`

卸载：

```bash
sudo /opt/oci-start/uninstall.sh
```

## 项目结构

```
oci-start-new/
├── pom.xml                    # 父级 POM
├── oci-common/                # 公共工具、DTO、枚举
├── oci-dao/                   # JPA 实体和仓库
├── oci-server/                # Spring Boot 应用
│   ├── src/main/java/.../
│   │   ├── controller/        # MVC 控制器
│   │   ├── config/            # 安全、异步、缓存配置
│   │   ├── service/           # 业务服务
│   │   ├── utils/             # OCI SDK 工具
│   │   └── pojo/              # 请求/响应 DTO
│   └── src/main/resources/
│       ├── application.yml    # 应用配置
│       ├── templates/         # Thymeleaf HTML 模板
│       └── static/            # CSS、JS
├── scripts/                   # Linux 安装包脚本
└── README.md
```
