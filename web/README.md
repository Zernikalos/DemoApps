# Web demo

This folder will contain the web samples/demos.

## Run

```bash
pnpm install
pnpm dev
```

Open the demos:

- `http://localhost:7070/` (index)
- `http://localhost:7070/examples/fox.html`
- `http://localhost:7070/examples/soldier2.html`
- `http://localhost:7070/examples/stormtrooper.html`

The dev server is configured to:

- serve the local SDK at `/sdk/zernikalos.js` (and `/zernikalos.js`)
- serve `.zko` files from the repository root at `/zko/...`

If the SDK is in a different location, override it:

```bash
ZK_SDK_PATH=/absolute/path/to/zernikalos.js pnpm dev
```

## Assets

Use the repository-level script to generate `.zko` files:

```bash
cd ..
node scripts/build-assets.mjs
```

## Examples

The documentation-style demos are plain HTML files under `examples/`:

- `examples/fox.html`
- `examples/soldier2.html`
- `examples/stormtrooper.html`

