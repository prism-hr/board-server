[#ftl]
<h1>
    <a href="${applicationUrl}/${resource.handle}/${post.id}">
    ${post.name}
    </a>
</h1>
<h2>
    <em>at</em> ${post.organizationName}
    <span class="defaultLocation"><i class="material-icons md-18">location_on</i> ${post.defaultLocation.name} </span>


</h2>

<div class="advert-content">
    [#if resource.documentLogo??]
        <div class="image-container">
            <div class="image-flex">
                <div class="image-logo">
                    <img src="${resource.documentLogo.cloudinaryUrl}">
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
            <a href="${applicationUrl}/${resource.handle}/${post.id}"
               class="btn btn-classic" target="_blank">Read More ></a>
        </div>
    </div>
</div>




