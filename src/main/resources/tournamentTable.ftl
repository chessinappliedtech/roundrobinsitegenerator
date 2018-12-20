<table class="table table-bordered table-hover">
    <caption style="text-align: left;caption-side: top">
        <h2>${tournamentDescription.tournamentTitle}</h2>
    </caption>
    <thead class="thead-light">
    <tr>
        <th></th>
        <th>Игрок</th>
<#list 1..playersCount as index>
        <th class="text-center">${index}</th>
</#list>
        <th class="text-center">Сыграно<br>партий</th>
        <th class="text-center">Очки</th>
        <th class="text-center">Побед</th>
        <th class="text-center">Neu (SB)</th>
        <th class="text-center">Koya</th>
        <th class="text-center">Место</th>
    </tr>
    </thead>
    <tbody>
<#list tournamentTable.tournamentPlayers as tournamentPlayer>
    <tr>
        <td>${tournamentPlayer?index+1}</td>
        <td><a href="${tournamentPlayer.page}">${tournamentPlayer.firstName} ${tournamentPlayer.lastName}</a></td>
<#list tournamentPlayer.scores as score>
<#if tournamentPlayer?index == score?index>
        <td style="background: gray"></td>
<#else>
        <td class="text-center">${score!}</td>
</#if>
</#list>
        <td class="text-center">${tournamentPlayer.gamesPlayed}</td>
        <td class="text-center">${tournamentPlayer.score}</td>
        <td class="text-center">${tournamentPlayer.wins}</td>
        <td class="text-center">${tournamentPlayer.neustadtl}</td>
        <td class="text-center">${tournamentPlayer.koya}</td>
        <td class="text-center"><b>${tournamentPlayer.rank}</b></td>
    </tr>
</#list>
    </tbody>
</table>