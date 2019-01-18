# HttpComponentBenchMark
Here I have chosen 3 famous http components which could be easily introduced on android platform, and test their performance.

## Conclusion

> **Network and data process**: Netty > OkHttp > HttpClient
>
> **Cpu usage**: HttpClient < Netty < OkHttp
>
> **Memory usage**: Netty < OkHttp < HttpClient

## Detail

##### request url1 image over https:
```
https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1547809902281&di=e95bbe43875e038df0176f992ccd3e25&imgtype=0&src=http%3A%2F%2Fvpic.video.qq.com%2F39649808%2Fk0165g5o94a_ori_3.jpg
```

##### request url2 text/html over http:
```
http://www.weather.com.cn/
```

#### versions

|Component|version|
|:-:|:-:|
|Netty|4.1.32|
|OkHttp|3.12.1|
|HttpClient|android legacy|

network environment: china telecom 4G (indoor)
device: Samsung S9+ (Snapdragon 845)

#### speed bench mark (time is measured in millisecond)

> **Test way**: start from the request being launched, stop by the time all response has been printed in log.

||JBoss Netty|Square OkHttp|Apache HttpClient|
|:--:|:--:|:--:|:--:|
|url1 (https file)|233.6|274.4|307.0|
|promotion(%)|-23.9%|-10.6%|--|
|url2 (http text)|115.9|131.6|216.9|
|promotion(%)|-46.6%|-39.3%|--|

#### performance bench mark (cpu & memory are measured in percentage and MB)

> **Test way**: Repeating downloading url1, caching all the image into memory and print it in log.
>
||JBoss Netty|Square OkHttp|Apache HttpClient|
|:--:|:--:|:--:|:--:|
|cpu(%)|6%|8%|5%|
|promotion(%)|+20%|+60%|--|
|memory(MB)|30|91|110|
|promotion(%)|-72.7%|-17.3%|--|

