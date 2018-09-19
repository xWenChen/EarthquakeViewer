# EarthquakeViewer
1、运行系统：Android

2、该程序可以显示世界范围内已经发生过的地震，用户可以设置地震信息的限制条件。

3、数据源来自USGS（美国地质勘探局）网站，使用了网站提供的API。

4、程序用到了异步加载（AsyncLoad），网络编程（URLConnection），偏好设置（SharedPreference），基础布局知识（TextView，Button）等等知识。

5、因为自己的手机没有root，故无法使用Google Service，进而无法使用Google Map，所以地理位置的设置功能没有添加完整。如果条件允许，可以自行添加地理位置功能。

6、最近更新，在Google地图无法使用的情况下，我使用了百度地图的API，也实现了一部分地图显示的功能
 
程序演示：
 
进入程序会从网上下载数据，若未联网可以提醒。此界面为主界面
 
![主页面](https://github.com/xWenChen/EarthquakeViewer/blob/master/main_ui.jpg)

点击右上设置按钮，可以进行地震参数的设置，用户可以选择保存修改或者放弃修改

![设置界面](https://github.com/xWenChen/EarthquakeViewer/blob/master/setting.jpg)
 
点击下拉图标，可以展示此设置下的URL

![更多设置信息](https://github.com/xWenChen/EarthquakeViewer/blob/master/setting_more.jpg)
 
设置里面有个特殊地方就是用户可以进行地理位置的设置，使用的是百度地图的API。但是默认情况下设置不会限制地理位置

![设置地图](https://github.com/xWenChen/EarthquakeViewer/blob/master/setting_map.jpg)
 
在主界面，用户点击设置按钮旁边的地图按钮，可以在地图中显示地震地点，每个地震地点会打上一个标记
 
![地震地图](https://github.com/xWenChen/EarthquakeViewer/blob/master/total_map.jpg)
 
地图可以放大缩小，点击地图上的标记会显示当前地点地震的等级，用按钮显示，点击按钮会出现更加详细的信息

![地震等级](https://github.com/xWenChen/EarthquakeViewer/blob/master/map_info.jpg)
