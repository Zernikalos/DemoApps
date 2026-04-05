# DemoApps scripts

## build-assets.mjs

Converts every supported 3D asset under `assets/raw/` into `.zko` files under `assets/zko/`, keeping the same folder structure.

### Supported inputs

- `.glb`, `.gltf`
- `.dae` (Collada)
- `.fbx`
- `.obj`

### Usage

From `DemoApps/`:

```bash
node scripts/build-assets.mjs
```

### Choosing the CLI

By default, the script tries to run the **local** CLI from:

`../ZKBuilder/packages/zkbuilder-cli/bin/zkcli`

To verify against the **published** CLI, run with:

```bash
ZKCLI_MODE=dlx node scripts/build-assets.mjs
```

That uses:

```bash
pnpm dlx @zernikalos/zkbuilder-cli zkcli ...
```

