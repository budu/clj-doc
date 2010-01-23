;;;;  html_simple.clj
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

(ns clj-doc.markups.html-simple
  "Simple HTML markup."
  (use clj-doc.markups
       compojure.html))

(defn- encode-id-char [c]
  (if (re-seq #"[\w-_\.]" (str c))
    c
    (str "." (int c))))

(defn- encode-id [s]
  (apply str "I"
    (map encode-id-char (str s))))

(defn- page
  "Generator for html-simple pages."
  [title content]
  (html
    (doctype :html4)
    [:html
      [:head [:title title]]
      [:body content]]))

(defn- anchor
  "Generator for html's anchors."
  [& parts]
  (html [:a {:id (encode-id (apply str (interpose "." parts)))}]))

(defn- toc
  "Generic table of content generator."
  [name-fn id-fn coll]
  (interpose ", "
    (map #(let [n (name-fn %)]
            (link-to (str "#" (encode-id (id-fn n))) n)) coll)))

(defn- page-toc
  "Generator of the table of content for the whole page."
  [nss]
  (html [:div#page-toc (toc identity identity nss)]))

(defn- ns-toc
  "Generator of the table of content for the whole page."
  [namespace sections]
  (html [:div.ns-toc (toc name #(str namespace "." %) sections)]))

(defn- section-toc
  "Generator of the table of content for a section."
  [vars]
  (html [:div.section-toc (toc #(name (.sym %)) identity vars)]))

(defmarkup
  #^{:doc "Simple HTML markup."}
  html-simple
  :page         page
  :title        #(html [:h1 (escape-html %)])
  :page-toc     page-toc
  :anchor       anchor
  :namespace    #(html [:h2 (escape-html %)])
  :ns-toc       ns-toc
  :section-toc  section-toc
  :section      #(html [:h3 (escape-html %1)] [:div %2])
  :var-name     #(html [:h4 (escape-html %)])
  :var-arglist  #(html [:span (escape-html %)] [:br])
  :var-doc      #(html [:p (escape-html %)]))
