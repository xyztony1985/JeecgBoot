@echo off

setlocal
pushd %~dp0
cd /d jeecg-module-system\jeecg-system-start

rem 启动后端服务
echo start backend...

rem 运行项目
mvn spring-boot:run
