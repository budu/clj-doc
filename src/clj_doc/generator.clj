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

(defn gen-when
  "Like gen-if but returns nil if the generator isn't found."
  [element & args]
  (gen-if element args nil))

(defn var-type
  "Like explicit-var-type but returns :other instead of var's tag or
  value class."
  [var]
  (let [m (meta var)]
    (cond
      (= (:tag m) clojure.lang.MultiFn) :multimethod
      (:arglists m) (if (:macro m)
                      :macro
                      (if (:inline m)
                        :inline-function
                        :function))
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
      (gen-when :anchor (.sym var))
      (gen :var-name (gen-var-name var))
      (when (:arglists m)
        (apply str
          (map (partial gen :var-arglist) (:arglists m))))
      (gen :var-doc (or d "No documentation found.")))))

(defn section-title
  "Returns the given keyword name pluralized."
  [k]
  (str (name k) "s"))

(defn gen-namespace-doc
  "Generates documentation for the given namespace."
  [namespace]
  (require (symbol namespace))
  (let [vars (vals (ns-interns (symbol namespace)))
        grouped-vars (group-by var-type vars)]
    (apply str
      (gen-when :anchor namespace)
      (gen :namespace (str namespace " namespace"))
      (map (fn [[type vars]]
             (let [content (apply str (map gen-var-doc vars))]
               (gen-if :section
                 [ (section-title type)
                   (str (gen-when :section-toc vars) content) ]
                 content))) grouped-vars))))

(defn default-title
  "Returns a nice title for a given set of arguments."
  [& args]
  (apply str "Documentation for " (interpose ", " args)))

(defn gen-page
  "Generate a documentation page given one or more namespaces."
  [nss]
  (let [nss (if (coll? nss) (seq nss) (list nss))
        title (apply default-title nss)
        content (apply str
                  (gen :title title)
                  (when (> (count nss) 1)
                    (apply gen-when :ns-toc nss))
                  (map gen-namespace-doc nss))]
    (gen-if :page
      [title content]
      content)))
