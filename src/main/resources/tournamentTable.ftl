<table class="table table-bordered table-hover">
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
                    <td style="background: gray" class="fixed-square"></td>
                <#else>
                    <td class="text-center fixed-square">
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
