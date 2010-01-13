
clj-doc
-------

This is a library to help generate documentation from Clojure code to
various kind of output formats. Its output can be customized, it can
output to multiple files and search for namespace using regular
expressions.

The currently supported output formats are:

* Simple HTML
* Markdown
* Creole
* DokuWiki Syntax

## Usage

Normal use of clj-doc is done through two macros, gen-doc and
gen-doc-to-file. The later being only a special case of the former with
a filename as first argument. They can take an optional option map and a
sequence of namespace identifiers that can be grouped with simple
collections. To identify the required namespace, you can use symbols or
regular expressions. Each regex is expanded into a sequence of namespace
symbols.

### Examples

Here's some examples to makes things clearer.

#### Simple Usage

Load clj-doc and generate documentation for the clj-doc.markups.markdown
namespace using the default markup which is html-simple.

    user> (use 'clj-doc)
    user> (gen-doc clj-doc.markups.markdown)

#### More Complex Ones

Create one file for each one of clj-doc's markup namespaces in files
test0.txt to test3.txt.

    user> (gen-doc-to-file
            "test.txt"
            {:markup creole
             :separated-by namespace}
            #"clj-doc\.markups\.")

Creates two files, named test0.markdown and test1.markdown, one
including all documentation for clj-doc without markdown and
html-simple, the other with these two namespaces only.

    user> (gen-doc-to-file
            "test.markdown"
            {:markup markdown}
            #"(?=clj-doc)(?=(?!.*markdown))(?=(?!.*html-simple))"
            [clj-doc.markups.markdown clj-doc.markups.html-simple])

## Changelog

#### 0.1.0

* basic working version
 * all interns
 * unsorted
 * separated by namespaces
* generic generator (w/ html only at first)
* namespace structure

#### 0.2.0

* add new markups
 * markdown
 * dokuwiki
 * creole
* make gen-doc accept an option map to control the way the output is generated
* add an option to control output to single/multiple page(s)

#### 0.3.0

* add some output features
 * table of content for namespaces
 * arglists
 * type of var
 * alphabetical sort order
* discover namespace with regex
* add the concept of groups to separate pages by default
            
----

Copyright (c)2010 Nicolas Buduroi. All rights reserved.

The use and distribution terms for this software are covered by
the Eclipse Public License 1.0 which can be found in the file
epl-v10.html at the root of this distribution. By using this
software in any fashion, you are agreeing to be bound by the
terms of this license.

You must not remove this notice, or any other, from this software.
