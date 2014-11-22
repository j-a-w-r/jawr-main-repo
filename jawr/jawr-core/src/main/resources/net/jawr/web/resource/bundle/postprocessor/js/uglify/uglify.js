function minify(files, options) {
    options = defaults(options, {
        spidermonkey : false,
        outSourceMap : null,
        sourceRoot   : null,
        inSourceMap  : null,
        fromString   : true,
        warnings     : false,
        mangle       : {},
        output       : null ,
        compress     : {}
    });
    base54.reset();

    // 1. parse
    var toplevel = null,
        sourcesContent = {};

    if (options.spidermonkey) {
        toplevel = AST_Node.from_mozilla_ast(files);
    } else {
        if (typeof files == "string")
            files = [ files ];
        files.forEach(function(file){
            var code = file;
            sourcesContent[file] = code;
            toplevel = parse(code, {
                filename: "?",
                toplevel: toplevel
            });
        });
    }

    // 2. compress
    if (options.compress) {
        var compress = { warnings: options.warnings };
        merge(compress, options.compress);
        toplevel.figure_out_scope();
        var sq = Compressor(compress);
        toplevel = toplevel.transform(sq);
    }

    // 3. mangle
    if (options.mangle) {
        toplevel.figure_out_scope();
        toplevel.compute_char_frequency();
        toplevel.mangle_names(options.mangle);
    }

    // 4. output
    var inMap = options.inSourceMap;
    var output = {};
    if (typeof options.inSourceMap == "string") {
        inMap = fs.readFileSync(options.inSourceMap, "utf8");
    }
    if (options.outSourceMap) {
        output.source_map = SourceMap({
            file: options.outSourceMap,
            orig: inMap,
            root: options.sourceRoot
        });
        if (options.sourceMapIncludeSources) {
            for (var file in sourcesContent) {
                if (sourcesContent.hasOwnProperty(file)) {
                    output.source_map.get().setSourceContent(file, sourcesContent[file]);
                }
            }
        }

    }
    if (options.output) {
        merge(output, options.output);
    }
    var stream = OutputStream(output);
    toplevel.print(stream);

    if(options.outSourceMap){
        stream += "\n//# sourceMappingURL=" + options.outSourceMap;
    }

    return new net.jawr.web.minification.CompressionResult(stream + "", output.source_map + "");
    
};

