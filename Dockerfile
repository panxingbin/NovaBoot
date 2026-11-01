# syntax=docker/dockerfile:1
# NovaBoot 後端鏡像：多階段構建（Maven 打包 → JRE 運行）
#

# ---------- 構建階段 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 父 POM 通過 import nova-dependencies BOM 統一管理三方版本，
# 拆文件單獨 COPY 難保證模型完整，這裡直接整倉拷貝後一次性打包。
# 提示：倉庫若存在本地 node_modules/.m2repo 等，建議配合 .dockerignore 縮小構建上下文。
COPY . .
RUN mvn -q -DskipTests package

# ---------- 運行階段 ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /build/nova-server-admin/target/nova-server-admin.jar /app/nova-server-admin.jar

# 非 root 運行；/app/data/files 對應 nova.storage.local-path（檔案上傳本地存儲目錄）
# 不指定固定 uid：基礎鏡像（Ubuntu 24.04）已內置 uid 1000 的 ubuntu 用戶，讓 useradd 自行分配
RUN useradd --system --no-create-home nova \
    && mkdir -p /app/data/files \
    && chown -R nova:nova /app
USER nova

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/nova-server-admin.jar"]
