# EarthquakeViewer
1、运行系统：Android

2、该程序可以显示世界范围内已经发生过的地震，用户可以设置地震信息的限制条件。

3、数据源来自USGS（美国地质勘探局）网站，使用了网站提供的API。

4、程序用到了异步加载（AsyncLoad），网络编程（URLConnection），偏好设置（SharedPreference），基础布局知识（TextView，Button）等等知识。

5、因为自己的手机没有root，故无法使用Google Service，进而无法使用Google Map，所以地理位置的设置功能没有添加完整。如果条件允许，可以自行添加地理位置功能。

6、最近更新，在Google地图无法使用的情况下，我使用了百度地图的API，也实现了一部分地图显示的功能
 
程序演示：
 
进入程序会从网上下载数据，若未联网可以提醒
![主页面](https://github.com/xWenChen/EarthquakeViewer/blob/master/主页面.jpg)
