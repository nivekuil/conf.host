function toggleLights() {
  var lights = document.body.className;
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
