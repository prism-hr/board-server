[#ftl]
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>PRiSM - Board</title>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons"
          rel="stylesheet">
</head>
<body>

<div class="opportunities">
[#if options.badgeType == "SIMPLE"]
<div class="prism-connect">
[#else]
<div class="prism-connect opportunity list">
[/#if]
    <div class="prism-header">
        <div class="logo">
            <a href="${applicationUrl}" class="navbar-brand" target="_blank"><img
                src="${applicationUrl}/assets/prism.svg" alt="PRiSM"></a>
        </div>
        <div class="sub-header"> - Board</div>
    [#if options.badgeListType == "SLIDER"]
        <div class="control">
            <a class="btn control_prev"> &#60 </a>
            <span class="position-number"></span> /
            <span class="position-total"></span>
            <a class="btn control_next"> &#62 </a>
        </div>
    [/#if]
    </div>

[#if options.badgeType == "SIMPLE"]
    <div class="prism-main">
        [#include "badge_footer.ftl"]
    </div>
[#else] [#-- LIST --]

    <div class="prism-main ${options.badgeListType?lower_case}">
        <ul>
            [#list posts as post]
                <li>
                    [#include "badge_post_partial.ftl"]
                </li>
            [/#list]
        </ul>
    </div>

    <div class="prism-footer">
        [#include "badge_footer.ftl"]
    </div>
[/#if]
</div>
</div>

</body>
</html>
