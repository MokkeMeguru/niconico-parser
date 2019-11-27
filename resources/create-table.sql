DROP TABLE IF EXISTS article_header;
CREATE TABLE article_header (
       article_id INTEGER PRIMARY KEY,
       article_title TEXT DEFAULT NULL,
       article_title_yomi TEXT DEFAULT NULL,
       article_category TEXT DEFAULT 'l',
       article_date TEXT DEFAULT '20000101000000'
)
