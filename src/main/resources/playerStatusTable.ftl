<html>
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
        <th>Opponent</th>
        <th>Game</th>
        <th>Color</th>
        <th>Score</th>
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
            <td>${game.index}</td>
            <td>${game.color}</td>
            <td>${game.score}</td>
        </#if>
    </#list>
</#list>
    <tr>
        <td>Summary</td>
        <td>${total.gamesPlayed}</td>
        <td></td>
        <td>${total.score}</td>
    </tr>
</table>
</body>
</html>