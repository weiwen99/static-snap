# static-snap (s5)

<b>S</b>imple <b>S</b>cala <b>S</b>tatic <b>S</b>nap <b>S</b>ervice — a lightweight, high-performance static file server built with Scala 3 and http4s. Serves files and directories with an interactive Material Design web interface, supporting sorting, filtering, and Markdown rendering.

## Features

- **Directory Listing** — Interactive HTML index with Material Design UI, sortable by name, size, type, and timestamps
- **Markdown Rendering** — `.md` files are automatically rendered as HTML via Markdeep
- **Range Requests** — Supports partial content for audio/video seeking
- **GraalVM Native Image** — Compiles to a single native binary for fast startup
- **Security** — Path traversal prevention, restricts access to the configured root directory

## Getting Started

### Prerequisites

- JDK 21+（构建 Native Image 所需的 GraalVM 由 Mill 自动下载）

### Build

有两种打包方式可选：

| | Fat JAR | Native Image |
|---|---|---|
| 构建命令 | `./mill app.assembly` | `./mill app.nativeImage` |
| 产物路径 | `out/app/assembly.dest/s5.jar` | `out/app/nativeImage.dest/s5` |
| 运行依赖 | 需要 JDK | 无，单文件可执行 |
| 构建速度 | 快（~10s） | 慢（需要数分钟） |
| 启动速度 | 慢（JVM 冷启动） | 快（毫秒级启动） |
| 文件大小 | ~33M | ~52M |

### Run

**Fat JAR:**

```bash
./out/app/assembly.dest/s5.jar /path/to/serve
# 或: java -jar out/app/assembly.dest/s5.jar /path/to/serve
```

**Native Image:**

```bash
./out/app/nativeImage.dest/s5 /path/to/serve
```

### Install

将可执行文件复制到 `$PATH` 下的目录即可全局使用：

```bash
# Native Image
cp out/app/nativeImage.dest/s5 /usr/local/bin/

# Fat JAR
cp out/app/assembly.dest/s5.jar /usr/local/bin/
```

### Usage

```
Usage: s5 [OPTIONS] <root-directory>

Options:
  -h, --host HOST    Bind address (default: 0.0.0.0)
  -p, --port PORT    Listen port (default: 8888)

Arguments:
  <root-directory>   Directory to serve (must exist)
```

Example:

```bash
s5 --host 127.0.0.1 --port 9000 /path/to/files
```

## Development

```bash
# Compile
./mill app.compile

# Run tests
./mill __.test

# Run the server directly via Mill
./mill app.run -- .

# Run with hot reload
./rewatch

# Auto-fix formatting and lint issues
./fix.sh

# Run format check, linting, and tests
./check.sh
```
