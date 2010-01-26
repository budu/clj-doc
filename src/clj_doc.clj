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
  (use [clojure.contrib duck-streams seq-utils]
       clj-doc.generator
       clj-doc.markups
       clj-doc.utils))

(defn gen-doc*
  "Driver function for gen-doc. Needs its arguments to be quoted and
  must be enclosed within an with-markup call."
  [options & nss]
  (let [{:keys [separated-by]} options
        nss (if (= separated-by 'namespace)
              (flatten nss)
              nss)]
    (zipmap nss (map gen-page nss))))

(defmacro with-markup
  "Makes the given markup the current one."
  [mk & body]
  `(let [mk# ((get-available-markups) ~(list 'quote mk))]
     (binding [*current-markup* mk#]
       ~@body)))

(defn- parse-options-namespaces
  "Retrieves the option map and quotes its values. Also retrieves the
  list of namespaces and convert regular expressions to namespace list."
  [options-namespaces]
  (let [options (first options-namespaces)
        [options namespaces] (if (map? options)
                               [options (rest options-namespaces)]
                               [{}      options-namespaces])
        namespaces (map #(if (pattern? %)
                           (find-nss %)
                           %) namespaces)]
    [ options namespaces ]))

(defmacro gen-doc
  "Returns a map of strings containing the documentation for the
  namespaces given formatted with the specified markup if
  available. Namespaces can be specified with symbols, sequences of
  symbols or regular expressions. The returned map is keyed by lists of
  resolved namespace symbols. The default output format is HTML."
  [& options-namespaces]
  (let [[options namespaces] (parse-options-namespaces
                               options-namespaces)
        {:keys [markup]} options
        generate `(gen-doc* ~(quasiquote* options)
                    ~@(map #(list 'quote %) namespaces))]
    (if markup
      `(with-markup ~markup ~generate)
      generate)))

(defmacro gen-doc-to-file
  "Same as gen-doc but output the documentation to the specified
  file(s)."
  [fmt & options-namespaces]
  `(let [results# (vals (gen-doc ~@options-namespaces))]
     (if (= 1 (count results#))
       (spit ~fmt (first results#))
       (doseq [[i# page#] (zipmap (iterate inc 0) results#)]
         (spit (append-to-filename ~fmt i#) page#)))))
