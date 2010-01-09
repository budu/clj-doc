;;;;  generator.clj
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

(ns clj-doc.generator
  "This namespace contains all parts to make a generic documentation
  generator for Clojure code."
  (use clj-doc.markups.html-simple))

(def #^{:doc "The default markup to be used if none specified."}
  default-markup html-simple)

(def #^{:doc "The markup currently used by clj-doc."}
  *current-markup* default-markup)

(defn gen
  "Generates the specified element with the given arguments passed to
  the corresponding generator taken from the *current-markup* map."
  [element & args]
  (let [mk *current-markup*]
    (apply (or (and mk (mk element)) str) args)))

(defn gen-if
  "Like gen but if the markup doesn't have the requested element
  generator, it will returns the given default value."
  [element args default]
  (let [mk *current-markup*]
    (if (element mk)
      (apply gen element args)
      default)))

(defn gen-var-doc
  "Generates documentation for the given var."
  [var]
  (let [m (meta var)
        d (:doc  m)]
    (str
      (gen :var-name (:name m))
      (gen :var-doc (or d "No documentation found.")))))

(defn gen-namespace-doc
  "Generates documentation for the given namespace."
  [namespace]
  (require (symbol namespace))
  (let [interns (ns-interns (symbol namespace))]
    (apply str (map gen-var-doc (vals interns)))))
