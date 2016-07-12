$(function() {
  $("table a").click(
    function (event) {
      someMethodName(event,
                     this.parentNode.parentNode.childNodes[0].title,
                     this.href);
    });
});

function someMethodName(event, type, href) {
  event.preventDefault();
  if (window.fetch) {
    window.fetch(href)
      .then(response => response.text())
      .then(text => {
        let contents = document.getElementById("contents");
        contents.innerHTML = text;
        contents.className = type;
        hljs.highlightBlock(contents);
      });
  }
}
