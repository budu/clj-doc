;;;;  dokuwiki.clj
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

(ns clj-doc.markups.dokuwiki
  "DokuWiki markup."
  (use clj-doc.markups
       clojure.contrib.str-utils))

(defn tidy [s] (re-gsub #"\s+" " " s))

(defmarkup
  #^{:doc "DokuWiki markup."}
  dokuwiki
  :title        #(str "\n===== " % " =====")
  :namespace    #(str "\n==== "  % "  ====")
  :var-name     #(str "\n=== "   % "   ===")
  :var-doc      #(str "\n" (tidy %)))