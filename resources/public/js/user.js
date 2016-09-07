$(function() {
  $("table a").click(
    function (event) {
      openFile(event,
               this.parentNode.parentNode.childNodes[0].title,
               this.href);
    });
});

function openFile(event, type, href) {
  event.preventDefault();

  if (window.fetch) {
    window.fetch(href)
      .then(response => response.text())
      .then(text => {
        let contents = document.getElementById("contents");
        contents.innerHTML = text;
        if (type) {
          contents.className = type;
          hljs.highlightBlock(contents);
        };
      });
  }
}
