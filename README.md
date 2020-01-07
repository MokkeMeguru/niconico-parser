# niconico-parser
ニコニコ大百科のHTML を分析するためのツール

## Requirements
- Memory 8GB~   
CSVデータを読み込む
- shell  
ファイル操作。Linux が Reccomend
- leiningen  
このプログラムの言語処理系
- sqlite3  
DB 管理
- 沢山のディスクスペース ~ 100G      
ニコニコ大百科のデータ加工・保存のために必要

## Usage
### ニコニコ大百科データセットから
まずzipファイルを解凍し、整形します。
この手順については [doc/init_corpus.org](./doc/init_corpus.org) を参照してください。

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
"article": [<article>]
"related_words": [[<related_words>]]}
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

## related_words について
related_words は主に関連項目から収集される項目名のリストです。このリストは階層が存在する場合が多く見られるため、やや複雑な構造を取っています。

また、一つの項目名で、 `hoge・bar` という風に2つのものを指している場合があります。この場合については、名前の `foo・huga` との差別化を図るために、 `hoge///bar` と `///` で区切る処理を行っています。

# bug について
Please notice via Issue or my Twitter (DM or reply)
