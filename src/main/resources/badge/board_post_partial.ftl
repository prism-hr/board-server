[#ftl]
<h1>
    <a href="${applicationUrl}/${board.handle}/${post.id}">
    ${post.name}
    </a>
</h1>
<h2>at <span>${department.name}</span></h2>
<div class="short-description-holder">
    <ul class="short-description-details">
        <li>
        [#if post.deadTimestamp??]
            <span class="title">Closing Date</span>
        ${post.deadTimestamp.format('dd MMM yyyy')}
        [#else]
            <span class="title">No Closing Date</span>
        [/#if]
        </li>
    </ul>
</div>

[#if board.documentLogo??]
<div class="image-container">
    <div class="image-flex">
        <div class="image-logo">
            <img src="${board.documentLogo.cloudinaryUrl}">
        </div>
    </div>
</div>
[/#if]

<p class="summary">
${post.summary}
</p>

