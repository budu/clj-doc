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
  (use [clojure.contrib seq-utils]
       clj-doc.markups.html-simple))

(def #^{:doc "The default markup to be used if none specified."}
  default-markup html-simple)

(def #^{:doc "The markup currently used by clj-doc."}
  *current-markup* default-markup)

(def #^{:doc "The options currently used by clj-doc."}
  *options* {})

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

(defn var-type
  "Like explicit-var-type but returns :other instead of var's tag or
  value class."
  [var]
  (let [m (meta var)]
    (cond
      (= (:tag m) clojure.lang.MultiFn) :multimethod
      (:arglists m) (cond (:macro m)  :macro
                          (:inline m) :inline-function
                          :default    :function)
      :default :other)))

(defn explicit-var-type
  "Returns a keyword identifying what kind of var is the one given, else
  its tag or if missing the class of its value. Supports the following
  keywords: :function, :inline-function, :macro and :multimethod."
  [var]
  (let [m (meta var)
        t (var-type var)]
    (cond
      (not= t :other) t
      (:tag m) (:tag m)
      :default (class (var-get var)))))

(defn gen-var-name
  "Generate a qualified var name."
  [var]
  (let [t (var-type var)]
    (str (:name (meta var))
      (when (not= t :other)
        (.replaceAll (str " " t) ":|-" "")))))

(defn gen-var-doc
  "Generates documentation for the given var."
  [var]
  (let [m (meta var)
        d (:doc  m)]
    (str
      (gen :anchor (.sym var))
      (gen :var-name (gen-var-name var))
      (when (:arglists m)
        (apply str
          (map (partial gen :var-arglist) (:arglists m))))
      (gen :var-doc (or d "No documentation found.")))))

(defn section-title
  "Returns the given keyword's name pluralized."
  [k]
  (str (name k) "s"))

(defn gen-section
  "Generates a section given a section name and a sequence of vars."
  [namespace section vars]
  (let [content (apply str (map gen-var-doc vars))]
    (str
      (gen :anchor namespace (name section))
      (gen-if :section
        [ (section-title section)
          (str (gen :section-toc vars) content) ]
        content))))

(defn group-vars
  "Groups vars into sections according to the current :sections option
  if present. Returns the vars grouped in a map."
  [vars]
  (if-let [sections (:sections *options*)]
    (group-by (fn [v]
                (let [t (var-type v)]
                  (if (some #(= t %) sections) t :other)))
              vars)))

(defn gen-namespace-doc
  "Generates documentation for the given namespace."
  [namespace]
  (require (symbol namespace))
  (let [vars (vals (ns-interns (symbol namespace)))
        grouped-vars (group-vars vars)]
    (apply str
      (gen :anchor namespace)
      (gen :namespace (str namespace " namespace"))
      (gen :ns-toc namespace (keys grouped-vars))
      (map #(apply gen-section namespace %) grouped-vars))))

(defn default-title
  "Returns a nice title for a given set of arguments or the :title
  option."
  [& args]
  (or (:title *options*)
      (apply str "Documentation for " (interpose ", " args))))

(defn gen-page
  "Generate a documentation page given one or more namespaces."
  [options nss]
  (binding [*options* options]
    (let [nss (if (coll? nss) (seq nss) (list nss))
          title (apply default-title nss)
          content (apply str
                         (gen :title title)
                         (when (> (count nss) 1)
                           (gen :page-toc nss))
                         (map gen-namespace-doc nss))]
      (gen-if :page
              [title content]
              content))))
