
clj-doc
-------

This is a library to help generate documentation from Clojure code to
various kind of output formats. It's output can be customized, output to
multiple files and can search for namespace using regular expressions.

The current supported output formats are:

* Simple HTML
* Markdown
* Creole
* DokuWiki Syntax

Normal use of clj-doc is done through two macros, gen-doc and
gen-doc-to-file. The later being only a special case of the former with
a filename as first argument. They can take an optional option map and a
sequence of namespace identifiers that can be grouped with simple
collections. To identify the required namespace, you can use symbols or
regular expressions. Each regex is expanded into a sequence of namespace
symbols.

### Examples

#### Simple Usage

    user> (use 'clj-doc)
    user> (gen-doc clj-doc.markups.markdown)

#### More Complex One

    user> (gen-doc-to-file
            "test.txt"
            {:markup creole
             :separated-by namespace}
            #"clj-doc\.markups\.")

----

Copyright (c)2010 Nicolas Buduroi. All rights reserved.

The use and distribution terms for this software are covered by
the Eclipse Public License 1.0 which can be found in the file
epl-v10.html at the root of this distribution. By using this
software in any fashion, you are agreeing to be bound by the
terms of this license.

You must not remove this notice, or any other, from this software.
