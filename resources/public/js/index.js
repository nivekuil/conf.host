function toggleLights() {
  let lights = document.body.className;
  if (lights === "on") {
    document.body.className = "off";
    document.cookie = "lights=off";
    document.getElementById("toggle").innerHTML = "Lights on";
  }
  else {
    document.body.className = "on";
    document.cookie = "lights=on";
    document.getElementById("toggle").innerHTML = "Lights off";
  }
}

$(document).ready(function() {
  $("form").submit(function(e) {
    e.preventDefault();
    console.log("yo");
    let url = $(location).attr('href');
    window.fetch(url, {method: 'POST'})
      .then(response => response.text())
      .then(text => $("#message").html(text));
  });

});
