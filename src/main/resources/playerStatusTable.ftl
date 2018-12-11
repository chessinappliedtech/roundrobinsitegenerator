<html lang="en">
<head>
    <title>${player.firstname} ${player.lastname}</title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="playerStatusTable.css">
</head>
<body>
<table id="player-status">
    <caption style="text-align:left">
        <h2>${player.firstname} ${player.lastname}</h2>
    </caption>
    <tr>
        <th>Противник</th>
        <th>№</th>
        <th>Цвет</th>
        <th>Результат</th>
    </tr>
<#list opponents as opponent>
    <#list opponent.games as game>
        <#if game?is_first>
            <tr>
            <td rowspan="${maxGames}"><a href="${opponent.page}">${opponent.firstname} ${opponent.lastname}</a></td>
            <td>${game.index}</td>
            <td>${game.color}</td>
            <td>${game.score}</td>
            </tr>
        <#else>
            <tr>
            <td>${game.index}</td>
            <td>${game.color}</td>
            <td>${game.score}</td>
            </tr>
        </#if>
    </#list>
</#list>
    <tr>
        <td>Итог</td>
        <td>${total.gamesPlayed}</td>
        <td></td>
        <td>${total.score}</td>
    </tr>
</table>
</body>
</html>