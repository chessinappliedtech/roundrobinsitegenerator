<table class="table table-bordered table-hover">
    <caption style="text-align: left;caption-side: top">
        <h2>${getTournamentTitle()}</h2>
    </caption>
    <thead class="thead-light">
    <tr>
        <#list getHeaderRowView().getCells() as headerCell>
            <th class="text-center">${headerCell.getValue()}</th>
        </#list>
    </tr>
    </thead>
    <tbody>
    <#list getPlayerRowViews() as playerRow>
        <tr>
            <#list playerRow.getCells() as headerCell>
                <#if isDiagonalCell(headerCell)>
                    <td style="background: gray"></td>
                <#else>
                    <td class="text-center">
                        <#if headerCell.getLink()??>
                            <a href="${headerCell.getLink()}">${headerCell.getValue()}</a>
                        <#else>
                            ${headerCell.getValue()}
                        </#if>
                    </td>
                </#if>
            </#list>
        </tr>
    </#list>
    </tbody>
</table>
