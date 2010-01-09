;;;;  clj_doc.clj
;;;;
;;;;  Copyright (c)2010 Nicolas Buduroi. All rights reserved.
;;;;
;;;;  The use and distribution terms for this software are covered by
;;;;  the Eclipse Public License 1.0 which can be found in the file
;;;;  epl-v10.html at the root of this distribution. By using this
;;;;  software in any fashion, you are agreeing to be bound by the
;;;;  terms of this license.
;;;;
;;;;  You must not remove this notice, or any other, from this software.

(ns clj-doc
  "This library provides the gen-doc and gen-doc-to-file macros and
  accompanying driver functions to generate documentation for Clojure
  code."
  (use [clojure.contrib duck-streams]
    clj-doc.generator
    clj-doc.markups))

(def #^{:doc "List of available markups, which need a corresponding
  namespace."}
  available-markups
  '[ clj-doc.markups.creole
     clj-doc.markups.dokuwiki
     clj-doc.markups.html-simple
     clj-doc.markups.markdown ])

(defn gen-doc*
  "Driver function for gen-doc. Needs its arguments to be quoted and
  must be enclosed within an with-markup call."
  [& nss]
  (let [title (apply str "Documentation for " (interpose ", " nss))]
    (let [content (apply str
                    (gen :title title)
                    (interleave
                      (map (partial gen :namespace) nss)
                      (map gen-namespace-doc nss)))]
      (gen-if :page
        [title content]
        content))))

(defmacro with-markup
  "Makes the given markup the current one."
  [mk & body]
  `(do
     (apply use available-markups)
     (let [mk# ((apply find-markups available-markups) ~mk)]
       (binding [*current-markup* mk#]
         ~@body))))

(defmacro gen-doc
  "Returns a string containing the documentation for the namespaces
  given formatted with the specified markup if available. The default
  output is formatted in HTML."
  [& options-namespaces]
  (let [options (first options-namespaces)
        [options namespaces] (if (map? options)
                               [options (rest options-namespaces)]
                               [{}      options-namespaces])
        {:keys [markup]} options
        generate `(gen-doc* ~@(map #(list 'quote %) namespaces))]
    (if markup
      `(with-markup ~(list 'quote markup) ~generate)
      generate)))

(defmacro gen-doc-to-file
  "Same as gen-doc but output the documentation to the specified file."
  [filename & options-namespaces]
  `(spit ~filename (gen-doc ~@options-namespaces)))
