import type { Plugin } from "vite";
import { defineConfig } from "vite";
import fs from "node:fs";
import path from "node:path";

function serveSingleFile(urlPath: string, filePath: string, contentType: string): Plugin {
  return {
    name: `serve:${urlPath}`,
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        if (!req.url) return next();
        const reqUrl = req.url.split("?")[0];
        if (reqUrl !== urlPath) return next();

        try {
          const data = fs.readFileSync(filePath);
          res.statusCode = 200;
          res.setHeader("Content-Type", contentType);
          res.end(data);
        } catch (e) {
          res.statusCode = 404;
          res.setHeader("Content-Type", "text/plain; charset=utf-8");
          res.end(`Missing file: ${filePath}`);
        }
      });
    },
  };
}

function serveDirectory(urlPrefix: string, dirPath: string): Plugin {
  const prefix = urlPrefix.endsWith("/") ? urlPrefix : `${urlPrefix}/`;
  return {
    name: `serve-dir:${prefix}`,
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        if (!req.url) return next();
        const reqUrl = req.url.split("?")[0];
        if (!reqUrl.startsWith(prefix)) return next();

        const rel = reqUrl.slice(prefix.length);
        const abs = path.resolve(dirPath, rel);
        if (!abs.startsWith(path.resolve(dirPath))) {
          res.statusCode = 403;
          res.end("Forbidden");
          return;
        }

        try {
          const data = fs.readFileSync(abs);
          res.statusCode = 200;
          res.setHeader("Content-Type", "application/octet-stream");
          res.end(data);
        } catch (e) {
          res.statusCode = 404;
          res.setHeader("Content-Type", "text/plain; charset=utf-8");
          res.end(`Missing file: ${abs}`);
        }
      });
    },
  };
}

export default defineConfig(() => {
  const demoRoot = path.resolve(__dirname, ".."); // DemoApps
  const sdkDefault = path.resolve(
    demoRoot,
    "..",
    "Zernikalos",
    "engine",
    "build",
    "dist",
    "js",
    "productionExecutable",
    "zernikalos.js",
  );

  const sdkPath = process.env.ZK_SDK_PATH
    ? path.resolve(process.env.ZK_SDK_PATH)
    : sdkDefault;

  const zkoDir = path.resolve(demoRoot, "assets", "zko");

  return {
    server: {
      port: 7070,
      strictPort: true,
    },
    plugins: [
      // The sample HTMLs used a CDN-like URL: http://localhost:7070/zernikalos.js
      // Keep that compatibility and also expose it under /sdk/ for clarity.
      serveSingleFile("/zernikalos.js", sdkPath, "application/javascript; charset=utf-8"),
      serveSingleFile("/sdk/zernikalos.js", sdkPath, "application/javascript; charset=utf-8"),
      // Assets should be loaded from the root zko folder.
      serveDirectory("/zko/", zkoDir),
    ],
  };
});

