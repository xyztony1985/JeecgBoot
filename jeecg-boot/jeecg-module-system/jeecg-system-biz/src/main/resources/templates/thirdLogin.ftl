<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <title>第三方登录</title>
</head>
<body>
<#if failMsg??>
    <p>登录失败：${failMsg}</p>
<#else>
    <p>登录中...</p>
    <p id="js-error"></p>
    <script>
        <#noparse>
        window.addEventListener('error', e => {
            document.querySelector('#js-error').innerText = `js error:\n${e.error.stack}`;
        });
        </#noparse>

        window.onload = function () {
            setTimeout(function (){
                var thirdLoginInfo = '';
                var thirdLoginModel = '${thirdLoginModel!""}';
                if(thirdLoginModel){
                    thirdLoginInfo = JSON.parse(thirdLoginModel);
                    thirdLoginInfo['isObj'] = true
                }
                window.opener.postMessage(thirdLoginInfo, "*");
                window.close();
            },1000)
        }
    </script>
</#if>
</body>
</html>
