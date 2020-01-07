(ns niconico-parser.web.remove-tag)

(def remove-tags
  (set (map keyword #{"span" "em" "a"   "strong" "sub" "b" "nobr" "i" "hr" "br" "div" "p"})))
