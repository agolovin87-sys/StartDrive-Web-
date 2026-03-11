const http = require('http');
const fs = require('fs');
const path = require('path');

const HOST = '0.0.0.0';

const RESOURCES_DIR = path.join(__dirname, 'webApp/build/processedResources/js/main');
const WEBPACK_DIR = path.join(__dirname, 'webApp/build/kotlin-webpack/js/developmentExecutable');

const mimeTypes = {
  '.html': 'text/html',
  '.js': 'application/javascript',
  '.css': 'text/css',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.json': 'application/json',
};

function requestHandler(req, res) {
  let urlPath = req.url.split('?')[0];
  if (urlPath === '/') urlPath = '/index.html';

  let filePath = path.join(WEBPACK_DIR, urlPath);
  if (!fs.existsSync(filePath)) {
    filePath = path.join(RESOURCES_DIR, urlPath);
  }

  if (!fs.existsSync(filePath) || !fs.statSync(filePath).isFile()) {
    filePath = path.join(RESOURCES_DIR, 'index.html');
  }

  const ext = path.extname(filePath).toLowerCase();
  const contentType = mimeTypes[ext] || 'application/octet-stream';

  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(500);
      res.end('Server error: ' + err.message);
      return;
    }
    res.writeHead(200, {
      'Content-Type': contentType,
      'Cache-Control': 'no-store, no-cache, must-revalidate',
      'Pragma': 'no-cache',
    });
    res.end(data);
  });
}

const server5000 = http.createServer(requestHandler);
server5000.listen(5000, HOST, () => {
  console.log(`StartDrive web server running at http://${HOST}:5000`);
});

const server31997 = http.createServer(requestHandler);
server31997.listen(31997, HOST, () => {
  console.log(`StartDrive web server also running at http://${HOST}:31997 (external port 80)`);
});
