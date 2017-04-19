var uriParams={};

window.location.search.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(str, key, value) {
    uriParams[key] = decodeURIComponent(value);
});

function copyToClipboard(text) {
    var tmp = $('<input/>')
        .val(text)
        .appendTo(document.body)
        .select()

    document.execCommand('copy');

    tmp.remove()
}
