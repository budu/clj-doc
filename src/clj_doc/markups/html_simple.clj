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

(defn encode-id-char [c]
  (if (re-seq #"[\w-_\.]" (str c))
    c
    (str "." (int c))))

(defn encode-id [s]
  (apply str "I"
    (map encode-id-char (str s))))

(defn html-ns-toc
  "Generator the namespace table of content."
  [nss]
  (html [:div#ns-toc
          (interpose ", "
            (map #(html (link-to (str "#" (encode-id %)) %)) nss))]))

(defn html-section-toc
  "Generator the section table of content."
  [vars]
  (html [:div.section-toc
          (interpose ", "
            (map #(let [n (escape-html (name (.sym %)))]
                    html (link-to (str "#" (encode-id n)) n)) vars))]))

(defmarkup
  #^{:doc "Simple HTML markup."}
  html-simple
  :page         html-simple-page
  :title        #(html [:h1 (escape-html %)])
  :ns-toc       html-ns-toc
  :anchor       #(html [:a {:id (encode-id %)}])
  :namespace    #(html [:h2 (escape-html %)])
  :section-toc  html-section-toc
  :section      #(html [:h3 (escape-html %1)] [:div %2])
  :var-name     #(html [:h4 (escape-html %)])
  :var-arglist  #(html [:span (escape-html %)] [:br])
  :var-doc      #(html [:p (escape-html %)]))
