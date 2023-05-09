# CCDetect-lsp

Incremental, language agnostic and IDE agnostic duplicate code detection.

|                                          Code clone diagnostics                                           |                                           Diagnostics overview                                            |
| :-------------------------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------: |
| ![](https://user-images.githubusercontent.com/8071566/217652306-e46e8fd1-2ba4-4d46-8442-85538da18283.png) | ![](https://user-images.githubusercontent.com/8071566/217652682-dd38eb9c-a746-406e-85d5-f6144a8ba945.png) |

This is an LSP tool which does duplicate code detection, otherwise known as code-clone
detection. The goal of this tool is to create a completely incremental updating static
analysis tool, which works in the editor setting to provide code analysis in real-time.
The tool is editor and language agnostic, utilizing tree-sitter grammars to parse any
language incrementally. This tool and algorithm is the basis for my master thesis at The
University of Oslo.

## Installation

```bash
git clone https://github.com/jakobkhansen/CCDetect-lsp.git
cd CCDetect-lsp
make
gradle clean && gradle build
```

This will compile the CCDetect-lsp server in a jar file located in
`app/build/libs/app-all.jar`.

## Usage

### VSCode

[This plugin](https://github.com/jakobkhansen/CCDetect-vscode) runs CCDetect-lsp as a
language server in VSCode. However, since CCDetect-lsp relies on compiled C code (for
tree-sitter parsing), you should compile CCDetect-lsp from this repo, and replace the
plugins `launcher/launcher.jar` file with the jar you compiled, otherwise things will
likely not work.

### Neovim

The following config file starts up CCDetect-lsp for Java projects:

Replace the jar location with the location of your local CCDetect-lsp repo.

```lua
local lspconfig = require("lspconfig")
local util = lspconfig.util
local sysname = vim.loop.os_uname().sysname
local autocmd = vim.api.nvim_create_autocmd

local command = vim.api.nvim_command

local JAVA_HOME = os.getenv("JAVA_HOME")

local function get_java_executable()
    local executable = JAVA_HOME and util.path.join(JAVA_HOME, "bin", "java") or "java"

    return sysname:match("Windows") and executable .. ".exe" or executable
end

local jar = vim.env.HOME .. "/(Location of CCDetect-lsp)/app/build/libs/app-all.jar"

local cmd = { get_java_executable(), "-Xmx8G", "-jar", jar }

local function on_show_document(err, result, ctx, config, params)
    local uri = result.uri
    command("e +" .. result.selection.start.line + 1 .. " " .. uri)

    return result
end

local function start_ccdetect()
    vim.lsp.start({
        cmd = cmd,
        name = "CCDetect",
        root_dir = vim.fs.dirname(vim.fs.find({ ".git" }, { upward = true })[1]),
        handlers = {
            ["window/showDocument"] = on_show_document,
        },
        init_options = {
            language = "java",
            fragment_query = "(method_declaration) @method",
            clone_token_threshold = 100,
            dynamic_detection = true,
            update_on_save = true,
        },
    })
end

autocmd("FileType", { pattern = "java", callback = start_ccdetect })
```

Using [Telescope](https://github.com/nvim-telescope/telescope.nvim)'s diagnostic picker
gives you a nice overview of all clones in the project.

## Options

The following init options are available to configure CCDetect-lsp:

```lua
{
    -- Filetype of files which should be scanned. Grammars are installed under `grammars/`
    -- as a git submodule
    language = "java",

    -- Tree-sitter query which determines which part of each file should be considered
    fragment_query = "(method_declaration) @method",

    -- Number of duplicate tokens required for a code snippet to be considered a clone
    clone_token_threshold = 100,

    -- Should incremental detection be enabled? (Likely faster)
    dynamic_detection = true,

    -- Update only on save (Better for large projects)
    update_on_save = true,

    -- Special option when non-leaf nodes of the AST contains tokens which should be considered.
    -- For C, "string_literal" is a good option to add, since tree-sitter grammar of C doesn't
    -- have a leaf node which contains the string-literal value of a string
    extra_nodes = {}

},
```

## Recommended language setup

For any language which is to be analyzed, there are two language-specific options you need
to change in your configuration. The `language` option is straightforward, if it is
supported, set this to the file extension of the language (such as `js`, `py`, `rs`).
For the `fragment_query` option, this should be set to what tree-sitter AST node you want
to be selected for clone detection. This could for example be functions, methods, classes,
or just the root node of the AST, which means the entire program will be considered for
clone detection. A separate option to use just the root node might be added, which makes
it simpler to get a new language up and running without knowing anything about its AST.

The following section gives some possible fragment queries you can set for different
languages:

```txt
Javascript, file: (program) @program
Javascript, functions: (function_declaration) @function

Java, file: (program) @program
Java, methods and constructors: (method_declaration) @method (constructor_declaration) @constructor

C, file: (translation_unit) @program
C, functions: (function_definition) @function

Python, file: (module) @module
Python, functions: (function_definition) @function
Python, classes: (class_definition) @class

Rust, file: (source_file) @file
Rust, functions: (function_item) @function

Go, file: (source_file) @file
Go, functions: (function_declaration) @function
```

## Demo

To test out CCDetect-LSP in a Docker environment, run:

```bash
git clone https://github.com/jakobkhansen/CCDetect-lsp.git
cd CCDetect-lsp

# Builds, starts and connects to a Docker container where CCDetect-LSP and a
# pre-configured Neovim is available
./demo

# Or run this which launches the same container, but also automatically starts Neovim in a
# project at the location of a known clone.

./demoworldwind
```

Note that building the image can take 5-10 minutes, as dependencies need to be downloaded
and CCDetect-LSP needs to be built.

In the Docker container, the `nvim` command, launches Neovim with a pre-configured setup
for Java projects. `~/.config/nvim/init.lua` can be edited to change configurations such
as language and token threshold.

To run CCDetect-LSP in Neovim, clone a repo, cd into its root folder and run `nvim`. When
Neovim is open, open any file of the selected file-type (`.java` by default).

To navigate files and interact with clones, the following hotkeys have been defined:

```txt
Ctrl+t = Fuzzy-find files (:Telescope find_files)
Ctrl+f = File-tree (:Neotree toggle)
Ctrl+c = Code clone view (:Telescope diagnostics)
Ctrl+a = Code action (:lua vim.lsp.buf.code_action())
```

Note that each run of `./demo` builds an image and starts a Docker container, it might be
smart to reconnect to an already built demo container, to avoid the long build time and
the space usage of the image.
