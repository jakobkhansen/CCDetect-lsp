# CCDetect-lsp

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
    if vim.g.javaserveroff ~= nil then
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
                update_on_save = false,
            },
        })
    end
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

## Incremental clone detection algorithm

TODO
