@echo off

setlocal
pushd %~dp0
cd /d jeecg-module-system\jeecg-system-start

rem 启动后端服务
echo 正在启动后端服务...

rem 运行项目
mvn spring-boot:run 
