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
  "Returns a keyword identifying what kind of var is the one given.
  Supports the following keywords: :function, :inline-function, :macro
  and :multimethod."
  [var]
  (let [m (meta var)
        t (cond
            (= (:tag m) clojure.lang.MultiFn) :multimethod
            (:arglists m) (cond (:macro m)    :macro
                                (:inline m)   :inline-function
                                :default      :function)
            :default                          :other)]
    (if (:private m)
      (keyword (str "private-" (name t)))
      t)))

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
  (.replace (str (name k) "s") "-" " "))

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

(defn- in-or-other
  "Returns its given argument if there's no sections options. Takes the
  type of section given and returns it if it's found in :only-sections
  or :sections option, else returns :other. Also, if :sections is not a
  collection, always returns :other."
  [t]
  (let [sections (or (:only-sections *options*)
                     (:sections *options*))]
    (if sections
      (if (and (coll? sections) ; the only possibility is :none for now
               (some #(= t %) sections)) t :other)
      t)))

(defn- group-vars
  "Groups vars into sections according to the current :sections option
  if present. Returns the vars grouped in a map."
  [vars]
  (let [grouped-vars (group-by (comp in-or-other var-type) vars)]
    (if-let [only-sections (:only-sections *options*)]
      (select-keys grouped-vars only-sections)
      grouped-vars)))

(defn gen-namespace-doc
  "Generates documentation for the given namespace."
  [namespace]
  (require (symbol namespace))
  (let [vars (vals (ns-interns (symbol namespace)))
        grouped-vars (group-vars vars)]
    (apply str
      (gen :anchor namespace)
      (gen :namespace (str namespace " namespace"))
      (when (> (count grouped-vars) 1)
        (gen :ns-toc namespace (keys grouped-vars)))
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
