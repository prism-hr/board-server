[#ftl]
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>PRiSM</title>
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
                src="${applicationUrl}/assets/prism-white.png" alt="PRiSM"></a>
        </div>
        <div class="sub-header">Jobs & Work Experience</div>
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
        [#include "board_badge_footer.ftl"]
    </div>
[#else] [#-- LIST --]

    <div class="prism-main ${options.badgeListType?lower_case}">
        <ul>
            [#list posts as post]
                <li>
                    [#include "board_post_partial.ftl"]
                    <div class="prism-apply-holder">
                        <a href="${applicationUrl}/${board.handle}/${post.id}"
                           class="btn btn-success ng-scope" target="_blank">Read More</a>
                    </div>
                </li>
            [/#list]
        </ul>
    </div>

    <div class="prism-footer">
        [#include "board_badge_footer.ftl"]
    </div>
[/#if]
</div>
</div>

</body>
</html>
