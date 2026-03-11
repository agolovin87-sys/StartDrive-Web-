// Раздача папки pdd (билеты ПДД) в dev-сервере: сначала из cwd (куда Gradle копирует pdd), затем из processedResources
const path = require('path');
const fs = require('fs');

(function (config) {
  if (!config.devServer) config.devServer = {};
  var staticDirs = Array.isArray(config.devServer.static)
    ? config.devServer.static
    : config.devServer.static?.directory
      ? [config.devServer.static]
      : [];
  var cwd = process.cwd();
  var cwdPdd = path.join(cwd, 'pdd');
  var processedResources = null;
  var candidates = [
    path.resolve(__dirname, '..', 'build', 'processedResources', 'js', 'main'),
    path.resolve(cwd, 'webApp', 'build', 'processedResources', 'js', 'main'),
    path.resolve(cwd, 'build', 'processedResources', 'js', 'main'),
    path.resolve(cwd, '..', '..', '..', '..', 'webApp', 'build', 'processedResources', 'js', 'main')
  ];
  for (var i = 0; i < candidates.length; i++) {
    try {
      if (fs.existsSync(path.join(candidates[i], 'pdd', 'tickets'))) {
        processedResources = candidates[i];
        break;
      }
    } catch (_) {}
  }
  var pddDir = fs.existsSync(path.join(cwdPdd, 'tickets')) ? cwdPdd : (processedResources ? path.join(processedResources, 'pdd') : null);
  var newStatic = [];
  if (fs.existsSync(cwdPdd)) {
    newStatic.push({ directory: cwd, publicPath: '/' });
  }
  if (processedResources) {
    newStatic.push({ directory: processedResources, publicPath: '/' });
  }
  config.devServer.static = newStatic.length ? newStatic.concat(staticDirs) : staticDirs;
  if (pddDir) {
    var existingBefore = config.devServer.onBeforeSetupMiddleware;
    config.devServer.onBeforeSetupMiddleware = function (devServer) {
      if (existingBefore) existingBefore(devServer);
      devServer.app.get('/pdd/*', function (req, res, next) {
        var rawPath = req.path.replace(/^\/pdd\/?/, '');
        var segments = rawPath.split('/').filter(Boolean);
        var isNumericTicket = segments.length >= 1 && /^\d+\.json$/.test(segments[segments.length - 1]);
        if (isNumericTicket) {
          var num = segments[segments.length - 1].replace(/\.json$/, '');
          var ticketFileName = '\u0411\u0438\u043b\u0435\u0442 ' + num + '.json';
          if (segments.length === 2 && segments[0] === 'C_D') {
            segments = ['tickets', 'C_D', ticketFileName];
          } else {
            segments = ['tickets', ticketFileName];
          }
        } else {
          try {
            segments = rawPath.split('/').map(function (s) { return decodeURIComponent(s); });
          } catch (e) {
            return next();
          }
        }
        var safePath = path.join.apply(path, [pddDir].concat(segments));
        var normPdd = path.normalize(pddDir);
        var normSafe = path.normalize(safePath);
        if (normSafe.indexOf(normPdd) !== 0) return next();
        fs.stat(safePath, function (err, stat) {
          if (err || !stat.isFile()) return next();
          fs.readFile(safePath, function (err, data) {
            if (err) return next();
            var ext = path.extname(safePath).toLowerCase();
            var contentType = ext === '.json' ? 'application/json; charset=utf-8'
              : ext === '.svg' ? 'image/svg+xml'
              : ext === '.png' ? 'image/png'
              : ext === '.jpg' || ext === '.jpeg' ? 'image/jpeg'
              : ext === '.webp' ? 'image/webp'
              : 'application/octet-stream';
            res.setHeader('Content-Type', contentType);
            res.end(data);
          });
        });
      });
    };
  }
})(config);
