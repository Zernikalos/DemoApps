import { spawnSync } from "node:child_process";
import { existsSync, mkdirSync } from "node:fs";
import { readdirSync, statSync } from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(import.meta.dirname, ".."); // DemoApps
const rawRoot = path.join(repoRoot, "assets", "raw");
const outRoot = path.join(repoRoot, "assets", "zko");

const SUPPORTED_EXTS = new Set([".glb", ".gltf", ".fbx", ".obj", ".dae"]);

function walkFiles(dir) {
  const entries = readdirSync(dir, { withFileTypes: true });
  const out = [];
  for (const e of entries) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) {
      out.push(...walkFiles(p));
    } else if (e.isFile()) {
      out.push(p);
    } else {
      // ignore symlinks and others
    }
  }
  return out;
}

function detectInputFormat(filePath) {
  const ext = path.extname(filePath).toLowerCase();
  switch (ext) {
    case ".glb":
    case ".gltf":
      return "gltf";
    case ".fbx":
      return "fbx";
    case ".obj":
      return "obj";
    case ".dae":
      return "collada";
    default:
      return undefined;
  }
}

function getZkcliCommand() {
  // ZKCLI_MODE:
  // - "local": run from ../ZKBuilder/packages/zkbuilder-cli/bin/zkcli (default if exists)
  // - "dlx": run via "pnpm dlx @zernikalos/zkbuilder-cli zkcli"
  const mode = (process.env.ZKCLI_MODE ?? "").toLowerCase();
  const localZkcli = path.resolve(
    repoRoot,
    "..",
    "ZKBuilder",
    "packages",
    "zkbuilder-cli",
    "bin",
    "zkcli",
  );

  if ((mode === "" || mode === "local") && existsSync(localZkcli)) {
    return { cmd: localZkcli, argsPrefix: [] };
  }

  return { cmd: "pnpm", argsPrefix: ["dlx", "@zernikalos/zkbuilder-cli", "zkcli"] };
}

function ensureDir(filePath) {
  mkdirSync(path.dirname(filePath), { recursive: true });
}

function shouldRebuild(inputPath, outputPath) {
  if (!existsSync(outputPath)) return true;
  const inStat = statSync(inputPath);
  const outStat = statSync(outputPath);
  return inStat.mtimeMs > outStat.mtimeMs;
}

function main() {
  if (!existsSync(rawRoot)) {
    console.error(`Missing raw assets folder: ${rawRoot}`);
    process.exit(1);
  }
  ensureDir(outRoot);

  const { cmd, argsPrefix } = getZkcliCommand();

  const allFiles = walkFiles(rawRoot);
  const inputs = allFiles.filter((f) => SUPPORTED_EXTS.has(path.extname(f).toLowerCase()));

  if (inputs.length === 0) {
    console.log(`No supported assets found under ${rawRoot}`);
    return;
  }

  let converted = 0;
  let skipped = 0;

  for (const input of inputs) {
    const rel = path.relative(rawRoot, input);
    const output = path.join(outRoot, rel).replace(/\.[^.]+$/, ".zko");

    if (!shouldRebuild(input, output)) {
      skipped += 1;
      continue;
    }

    ensureDir(output);

    const inputFormat = detectInputFormat(input);
    const args = [
      ...argsPrefix,
      "-i",
      input,
      "-o",
      output,
      "--of",
      "proto",
    ];

    // Be explicit to avoid extension-based edge cases.
    if (inputFormat) args.push("-f", inputFormat);

    console.log(`Converting: ${rel} -> ${path.relative(repoRoot, output)}`);
    const r = spawnSync(cmd, args, { stdio: "inherit" });
    if (r.status !== 0) process.exit(r.status ?? 1);

    converted += 1;
  }

  console.log(`Done. Converted: ${converted}, skipped (up-to-date): ${skipped}`);
  console.log(`Output folder: ${outRoot}`);
  console.log(
    `Tip: set ZKCLI_MODE=dlx to verify the published CLI (requires pnpm + registry access).`,
  );
}

main();

