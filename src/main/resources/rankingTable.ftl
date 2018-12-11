<table id="current-ranking">
    <caption style="text-align:left"><h3>Current ranking</h3></caption>
    <tr>
        <th>Player name</th>
        <th>Rank</th>
        <th>Score</th>
        <th>Games played</th>
    </tr>
<#list rankingTable.rankedPlayers as rankedPlayer>
    <tr>
        <td><a href="${rankedPlayer.page}">${rankedPlayer.firstname} ${rankedPlayer.lastname}</a></td>
        <td>${rankedPlayer.rank}</td>
        <td>${rankedPlayer.score}</td>
        <td>${rankedPlayer.gamesPlayed}</td>
    </tr>
</#list>
</table>
