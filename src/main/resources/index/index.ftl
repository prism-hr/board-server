[#ftl]
<!doctype html>
<html lang="en">
<head>
    <title>${title}</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta property="og:title" content="${title}">
    <meta property="og:description" content="${description}">
    <meta property="og:url" content="${url}">
[#if image??]
    <meta property="og:image" content="${image}">
[/#if]

    <base href="/">

    <link rel="icon" type="image/x-icon" href="favicon.ico">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
</head>
<body>
<b-app-root>
    <div class="preloader">
        <div class="spinner"></div>
    </div>
</b-app-root>
</body>
</html>
