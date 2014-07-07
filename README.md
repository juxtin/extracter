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

## Limitations and Caveats

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

## License

Copyright © 2014 Justin Holguin
