[#ftl]
<h1>
    <a href="${applicationUrl}/${board.handle}/${post.id}">
    ${post.name}
    </a>
</h1>
<h2>
    <em>at</em> ${post.organizationName}
    <span class="location"><i class="material-icons md-18">location_on</i> ${post.location.name} </span>


</h2>

<div class="advert-content">
    [#if board.documentLogo??]
        <div class="image-container">
            <div class="image-flex">
                <div class="image-logo">
                    <img src="${board.documentLogo.cloudinaryUrl}">
                </div>
            </div>
        </div>
    [/#if]
    <div class="summary-content">
    [#if post.deadTimestamp??]<div class="closing-date">Closing Date: ${post.deadTimestamp.format('dd MMM yyyy')}</div>[/#if]
        <div class="summary">
        ${post.summary}
        </div>
        <div class="prism-apply-holder">
            <a href="${applicationUrl}/${board.handle}/${post.id}"
               class="btn btn-classic" target="_blank">Read More ></a>
        </div>
    </div>
</div>




