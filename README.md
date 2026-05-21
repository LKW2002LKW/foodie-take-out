# 吃货联盟餐饮聚合平台

![Java](https://img.shields.io/badge/Java-11+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-6DB33F)
![Vue](https://img.shields.io/badge/Vue-3.x-42b883)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1)
![Redis](https://img.shields.io/badge/Redis-6%2F7-red)
![Status](https://img.shields.io/badge/Status-Learning%20Project-lightgrey)

一个前后端分离的餐饮聚合平台示例项目，包含平台端、商家端、用户端三套业务系统，以及对应的三套后端服务。项目覆盖商户入驻、菜品管理、用户下单、订单流转、评价管理、统计报表、对象存储上传等典型外卖/餐饮平台场景。

详细运行说明见：[使用手册](./使用手册.md)

## 项目特点

- 三端业务拆分清晰：平台端、商家端、用户端独立开发与运行
- 后端采用多模块 Maven 工程，公共能力与业务模块解耦
- 集成 MySQL、Redis、MinIO，覆盖数据存储、缓存、文件上传能力
- 内置 Knife4j 接口文档，便于联调和测试
- 前端基于 Vue 3 + Vite，管理端与移动端技术栈分层明确
- 短信验证码、微信登录、订单支付支持本地模拟流程，便于演示和答辩

## 系统组成

### 平台端

- 商户管理
- 用户管理
- 订单监控
- 财务报表查看
- 系统配置查看

### 商家端

- 商户注册与登录
- 菜品分类管理
- 菜品与套餐管理
- 订单处理
- 收入结算查看
- 评价回复

### 用户端

- 用户注册与登录
- 浏览商户与菜品
- 购物车管理
- 提交订单
- 模拟支付
- 订单评价与通知查看

## 技术栈

### 后端

- Java 11 目标版本
- Spring Boot 2.7.x
- Spring Security
- MyBatis-Plus
- Druid
- MySQL 8
- Redis
- MinIO
- Knife4j

### 前端

- Vue 3
- Vite
- Pinia
- Vue Router
- Axios
- Element Plus（平台端 / 商家端）
- Vant（用户端）
- ECharts

## 项目结构

```text
foodie-take-out
├── foodie-common             # 公共模块：配置、工具类、统一返回、异常处理等
├── foodie-pojo               # 实体、DTO、VO 等数据模型
├── foodie-platform-server    # 平台端后端服务
├── foodie-merchant-server    # 商家端后端服务
├── foodie-user-server        # 用户端后端服务
├── vue
│   ├── foodie_plateform      # 平台端前端
│   ├── foodie_merchant       # 商家端前端
│   └── my_foodie_app         # 用户端前端
├── sql
│   └── foodie_alliance.sql   # 数据库初始化脚本
└── 使用手册.md
```

## 运行环境

建议准备以下环境：

- JDK 11+（手册推荐 JDK 17）
- Maven 3.8+
- Node.js 18 或 20
- npm 9+
- MySQL 8.x
- Redis 6.x / 7.x
- MinIO

## 快速开始

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd foodie-take-out
```

### 2. 初始化数据库

先创建数据库：

```sql
CREATE DATABASE foodie_alliance DEFAULT CHARACTER SET utf8mb4;
```

再导入脚本：

```bash
mysql -h 127.0.0.1 -P 3306 -uroot -p foodie_alliance < sql/foodie_alliance.sql
```

### 3. 准备基础服务

启动以下依赖服务：

- MySQL：`3306`
- Redis：`6379`
- MinIO：`9000`
- MinIO Console：`9001`

并在 MinIO 中创建桶：

```text
foodie-images
```

### 4. 配置后端连接信息

项目公共配置位于：

- [`foodie-common/src/main/resources/application-dev-common.yml`](./foodie-common/src/main/resources/application-dev-common.yml)
- [`foodie-common/src/main/resources/application-common.yml`](./foodie-common/src/main/resources/application-common.yml)

默认配置中的数据库、Redis、MinIO 地址指向 `192.168.1.11`，本地运行前通常需要改为你自己的环境，或者通过环境变量覆盖：

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=foodie_alliance
export DB_USERNAME=root
export DB_PASSWORD=your_password

export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD=your_password

export MINIO_ENDPOINT=http://127.0.0.1:9000
export MINIO_ACCESS_KEY=admin
export MINIO_SECRET_KEY=your_password
export MINIO_BUCKET=foodie-images
export MINIO_PUBLIC_URL=http://127.0.0.1:9000/foodie-images
```

### 5. 编译后端

在项目根目录执行：

```bash
mvn clean install -DskipTests
```

### 6. 启动后端服务

分别在独立终端中执行：

```bash
mvn -pl foodie-platform-server spring-boot:run
```

```bash
mvn -pl foodie-merchant-server spring-boot:run
```

```bash
mvn -pl foodie-user-server spring-boot:run
```

### 7. 启动前端项目

平台端前端：

```bash
cd vue/foodie_plateform
npm install
npm run dev
```

商家端前端：

```bash
cd vue/foodie_merchant
npm install
npm run dev
```

用户端前端：

```bash
cd vue/my_foodie_app
npm install
npm run dev
```

## 访问地址

| 模块 | 地址 |
| --- | --- |
| 平台端前端 | `http://localhost:5343` |
| 商家端前端 | `http://localhost:5342` |
| 用户端前端 | `http://localhost:5341` |
| 平台端接口文档 | `http://localhost:8081/doc.html` |
| 商家端接口文档 | `http://localhost:8082/doc.html` |
| 用户端接口文档 | `http://localhost:8083/doc.html` |

## 默认端口

| 服务 | 端口 |
| --- | --- |
| foodie-platform-server | `8081` |
| foodie-merchant-server | `8082` |
| foodie-user-server | `8083` |
| 平台端前端 | `5343` |
| 商家端前端 | `5342` |
| 用户端前端 | `5341` |

## 演示账号

- 平台端：`admin / 123456`
- 商家端：`admin01 / 123456`
- 用户端：首次使用请自行注册

数据库重置后请以实际数据为准。

## 前端环境变量说明

### 平台端前端 `vue/foodie_plateform`

- `VITE_API_BASE_URL`：默认 `http://localhost:8081/platform`

### 商家端前端 `vue/foodie_merchant`

- `VITE_API_BASE_URL`：默认 `http://localhost:8082`
- `VITE_AMAP_KEY`：高德地图 Web 服务 Key
- `VITE_AMAP_SECURITY_JS_CODE`：高德安全密钥

### 用户端前端 `vue/my_foodie_app`

- `VITE_API_BASE_URL`：默认 `/user`
- `VITE_AMAP_KEY`：高德地图 Web 服务 Key

## 模拟能力说明

以下功能默认采用本地模拟逻辑，不依赖真实第三方平台：

- 短信验证码
- 微信登录
- 订单支付

因此本地演示时通常不需要额外准备短信平台、微信开放平台或微信支付商户能力。

## 推荐启动顺序

1. MySQL
2. Redis
3. MinIO
4. 三个后端服务
5. 三个前端项目

## 常见问题

### 前端启动后接口报错

- 检查对应后端是否已经启动
- 检查前后端端口是否正确
- 检查前端 `VITE_API_BASE_URL` 是否配置正确

### 后端启动时报数据库连接失败

- 检查 MySQL 是否启动
- 检查 `foodie_alliance` 是否已创建并导入脚本
- 检查数据库用户名、密码、IP、端口配置

### 图片上传失败

- 检查 MinIO 是否启动
- 检查 `foodie-images` 桶是否存在
- 检查 `MINIO_ENDPOINT` 和 `MINIO_PUBLIC_URL` 是否正确

### 地图功能不可用

- 检查是否正确配置高德地图 Key

## 相关文档

- [使用手册](./使用手册.md)
- [数据库脚本](./sql/foodie_alliance.sql)
- [平台端前端说明](./vue/foodie_plateform/README.md)
- [平台端目录架构说明](./vue/foodie_plateform/平台端目录架构说明.md)
- [商家端目录架构说明](./vue/foodie_merchant/商家端目录架构说明.md)
- [用户端目录架构说明](./vue/my_foodie_app/项目目录架构说明.md)

## 说明

- 当前仓库更适合作为课程设计、毕业设计、项目演示或学习练习使用
- 默认配置中包含示例账号和内网地址，部署到正式环境前请先完成安全化处理
- 如需补充部署文档、接口说明、ER 图或演示截图，可在此 README 基础上继续扩展
