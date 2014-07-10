# extracter

A CLI tool that extracts comment docs from [Facter](http://docs.puppetlabs.com/facter) facts. This is incredibly useful if you happen to work on the documentation team at [Puppet Labs](http://puppetlabs.com).

## Requirements

To build or run from source, you'll need [Leiningen](http://leiningen.org/).

## Usage

Use the `lein run` command inside this directory to launch extracter. This is a simple tool, so the following examples cover most of the functionality:

To parse the facts from `facter/lib/facter` (the usual location for core facts) and output markdown to `docs.md`:

    lein run -i facter/lib/facter --markdown -o docs.md

To parse the facts from `facter/lib/facter` and output json to `facts.json`:

    lein run -i facter/lib/facter --json -o facts.json

## The Comment Docs Format

Extracter has pretty high expectations for the format of the comment docs. Essentially, it expects any number of documentation blocks in the following format to occur in a file *before anything else*:

    # Fact: format_example
    #
    # Purpose:
    #   A demonstration of the format of fact comment docs.
    #
    # Sections:
    #   Each line beginning with a capital letter gets a bullet point in the resulting markdown,
    #   but lines beginning with lowercase letters are concatenated to the preceding line.
    #   A section may have any number of points.
    #   There may be any number of sections with arbitrary titles, but the following sections are
    #   conventional: Purpose, Resolution, Caveats.
    #
    # Caveats:
    #   There must be at least two sections.
    #   Code, path names, and program output should be placed in backticks like so: `uname -u`.
    #   There should be a blank comment line at the end of each doc:
    #

The parser is about as forgiving as I could make it, but it's best to follow the above format as closely as possible.

## Auditing

After generating the output, the parser checks to see whether all the facts it expected to be there are actually there. It does this by scanning the input directory for lines in the following format:

    # Fact: fact_name

And cross-referencing all of the `fact_name`s with the parser's output. If anything is missing, it'll call those out at the end. If you use the Facter commit that's submoduled into this repository then you shouldn't see any examples of missing facts, but this extra step should help you figure out if there are any facts whose docs are present, but incorrectly formatted.

There's a big caveat here, though: auditing only guarantees that the _name_ of the fact appears in the final output; not that the rest of it looks the way it's supposed to. There could still be missing sections or subsections, just to name a couple of likely issues. You should never use extracter's output without reading over it first for obvious errors.

## TODO

- [ ] The tests in this repo are massively out of date.
- [x] The output should contain at least one fact. (Done: this is part of the audit now)
- [x] The initial scan for facts should cast a broader net. As it is, facts with a malformed "Fact: factname" declaration will probably slip by unnoticed. (Done: this scan is now about as aggressive as it reasonably could be)

## License

Copyright © 2014 Justin Holguin
