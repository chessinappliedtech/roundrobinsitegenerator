<html lang="en">
<head>
    <title>${player.firstname} ${player.lastname}</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="stylesheet"
          href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"
          integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO"
          crossorigin="anonymous">
</head>
<body>
<div class="container">
<table class="table table-bordered table-hover">
    <caption style="text-align:left;caption-side: top">
        <h2>${player.firstname} ${player.lastname}</h2>
    </caption>
    <thead class="thead-light">
    <tr>
        <th>Противник</th>
        <th class="text-center">№</th>
        <th class="text-center">Цвет</th>
        <th class="text-center">Результат</th>
    </tr>
    </thead>
    <tbody>
<#list opponents as opponent>
    <#list opponent.games as game>
        <#if game?is_first>
            <tr>
            <td rowspan="${maxGames}"><a href="${opponent.page}">${opponent.firstname} ${opponent.lastname}</a></td>
            <td class="text-center">${game.index}</td>
            <td class="text-center">${game.color}</td>
            <td class="text-center">${game.score}</td>
            </tr>
        <#else>
            <tr>
            <td class="text-center">${game.index}</td>
            <td class="text-center">${game.color}</td>
            <td class="text-center">${game.score}</td>
            </tr>
        </#if>
    </#list>
</#list>
    <tr>
        <td>Итог</td>
        <td class="text-center">${total.gamesPlayed}</td>
        <td></td>
        <td class="text-center">${total.score}</td>
    </tr>
    </tbody>
</table>
</div>
</body>
</html>