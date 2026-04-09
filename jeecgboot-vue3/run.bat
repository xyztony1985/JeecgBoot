@echo off

setlocal
pushd %~dp0

echo 正在启动前端服务...

@REM echo 正在安装依赖...
@REM pnpm install --registry=https://registry.npmmirror.com


echo 正在运行项目...
pnpm dev
