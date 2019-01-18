# HttpComponentBenchMark
Compare the performance among some modern http components

## Conclusion

> Test way: start from the request being launched, stop by the time all response has been printed in log.

##### request url1 image over https:
```
https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1547809902281&di=e95bbe43875e038df0176f992ccd3e25&imgtype=0&src=http%3A%2F%2Fvpic.video.qq.com%2F39649808%2Fk0165g5o94a_ori_3.jpg
```

##### request url2 text/html over http:
```
http://www.weather.com.cn/
```

bench mark

|Component|version|
|:-:|:-:|
|Netty|4.1.32|
|OkHttp|3.12.1|
|HttpClient|legacy|

||JBoss Netty|Square OkHttp|Apache HttpClient|
|:--:|:--:|:--:|:--:|
|url1|233.6|274.4|307.0|
|url2|115.9|131.6|216.9|
|promotion|23.9%-46.6%|10.6%-39.3%|--|