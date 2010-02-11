;;;;  utils.clj
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

(ns clj-doc.utils
  "Various utilities used by clj-doc."
  (use [clojure.contrib find-namespaces str-utils]))

(defn append-to-filename
  "Takes a file name and append the string representation of the given
  object before the last dot or at the end if there's none."
  [filename obj]
  (if (.contains filename ".")
    (re-gsub #"(\.)([^\.]*$)" (str obj ".$2") filename)
    (str filename obj)))

(defn ->str
  "Converts an object into a string or the objects contained in a
  sequence."
  [obj]
  (if (seq? obj)
    (map str obj)
    (str obj)))

(defn ->file
  "If the given object is not a file, try to construct one with it."
  [o]
  (if (= (class o) java.io.File)
    o
    (java.io.File. o)))

(defn dir?
  "Check if the given argument (a string or a file) is a directory."
  [f]
  (.isDirectory (->file f)))

(defn self-eval?
  "Check whether the given form is self-evaluating."
  [obj]
  (or (keyword?  obj)
      (number?   obj)
      (instance? Character obj)
      (string?   obj)
      (nil?      obj)))

(defn unquote?
  "Tests whether the given form is of the form (unquote ...)."
  [form]
  (and (seq? form) (= (first form) `unquote)))

(defn quasiquote*
  "Worker for quasiquote macro. See docstring there. For use in macros."
  [form]
  (cond
    (self-eval? form) form
    (unquote? form)   (second form)
    (symbol? form)    (list 'quote form)
    (vector? form)    (vec (map quasiquote* form))
    (map? form)       (zipmap (keys form) (map quasiquote* (vals form)))
    (set? form)       (apply hash-set (map quasiquote* form))
    (seq? form)       (list* `list (map quasiquote* form))
    :else             (list 'quote form)))

(defmacro quasiquote
  "Quote the supplied form as quote does, but evaluate unquoted parts.

  Example: (let [x 5] (quasiquote (+ ~x 6))) => (+ 5 6)"
  [form]
  (quasiquote* form))

(defn find-nss
  "Takes a regex and returns all matching namespaces."
  [ns-regex]
  (filter (comp (partial re-seq ns-regex) str)
    (find-namespaces-on-classpath)))

(defn pattern?
  "Check if the given argument is a Pattern instance."
  [obj]
  (= (class obj) java.util.regex.Pattern))

(defn to-title
  "Returns the given keyword's name pluralized."
  [k]
  (.replace (str (name k) "s") "-" " "))
