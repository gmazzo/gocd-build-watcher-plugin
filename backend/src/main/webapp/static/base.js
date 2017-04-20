
function copyToClipboard(text) {
    var tmp = $('<input/>')
        .val(text)
        .appendTo(document.body)
        .select()

    document.execCommand('copy');

    tmp.remove()
}
