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

(defn html-simple-page
  "Generator for html-simple page element."
  [title content]
  (html
    (doctype :html4)
    [:html
      [:head [:title title]]
      [:body content]]))

(defmarkup
  #^{:doc "Simple HTML markup."}
  html-simple
  :page         html-simple-page
  :title        #(html [:h1 (escape-html %)])
  :namespace    #(html [:h2 (escape-html %)])
  :var-name     #(html [:h3 (escape-html %)])
  :var-doc      #(html [:p (escape-html %)]))
