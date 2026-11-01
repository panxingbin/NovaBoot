# syntax=docker/dockerfile:1
# NovaBoot 前端鏡像：pnpm 構建（web/apps/admin）→ nginx 託管靜態資源

# ---------- 構建階段 ----------
FROM node:24-alpine AS build

# 倉庫未在 package.json 中鎖定 packageManager，固定安裝 pnpm 11 保證構建可重現
RUN npm i -g pnpm@11

WORKDIR /web

# 1) 先拷貝 workspace 清單與各包 package.json，單獨安裝依賴以利用層緩存
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/admin/package.json apps/admin/
RUN pnpm install --frozen-lockfile \
    && cp -a node_modules /tmp/deps-root-node_modules \
    && cp -a apps/admin/node_modules /tmp/deps-admin-node_modules

# 2) 拷貝源碼並構建（產物：apps/admin/dist）
# 主機上若已執行過 pnpm install，其 node_modules（含 darwin 等平台二進制）會隨 COPY 覆蓋
# 上一步安裝結果，故先移除再還原緩存的依賴目錄。
COPY . .
RUN rm -rf node_modules apps/admin/node_modules \
    && cp -a /tmp/deps-root-node_modules node_modules \
    && cp -a /tmp/deps-admin-node_modules apps/admin/node_modules \
    && pnpm --filter admin build

# ---------- 運行階段 ----------
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /web/apps/admin/dist /usr/share/nginx/html

EXPOSE 80
