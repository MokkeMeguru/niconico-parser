# niconico-parser
ニコニコ大百科のHTML を分析するためのツール


## Usage
### Web ページの URL から
```
lein parse-from-web -u https://dic.nicovideo.jp/a/<contents-title>
```

`<タイトル>.json` を得ることができます。

また、 json ファイルを整形するためのツールとして、 `format.sh` を作成しました。

json ファイルの入ったフォルダ内で

```
sh format.sh
```

とすることで、同一階層のすべての json ファイルを見ることができます。
## 形式
```
{"title": <記事タイトル>
"title-head": <記事タイトル（詳細）>
"keywords": [<記事につけられるキーワード>]
"article": [<article>]}
```

article は次のようになります。

1. dict 構造の場合
```
[{"type" : "element"
  "attrs" : <html attr の dict ex. {"id": "page-menu"}>
  "tag": <"ul" "td" "p"などの 要素名>
  "content" : <article (つまり下の階層)>
}]
```
2. 文字列の場合    
`<i>` や `<span>` といった装飾タグについては省くようにプログラムしてあります。
そのため、それらを省いた要素は dict 構造ではなく string になっています。

つまり、以下のような構造が得られます。
```
["hogehoge は foo である。"
"foo は bar とも言われている。"
{...}
{...}
]
```

どのタグを削除したかは、 [./doc/remove_tags.org](./doc/remove_tags.org) を参照してください。
