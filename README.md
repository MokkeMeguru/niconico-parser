# niconico-parser
ニコニコ大百科のHTML を分析するためのツール


## Usage
### Web ページの URL から
```
lein parse-from-web -u https://dic.nicovideo.jp/a/<contents-title>
```

<"タイトル">.json を得ることができます。
## 形式


### Run the application locally

`lein ring server`

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`

## License

Copyright ©  FIXME
