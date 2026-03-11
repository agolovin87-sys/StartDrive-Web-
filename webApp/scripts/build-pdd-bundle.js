"use strict";
var fs = require("fs");
var path = require("path");

var resourcesDir = path.join(__dirname, "..", "src", "jsMain", "resources");
var pddTicketsDir = path.join(resourcesDir, "pdd", "tickets");
var outFile = path.join(resourcesDir, "pdd-tickets-bundle.json");

function readTicketNum(filename) {
  var m = filename.match(/Билет\s*(\d+)\.json$/) || filename.match(/(\d+)\.json$/);
  return m ? m[1] : null;
}

function buildBundle() {
  var bundle = { A_B: {}, C_D: {} };

  function addCategory(catDir, key) {
    if (!fs.existsSync(catDir)) return;
    try {
    var files = fs.readdirSync(catDir);
    files.forEach(function (name) {
      var num = readTicketNum(name);
      if (num) {
        var filePath = path.join(catDir, name);
        try {
          var raw = fs.readFileSync(filePath, "utf8");
          var data = JSON.parse(raw);
          bundle[key][num] = data;
        } catch (e) {
          console.warn("Skip " + filePath + ": " + e.message);
        }
      }
    });
    } catch (e) {
      console.warn("Skip category " + catDir + ": " + e.message);
    }
  }

  if (fs.existsSync(pddTicketsDir)) {
    addCategory(pddTicketsDir, "A_B");
    addCategory(path.join(pddTicketsDir, "C_D"), "C_D");
  } else {
    console.warn("Tickets dir not found: " + pddTicketsDir + " (run copyPddToWeb first)");
  }

  var dir = path.dirname(outFile);
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
  var jsonStr = JSON.stringify(bundle);
  fs.writeFileSync(outFile, jsonStr, "utf8");
  var outJs = path.join(resourcesDir, "pdd-tickets-bundle.js");
  var jsContent = "window.__PDD_TICKETS_BUNDLE__=" + jsonStr.replace(/<\/script/gi, "<\\/script") + ";";
  fs.writeFileSync(outJs, jsContent, "utf8");
  console.log("Wrote " + outFile + " and " + outJs + " (A_B: " + Object.keys(bundle.A_B).length + ", C_D: " + Object.keys(bundle.C_D).length + " tickets)");
}

buildBundle();
