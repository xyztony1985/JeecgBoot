@echo off

setlocal
pushd %~dp0
cd /d jeecg-module-system

rem 启动后端服务
echo start backend...

rem 运行项目
mvn spring-boot:run -pl jeecg-system-start -am
