<table id="tournament-table">
    <caption style="text-align: left">
        <h2>${tournamentDescription.tournamentTitle}</h2>
    </caption>
    <tr>
        <th></th>
        <th>Игрок</th>
<#list 1..playersCount as index>
        <th>${index}</th>
</#list>
        <th>Очки</th>
        <th>Сыграно партий</th>
        <th>Место</th>
    </tr>
<#list tournamentTable.tournamentPlayers as tournamentPlayer>
    <tr>
        <td>${tournamentPlayer?index+1}</td>
        <td><a href="${tournamentPlayer.page}">${tournamentPlayer.firstname} ${tournamentPlayer.lastname}</a></td>
<#list tournamentPlayer.scores as score>
<#if tournamentPlayer?index == score?index>
        <td style="background: gray"></td>
<#else>
        <td>${score!}</td>
</#if>
</#list>
        <td>${tournamentPlayer.score}</td>
        <td>${tournamentPlayer.gamesPlayed}</td>
        <td>${tournamentPlayer.rank}</td>
    </tr>
</#list>
</table>