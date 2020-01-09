import pandas as pd
import json
from pathlib import Path
from pprint import pprint
from typing import List

############################
# グローバル変数 (適宜変更します) #
############################
# CSVのヘッダ
header_name = ('article_id', 'article', 'update-date',
               'links', 'title', 'title_yomi', 'category')
dtypes = {'article_id': 'uint16',
          'article': 'object',
          'update-date': 'object',
          'links': 'object',
          'title': 'object',
          'title_yomi': 'object',
          'category': 'object'}

# サンプルの CSV
sample_filepath = "/home/meguru/Documents/nico-dict/zips/rev2014/rev201402-jsoned.csv"
sample_filepath = Path(sample_filepath)

# サンプルの CSVs
fileparent = Path("/home/meguru/Documents/nico-dict/zips")
filepaths = [
    "rev2014/rev201401-jsoned.csv",
    "rev2014/rev201402-jsoned.csv",
    "rev2013/rev201301-jsoned.csv",
    "rev2013/rev201302-jsoned.csv",
    "rev2013/rev201303-jsoned.csv",
    "rev2013/rev201304-jsoned.csv",
    "rev2013/rev201305-jsoned.csv",
    "rev2013/rev201306-jsoned.csv",
    "rev2013/rev201307-jsoned.csv",
    "rev2013/rev201308-jsoned.csv",
    "rev2013/rev201309-jsoned.csv",
    "rev2013/rev201310-jsoned.csv",
    "rev2013/rev201311-jsoned.csv",
    "rev2013/rev201312-jsoned.csv"
]

filepaths = filter(lambda path: path.exists(),  map(
    lambda fpath: fileparent / Path(fpath), filepaths))
##################


def read_df(csvfile: Path, with_info: bool = False):
    """read jsoned.csv file
    args:
    - csvfile: Path
    a file path you want to read
    - with_info: bool
    with showing csv's information
    returns:
    - df
    readed data frame
    notes:
    if you call this function, you will got some log message
    """
    df = pd.read_csv(csvfile, names=header_name, dtype=dtypes)
    print('[Info] readed a file {}'.format(csvfile))
    if with_info:
        df.info()
    return df


def read_dfs(fileparent: Path, csvfiles: List[Path]):
    """read jsoned.csv files
    args:
    - fileparent: Path
    parent file path you want to read
    - csvfiles: List[Path]
    file paths you want to read
    returns:
    - dfl
    concated dataframe
    note:
    given.
        fileparent = \"/path/to\"
        csvfiles[0] = \"file\"
    then.
        search file <= \"/path/to/file\"
    """
    dfl = []
    for fpath in filepaths:
        dfi = pd.read_csv(fileparent / fpath,
                          index_col=None, names=header_name, dtype=dtypes)
        dfl.append(dfi)
    dfl = pd.concat(dfl, axis=0, ignore_index=True)
    return dfl


# 以下メモリ消費量を抑えるためコメントアウト

#  df = read_df(sample_filepath, True)


# [Info] readed a file /home/meguru/Documents/nico-dict/zips/rev2014/rev201402-jsoned.csv
# <class 'pandas.core.frame.DataFrame'>
# RangeIndex: 6499 entries, 0 to 6498
# Data columns (total 7 columns):
# article_id     6499 non-null int64
# article        6499 non-null object
# update-date    6499 non-null int64
# links          6499 non-null object
# title          6491 non-null object
# title_yomi     6491 non-null object
# category       6491 non-null object
# dtypes: int64(2), object(5)
# memory usage: 355.5+ KB
# 3         364944  {"type":"element","attrs":null,"tag":"body","c...  ...                       ロシン        a
# 4        4923051  {"type":"element","attrs":null,"tag":"body","c...  ...                   キタハラトモエ        a
# ...          ...                                                ...  ...                       ...      ...
# 6494     1519418  {"type":"element","attrs":null,"tag":"body","c...  ...                  フジコエフフジオ        a
# 6495     5120187  {"type":"element","attrs":null,"tag":"body","c...  ...                カメンライダーガイム        a
# 6496     5055652  {"type":"element","attrs":null,"tag":"body","c...  ...  シバイヌコサンノカンレンショウヒンイチランソノニ        a
# 6497     5141490  {"type":"element","attrs":null,"tag":"body","c...  ...                     ミスルトゥ        a
# 6498     5020395  {"type":"element","attrs":null,"tag":"body","c...  ...                    ザイムショウ        a

# [6499 rows x 7 columns]

# df['links'][0]
# => '[{"type":"element","attrs":{"href":"http://www.keiba.or.jp/top.html"},"tag":"a","content":["高知けいばオフィシャルサイト"]}]'
# json.loads(df['links'][0])
# => [{'type': 'element', 'attrs': {'href': 'http://www.keiba.or.jp/top.html'}, 'tag': 'a', 'content': ['高知けいばオフィシャルサイト']}]
# len(json.loads(df['links'][0]))
# '[{"type":"element","attrs":{"href":"http://www.keiba.or.jp/top.html"},"tag":"a","content":["高知けいばオフィシャルサイト"]}]'


# dfs= pd.DataFrame()
# dfs['links']= df['links'].map(lambda x: len(json.loads(x)))
# dfs['category']=df['category']
# dfsg=dfs.groupby('category')
# dfsg.describe()



# dfl = []
# for fpath in filepaths:
#     dfi = pd.read_csv(fileparent / fpath, index_col=None,
#                       names=header_name)
#     dfl.append(dfi)
# dfl = pd.concat(dfl, axis=0, ignore_index=True)
# dfl = read_dfs(fileparent, filepaths)
# dfls = pd.DataFrame()
# dfls['links'] = dfl['links'].map(lambda x: len(json.loads(x)))
# dfls['category'] = dfl['category']
# dflsg = dfls.groupby('category')

# dlfsg.describe()
#              links
#              count       mean         std  min  25%  50%   75%     max
# category
# a         193264.0  32.400566  153.923988  0.0  0.0  2.0  10.0  4986.0
# c           1019.0  34.667321   77.390967  0.0  1.0  2.0  34.0   449.0
# i            247.0   6.137652    6.675194  0.0  1.0  3.0  10.0    28.0
# l          24929.0  20.266477  100.640253  0.0  0.0  1.0   5.0  1309.0
# v           3414.0  14.620387   22.969974  0.0  1.0  6.0  16.0   176.0

# dfts = pd.DataFrame ()
# dfts ['links'] = dfl['links'].map(lambda x: len(json.loads(x)))
# dfts['article_size'] = dfl['article'].map(lambda x: len(str(x)))

# dfts.corr()
#                  links  article_size
# links         1.000000      0.713465
# article_size  0.713465      1.000000

# dfts ['category'] = dfl ['category']
# dfts[dfts['category'] == "c"].loc[:, ["links", "article_size"]]
