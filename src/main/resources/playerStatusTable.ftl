<html lang="en">
<head>
    <title>${player.firstName} ${player.lastName}</title>
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
        <h2>${player.firstName} ${player.lastName}</h2>
    </caption>
    <thead class="thead-light">
    <tr>
        <th>Противник</th>
        <th class="text-center">№</th>
        <th class="text-center">Цвет</th>
        <th class="text-center">Результат</th>
        <th class="text-center">Дата</th>
    </tr>
    </thead>
    <tbody>
<#list opponents as opponent>
    <#list opponent.games as game>
        <#if game?is_first>
            <tr>
            <td rowspan="${maxGames}"><a href="${opponent.page}">${opponent.firstName} ${opponent.lastName}</a></td>
            <td class="text-center">
                <#if game.lichess??>
                    <a href="${game.lichess}">${game.index}</a>
                <#else>
                    ${game.index}
                </#if>
            </td>
            <td class="text-center">${game.color}</td>
            <td class="text-center">${game.score}</td>
            <td class="text-center">${game.date}</td>
            </tr>
        <#else>
            <tr>
            <td class="text-center">
                <#if game.lichess??>
                    <a href="${game.lichess}">${game.index}</a>
                <#else>
                    ${game.index}
                </#if>
            </td>
            <td class="text-center">${game.color}</td>
            <td class="text-center">${game.score}</td>
            <td class="text-center">${game.date}</td>
            </tr>
        </#if>
    </#list>
</#list>
    <tr>
        <td>Итог</td>
        <td class="text-center">${total.gamesPlayed}</td>
        <td></td>
        <td class="text-center">${total.score}</td>
        <td></td>
    </tr>
    </tbody>
</table>
</div>
</body>
</html>