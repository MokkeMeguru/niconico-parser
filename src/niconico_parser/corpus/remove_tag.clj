(ns niconico-parser.corpus.remove-tag)

(def remove-tags
  (set (map keyword #{"span" "em" "a"   "strong" "sub" "b" "nobr" "i" "hr" "br" "blockquote"  "div" })))
