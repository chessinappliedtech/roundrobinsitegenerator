<table id="tournament-table">
    <caption style="text-align: left">
        <h2>${tournamentTitle}</h2>
    </caption>
    <tr>
        <th></th>
        <th>Player</th>
<#list 1..playersCount as index>
        <th>${index}</th>
</#list>
        <th>Score</th>
        <th>Rank</th>
    </tr>
<#list tournamentTable.tournamentPlayers as tournamentPlayer>
    <tr>
        <td>${tournamentPlayer?index+1}</td>
        <td><a href="${tournamentPlayer.page}">${tournamentPlayer.firstname} ${tournamentPlayer.lastname}</a></td>
<#list tournamentPlayer.scores as score>
        <td>${score!}</td>
</#list>
        <td>${tournamentPlayer.score}</td>
        <td>${tournamentPlayer.rank}</td>
    </tr>
</#list>
</table>