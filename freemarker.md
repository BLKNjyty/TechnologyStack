## Freemarker

### 1.概述

FreeMarker是模板引擎，即基于模板和要改变的数据，并用来生成输出文本(HTML网页，电子有i按，配置文件，源代码等)的通用工具，是一个java类库。

简单的说，就是将业务代码的试图展示功能剥离出来，交给Freemarker进行输出，java代码只需要准备要显示的数据即可。

![freemarker1](images\freemarker1.png)

这种方式通常被称为 [MVC (模型 视图 控制器) 模式](http://freemarker.foofun.cn/gloss.html#gloss.MVC)，对于动态网页来说，是一种特别流行的模式。 它帮助从开发人员(Java 程序员)中分离出网页设计师(HTML设计师)。设计师无需面对模板中的复杂逻辑， 在没有程序员来修改或重新编译代码时，也可以修改页面的样式。

### 2.基本数据类型

#### 2.1布尔类型

不可以直接输出,如果需要输出需要转换为字符串，当然转换的字符串可以自定义。(详情可见freemarker的中文文档-内建函数)

```html
${flag?c}
${flag?string}
${flag?string('yes','no')}
${flag?string('喜欢','不喜欢')}
```

#### 2.2日期类型

同上，不可以直接显示，需要用到内建函数

```html
//年月日
${createdate?date}
//时分秒
${createdate?time}
//年月日时分秒
${createdate?datetime}
//自定义格式
${createdate?string('yyyy/MM/dd HH:mm:ss')}
```

#### 2.3数值类型

可以直接取值

```html
${num}//1,000
${num:c}//转化为字符串型，没有,
${num?string.currency}//转化为货币比字符串$...
${num?string.percent}//转化为百分比
${num?string["0.##"]}//保留小数点后两位
```

#### 2.4字符串类型

可以直接取出。

更多内置函数见，freemarker 的中文手册。

```
${'abc'?substring(0, 0)}
${" green mouse"?cap_first}
${"redirect"?starts_with("red")}
${"abcdef"?remove_beginning("abc")}
//字符串为空的处理办法
${str!}//不存在是默认显示空字符串
${str!'默认值'}//不存在时默认显示默认值
${(str??)?string}//转为为布尔类型字符串输出，??用来判断字符串是否为空
```

#### 2.5序列类型

包括数组，List，Set等

```
//数组
<#list stars as star>
	${star?index}--${star}<br>
</#list>
${stars?size}
${stars?firse}

//对于List
//倒序
<#list cities?reverse as city>
	${city}
</#list>
//升序
<#list cities?sort as city>
	${city}
</#list>
//降序
<#list cities?sort?reverse as city>
	${city}
</#list>


//List里面的时Bean对象
<#list users?sort?reverse as user>
	${user.userId}
</#list>
```

#### 2.6 hash类型

```
<#list hash?keys as key>
	${key}--${hash[key]}
</#list>
<#list hash?values as value>
	${value}
</#list>


```

### 3.常用指令

具体参见中文文档---指令参考

#### 3.1 assign

```
<#assign str="哈哈">
${str}<br>
<#assign num=1,nums=[1,2,3]>
```

#### 3.2 if elseif

```
<#if condition>
  ...
<#elseif condition2>
  ...
<#elseif condition3>
  ...
...
<#else>
  ...
</#if>
```

#### 3.3 list

见2.5序列

#### 3.4 mocro

```
<#macro test foo bar baaz>
  Test text, and the params: ${foo}, ${bar}, ${baaz}
</#macro>
<#-- call the macro: -->
<@test foo="a" bar="b" baaz=5*5-2/>

```

```
//浏览器
 Test text, and the params: a, b, 23
```

```
//占位符
<#macro do_twice>
  1. <#nested>
  2. <#nested>
</#macro>
<@do_twice>something</@do_twice>
```

#### 3.5 import指令

```
<#import "/libs/mylib.ftl" as my>

<@my.copyright date="1999-2002"/>
```

#### 3.6 include指令

```
Copyright 2001-2002 ${me}<br>
All rights reserved.
```

```
<#assign me = "Juila Smith">
<h1>Some test</h1>
<p>Yeah.
<hr>
<#include "/common/copyright.ftl">
```

```
//输出
<h1>Some test</h1>
<p>Yeah.
<hr>
Copyright 2001-2002 Juila Smith
All rights reserved.
```

### 4.页面的静态化

比如京东主页，数据不是频繁改变，当用户访问量特别大的时候，如果页面的信息都是从数据库获得，那么对数据库压力比较大。所以，需要静态化技术，如freemarker，先设生成html页面，通过include命令包含进来就可以。

### 5.运算符

```
//算数运算符
+ - * 、 %
不可：${a}+${b}  应该:${a+b}
其中对于字符串会拼接在一起，${"hah"+"heheh"}，和java语法一样
//逻辑运算符
&& || ！
//比较运算符
gt lt gte lte  ==  ！=
//空值运算符
??为判空操作。但是必须转化为字符串才能输出 ${(name??)?string}
!设置默认值 ${name!'zhangsan'}
```

