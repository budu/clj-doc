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

(defn file-format-namespaces
  "Turns a namespace to string and replace dots by dashes. If there's
  more than one namespace, concatenate them separated by underscores."
  [nss]
  (.replace 
    (apply str
      (interpose "_" (if (seq? nss)
                       nss
                       (list nss))))
      "." "-"))

(defn file-format
  "Replace the first occurence of %ns in the given format string by the
  specified formatted sequence of namespaces."
  [fmt nss]
  (.replace fmt "%ns" nss))

(defn file-format-or-append
  "Same as file-format, but if no replacement occurs, the namespaces
  string is appended at the end of the filename, before the extension if
  there's one."
  [fmt nss]
  (let [filename (file-format fmt nss)]
    (if (= fmt filename)
      (append-to-filename fmt nss)
      filename)))

(defn spit-doc
  "Helper for writing documentation to a file."
  [fmt nss page]
  (spit (file-format fmt (file-format-namespaces nss)) page))

(defmacro gen-doc-to-file
  "Same as gen-doc but output the documentation to the specified
  file(s)."
  [fmt & options-namespaces]
  `(let [results# (gen-doc ~@options-namespaces)]
     (if (= 1 (count results#))
       (apply spit-doc ~(if (or (empty? fmt)
                               (dir? fmt))
                          (str fmt "%ns") fmt) (first results#))
       (doseq [[nss# page#] results#]
         (spit (file-format-or-append ~fmt
                 (file-format-namespaces nss#)) page#)))))
