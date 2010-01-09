;;;;  markups.clj
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

(ns clj-doc.markups
  "This namespace contains all functions needed to build and find
  markups."
  (require [clojure.contrib.ns-utils :as ns-utils]))

(defstruct #^{:doc "Struct to contains markup element generators."}
  markup
  :page
  :title
  :namespace
  :var-name
  :var-doc)

(defn create-markup
  "Shortcut for struct-map markup."
  [& elements]
  (apply struct-map markup elements))

(defmacro defmarkup
  "Define a markup using create-markup so that it can be found later by
  the markups function."
  [name & elements]
  `(def ~(with-meta name
           (merge (meta name) {:type ::Markup}))
     (create-markup ~@elements)))

(defn markup?
  "Returns true if the given var is a markup."
  [var]
  (= ((comp type resolve) var)
    ::Markup))

(defn find-markups
  "Returns a map of all markups found in the current namespace, keyed by
  their names, if there no arguments. Else search in the given
  namespaces."
  [& nss]
  (let [mks (apply concat
              (map #(filter markup? (ns-utils/ns-vars %))
                (if (empty? nss) [*ns*] nss)))]
    (zipmap mks (map eval mks))))

(defmethod print-method
  ::Markup
  [o w]
  (.write w (str o)))
