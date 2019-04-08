<html lang="en">
<head>
    <title>${tournamentDescription.tournamentTitle}</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="stylesheet"
          href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"
          integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO"
          crossorigin="anonymous">
    <style>
        .fixed-square {
            min-width: 50px;
            height: 50px;
        }
    </style>
</head>
<body>
<div class="container-fluid">
<h2>${tournamentDescription.tournamentTitle}</h2><br>
${view}
<#if tournamentDescription.linkvalue??>
    <br>
    <a href="${tournamentDescription.linkvalue}">${tournamentDescription.linkname}</a><br>
    <br>
</#if>
<#include "regulations.ftl"/>
</div>
</body>
</html>