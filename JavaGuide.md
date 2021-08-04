

## java

### 1.Stream

#### 定义：

Stream（流）是一个来自数据源的元素队列并支持聚合操作

- 元素是特定类型的对象，形成一个队列。 Java中的Stream并不会存储元素，而是按需计算。
- **数据源** 流的来源。 可以是集合，数组，I/O channel， 产生器generator 等---不可以是map！！
- **聚合操作** 类似SQL语句一样的操作， 比如filter, map, reduce, find, match, sorted等。

#### 接口继承关系图：

![1623152105(1)](images\1623152105(1).jpg)



图中4种*stream*接口继承自`BaseStream`，其中`IntStream, LongStream, DoubleStream`对应三种基本类型（`int, long, double`，注意不是包装类型），`Stream`对应所有剩余类型的*stream*视图。为不同数据类型设置不同*stream*接口，可以1.提高性能，2.增加特定接口函数。

#### 和collections的比较

- **无存储**。*stream*不是一种数据结构，它只是某种数据源的一个视图，数据源可以是一个数组，Java容器或I/O channel等。
- **为函数式编程而生**。对*stream*的任何修改都不会修改背后的数据源，比如对*stream*执行过滤操作并不会删除被过滤的元素，而是会产生一个不包含被过滤元素的新*stream*。
- **惰式执行**。*stream*上的操作并不会立即执行，只有等到用户真正需要结果的时候才会执行。
- **可消费性**。*stream*只能被“消费”一次，一旦遍历过就会失效，就像容器的迭代器那样，想要再次遍历必须重新生成。

#### 常见api：

对*stream*的操作分为为两类，**中间操作(\*intermediate operations\*)和结束操作(\*terminal operations\*)**，二者特点是：

1. **中间操作总是会惰式执行**，调用中间操作只会生成一个标记了该操作的新*stream*，仅此而已。
2. **结束操作会触发实际计算**，计算发生时会把所有中间操作积攒的操作以*pipeline*的方式执行，这样可以减少迭代次数。计算完成之后*stream*就会失效。

| 操作类型 | 接口方法                                                     |
| -------- | ------------------------------------------------------------ |
| 中间操作 | concat() distinct() filter() flatMap() limit() map() peek() skip() sorted() parallel() sequential() unordered() |
| 结束操作 | allMatch() anyMatch() collect() count() findAny() findFirst() forEach() forEachOrdered() max() min() noneMatch() reduce() toArray() |

区分中间操作和结束操作最简单的方法，就是看方法的返回值，返回值为*stream*的大都是中间操作，否则是结束操作。

##### forEach

```java
// 使用Stream.forEach()迭代
Stream<String> stream = Stream.of("I", "love", "you", "too");
stream.forEach(str -> System.out.println(str));
```

##### filter

```java
// 保留长度等于3的字符串
Stream<String> stream= Stream.of("I", "love", "you", "too");
stream.filter(str -> str.length()==3)
    .forEach(str -> System.out.println(str));
```

##### distinct

```java
Stream<String> stream= Stream.of("I", "love", "you", "too", "too");
stream.distinct()
    .forEach(str -> System.out.println(str));
//去重 将第二个too去掉
```

##### sorted

排序函数有两个，一个是用自然顺序排序，一个是使用自定义比较器排序，函数原型分别为`Stream<T>　sorted()`和`Stream<T>　sorted(Comparator<? super T> comparator)`

```java
Stream<String> stream= Stream.of("I", "love", "you", "too");
stream.sorted((str1, str2) -> str1.length()-str2.length())
    .forEach(str -> System.out.println(str));
//按照长度升序排序
```

##### map

函数原型为`<R> Stream<R> map(Function<? super T,? extends R> mapper)`，作用是返回一个对当前所有元素执行执行`mapper`之后的结果组成的`Stream`。直观的说，就是对每个元素按照某种操作进行转换，转换前后`Stream`中元素的个数不会改变，但元素的类型取决于转换之后的类型。

```java
Stream<String> stream　= Stream.of("I", "love", "you", "too");
stream.map(str -> str.toUpperCase())
    .forEach(str -> System.out.println(str));
```

项目常用：

```java
List<ADto> rules = elist.stream().map(e -> {
            ADto aDto = new ADto();
            aDto.setBagId(e.getBagId());
            aDto.setLocale(e.getLocale());
            aDto.setRuleId(e.getRuleId());
            return aDto;
        }).collect(Collectors.toList());
//或者封装一个方法
List<ADto> rules = elist.stream().map(e -> convertA(e)
        ).collect(Collectors.toList());
```

##### peek

`Stream<T> peek(Consumer<? super T> action)`

```java
Stream.of("one", "two", "three","four").peek(u -> u.toUpperCase())
                .forEach(System.out::println);
//输出：one  two  three   four
```

> map和peek的区别

Consumer是没有返回值的，它只是对Stream中的元素进行某些操作，但是操作之后的数据并不返回到Stream中，所以Stream中的元素还是原来的元素。

而Function是有返回值的，这意味着对于Stream的元素的所有操作都会作为新的结果返回到Stream中。

```java
//peek常用案例
  List<User> userList=Stream.of(new User("a"),new User("b"),new User("c")).peek(u->u.setName("kkk")).collect(Collectors.toList());
        log.info("{}",userList);
//10:25:59.784 [main] INFO com.flydean.PeekUsage - [PeekUsage.User(name=kkk), PeekUsage.User(name=kkk), PeekUsage.User(name=kkk)]
```

##### flatmap

函数原型为`<R> Stream<R> flatMap(Function<? super T,? extends Stream<? extends R>> mapper)`，作用是对每个元素执行`mapper`指定的操作，并用所有`mapper`返回的`Stream`中的元素组成一个新的`Stream`作为最终返回结果。说起来太拗口，通俗的讲`flatMap()`的作用就相当于把原*stream*中的所有元素都"摊平"之后组成的`Stream`，转换前后元素的个数和类型都可能会改变。

```java
Stream<List<Integer>> stream = Stream.of(Arrays.asList(1,2), Arrays.asList(3, 4, 5));
stream.flatMap(list -> list.stream())
    .forEach(i -> System.out.println(i));
```

上述代码中，原来的`stream`中有两个元素，分别是两个`List<Integer>`，执行`flatMap()`之后，将每个`List`都“摊平”成了一个个的数字，所以会新产生一个由5个数字组成的`Stream`。所以最终将输出1~5这5个数字。

形象的例子：

现在学校通知关于数学教学的通知给家长。思路：找到所有教数学的老师，找到老师教的学生，找到学生的家长进行通知。

```java
public static void main(String[] args) {
        // 找到所有数学老师的学生的家长的电话,并找他们开家长会
        List<Parents> collect = teacs.stream()
                // 过滤数学老师
                .filter(t -> Subject.Math.getValue().equals(t.getSubject()))
                // 通过老师找学生
                .flatMap(t -> stus.stream().filter(s -> s.getTechId().equals(t.getId())))
                // 过滤重复的学生(使用student的equals和hashCode方法)
                .distinct()
                // 通过学生找家长(这里就简化为创建家长对象)
                .map(s -> {
                    Parents p = new Parents();
                    p.setId(UUID.randomUUID().toString());
                    p.setChirldId(s.getId());
                    p.setName(s.getName().toUpperCase() + "'s Parent");
                    p.setEmail((int) (Math.random() * 1000000) + "@qq.com");
                    return p;
                })
                .collect(Collectors.toList());
        // 打印到控制台看看
        collect.stream()
                .forEach(System.out::println);
    }
```

对于调用flatmap的流的每一个元素，执行flatmap入参中的函数式方法，由于该函数式方法必须返回一个stream<T>类型的流，这样对于调用flatmap的操作来说，就收集了另一种类型(<T>)的流，并在后续的操作中将<T>类型进行合并，最终产生一个stream<T>的流，而不是一个stream<stream<T>>类型的流。

> flapmap和map的区别：

#### 规约操作

规约操作（*reduction operation*）又被称作折叠操作（*fold*），是通过某个连接动作将所有元素汇总成一个汇总结果的过程。元素求和、求最大值或最小值、求出元素总个数、将所有元素转换成一个列表或集合，都属于规约操作。*Stream*类库有两个通用的规约操作`reduce()`和`collect()`，也有一些为简化书写而设计的专用规约操作，比如`sum()`、`max()`、`min()`、`count()`等。

##### reduce

`Optional<T> reduce(BinaryOperator<T> accumulator)`

`T reduce(T identity, BinaryOperator<T> accumulator)`

`<U> U reduce(U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner)`

！！擅长生成一个值！！

`reduce()`最常用的场景就是从一堆值中生成一个值。用这么复杂的函数去求一个最大或最小值，你是不是觉得设计者有病。其实不然，因为“大”和“小”或者“求和"有时会有不同的语义。

```java
// 找出最长的单词
Stream<String> stream = Stream.of("I", "love", "you", "too");
Optional<String> longest = stream.reduce((s1, s2) -> s1.length()>=s2.length() ? s1 : s2);
//Optional<String> longest = stream.max((s1, s2) -> s1.length()-s2.length());
System.out.println(longest.get());
```

```java
//求出一组单词的长度之和。这是个“求和”操作，操作对象输入类型是String，而结果类型是Integer。
Stream<String> stream = Stream.of("I", "love", "you", "too");
Integer lengthSum = stream.reduce(0,　// 初始值　// (1)
        (sum, str) -> sum+str.length(), // 累加器 // (2)
        (a, b) -> a+b);　// 部分和拼接器，并行执行时才会用到 // (3)
// int lengthSum = stream.mapToInt(str -> str.length()).sum();
System.out.println(lengthSum);
```

##### collect！！！！！！

！！擅长生成一个集合或者是Map等复杂对象！！

```java
// 将Stream转换成容器或Map
//上述代码分别列举了如何将Stream转换成List、Set和Map
Stream<String> stream = Stream.of("I", "love", "you", "too");
List<String> list = stream.collect(Collectors.toList()); // (1)
// Set<String> set = stream.collect(Collectors.toSet()); // (2)
// Map<String, Integer> map = stream.collect(Collectors.toMap(Function.identity(), String::length)); // (3)
//System.out.println(map);
//{love=4, too=3, I=1, you=3}
```

> *Function*是一个接口，那么`Function.identity()`是什么意思呢？这要从两方面解释：

1. Java 8允许在接口中加入具体方法。接口中的具体方法有两种，*default*方法和*static*方法，`identity()`就是*Function*接口的一个静态方法。
2. `Function.identity()`是Lambda表达式对象，意思是返回的输出和输入一样，等价于形如`t -> t`形式的Lambda表达式。

> ::

诸如`String::length`的语法形式叫做方法引用（*method references*），这种语法用来替代某些特定形式Lambda表达式。如果Lambda表达式的全部内容就是调用一个已有的方法，那么可以用方法引用来替代Lambda表达式。方法引用可以细分为四类：

| 方法引用类别       | 举例             |
| ------------------ | ---------------- |
| 引用静态方法       | `Integer::sum`   |
| 引用某个对象的方法 | `list::add`      |
| 引用某个类的方法   | `String::length` |
| 引用构造方法       | `HashMap::new`   |

>转成list，set

*collect()*方法定义为`<R> R collect(Supplier<R> supplier, BiConsumer<R,? super T> accumulator, BiConsumer<R,R> combiner)`，三个参数依次对应上述三条分析。不过每次调用*collect()*都要传入这三个参数太麻烦，收集器*Collector*就是对这三个参数的简单封装。

```java
//　将Stream规约成List
Stream<String> stream = Stream.of("I", "love", "you", "too");
List<String> list = stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);// 方式１
//List<String> list = stream.collect(Collectors.toList());// 方式2
System.out.println(list);
```

当想要确切的制定返回的类型，而不是接口类型的时候

```java
// 使用toCollection()指定规约容器的类型
ArrayList<String> arrayList = stream.collect(Collectors.toCollection(ArrayList::new));// (3)
HashSet<String> hashSet = stream.collect(Collectors.toCollection(HashSet::new));// (4)
```

> 转成map

```java
// 使用toMap()统计学生GPA
Map<Student, Double> studentToGPA =
     students.stream().collect(Collectors.toMap(Functions.identity(),// 如何生成key
                                     student -> computeGPA(student)));// 如何生成value
```

```java
// Partition students into passing and failing
Map<Boolean, List<Student>> passingFailing = students.stream()
         .collect(Collectors.partitioningBy(s -> s.getGrade() >= PASS_THRESHOLD));
```

```java
// Group employees by department
Map<Department, List<Employee>> byDept = employees.stream()
            .collect(Collectors.groupingBy(Employee::getDepartment));
//增强版groupingBy
// 使用下游收集器统计每个部门的人数
Map<Department, Integer> totalByDept = employees.stream()
                    .collect(Collectors.groupingBy(Employee::getDepartment,
                                                   Collectors.counting()));// 下游收集器
//下游收集器还可以有更下游的收集器
// 按照部门对员工分布组，并只保留员工的名字
Map<Department, List<String>> byDept = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment,
                        Collectors.mapping(Employee::getName,// 下游收集器
                                Collectors.toList())));// 更下游的收集器
                               
```

```java
//Collectors.joining()
// 使用Collectors.joining()拼接字符串
Stream<String> stream = Stream.of("I", "love", "you");
//String joined = stream.collect(Collectors.joining());// "Iloveyou"
//String joined = stream.collect(Collectors.joining(","));// "I,love,you"
String joined = stream.collect(Collectors.joining(",", "{", "}"));// "{I,love,you}"
```

#### 收集器

收集器（*Collector*）是为`Stream.collect()`方法量身打造的工具接口（类）。考虑一下将一个*Stream*转换成一个容器（或者*Map*）需要做哪些工作？我们至少需要两样东西：

1. 目标容器是什么？是*ArrayList*还是*HashSet*，或者是个*TreeMap*。
2. 新元素如何添加到容器中？是`List.add()`还是`Map.put()`

如果并行的进行规约，还需要告诉*collect()* 3. 多个部分结果如何合并成一个。

详情见上面的例子。



```java
//测试代码
public class Java8TestStream {
    public static void main(String args[]){
        System.out.println("使用 Java 7: ");

        // 计算空字符串
        List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl");
        System.out.println("列表: " +strings);
        long count = getCountEmptyStringUsingJava7(strings);

        System.out.println("空字符数量为: " + count);
        count = getCountLength3UsingJava7(strings);

        System.out.println("字符串长度为 3 的数量为: " + count);

        // 删除空字符串
        List<String> filtered = deleteEmptyStringsUsingJava7(strings);
        System.out.println("筛选后的列表: " + filtered);

        // 删除空字符串，并使用逗号把它们合并起来
        String mergedString = getMergedStringUsingJava7(strings,", ");
        System.out.println("合并字符串: " + mergedString);
        List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5);

        // 获取列表元素平方数
        List<Integer> squaresList = getSquares(numbers);
        System.out.println("平方数列表: " + squaresList);
        List<Integer> integers = Arrays.asList(1,2,13,4,15,6,17,8,19);

        System.out.println("列表: " +integers);
        System.out.println("列表中最大的数 : " + getMax(integers));
        System.out.println("列表中最小的数 : " + getMin(integers));
        System.out.println("所有数之和 : " + getSum(integers));
        System.out.println("平均数 : " + getAverage(integers));
        System.out.println("随机数: ");

        // 输出10个随机数
        Random random = new Random();

        for(int i=0; i < 10; i++){
            System.out.println(random.nextInt());
        }

        System.out.println("使用 Java 8: ");
        
        
        System.out.println("列表: " +strings);
		//过滤器！！！！！
        count = strings.stream().filter(string->string.isEmpty()).count();
        System.out.println("空字符串数量为: " + count);

        count = strings.stream().filter(string -> string.length() == 3).count();
        System.out.println("字符串长度为 3 的数量为: " + count);
		
        
        //过滤器后转化为其他类型
        filtered = strings.stream().filter(string ->!string.isEmpty()).limit(3).collect(Collectors.toList());
        System.out.println("筛选后的列表: " + filtered);
/**
	//用这种方法也已将类集合选择其中两个属性转化为map形式
	Map<String, String> map = tagsList.stream().collect(Collectors.toMap(Tag::getTagName, Tag::getTagColor));
	
*/
        mergedString = strings.stream().filter(string ->!string.isEmpty()).collect(Collectors.joining(", "));
        System.out.println("合并字符串: " + mergedString);

        squaresList = numbers.stream().map( i ->i*i).distinct().collect(Collectors.toList());
        System.out.println("Squares List: " + squaresList);
        System.out.println("列表: " +integers);

        IntSummaryStatistics stats = integers.stream().mapToInt((x) ->x).summaryStatistics();
        List<Integer> reverseIntegers = integers.stream().map(i -> i * i).sorted((x, y) -> y - x).collect(Collectors.toList());
        System.out.println("数字平方倒序输出："+reverseIntegers);

        System.out.println("列表中最大的数 : " + stats.getMax());
        System.out.println("列表中最小的数 : " + stats.getMin());
        System.out.println("所有数之和 : " + stats.getSum());
        System.out.println("平均数 : " + stats.getAverage());
        System.out.println("随机数: ");

        random.ints().limit(10).sorted().forEach(System.out::println);

        // 并行处理
        count = strings.parallelStream().filter(string -> string.isEmpty()).count();
        System.out.println("空字符串的数量为: " + count);
    }

    private static int getCountEmptyStringUsingJava7(List<String> strings){
        int count = 0;

        for(String string: strings){

            if(string.isEmpty()){
                count++;
            }
        }
        return count;
    }

    private static int getCountLength3UsingJava7(List<String> strings){
        int count = 0;

        for(String string: strings){

            if(string.length() == 3){
                count++;
            }
        }
        return count;
    }

    private static List<String> deleteEmptyStringsUsingJava7(List<String> strings){
        List<String> filteredList = new ArrayList<String>();

        for(String string: strings){

            if(!string.isEmpty()){
                filteredList.add(string);
            }
        }
        return filteredList;
    }

    private static String getMergedStringUsingJava7(List<String> strings, String separator){
        StringBuilder stringBuilder = new StringBuilder();

        for(String string: strings){

            if(!string.isEmpty()){
                stringBuilder.append(string);
                stringBuilder.append(separator);
            }
        }
        String mergedString = stringBuilder.toString();
        return mergedString.substring(0, mergedString.length()-2);
    }

    private static List<Integer> getSquares(List<Integer> numbers){
        List<Integer> squaresList = new ArrayList<Integer>();

        for(Integer number: numbers){
            Integer square = new Integer(number.intValue() * number.intValue());

            if(!squaresList.contains(square)){
                squaresList.add(square);
            }
        }
        return squaresList;
    }

    private static int getMax(List<Integer> numbers){
        int max = numbers.get(0);

        for(int i=1;i < numbers.size();i++){

            Integer number = numbers.get(i);

            if(number.intValue() > max){
                max = number.intValue();
            }
        }
        return max;
    }

    private static int getMin(List<Integer> numbers){
        int min = numbers.get(0);

        for(int i=1;i < numbers.size();i++){
            Integer number = numbers.get(i);

            if(number.intValue() < min){
                min = number.intValue();
            }
        }
        return min;
    }

    private static int getSum(List numbers){
        int sum = (int)(numbers.get(0));

        for(int i=1;i < numbers.size();i++){
            sum += (int)numbers.get(i);
        }
        return sum;
    }

    private static int getAverage(List<Integer> numbers){
        return getSum(numbers) / numbers.size();
    }
}

```

### 2.时间

> java.util.Date。实现类，其对象具有时间、日期组件。
>
> java.util.Calendar。抽象类，其对象具有时间、日期组件。
>
> java.sql.Date。实现类，其对象具有日期组件。---无日期组件
>
> java.sql.Time。实现类，其对象具有时间组件。----无时间组件
>
> java.sql.Timestamp。实现类，其对象具有时间日期组件。
>
> java.text.DateFormat。抽象类，其对象格式化时间日期。

#### 例子：

```java
/**
		*	展示各个日期时间组件的输出类型
		*/
		java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
		System.out.println(sqlDate.toString()); // 输出结果：2015-06-25
		
		java.sql.Time sqlTime = new java.sql.Time(System.currentTimeMillis());
		System.out.println(sqlTime.toString()); // 输出结果：09:13:43
		
		java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
		System.out.println(sqlTimestamp.toString()); // 输出结果：2015-06-25 09:13:43.561
		
		java.util.Date utilDate = new java.util.Date(System.currentTimeMillis());
		System.out.println(utilDate.toString()); // 输出结果：Thu Jun 25 09:13:43 CST 2015
		
		java.util.Calendar cl = java.util.Calendar.getInstance();
		System.out.println(cl.getTime().toString()); // 输出结果：Thu Jun 25 09:13:43 CST 2015

```

#### SimpleDateFormat

```java
/**
		 * java.text.SimpleDateFormat的用法
		 */
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp now = new Timestamp(System.currentTimeMillis());
		System.out.println(now); // 2015-06-25 14:27:41.477
		String time = df.format(now);
		System.out.println(time); // 2015-06-25 14:27:41
		System.out.println(Timestamp.valueOf(time)); // 2015-06-25 14:27:41.0
 
		Date now1 = new Date();
		System.out.println(now); // 2015-06-25 14:27:41.477
		String time1 = df.format(now1);
		System.out.println(time1); // 2015-06-25 14:27:41
		System.out.println(Timestamp.valueOf(time1)); // 2015-06-25 14:27:41.0

		//字符串类型转换为日期类型
		Date date;
        date = df.parse(strTime);
```

#### Calendar

```java
/**
	 * java.util.Calendar的用法
	 */
	Calendar calendar1 = Calendar.getInstance();
	System.out.println(calendar1); // java.util.GregorianCalendar[time=1435214975097,areFieldsSet=true,
	// areAllFieldsSet=true,lenient=true,zone=sun.util.calendar.ZoneInfo[id="Asia/Shanghai",
	// offset=28800000,dstSavings=0,useDaylight=false,transitions=19,lastRule=null],
	// firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=1,YEAR=2015,MONTH=5,
	// WEEK_OF_YEAR=26,WEEK_OF_MONTH=4,DAY_OF_MONTH=25,DAY_OF_YEAR=176,DAY_OF_WEEK=5,DAY_OF_WEEK_IN_MONTH=4,
	// AM_PM=1,HOUR=2,HOUR_OF_DAY=14,MINUTE=49,SECOND=35,MILLISECOND=97,ZONE_OFFSET=28800000,DST_OFFSET=0]
 
	// 获取时间
	Date date1 = calendar1.getTime();
	System.out.println(date1); // Thu Jun 25 14:49:35 CST 2015
	System.out.println(calendar1.getWeeksInWeekYear()); // 52
	System.out.println(calendar1.get(Calendar.DAY_OF_MONTH)); // 25
	System.out.println(calendar1.get(Calendar.HOUR_OF_DAY)); // 14

```

#### 获取几天前的日期

##### timestamp

```java
/**
	 * 获取系统的当前时间
	 * 
	 * @return 返回 Timestamp 类型的时间
	 */
	public static Timestamp getNowTime_tamp() {
		// 获取当前时间
		Date now = new Date();
		Long tim = now.getTime();
		Timestamp time = new Timestamp(tim);
		return time;
    }
//获取7天前的日期
public static Timestamp getA_Few_dayTWOTime_tamp(int n) {
 Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE,-7);
        Date dateBegin = c.getTime();
        Timestamp beginTime = new Timestamp(dateBegin.getTime());
    return beginTime;
    /**
    *   返回字符串形式
    *	String yesterday = new SimpleDateFormat("yyyy-MM-dd ").format(cal.getTime());
	*	yesterday = yesterday.trim();
	*	return yesterday;
	*/
}
```



### 3.mapstruct

> 属性映射：专门用来处理 domin 实体类与 model 类的属性映射的，和BeanUtils的copyProperties作用差不多，但是比起强大。

#### [github地址](https://github.com/mapstruct/mapstruct/)

#### [例子](https://github.com/mmzsblog/mapstructDemo/tree/master/src/main)

- 依赖

```xml
 <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-jdk8</artifactId>
        <version>1.2.0.Final</version>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>1.2.0.Final</version>
    </dependency>
```

- 实体类和被映射类

```java
// 实体类
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Integer id;
    private String name;
    private String createTime;
    private LocalDateTime updateTime;
}

// 被映射类VO1:和实体类一模一样
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO1 {
    private Integer id;
    private String name;
    private String createTime;
    private LocalDateTime updateTime;
}

// 被映射类VO1:比实体类少一个字段
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO2 {
    private Integer id;
    private String name;
    private String createTime;

}
```

- 接口

```java
//spring: 生成的实现类上面会自动添加一个@Component注解，可以通过Spring的 @Autowired方式进行注入
//default: 这是默认的情况，mapstruct 不使用任何组件类型, 可以通过Mappers.getMapper(Class)方式获取自动生成的实例对象。
@Mapper(componentModel = "spring")
public interface UserCovertBasic {
    UserCovertBasic INSTANCE = Mappers.getMapper(UserCovertBasic.class);

    /**
     * 字段数量类型数量相同，利用工具BeanUtils也可以实现类似效果
     * @param source
     * @return
     */
    UserVO1 toConvertVO1(User source);
    User fromConvertEntity1(UserVO1 userVO1);
    //集合的转换
    List<UserVO1> toConvertVOList(List<User> source);

    /**
     * 字段数量类型相同,数量少：仅能让多的转换成少的，故没有fromConvertEntity2
     * @param source
     * @return
     */
    UserVO2 toConvertVO2(User source);
}
```

- 使用

```java
@RestController
public class TestController {

    @GetMapping("convert")
    public Object convertEntity() {
        User user = User.builder()
                .id(1)
                .name("张三")
                .createTime("2020-04-01 11:05:07")
                .updateTime(LocalDateTime.now())
                .build();
        List<Object> objectList = new ArrayList<>();

        objectList.add(user);

        // 使用mapstruct
        UserVO1 userVO1 = UserCovertBasic.INSTANCE.toConvertVO1(user);
        objectList.add("userVO1:" + UserCovertBasic.INSTANCE.toConvertVO1(user));
        objectList.add("userVO1转换回实体类user:" + UserCovertBasic.INSTANCE.fromConvertEntity1(userVO1));
        // 输出转换结果
        objectList.add("userVO2:" + " | " + UserCovertBasic.INSTANCE.toConvertVO2(user));
        // 使用BeanUtils
        UserVO2 userVO22 = new UserVO2();
        BeanUtils.copyProperties(user, userVO22);
        objectList.add("userVO22:" + " | " + userVO22);

        return objectList;
    }
}
```

可以查看IDE的编译结果

```java
@Component
public class UserCovertBasicImpl implements UserCovertBasic {
    public UserCovertBasicImpl() {
    }

    public UserVO1 toConvertVO1(User source) {
        if (source == null) {
            return null;
        } else {
            UserVO1 userVO1 = new UserVO1();
            userVO1.setId(source.getId());
            userVO1.setName(source.getName());
            userVO1.setCreateTime(source.getCreateTime());
            userVO1.setUpdateTime(source.getUpdateTime());
            return userVO1;
        }
    }

    public User fromConvertEntity1(UserVO1 userVO1) {
        if (userVO1 == null) {
            return null;
        } else {
            User user = new User();
            user.setId(userVO1.getId());
            user.setName(userVO1.getName());
            user.setCreateTime(userVO1.getCreateTime());
            user.setUpdateTime(userVO1.getUpdateTime());
            return user;
        }
    }

    public UserVO2 toConvertVO2(User source) {
        if (source == null) {
            return null;
        } else {
            UserVO2 userVO2 = new UserVO2();
            userVO2.setId(source.getId());
            userVO2.setName(source.getName());
            userVO2.setCreateTime(source.getCreateTime());
            return userVO2;
        }
    }
}
```

#### 特殊情况：

##### 类型不一致

- 新的被映射对象

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO3 {
    private String id;
    private String name;
    // 实体类该属性是String
    private LocalDateTime createTime;
    // 实体类该属性是LocalDateTime
    private String updateTime;
}
```

- 接口

```java
@Mappings({
            @Mapping(target = "createTime", expression = "java(com.java.mmzsblog.util.DateTransform.strToDate(source.getCreateTime()))"),
    })
    UserVO3 toConvertVO3(User source);

    User fromConvertEntity3(UserVO3 userVO3);
```

上面 `expression` 指定的表达式内容如下：

```java
public class DateTransform {
    public static LocalDateTime strToDate(String str){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse("2018-01-12 17:07:05",df);
    }

}
```

当字段类型不一致时，以下的类型之间是 `mapstruct` 自动进行类型转换的:

- 1、基本类型及其他们对应的包装类型。
  此时 `mapstruct` 会自动进行拆装箱。不需要人为的处理
- 2、基本类型的包装类型和string类型之间

除此之外的类型转换我们可以通过定义表达式来进行指定转换。

##### 字段不一致

- 新的被映射对象

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO4 {
    // 实体类该属性名是id
    private String userId;
    // 实体类该属性名是name
    private String userName;
    private String createTime;
    private String updateTime;
}
```

- 定义接口



```java
 @Mappings({
            @Mapping(source = "id", target = "userId"),
            @Mapping(source = "name", target = "userName")
    })
    UserVO4 toConvertVO(User source);
    
    User fromConvertEntity(UserVO4 userVO4);
```

当字段名不一致时，通过使用 `@Mappings` 注解指定对应关系，编译后即可实现对应字段的赋值。

##### 属性是枚举类型

- 实体类

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEnum {
    private Integer id;
    private String name;
    private UserTypeEnum userTypeEnum;
}
```

- 被映射对象

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO5 {
    private Integer id;
    private String name;
    private String type;
}
```

- 枚举类对象

```java
@Getter
@AllArgsConstructor
public enum UserTypeEnum {
    Java("000", "Java开发工程师"),
    DB("001", "数据库管理员"),
    LINUX("002", "Linux运维员");
    
    private String value;
    private String title;

}
```

- 接口

```java
    @Mapping(source = "userTypeEnum", target = "type")
    UserVO5 toConvertVO5(UserEnum source);

    UserEnum fromConvertEntity5(UserVO5 userVO5);
```

`mapstruct` 通过枚举类型的内容，帮我们把枚举类型转换成字符串，并给type赋值

### 4.PageHelper

#### 例子：

```java
public PageBean<ProjectEntity> queryAllByConditionAndPage(List<Long> projectIdList, String name, String productLine, String source, Integer status, Integer creator, Date submitTimeFrom, Date submitTimeTo, Date deadlineFrom, Date deadlineTo, Date estimateCompleteFrom, Date estimateCompleteTo,
                                                              String sortName, String sortOrder, Boolean isFilterCreated, int pageNo, int pageSize) {


        ProjectExample example = initSelectProjectExample(projectIdList, name, productLine, creator, status == null ? null : Collections.singletonList(status), source, submitTimeFrom, submitTimeTo, deadlineFrom, deadlineTo, estimateCompleteFrom, estimateCompleteTo, isFilterCreated);
        example.setOrderByClause(sortName + " " + sortOrder);
      //直接使用Pagehelper
        PageHelper.startPage(pageNo, pageSize);
        List<Project> list = projectMapper.selectByExample(example);
    //用PageInfo将结果集封装
        PageInfo<Project> pageInfo = new PageInfo<>(list);
        List<ProjectEntity> entityList = list.stream().map(this::converetToProjectEntity).collect(Collectors.toList());
    //pageInfo就可以方便的得到pageNum,pageSize,total,list
        return new PageBean<>(pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), entityList);
    }
```

### 5.复制

```java
//map是一个已有数据的值		
Map<String, Long> projectTabTotal = new HashMap<>();
        map.forEach((k, v) -> projectTabTotal.put(k.toString(), v));
```

```java
	List<Tag> tags = tumaTagQconfig.getTagsList();
        ArrayList<TagDTO> tagDTOS = new ArrayList<>();

        tags.forEach(tag->{
            TagDTO tagDTO = ConvertUtil.convert(tag, TagDTO.class);
            tagDTOS.add(tagDTO);
        });

```

### 6.[属性描述器PropertyDescriptor](https://unclecatmyself.github.io/2019/01/19/propertyDescriptor/)

> 简介：就是通过，传入属性名和类名，获取某个类的实例(也可以先改变类的属性值，再获取类的实例)

例：

```java
//set get toString 方法省略
public class Cat {

    private String name;

    private String describe;

    private int age;

    private int weight;
}
```

```
 //三种构造函数
 //1.传入属性名和类名 （实际是调用第二种构造函数，内部拼接了get和set方法调用了第二种构造函数）
 PropertyDescriptor CatPropertyOfName = new PropertyDescriptor("name", Cat.class);
 //2.第二种构造函数，传入属性名 类名 对应属性的get和set方法名
 PropertyDescriptor CatPropertyOfName = new PropertyDescriptor("name", Cat.class,"getName","setName");
 //3.第三种构造函数，属性名 属性对应的get和set方法名
        Class<?> classType = Cat.class;
        Method CatNameOfRead = classType.getMethod("getName");
        Method CatNameOfWrite = classType.getMethod("setName", String.class);
        PropertyDescriptor CatPropertyOfName = new PropertyDescriptor("name", CatNameOfRead,CatNameOfWrite);
        
        
     System.out.println(CatPropertyOfName.getPropertyType());
     System.out.println(CatPropertyOfName.getPropertyEditorClass());
     System.out.println(CatPropertyOfName.getReadMethod());
     System.out.println(CatPropertyOfName.getWriteMethod());     
```

```
结果：
class java.lang.String
null
public java.lang.String entity.Cat.getName()
public void entity.Cat.setName(java.lang.String)
```

应用：

1.

```java
 public static void main(String[] args) throws Exception {
        //获取类
        Class classType = Class.forName("com.example.demo.beans.Cat");
        Object catObj = classType.newInstance();
        //获取Name属性
        PropertyDescriptor catPropertyOfName = new PropertyDescriptor("name",classType);
        //得到对应的写方法
        Method writeOfName = catPropertyOfName.getWriteMethod();
        //将值赋进这个类中
        writeOfName.invoke(catObj,"river");
        Cat cat = (Cat)catObj;
        System.out.println(cat.toString());
    }
输出：
    Cat{name=’river’, describe=’null’, age=0, weight=0}
```

2.

```java
 public static void main(String[] args) throws Exception {
        //一开始的默认对象
        Cat cat = new Cat("river","黑猫",2,4);
        //获取name属性
        PropertyDescriptor catPropertyOfName = new PropertyDescriptor("name",Cat.class);
        //得到读方法
        Method readMethod = catPropertyOfName.getReadMethod();
        //获取属性值
        String name = (String) readMethod.invoke(cat);
        System.out.println("默认：" + name);
        //得到写方法
        Method writeMethod = catPropertyOfName.getWriteMethod();
        //修改值
        writeMethod.invoke(cat,"copy");
        System.out.println("修改后：" + cat);
    }
输出：
    默认：river 修改后：Cat{name=’copy’, describe=’黑猫’, age=2, weight=4}
```

3.取http的请求头中的token和appid（见携程实习 work 2 切面）：

```java
public class ReflectUtils {
//instance传入请求的实例，fieldname传入字符串 “requestHeader”
public static Object invokeGetter(Object instance, String fieldName) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
    PropertyDescriptor pd = new PropertyDescriptor(fieldName, instance.getClass());
    return pd.getReadMethod().invoke(instance);
}
}
```

```java
 RequestHeaderType questHeader=(RequestHeaderType) ReflectUtils.invokeGetter(request,“requestHeader”);
questHeader.getToken();
questHeader.getAppId();
```

### 7.[Optional](https://www.cnblogs.com/rjzheng/p/9163246.html)

#### 使用例子：

例1：

```java
//嵌套式对象判断---旧写法
public String getCity(User user)  throws Exception{
		if(user!=null){
			if(user.getAddress()!=null){
				Address address = user.getAddress();
				if(address.getCity()!=null){
					return address.getCity();
				}
			}
		}
		throw new Excpetion("取值错误"); 
	}
//新的写法
public String getCity(User user) throws Exception{
	return Optional.ofNullable(user)
				   .map(u-> u.getAddress())
				   .map(a->a.getCity())
				   .orElseThrow(()->new Exception("取指错误"));
}
```

例2：

```java
//单个判断----旧
if(user!=null){
	dosomething(user);
}
//新的写法
 Optional.ofNullable(user)
         .ifPresent(u->{
		    dosomething(u);
		 });
```

例3：

```java
//单个 属性判断----旧
public User getUser(User user) throws Exception{
	if(user!=null){
		String name = user.getName();
		if("zhangsan".equals(name)){
			return user;
		}
	}else{
		user = new User();
		user.setName("zhangsan");
		return user;
	}
}
//新
public User getUser(User user) {
	return Optional.ofNullable(user)
	               .filter(u->"zhangsan".equals(u.getName()))
	               .orElseGet(()-> {
                		User user1 = new User();
                		user1.setName("zhangsan");
                		return user1;
	               });
}
```

#### api

- (1)Optional(T value),empty(),of(T value),ofNullable(T value) 均是返回Optional对象

  ​	Optional(T value)是构造函数

  ​	of(T value)内部使用了构造函数，value为空的时候报，NullPointerException

  ​															value不为空的时候，正常构造一个Optional对象

  ​	empty()的作用是返回一个value=null的Optional对象

  ​	ofNullable不会报空指针异常，都会返回一个Option对象

- (2)orElse(T other)，orElseGet(Supplier<? extends T> other)和orElseThrow(Supplier<? extends X> exceptionSupplier)

  ```java
  @Test
  public void test() {
  	User user = null;
  	user = Optional.ofNullable(user).orElse(createUser());
  	user = Optional.ofNullable(user).orElseGet(() -> createUser());
  	User user = null;
  	Optional.ofNullable(user).orElseThrow(()->new Exception("用户不存在"));
  }
  public User createUser(){
  	User user = new User();
  	user.setName("zhangsan");
  	return user;
  }
  ```

  区别：当user值不为null时，orElse函数依然会执行createUser()方法，而orElseGet函数并不会执行createUser()方法；orElseThrow是value值为null时,直接抛一个异常出去

- (3) map(Function<? super T, ? extends U> mapper)和flatMap(Function<? super T, Optional<U>> mapper)

  ```java
  //map
  public class User {
  	private String name;
  	public String getName() {
  		return name;
  	}
  }
  String city = Optional.ofNullable(user).map(u-> u.getName()).get();
  
  ```

  ```java
  //flatmap
  public class User {
  	private String name;
  	public Optional<String> getName() {
  		return Optional.ofNullable(name);
  	}
  }
  
  String city = Optional.ofNullable(user).flatMap(u-> u.getName()).get();
  ```

- (4) isPresent()和ifPresent(Consumer<? super T> consumer)

  ```java
  User user = Optional.ofNullable(user);
  if (Optional.isPresent()){
     // TODO: do something
  }
  
  Optional.ofNullable(user).ifPresent(u->{
  			// TODO: do something
  		});
  ```

- (5) filter(Predicate<? super T> predicate)

  ```java
  Optional<User> user1 = Optional.ofNullable(user).filter(u -> u.getName().length()<6);
  ```

### 8.[动态代理](https://louluan.blog.csdn.net/article/details/24589193)

#### class文件简介和加载

Java编译器编译好Java文件之后，产生.class 文件在磁盘中。这种class文件是二进制文件，内容是只有JVM虚拟机能够识别的机器码。JVM虚拟机读取字节码文件，取出二进制数据，加载到内存中，解析.class 文件内的信息，生成对应的 Class对象:

![1622529367(1)](images\1622529367(1).jpg)

代码模拟字节码加载成class对象的过程：

```java

/**
 * 程序猿类
 * @author louluan
 */
public class Programmer {
 
	public void code()
	{
		System.out.println("I'm a Programmer,Just Coding.....");
	}

```

```java

/**
 * 自定义一个类加载器，用于将字节码转换为class对象
 * @author louluan
 */
public class MyClassLoader extends ClassLoader {
 
	public Class<?> defineMyClass( byte[] b, int off, int len) 
	{
		return super.defineClass(b, off, len);
	}
	
```

```java
public class MyTest {
 
	public static void main(String[] args) throws IOException {
		//读取本地的class文件内的字节码，转换成字节码数组
		File file = new File(".");
		InputStream  input = new FileInputStream(file.getCanonicalPath()+"\\bin\\samples\\Programmer.class");
		byte[] result = new byte[1024];
		
		int count = input.read(result);
		// 使用自定义的类加载器将 byte字节码数组转换为对应的class对象
		MyClassLoader loader = new MyClassLoader();
		Class clazz = loader.defineMyClass( result, 0, count);
		//测试加载是否成功，打印class 对象的名称
		System.out.println(clazz.getCanonicalName());
                
               //实例化一个Programmer对象
               Object o= clazz.newInstance();
               try {
                   //调用Programmer的code方法
                    clazz.getMethod("code", null).invoke(o, null);
                   } catch (IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
                     e.printStackTrace();
                  }
 }
}
```

#### 运行期间生成二进制字节码(即运行时动态创建类）

 由于JVM通过字节码的二进制信息加载类的，那么，如果我们在运行期系统中，遵循Java编译系统组织.class文件的格式和结构，生成相应的二进制数据，然后再把这个二进制数据加载转换成对应的类，这样，就完成了在代码中，动态创建一个类的能力了。

![1622529949(1)](images\1622529949(1).jpg)



##### 	ASM框架

ASM 是一个 Java 字节码操控框架。它能够以二进制形式修改已有类或者动态生成类。ASM 可以直接产生二进制 class 文件，也可以在类被加载入 Java 虚拟机之前动态改变类行为。ASM 从类文件中读入信息后，能够改变类行为，分析类信息，甚至能够根据用户要求生成新类。

通俗的说：就是在代码里生成字节码，并且动态的加载class对象，创建实例。

##### 	Javassist

​		Javassist是一个开源的分析、编辑和创建Java字节码的类库。

#### 代理的构成

代理模式上，基本上有Subject角色，RealSubject角色，Proxy角色。其中：Subject角色负责定义RealSubject和Proxy角色应该实现的接口；RealSubject角色用来真正完成业务服务功能；Proxy角色负责将自身的Request请求，调用realsubject 对应的request功能来实现业务功能，自己不真正做业务。
![1622530467(1)](images\1622530467(1).jpg)

​			这张图是**静态代理模式**，`当在代码阶段规定这种代理关系，Proxy类通过编译器编译成class文件，当系统运行时，此class已经存在了。这种静态的代理模式固然在访问无法访问的资源，增强现有的接口业务功能方面有很大的优点，但是大量使用这种静态代理，会使我们系统内的类的规模增大，并且不易维护；并且由于Proxy和RealSubject的功能 本质上是相同的，Proxy只是起到了中介的作用，这种代理在系统中的存在，导致系统结构比较臃肿和松散。`

 为了解决这个问题，就有了动态地创建Proxy的想法：在运行状态中，需要代理的地方，根据Subject 和RealSubject，动态地创建一个Proxy，用完之后，就会销毁，这样就可以避免了Proxy 角色的class在系统中冗杂的问题了。

##### 	实例：

​	 将车站的售票服务抽象出一个接口TicketService,包含问询，卖票，退票功能，车站类Station实现了TicketService接口，车票代售点StationProxy则实现了代理角色的功能。

​	![1622530886(1)](images\1622530886(1).jpg)

​		> 静态代理模式

```java

/**
 * 售票服务接口 
 * @author louluan
 */
public interface TicketService {
 
	//售票
	public void sellTicket();
	
	//问询
	public void inquire();
	
	//退票
	public void withdraw();
}
```

```java
/**
 * 售票服务接口实现类，车站
 * @author louluan
 */
public class Station implements TicketService {
 
	@Override
	public void sellTicket() {
		System.out.println("\n\t售票.....\n");
	}
 
	@Override
	public void inquire() {
        System.out.println("\n\t问询。。。。\n");
	}
 
	@Override
	public void withdraw() {
        System.out.println("\n\t退票......\n");
	}
 
}
```

```java

/**
 * 车票代售点
 * @author louluan
 *
 */
public class StationProxy implements TicketService {
 
	private Station station;
 
	public StationProxy(Station station){
		this.station = station;
	}
	
	@Override
	public void sellTicket() {
 
		// 1.做真正业务前，提示信息
		this.showAlertInfo("××××您正在使用车票代售点进行购票，每张票将会收取5元手续费！××××");
		// 2.调用真实业务逻辑
		station.sellTicket();
		// 3.后处理
		this.takeHandlingFee();
		this.showAlertInfo("××××欢迎您的光临，再见！××××\n");
 
	}
 
	@Override
	public void inquire() {
		// 1做真正业务前，提示信息
		this.showAlertInfo("××××欢迎光临本代售点，问询服务不会收取任何费用，本问询信息仅供参考，具体信息以车站真实数据为准！××××");
		// 2.调用真实逻辑
		station.inquire();
		// 3。后处理
		this.showAlertInfo("××××欢迎您的光临，再见！××××\n");
	}
 
	@Override
	public void withdraw() {
		// 1。真正业务前处理
		this.showAlertInfo("××××欢迎光临本代售点，退票除了扣除票额的20%外，本代理处额外加收2元手续费！××××");
		// 2.调用真正业务逻辑
		station.withdraw();
		// 3.后处理
		this.takeHandlingFee();
 
	}
 
	/*
	 * 展示额外信息
	 */
	private void showAlertInfo(String info) {
		System.out.println(info);
	}
 
	/*
	 * 收取手续费
	 */
	private void takeHandlingFee() {
		System.out.println("收取手续费，打印发票。。。。。\n");
	}
 
}

```

如果我们用上述的ASM或者Javassist框架创建动态代理的时候，需要书写许多业务代码。所以....

#### InvocationHandler

将业务代码部分分割出来，

![1622531431(1)](images\1622531431(1).jpg)

有上图可以看出，代理类处理的逻辑很简单：在调用某个方法前及方法后做一些额外的业务。换一种思路就是：在触发（invoke）真实角色的方法之前或者之后做一些额外的业务。那么，为了构造出具有通用性和简单性的代理类，可以将所有的触发真实角色动作交给一个触发的管理器，让这个管理器统一地管理触发。这种管理器就是Invocation Handler。
通俗的理解，静态代理的方法中，代理Proxy中的方法，都是调用了realSubject中的对应的方法

而动态代理中，外界对Proxy校色中每个方法的调用，Proxy都会交给InvocationHandler来处理，而InvocationHandler则调用具体对象的角色进行处理。

![1622531849(1)](images\1622531849(1).jpg)

所以，**代理Proxy 和RealSubject 应该实现相同的功能**，也就是都具有某个类的public方法！！！！

在面向对象的编程之中，如果我们想要约定Proxy 和RealSubject可以实现相同的功能，有两种方式：

    a.一个比较直观的方式，就是定义一个功能接口，然后让Proxy 和RealSubject来实现这个接口。
    
    b.还有比较隐晦的方式，就是通过继承。因为如果Proxy 继承自RealSubject，这样Proxy则拥有了RealSubject的功能，Proxy还可以通过重写RealSubject中的方法，来实现多态。
这就对用了两种动态代理的方式jdk！！和cglib！！！

#### JDK动态代理

 比如现在想为RealSubject这个类创建一个动态代理对象，JDK主要会做以下工作：

    1.   获取 RealSubject上的所有接口列表；
    2.   确定要生成的代理类的类名，默认为：com.sun.proxy.$ProxyXXXX ；
    
    3.   根据需要实现的接口信息，在代码中动态创建 该Proxy类的字节码；
    
    4 .  将对应的字节码转换为对应的class 对象；
    
    5.   创建InvocationHandler 实例handler，用来处理Proxy所有方法调用；
    
    6.   Proxy 的class对象 以创建的handler对象为参数，实例化一个proxy对象
##### 	理论：

​		通过java.lang.reflect.Proxy的newProxyInstance（ClassLoader loader,Class<?>[] interfaces,InvocationHandler）方法创建代理。

​	对于参数InvocationHandler，我们需要实现invoke方法：
​	在调用代理对象中的每一个方法时，在代码内部，都是直接调用了InvocationHandler 的invoke方法，而invoke方法根据代理类传递给自己的method参数来区分是什么方法。

##### 	实例：



​	现在定义两个接口Vehicle和Rechargable，Vehicle表示交通工具类，有drive()方法；Rechargable接口表示可充电的（工具），有recharge() 方法；

  定义一个实现两个接口的类ElectricCar，类图如下：

​	![1622532660(1)](images\1622532660(1).jpg)

为ElectricCar创建动态代理类：

```java
public class Test {
 
	public static void main(String[] args) {
 
		ElectricCar car = new ElectricCar();
		// 1.获取对应的ClassLoader
		ClassLoader classLoader = car.getClass().getClassLoader();
 
		// 2.获取ElectricCar 所实现的所有接口
		Class[] interfaces = car.getClass().getInterfaces();
		// 3.设置一个来自代理传过来的方法调用请求处理器，处理所有的代理对象上的方法调用
		InvocationHandler handler = new InvocationHandlerImpl(car);
		/*
		  4.根据上面提供的信息，创建代理对象 在这个过程中， 
                         a.JDK会通过根据传入的参数信息动态地在内存中创建和.class 文件等同的字节码
		         b.然后根据相应的字节码转换成对应的class， 
                         c.然后调用newInstance()创建实例
		 */
		Object o = Proxy.newProxyInstance(classLoader, interfaces, handler);
		Vehicle vehicle = (Vehicle) o;
		vehicle.drive();
		Rechargable rechargeable = (Rechargable) o;
		rechargeable.recharge();
	}
}

```

```java
public class InvocationHandlerImpl implements InvocationHandler {
 
	private ElectricCar car;
	
	public InvocationHandlerImpl(ElectricCar car)
	{
		this.car=car;
	}
	
	@Override
	public Object invoke(Object paramObject, Method paramMethod,
			Object[] paramArrayOfObject) throws Throwable {
		System.out.println("You are going to invoke "+paramMethod.getName()+" ...");
		paramMethod.invoke(car, null);
		System.out.println(paramMethod.getName()+" invocation Has Been finished...");
		return null;
	}
 /**
 * 电能车类，实现Rechargable，Vehicle接口
 * @author louluan
 */
public class ElectricCar implements Rechargable, Vehicle {
 
	@Override
	public void drive() {
		System.out.println("Electric Car is Moving silently...");
	}
 
	@Override
	public void recharge() {
		System.out.println("Electric Car is Recharging...");
	}
 
}

/**
 * 可充电设备接口
 * @author louluan
 */
public interface Rechargable {
 
	public void recharge();
}

/**
 * 交通工具接口
 * @author louluan
 */
public interface Vehicle {
	public void drive();
}
```

结果：

![1622533627(1)](images\1622533627(1).jpg)

##### 保存动态代理的字节码：

```java
public class ProxyUtils {
 
	/*
	 * 将根据类信息 动态生成的二进制字节码保存到硬盘中，
	 * 默认的是clazz目录下
         * params :clazz 需要生成动态代理类的类
         * proxyName : 为动态生成的代理类的名称
         */
	public static void generateClassFile(Class clazz,String proxyName)
	{
		//根据类信息和提供的代理类名称，生成字节码
                byte[] classFile = ProxyGenerator.generateProxyClass(proxyName, clazz.getInterfaces()); 
		String paths = clazz.getResource(".").getPath();
		System.out.println(paths);
		FileOutputStream out = null;  
        
        try {
            //保留到硬盘中
            out = new FileOutputStream(paths+proxyName+".class");  
            out.write(classFile);  
            out.flush();  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            try {  
                out.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
	}
	
}

```



数据库现在我们想将生成的代理类起名为“ElectricCarProxy”，并保存在硬盘，应该使用以下语句：

```java
ProxyUtils.generateClassFile(car.getClass(), "ElectricCarProxy");
```

可以用但编译工具jd-gui.exe进行查看。

##### 总结：

仔细观察可以看出生成的动态代理类有以下特点:
1.继承自 java.lang.reflect.Proxy，实现了 Rechargable,Vehicle 这两个ElectricCar实现的接口；

2.类中的所有方法都是final 的；

3.所有的方法功能的实现都统一调用了InvocationHandler的invoke()方法。

![1622534527(1)](images\1622534527(1).jpg)

#### Cglib动态代理

​	JDK中提供的生成动态代理类的机制有个鲜明的特点是： 某个类必须有实现的接口，而生成的代理类也只能代理某个类接口定义的方法，比如：如果上面例子的ElectricCar实现了继承自两个接口的方法外，另外实现了方法bee() ,则在产生的动态代理类中不会有这个方法了！更极端的情况是：如果某个类没有实现接口，那么这个类就不能同JDK产生动态代理了！

​	幸好我们有cglib。“CGLIB（Code Generation Library），是一个强大的，高性能，高质量的Code生成类库，它可以在运行期扩展Java类与实现Java接口。”

cglib 创建某个类A的动态代理类的模式是：

1. 查找A上的所有非final 的public类型的方法定义；

2. 将这些方法的定义转换成字节码；

3. 将组成的字节码转换成相应的代理的class对象；

4. 实现 MethodInterceptor接口，用来处理 对代理类上所有方法的请求（这个接口和JDK动态代理InvocationHandler的功能和角色是一样的）

   ##### 实例：

   ```java
   
   /**
    * 程序猿类
    * @author louluan
    */
   public class Programmer {
    
   	public void code()
   	{
   		System.out.println("I'm a Programmer,Just Coding.....");
   	}
   }
   
   ```

   ```java
   
   /*
    * 实现了方法拦截器接口
    */
   public class Hacker implements MethodInterceptor {
   	@Override
   	public Object intercept(Object obj, Method method, Object[] args,
   			MethodProxy proxy) throws Throwable {
   		System.out.println("**** I am a hacker,Let's see what the poor programmer is doing Now...");
   		proxy.invokeSuper(obj, args);
   		System.out.println("****  Oh,what a poor programmer.....");
   		return null;
   	}
    
   
   ```

   ```java
   
   public class Test {
    
   	public static void main(String[] args) {
   		Programmer progammer = new Programmer();
   		
   		Hacker hacker = new Hacker();
   		//cglib 中加强器，用来创建动态代理
   		Enhancer enhancer = new Enhancer();  
                    //设置要创建动态代理的类
   		enhancer.setSuperclass(progammer.getClass());  
                  // 设置回调，这里相当于是对于代理类上所有方法的调用，都会调用CallBack，而Callback则需要实行intercept()方法进行拦截
                   enhancer.setCallback(hacker);
                   Programmer proxy =(Programmer)enhancer.create();
                   proxy.code();
           
   	}
   }
   
   ```

   结果：

   ![1622535940(1)](images\1622535940(1).jpg)

### 9.String.format

文本处理工具，为我们提供了强大的字符串转化功能。

#### api：

```java
// 使用当前本地区域对象（Locale.getDefault()）格式化字符串
String String.format(String fmt, Object... args);

// 自定义本地区域对象格式化字符串
String String.format(Locale locale, String fmt, Object... args);
```

#### 字符、字符串的格式化：

> 格式：%【index$】【标识】【最小宽度】

常用标识：

 -  -标识最小宽度内左对齐，右边用空格补上

可用转化符：

```
  s，字符串类型。
  c，字符类型，实参必须为char或int、short等可转换为char类型的数据类型，否则抛IllegalFormatConversionException异常。

  b，布尔类型，只要实参为非false的布尔类型，均格式化为字符串true，否则为字符串false。

  n，平台独立的换行符（与通过 System.getProperty("line.separator") 是一样的）
```

实例：

```java
String raw = "hello";
//用于设置格式化后的字符串最小长度，若使用 [最小宽度] 而无设置 [标识] ，那么当字符串长度小于最小宽度时，则以左边补空格的方式凑够最小宽度。
String str = String.format("%1$7s", raw);
// 简化,
//String str = String.format("%7s", raw);
//输出：将"hello"格式化为"  hello"

String raw = "hello";
//此时设置了标识-  标识最小宽度内左对齐，右边用空格补上
String str = String.format("%1$-7s", raw);
// 简化
//String str = String.format("%-7s", raw);
//输出：将"hello"格式化为"hello  "

//1$标识占位符对应的索引
String str = String.format("%1$s，%2$s", raw,"baby");
//输出：hello，baby
String str = String.format("%1$s，%1$s", raw,"baby");
//输出：hello，hello
```

#### 整数的格式化：

> 格式：%【index$】【标识】*【最小宽度】转换符 （布尔b,十进制整数d,十六进制整数x，）

常用标识：

```
-，在最小宽度内左对齐,不可以与0标识一起使用。
0，若内容长度不足最小宽度，则在左边用0来填充。
#，对8进制和16进制，8进制前添加一个0,16进制前添加0x。
+，结果总包含一个+或-号。
空格，正数前加空格，负数前加-号。
,，只用与十进制，每3位数字间用,分隔。
(，若结果为负数，则用括号括住，且不显示符号。
```

可用转化符：

```
b，布尔类型，只要实参为非false的布尔类型，均格式化为字符串true，否则为字符串false。
d，整数类型（十进制）。
x，整数类型（十六进制）。
o，整数类型（八进制）
n，平台独立的换行符, 也可通过System.getProperty("line.separator")获取
```

实例：

```java
int num = 1;
String str = String.format("%04d", num)
//结果：将1显示为0001
int num = -1000;
String str = String.format("%(,d", num)
//结果：将-1000显示为(1,000)
```

#### 浮点数格式化：

> 格式：%【index$】【标识】*【最小宽度】【.精度】

可用标识：

```
-，在最小宽度内左对齐,不可以与0标识一起使用。
0，若内容长度不足最小宽度，则在左边用0来填充。
#，对8进制和16进制，8进制前添加一个0,16进制前添加0x。
+，结果总包含一个+或-号。
空格，正数前加空格，负数前加-号。
,，只用与十进制，每3位数字间用,分隔。
(，若结果为负数，则用括号括住，且不显示符号。
```

可用转化符：

```
b，布尔类型，只要实参为非false的布尔类型，均格式化为字符串true，否则为字符串false。
n，平台独立的换行符, 也可通过System.getProperty("line.separator")获取。
f，浮点数型（十进制）。显示9位有效数字，且会进行四舍五入。如99.99。
a，浮点数型（十六进制）。
e，指数类型。如9.38e+5。
g，浮点数型（比%f，%a长度短些，显示6位有效数字，且会进行四舍五入）
```

实例：

```
double num = 123.4567899;
System.out.print(String.format("%f %n", num)); // 123.456790 
System.out.print(String.format("%a %n", num)); // 0x1.edd3c0bb46929p6 
System.out.print(String.format("%g %n", num)); // 123.457
```

#### 日期格式化：

>格式：%【index$】t转化符

可用转换符：

```
//日期
c，星期六 十月 27 14:21:20 CST 2007
F，2007-10-27
D，10/27/07
r，02:25:51 下午
T，14:28:16
R，14:28
b, 月份简称
B, 月份全称
a, 星期简称
A, 星期全称
C, 年前两位（不足两位补零）
y, 年后两位（不足两位补零）
j, 当年的第几天
m, 月份（不足两位补零）
d, 日期（不足两位补零）
e, 日期（不足两位不补零）

//时间
H, 24小时制的小时（不足两位补零）
k, 24小时制的小时（不足两位不补零）
I, 12小时制的小时（不足两位补零）
i, 12小时制的小时（不足两位不补零）
M, 分钟（不足两位补零）
S, 秒（不足两位补零）
L, 毫秒（不足三位补零）
N, 毫秒（不足9位补零）
p, 小写字母的上午或下午标记，如中文为“下午”，英文为pm
z, 相对于GMT的时区偏移量，如+0800
Z, 时区缩写，如CST
s, 自1970-1-1 00:00:00起经过的秒数
Q, 自1970-1-1 00:00:00起经过的豪秒
```

#### 特殊转换符：

<，用于格式化前一个转换符所描述的参数。

```
int num = 1000;
String str = String.format("%d %<,d", num);
// 结果"1000 1,000
```

### 11.常用工具类

#### 	11.1.JsonUtils

```java
public class JsonUtils {

    private static Logger log = LoggerFactory.getLogger(JsonUtils.class);

    private static final Gson GSON = new Gson();

    public static String toJsonGson(Object object) {
        try {
            return GSON.toJson(object);
        } catch (Exception e) {
            log.error("toJsonGson error", e);
        }
        return null;
    }

    public static <T> T fromJsonGson(String json, Class<T> clazz) {
        try {
            return GSON.fromJson(json, clazz);
        } catch (Exception e) {
            log.error("fromJsonGson error", e);
        }
        return null;
    }

    public static <T> List<T> fromJsonListGson(String json, Class<T> clazz) {
        return GSON.fromJson(json, TypeToken.getParameterized(List.class, clazz).getType());
    }

}
```

#### 11.2.BeanUtils

```java

import com.ctrip.ibu.tuma.exception.BeanUtilsException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 类转换
 */
public class BeanUtils {
    /**
     * <pre>
     *     List<UserBean> userBeans = userDao.queryUsers();
     *     List<UserDTO> userDTOs = BeanUtil.batchTransform(UserDTO.class, userBeans);
     * </pre>
     */
    public static <T> List<T> batchTransform(final Class<T> clazz, List<?> srcList) {
        if (CollectionUtils.isEmpty(srcList)) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(srcList.size());
        for (Object srcObject : srcList) {
            result.add(transform(clazz, srcObject));
        }
        return result;
    }

    /**
     * 封装{@link org.springframework.beans.BeanUtils  copyProperties}，惯用与直接将转换结果返回
     *
     * <pre>
     *      UserBean userBean = new UserBean("username");
     *      return BeanUtil.transform(UserDTO.class, userBean);
     * </pre>
     */
    public static <T> T transform(Class<T> clazz, Object src) {
        if (src == null) {
            return null;
        }
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new BeanUtilsException(e);
        }
        org.springframework.beans.BeanUtils.copyProperties(src, instance, getNullPropertyNames(src));
        return instance;
    }

    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }


    /**
     * 用于将一个列表转换为列表中的对象的某个属性映射到列表中的对象
     *
     * <pre>
     *      List<UserDTO> userList = userService.queryUsers();
     *      Map<Integer, userDTO> userIdToUser = BeanUtil.mapByKey("userId", userList);
     * </pre>
     *
     * @param key 属性名
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapByKey(String key, List<?> list) {
        Map<K, V> map = new HashMap<>();
        if (CollectionUtils.isEmpty(list)) {
            return map;
        }
        try {
            Class<?> clazz = list.get(0).getClass();
            Field field = deepFindField(clazz, key);
            if (field == null) throw new IllegalArgumentException("Could not find the key");
            field.setAccessible(true);
            for (Object o : list) {
                map.put((K) field.get(o), (V) o);
            }
        } catch (Exception e) {
            throw new BeanUtilsException(e);
        }
        return map;
    }

    /**
     * 根据列表里面的属性聚合
     *
     * <pre>
     *       List<ShopDTO> shopList = shopService.queryShops();
     *       Map<Integer, List<ShopDTO>> city2Shops = BeanUtil.aggByKeyToList("cityId", shopList);
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, List<V>> aggByKeyToList(String key, List<?> list) {
        Map<K, List<V>> map = new HashMap<>();
        if (CollectionUtils.isEmpty(list)) {// 防止外面传入空list
            return map;
        }
        try {
            Class<?> clazz = list.get(0).getClass();
            Field field = deepFindField(clazz, key);
            if (field == null) {
                throw new IllegalArgumentException("Could not find the key");
            }
            field.setAccessible(true);
            for (Object o : list) {
                K k = (K) field.get(o);
                map.computeIfAbsent(k, k1 -> new ArrayList<>());
                map.get(k).add((V) o);
            }
        } catch (Exception e) {
            throw new BeanUtilsException(e);
        }
        return map;
    }

    /**
     * 用于将一个对象的列表转换为列表中对象的属性集合
     *
     * <pre>
     *     List<UserDTO> userList = userService.queryUsers();
     *     Set<Integer> userIds = BeanUtil.toPropertySet("userId", userList);
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <K> Set<K> toPropertySet(String key, List<?> list) {
        Set<K> set = new HashSet<>();
        if (CollectionUtils.isEmpty(list)) {// 防止外面传入空list
            return set;
        }
        try {
            Class<?> clazz = list.get(0).getClass();
            Field field = deepFindField(clazz, key);
            if (field == null) {
                throw new IllegalArgumentException("Could not find the key");
            }
            field.setAccessible(true);
            for (Object o : list) {
                set.add((K) field.get(o));
            }
        } catch (Exception e) {
            throw new BeanUtilsException(e);
        }
        return set;
    }


    private static Field deepFindField(Class<?> clazz, String key) {
        Field field = null;
        while (!clazz.getName().equals(Object.class.getName())) {
            try {
                field = clazz.getDeclaredField(key);
                if (field != null) {
                    break;
                }
            } catch (Exception e) {
                clazz = clazz.getSuperclass();
            }
        }
        return field;
    }


    /**
     * 获取某个对象的某个属性
     */
    public static Object getProperty(Object obj, String fieldName) {
        try {
            Field field = deepFindField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(obj);
            }
        } catch (Exception e) {
            throw new BeanUtilsException(e);
        }
        return null;
    }

    /**
     * 设置某个对象的某个属性
     */
    public static void setProperty(Object obj, String fieldName, Object value) {
        try {
            Field field = deepFindField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(obj, value);
            }
        } catch (Exception e) {
            throw new BeanUtilsException(e);
        }
    }

    /**
     * @param source
     * @param target
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        org.springframework.beans.BeanUtils.copyProperties(source, target, ignoreProperties);
    }
}

```

#### 11.3DateUtils

```java
//日期的获取和各个格式的转换

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public class DateUtils {
    private static final ZoneId ZONE_ID = ZoneOffset.of("+8");

    public static LocalDateTime nowAtTime() {
        return LocalDateTime.now(ZONE_ID);
    }

    public static LocalDate nowAtDate() {
        return LocalDate.now(ZONE_ID);
    }

    public static Timestamp nowAtTimestamp() {
        return toTimestamp(nowAtTime());
    }

    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZONE_ID).toInstant());
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
    }

    public static Timestamp toTimestamp(LocalDate localDate) {
        return Timestamp.valueOf(localDate.atStartOfDay());
    }

    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZONE_ID).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZONE_ID).toLocalDateTime();
    }

    public static boolean isWeekend(LocalDate startDate) {
        DayOfWeek dayOfWeek = startDate.getDayOfWeek();
        return DayOfWeek.SATURDAY.equals(dayOfWeek) || DayOfWeek.SUNDAY.equals(dayOfWeek);
    }

    public static boolean isWorkday(LocalDate startDate) {
        return !isWeekend(startDate);
    }

    public static int getWorkdaysBetween(LocalDate startDate, LocalDate endDate) {
        int workdays = 0;
        for (LocalDate tempDate = startDate; tempDate.isBefore(endDate); tempDate = tempDate.plusDays(1)) {
            if (isWorkday(tempDate)) {
                workdays++;
            }
        }
        return workdays;
    }

}
```

#### 11.4 okhttp3工具类

```java
public interface OsgClient {

    List<CostCenterDto> getCostCenter();

}

```



```java
public class AbstractOsgClient {
    protected Optional<JSONObject> postForJson(String url, String accessToken, JSONObject requestBody) {

        JSONObject postJson = new JSONObject();
        postJson.put("access_token", accessToken);
        postJson.put("request_body", requestBody);

        String res = null;
        try {
            res = HttpHelper.post(url, JSON.toJSONString(postJson));
        } catch (IOException e) {
            log.error("[[type=OsgAppServiceImpl]] can not connect to url:{} ", url, e);
            return Optional.empty();
        }
        JSONObject responseJson = JSON.parseObject(res);
        return Optional.of(responseJson);
    }
}
```

```java
public class HttpHelper {
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient();

    private HttpHelper(){

    }

    public static  String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if(responseBody == null){
                return null;
            }
            return responseBody.string();
        }
    }
}


```

```java
public class OsgClientImpl extends AbstractOsgClient implements OsgClient{
    TumaQconfig tumaQconfig;
    public OsgClientImpl(TumaQconfig tumaQconfig){
        this.tumaQconfig=tumaQconfig;
    }
    @Override
    public List<CostCenterDto> getCostCenter() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("start", 0);
        requestBody.put("length", 10);
        //参数：请求路径，请求头，请求体
        Optional<JSONObject> resOptional = postForJson(OsgApiConstant.API_GET_COST_CENTER, tumaQconfig.getAccessTokenGetAllCostCenter(), requestBody);
        if (!resOptional.isPresent()) {
            log.error("[[type=OsgClientImpl]] can not get costcenter info");
            return Collections.emptyList();
        }

        JSONObject res = resOptional.get();
        JSONArray responseData = res.getJSONArray("data");
        if (ObjectUtils.isEmpty(responseData)) {
            String errorMessage = res.getString("message");
            log.error("[[type=OsgClientImpl]] can not get costcenter info, osg response message: {}", errorMessage);
            return Collections.emptyList();
        }
        return JSON.parseArray(responseData.toJSONString(), CostCenterDto.class);

    }


}

```

#### 11.5 分页查询再做某事

```java
 batchExecuteAndDo(bagTaskId,(data -> {
                    List<BagTaskInfo> bagTaskInfos= (List<BagTaskInfo>) data;
                    try {
                        dealMachineTranslation(bagTaskInfos, finalBagMtConfigs,bagTaskId,requirement,costcenterName,costcenterCode,bagTask);
                    } catch (Exception e) {
                        log.error("[[type=BagMachineTranslationService]]Error dealMachineTranslation");
                    }
                }));
```

```java
protected void batchExecuteAndDo(Long bagTaskId,BatchExecutor executor) throws SQLException {
        int pageNo = 1;
        int pageSize = 1000;
        BootStrapTableData<BagTaskInfo> bootStrapTableData;
        do {
            bootStrapTableData = bagTaskInfoDao.queryByBagTaskIdPageable(bagTaskId,pageNo, pageSize);
            if (ObjectUtils.isEmpty(bootStrapTableData.getRows())) {
                break;
            }
            executor.execute(bootStrapTableData.getRows());
            ++pageNo;
        } while (bootStrapTableData.getRows().size() == pageSize);
    }
protected  void dealMachineTranslation(){
    
}
```

```java
public interface BatchExecutor {

   void execute(Object data) throws SQLException;

}
```

#### 11.6文件上传下载

##### 异步形式：

![上传下载1](D:\Users\yjin5\ibu-doc\knowsCollection\images\上传下载1.jpg)

点击下载：出现

![上传下载2](D:\Users\yjin5\ibu-doc\knowsCollection\images\上传下载2.jpg)

右边的提示，当变为绿色对号时，生成报表成功，点击文件名可下载相应的excel表格。

##### 表设计：

![上传下载3](D:\Users\yjin5\ibu-doc\knowsCollection\images\上传下载3.jpg)

##### 步骤：

1.先创建notify_record，状态为正在生成报表

2.qmq发送消息异步上传到s3，然后生成file_stroe数据，更新notify_record

3.用户点击文件名下载：根据id找notify_record，找到的对应的fileStorage，下载。

##### 代码：

```java
//生产者
String fileName = String.format("MT_cost_report_%1$s-%2$s.xlsx", DateFormatUtils.format(bagMtReportRequestDto.getBeginTime(), DATE_FORMAT_FILE_EXPORT),DateFormatUtils.format(bagMtReportRequestDto.getEndTime(), DATE_FORMAT_FILE_EXPORT));
        NotifyRecordEntity notifyRecordEntity = new NotifyRecordEntity();
        notifyRecordEntity.setTitle(NotifyType.ReportExporting.getDesc());
        notifyRecordEntity.setContent(fileName);
        notifyRecordEntity.setType(NotifyType.ReportExporting.getIndex());
        notifyRecordEntity.setSender(NotifyConstant.DEFAULT_MAIL_SENDER);
        notifyRecordEntity.setReceiverId(user.getId());
        notifyRecordEntity.setReceiverType(NotifyReceiverType.User.getIndex());
        notifyRecordEntity.setStatus(NotifyStatus.INITIATED.getIndex());
        notifyRecordEntity.setCreateTime(new Date());
        notifyRecordEntity.setRefTable(NotifyRefTable.ReportExport.getIndex());
        notifyRecordEntity.setCreateTime(new Date());
        notifyRecordEntity.setRefTableId(-1L);
        Long notifyRecordId = notifyRecordDao.insertWithKeyHolder(notifyRecordEntity);
        BagMtExportEntity bagMtExportEntity = new BagMtExportEntity();
        bagMtExportEntity.setFilename(fileName);
        bagMtExportEntity.setCurrentUserId(user.getId());
        bagMtExportEntity.setNotifyId(notifyRecordId);
        bagMtExportEntity.setBagMtReportRequestDto(bagMtReportRequestDto);

        bagDataPushQmQProducer.sendBagMtReportExport(bagMtExportEntity);
```

```java
//消费者
public void export(BagMtExportEntity bagMtReportEntity) throws SQLException, IOException {

        byte[] fileByteArrayToUpload = getFileByteArrayToUpload(bagMtReportEntity.getFilename(), bagMtReportEntity.getBagMtReportRequestDto());
        String s3FilePath = tumaFileHelper.uploadFile(fileByteArrayToUpload);
        FileStorageEntity fileStorageEntity = new FileStorageEntity();
        fileStorageEntity.setFileStorageType(FileStorageType.BAG.getIndex());
        fileStorageEntity.setFileStorageRefId(bagMtReportEntity.getNotifyId());
        fileStorageEntity.setCreator(bagMtReportEntity.getCurrentUserId());
        fileStorageEntity.setFileId(bagMtReportEntity.getFilename());
        fileStorageEntity.setFileUrl(s3FilePath);
        fileStorageEntity.setFileHash(ShaMd5.encryptMD5(fileByteArrayToUpload));
        Long fileStorageId = fileStorageDao.insert(fileStorageEntity);

        NotifyRecordEntity notifyRecord = notifyRecordDao.queryById(bagMtReportEntity.getNotifyId());
        notifyRecord.setExtraData(JsonUtils.toJsonGson(fileStorageEntity));
        notifyRecord.setTitle(NotifyType.processed.getDesc());
        notifyRecord.setType(NotifyType.processed.getIndex());
        notifyRecord.setRefTableId(fileStorageId);
        notifyRecord.setStatus(NotifyStatus.TO_NOTIFY.getIndex());
        notifyRecordDao.updateById(notifyRecord);
    }

    private byte[] getFileByteArrayToUpload(String fileName, BagMtReportRequestDto bagMtReportRequestDto) throws IOException, SQLException {
        List<BagMtReportDto> bagMtReportDtos = bagMachineTransaltionService.queryBagMachineTranslationReport(bagMtReportRequestDto.getBeginTime(), bagMtReportRequestDto.getEndTime(), bagMtReportRequestDto.getBagTaskId());

        Class typeClass=BagMtReportDto.class;
        List<Object> dataList = bagMtReportDtos.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return ExcelHelper.writeBytes(fileName, dataList, typeClass);
    }
```

```java
//下载
public ResponseEntity<byte[]> downloadFile(Long id) throws SQLException, IOException {
        NotifyRecord record = notifyRecordDao.queryByPk(id);
        if (record == null) {
            throw new IllegalStateException("download failed.");
        }
        String extraData = record.getExtraData();
        FileStorage fileStorage = SerializeHelper.deserializeJson(extraData, FileStorage.class);
        byte[] fileBytes = IOUtils.toByteArray(fileWSService.getFileStream(fileStorage.getFileUrl()));
        HttpHeaders headers = Servlets.getFileDownloadHeader(fileStorage.getFileId());
        headers.set("Set-Cookie", "fileDownload=true;Path=/");
        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);

    }
```

#### 11.7 发送邮件

```html
//定义邮件模板
<html lang="en" xml:lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:Web="http://schemas.live.com/Web/">

<head>
    <meta content="text/html; charset=utf-8" http-equiv="content-type" />
</head>
<body>
<strong>Content:</strong><br />

    #if( $wfStatus &&  $wfStatus == "rejected")
    <p>You translation project<a href="$projectUrl"> [$projectName]</a> has been rejected. Please check the comment below:</p>
    <p>
        Reject comment:
        If you need to send the project again, please do so after modifying it as requested the soonest, or there will be a risk of delay. Thank you.
    </p>>
    #end
    #if( $wfStatus &&  $wfStatus == "sending")
    <p>Please check <a href="$projectUrl"> [$projectName]</a> and send again.</p>

    #end
    #if( $wfStatus &&  $wfStatus == "deadline")
    <p><a href="$projectUrl">[$projectName]</a> will be completed by $projectCompletedTime</p>
    #end
    #if( $wfStatus &&  $wfStatus == "completed")
    <p>See details here: <a href="$projectUrl">[$projectName]</a></p>
    #end

</body>
```

```java
//定义主题和模板位置
public interface EmailConstant {

    String EMAIL_SUBJECT_COMMENT_NOTIFY = "New comments for [%1$d][%2$s] - Tuma";

    String TEMPLATE_PATH_COMMENT_NOTIFY = "mail/comment_notify.vm";

    String EMAIL_SUBJECT_PROJECT_STATUS_CHANGED= "Your translation project [%1$s] has been %2$s";

    String  TEMPLATE_PATH_PROJECT_STATUS_CHANGED= "mail/project_status_changed_notify.vm";

}
```

发送邮件业务代码：

```java
private void sendEmailWhenProjectStatusChanged(ProjectEntity project,String estCompletionTimeStr,String subjectContent,String wfStatus){
        String subject=String.format(EmailConstant.EMAIL_SUBJECT_PROJECT_STATUS_CHANGED,project.getName(),subjectContent);
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("projectName",project.getName());
        String projectUrl="http://shark.ibu.ctripcorp.com/project/"+project.getProjectId()+"/key";
        contentMap.put("projectUrl",projectUrl);
        contentMap.put("wfStatus",wfStatus);
        if(!StringUtils.isEmpty(estCompletionTimeStr)){
            contentMap.put("projectCompletedTime",estCompletionTimeStr);
        }
        String userName = securityUserIf.queryUserById(project.getCreator()).getUserName();
        String[] userNames={userName};
        YemailMailEntity entity = yemailService.convertToCtranUserYemailEntity(contentMap,EmailConstant.TEMPLATE_PATH_PROJECT_STATUS_CHANGED,userNames,subject);
        CommonResponse send = yemailService.send(entity);
        if(!send.isSuccess()){
            log.error("[[type=ProjectService]] can not send email, maybe the user not existed or not enabled, mail:{}", JsonUtils.toJsonGson(entity));
        }
   }

```

使用到的工具类：

```java
public class YemailServiceImpl implements IYemailService {


    @Override
    public CommonResponse send(YemailMailEntity entity) {
        try {
            YemailClient.send(entity);
            return CommonResponse.success();
        } catch (YemailException e) {
            log.error("[[type=YemailServiceImpl]] can not send email, maybe the user not existed or not enabled, mail:{}", JsonUtils.toJsonGson(entity), e);
            return CommonResponse.error("can not send email, maybe the user not existed or not enabled");
        }
    }

    @Override
    public YemailMailEntity convertToYemailEntity(Map<String, Object> templateVariableMap, String templatePath, String[] groups, String subject) {
        String mailBody = VelocityEngineUtils.mergeTemplate(templatePath, templateVariableMap);
        YemailMailEntity entity = new YemailMailEntity();
        entity.setBodyContent(mailBody);
        entity.setRecipientGroups(groups);
        entity.setSubject(subject);
        return entity;
    }

    @Override
    public YemailMailEntity convertToCtranUserYemailEntity(Map<String, Object> templateVariableMap, String templatePath, String[] users, String subject) {
        YemailMailEntity entity = convertToYemailEntity(templateVariableMap, templatePath, users, subject);
        entity.setMailGroupType(YemailGroupType.TUMA_USER);
        return entity;
    }

    @Override
    public YemailMailEntity convertToCtranGroupYemailEntity(Map<String, Object> templateVariableMap, String templatePath, String[] users, String subject) {
        YemailMailEntity entity = convertToYemailEntity(templateVariableMap, templatePath, users, subject);
        entity.setMailGroupType(YemailGroupType.CTRAN_GROUP);
        return entity;
    }

}
```

```java
public class VelocityEngineUtils {

    private static final String DEFAULT_ENCODING = "utf-8";

    public static void mergeTemplate(
            VelocityEngine velocityEngine, String templateLocation, String encoding,
            Map<String, Object> model, Writer writer) throws VelocityException {

        VelocityContext velocityContext = new VelocityContext(model);
        velocityEngine.mergeTemplate(templateLocation, encoding, velocityContext, writer);
    }

    public static String mergeTemplateIntoString(VelocityEngine velocityEngine, String templateLocation,
                                                 String encoding, Map<String, Object> model) throws VelocityException {

        StringWriter result = new StringWriter();
        mergeTemplate(velocityEngine, templateLocation, encoding, model, result);
        return result.toString();
    }

    public static String mergeTemplate(final String tempLocation, final Map<String, Object> model) {
        VelocityEngine ve = getInstance();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        return VelocityEngineUtils.mergeTemplateIntoString(ve, tempLocation, DEFAULT_ENCODING, model);

    }

    public static String templateToString(final String tempLocation) {
        Velocity.init();
        Template t = Velocity.getTemplate(tempLocation);

        VelocityContext ctx = new VelocityContext();

        Writer writer = new StringWriter();
        t.merge(ctx, writer);
        return writer.toString();

    }

    public static VelocityEngine getInstance() {
        return VelocityEngineHodler.instance;
    }

    private static class VelocityEngineHodler {

        private static VelocityEngine instance = new VelocityEngine();
    }

}

```



### 12.String

#### 	12.1 String的长度限制？

​	String类的长度的返回类型是int

![1623984643(1)](images\1623984643(1).jpg)

整数在java中是有限制的，我们通过源码来看看int类型对应的包装类Integer可以看到，其长度最大限制为2^31 -1，那么说明了数组的长度是0~2^31-1，那么计算一下就是（2^31-1 = 2147483647 = 4GB）也就是说理论上string的长度可以达到2亿多。

![1623984821(1)](images\1623984821(1).jpg)

但是实际操作的时候，以字面量的形式构造字符串，最长也就是0-65564。

这是因为我们将字符串定义成了字面量的形式，编译时JVM是会将其存放在常量池中，这时候JVM对这个常量池存储String类型做出了限制。通过翻阅java虚拟机手册对class文件格式的定义以及常量池中对String类型的结构体定义我们可以知道对于索引定义了u2，就是无符号占2个字节，2个字节可以表示的最大范围是2^16 -1 = 65535。

其实是65535，但是由于JVM需要1个字节表示结束指令，所以这个范围就为65534了。

#### 12.2 StringBuffer StringBuilder

String 不可变，private final char value[]用字符数组保存字符串，加了final，所以不可变

StringBuffer 和StringBuilder继承AbstractStringBuilder，定义了一些字符串的操作，append，insert，indexOf等。

StringBuffer由于在操作方法上加了同步锁synchronized，所以线程安全。

### 13.概念

##### 多态：

引用变量的具体类型和其的方法调用，在编译是不确定，只有在运行时才知道，会指向哪个类的实例对象。

java实现多态的两种形式:继承(多个子类对同一方法的重写)和接口(实现接口并覆盖接口中同一方法)

```java

public class demo04 {
    public static void main(String[] args) {
        People p=new Stu();
        p.eat();
        //调用特有的方法
        Stu s=(Stu)p;
        s.study();
        //((Stu) p).study();
    }
}
class People{
    public void eat(){
        System.out.println("吃饭");
    }
}
class Stu extends People{
    @Override
    public void eat(){
        System.out.println("吃水煮肉片");
    }
    public void study(){
        System.out.println("好好学习");
    }
}
class Teachers extends People{
    @Override
    public void eat(){
        System.out.println("吃樱桃");
    }
    public void teach(){
        System.out.println("认真授课");
    }
```

##### 自动装箱，拆箱

装箱：将基本数据类型用对用的引用类型包装起来

拆箱:将包装类型转化为基本数据类型

##### 无参构造方法的作用

子类执行构造方法前，如果子类没用super()调用父类的构造方法时，会调用父类的无参构造方法。而此时父类如果没有无参构造方法，会发生编译错误。

##### 接口和抽象类：

方法而言：接口方法默认public，且java8之前没默认实现（java8有默认实现和静态方法）。抽象类的方法可以有实现

变量而言：接口中的变量都是static,final类型，类不一定

继承：接口可以多继承，类不可以

设计：抽象类是类的抽象，接口是对行为的抽象

### 14==，equals，hascode

### 15.字符流和字节流？为什么有字节流还需要字符流？

字节流主要是处理二进制数据比较好，处理单元是字节，比如图片、音频。

但是对于文本的处理，字符流比较好，处理单元是Unicode码，处理后放入内存(缓冲区)，当字符流关闭后，会刷进磁盘，存储在磁盘上的数据有各种编码方式，所以相同的字符会有不同的二进制标识。(字符流可以指定编码，防止乱码)

实际上字符流是这样工作的：

- 输出字符流：把要写入文件的字符序列（实际上是**Unicode码元序列**）转为指定编码方式下的**字节序列**，然后再写入到文件中；
- 输入字符流：把要读取的**字节序列**按指定编码方式解码为相应字符序列（实际上是**Unicode码元序列**）从而可以存在内存中。

![1626872727(1)](images\1626872727(1).jpg)

```java
public static void main(String[] args) throws Exception {   // 异常抛出，  不处理    
// 第1步：使用File类找到一个文件    
     File f = new File("d:" + File.separator + "test.txt"); // 声明File  对象    
// 第2步：通过子类实例化父类对象    
     OutputStream out = null;            
// 准备好一个输出的对象    
     out = new FileOutputStream(f);      
// 通过对象多态性进行实例化    
// 第3步：进行写操作    
     String str = "Hello World!!!";      
// 准备一个字符串    
     byte b[] = str.getBytes();          
// 字符串转byte数组    
     out.write(b);                      
// 将内容输出    
 // 第4步：关闭输出流    
    // out.close();                  
// 此时没有关闭    
        }    
    } 
```

此时没有关闭字节流操作，但是文件中也依然存在了输出的内容，证明字节流是直接操作文件本身的。而下面继续使用字符流完成，再观察效果。

```java
public class WriterDemo03 {    
    public static void main(String[] args) throws Exception { // 异常抛出，  不处理    
        // 第1步：使用File类找到一个文件    
        File f = new File("d:" + File.separator + "test.txt");// 声明File 对象    
        // 第2步：通过子类实例化父类对象    
        Writer out = null;                 
// 准备好一个输出的对象    
        out = new FileWriter(f);            
// 通过对象多态性进行实例化    
        // 第3步：进行写操作    
        String str = "Hello World!!!";      
// 准备一个字符串    
        out.write(str);                    
// 将内容输出    
        // 第4步：关闭输出流    
        // out.close();                   
// 此时没有关闭    
    }    
}
```

程序运行后会发现文件中没有任何内容，这是因为字符流操作时使用了缓冲区，而 在关闭字符流时会强制性地将缓冲区中的内容进行输出，但是如果程序没有关闭，则缓冲区中的内容是无法输出的，所以得出结论：字符流使用了缓冲区，而字节流没有使用缓冲区。

### 16.[红黑树](https://www.cnblogs.com/skywang12345/p/3245399.html)

##### 定义：

红黑树是一种二叉查找树，只不过其实自平衡的，在插入或者删除的时候其能够重新自处理达到平衡状态。且其节点有两种颜色，红色和黑色。

##### 性质：

>  黑色节点：根节点，叶子结点(指的是为null的节点)，红色节点的两个子节点，必是黑色的。
>
> 且任一节点到每个叶子结点具有相同的数量的黑色节点。

![1626945340(1)](images\1626945340(1).jpg)

红黑树的时间复杂度为: O(lgn)

### 17.volatile和Synchronized

#### 并发编程的三个重要特性：

> 1. **原子性** : 一个的操作或者多次操作，要么所有的操作全部都得到执行并且不会收到任何因素的干扰而中断，要么所有的操作都执行，要么都不执行。`synchronized` 可以保证代码片段的原子性。
> 2. **可见性** ：当一个变量对共享变量进行了修改，那么另外的线程都是立即可以看到修改后的最新值。`volatile` 关键字可以保证共享变量的可见性。
> 3. **有序性** ：代码在执行的过程中的先后顺序，Java 在编译器以及运行期间的优化，代码的执行顺序未必就是编写代码时候的顺序。`volatile` 关键字可以禁止指令进行重排序优化。

#### volatile的两个重要作用：

> 保证变量在多线程间的可见性；防止指令重排

#### 可见性：

​	为什么需要volatile保证可见性？

> 我们需要简单了解一下我们CPU的内存模型，我们知道在计算机执行程序的时候，指令都是在CPU中执行的，但是我们的数据是存储在计算机的主内存中（物理内存）的。这就是导致一个不可避免的问题，就是内存的读取和写入的速度与CPU的执行指令速度相比差距是很大的，这样就会造成了与内存交互时程序执行效率大大降低，因此在CPU中就设计了**高速缓存**。

![1627474849(1)](images\1627474849(1).jpg)

这个高速缓存解决了效率的问题，带来了准确度的问题？即缓存一致性问题。

> 就是我们CPU在执行的时候，会把主内存中的数据复制一份到我们的CPU高速缓存中去，如果我们有多个线程去执行一个程序，那么我们每个线程都复制了一份到高速缓存中，如我们现在有二个线程去执行一个程序，目的是每个线程把 num 的值加 1 ，num 的默认值是 0，结果应该是 num=2，但是可能有这种情况，我们的线程1 把 num=0 存入高速缓存中，进行加 1，现在线程1高速缓存中的 num=1，但是线程还没写入主内存中，主内存中还是num=0，线程2又把 num=0 存入高速缓存中，进行加1，现在线程2高速缓存中的也 num=1，然后线程1高速缓存写入主内存，主存中 num=1，然后线程2高速缓存写入主内存，主内存还是 num=1。

所以需要volatile来保证一致性。工作原理：

> 假设我们线程1运行将其加 1，num=1了，这时因为加了volatile关键字，我们线程1的高速缓存会立即写入主内存中（注意： 我们该线程不仅会将volatile关键字修饰的变量立即写入主内存，还会把当前线程（方法）中所有可见的变量立即写入主内存中），并且通过我们其他的线程 num 的值被我改啦，你们之前拿到的值都不可能用啦，去主内存中重新取下值吧。
>
> 然后到我们线程2运行，因为收到了通知，其高速缓存的 num=0 已失效，不能用啦，就又去主存中又去了一次，num=1，然后对其加1，num=2，并将其写入主存中，也会通知其他线程 num 的值又被改啦。

#### 顺序性：

我们 volatile关键字还有一个作用，就是禁止指令重排序，也就是我们上面说的保证其有序性，有序性：即程序执行的顺序按照代码的先后顺序执行。

> 指令重排：在Java内存模型中，允许编译器和处理器对指令进行重排序，但是重排序过程不会影响到单线程程序的执行，在单线程下有着一个 as-if-serial 的概念 。

单线程模式下指令重排不会有问题，因为其遵守 不管怎么重排，结果不能改变  原则(**as-if-seria**)。

但是多线程下指令重排有可能出问题：

```java
//线程1
while (a < b){
}
return b;

//线程2
a = 8;
b = 1;
```

在线程2正常不进行指令重排序的情况下，线程1跳出while循环时的值，可能是 a=8 , b=5 ，那么返回值可能是b=5

但是如果线程2发生了指令重排序的情况，这里线程1跳出while循环时的值，可能是 a=3 , b=1，那么返回值可能是b=1

#### Synchronized的两个作用:

##### 保证操作的原子性和数据的可见性。

>  可见性：synchronized关键字可以保证被它修饰的方法或者代码块在任意时刻只能有一个线程执行。
>
>   原子性：synchronized修饰的代码或者方法执行具有原子性。

##### 为什么说synchronized属于重量级锁？

> 因为监视器锁（monitor）是依赖于底层的操作系统的 `Mutex Lock` 来实现的，Java 的线程是映射到操作系统的原生线程之上的。如果要挂起或者唤醒一个线程，都需要操作系统帮忙完成，而操作系统实现线程之间的切换时需要从用户态转换到内核态，这个状态之间的转换需要相对比较长的时间，时间成本相对较高。但是java6之后已经进行了优化。

##### synchronized的使用方式：

> 1.修饰实例方法：在实例对象上加锁，进入代码块要获得当前实例的锁
>
> synchronized void method() {    //业务代码 }
>
> 2.**修饰静态方法:** 也就是给当前类加锁，会作用于类的所有对象实例 ，进入同步代码前要获得 **当前 class 的锁**。因为静态成员不属于任何一个实例对象，是类成员（ _static 表明这是该类的一个静态资源，不管 new 了多少个对象，只有一份_）。所以，如果一个线程 A 调用一个实例对象的非静态 `synchronized` 方法，而线程 B 需要调用这个实例对象所属类的静态 `synchronized` 方法，是允许的，不会发生互斥现象，**因为访问静态 `synchronized` 方法占用的锁是当前类的锁，而访问非静态 `synchronized` 方法占用的锁是当前实例对象锁**
>
> synchronized static void method() {    //业务代码 }
>
> 3.**修饰代码块** ：指定加锁对象，对给定对象/类加锁。`synchronized(this|object)` 表示进入同步代码库前要获得**给定对象的锁**。`synchronized(类.class)` 表示进入同步代码前要获得 **当前 class 的锁**
>
> synchronized(this) {    //业务代码 }

##### synchronized关键字的底层原理

1.同步语句块

```
public class SynchronizedDemo {
    public void method() {
        synchronized (this) {
            System.out.println("synchronized 代码块");
        }
    }
}
```

​		通过 JDK 自带的 `javap` 命令查看 `SynchronizedDemo` 类的相关字节码信息：首先切换到类的对应目录执行 `javac 	SynchronizedDemo.java` 命令生成编译后的 .class 文件，然后执行`javap -c -s -v -l SynchronizedDemo.class`。

![1627480370(1)](images\1627480370(1).jpg)

​	从上面我们可以看出：

**`synchronized` 同步语句块的实现使用的是 `monitorenter` 和 `monitorexit` 指令，其中 `monitorenter` 指令指向同步代码块的开始位置，`monitorexit` 指令则指明同步代码块的结束位置。**当执行 `monitorenter` 指令时，线程试图获取锁也就是获取 **对象监视器 `monitor`** 的持有权。

`wait/notify`等方法也依赖于`monitor`对象，这就是为什么只有在同步的块或者方法中才能调用`wait/notify`等方法，否则会抛出`java.lang.IllegalMonitorStateException`的异常的原因。

2.修饰方法

```java
public class SynchronizedDemo2 {
    public synchronized void method() {
        System.out.println("synchronized 方法");
    }
}
```

![1627480612(1)](images\1627480612(1).jpg)

`synchronized` 修饰的方法并没有 `monitorenter` 指令和 `monitorexit` 指令，取得代之的确实是 `ACC_SYNCHRONIZED` 标识，该标识指明了该方法是一个同步方法。JVM 通过该 `ACC_SYNCHRONIZED` 访问标志来辨别一个方法是否声明为同步方法，从而执行相应的同步调用。

**不过两者的本质都是对对象监视器 monitor 的获取。**

##### [java6之后做了那些优化？](https://www.cnblogs.com/wuqinglong/p/9945618.html)

参考资料：https://blog.csdn.net/newbie0107/article/details/100717866

​				[javaguide](https://snailclimb.gitee.io/javaguide/#/docs/java/multi-thread/2020%E6%9C%80%E6%96%B0Java%E5%B9%B6%E5%8F%91%E8%BF%9B%E9%98%B6%E5%B8%B8%E8%A7%81%E9%9D%A2%E8%AF%95%E9%A2%98%E6%80%BB%E7%BB%93)

### 18.[ReentrantLock的公平锁，响应中断和限时等待](https://baijiahao.baidu.com/s?id=1648624077736116382&wfr=spider&for=pc)

[ReentrantLock的选择性通知](https://www.jianshu.com/p/dde297897eee)

### 19.类内部执行顺序

#### 普通情况：

```java
public class Outer {
    private int age;
    private String name;
    public Outer(){
        System.out.println("Outer.init()");//3
    }
    {
        System.out.println("Outer.instance()");//2
    }
    static {
        System.out.println("Outer.static()");//1
    }
    public void func(){
        System.out.println(name+age);
    }
    class Inter{
        public static final int age2=10;
        private String name2;
        public Inter(){
            System.out.println("Inter.init()");//5
        }
        {
            System.out.println("Inter.instance()");//4
        }
        public void func(){
            System.out.println(name+age);
        }
    }
}
public class TestDemo {
    public static void main(String[] args) {
        Outer.Inter inter=new Outer().new Inter();
    }
}
```

执行顺序：首先执行的是外部类的静态代码块，外部类实例代码块，外部类构造函数，然后是内部类实例代码块，内部类构造函数。最后还有内部类的成员方法，代码中并没有加入。

#### 静态内部类：

静态内部类大体和实例内部类相似，不过内部类要调用外部类成员，需要提供有外部类引用的构造函数。

```java
public class Outer {
    private int age;
    private String name;
    public Outer(){
        System.out.println("Outer.init()");
    }
    {
        System.out.println("Outer.instance()");
    }
    static {
        System.out.println("Outer.static()");
    }
    public void func(){
        System.out.println("Outer.func()");
    }
    static class Inter{
        private static int age2=10;
        private String name2;
        private Outer outer;
        public Inter(Outer outer){
            this.outer=outer;
            System.out.println("Inter.init()");
        }
        public Inter(){
            System.out.println("Inter.init()");
        }
        public void func1(){
            outer.func();
            System.out.println("Inter.func()");
        }
    }
}
public class TestDemo {
    public static void main(String[] args) {
        Outer outer=new Outer();
        Outer.Inter inter=new Outer.Inter(outer);
        inter.func1();
    }
}
```

#### 继承时：

```java
public class Parent {
    private int age;
    private String name;
    public Parent(){
        System.out.println("parent.init()");//4
    }
    {
        System.out.println("parent.instance");//3
    }
    static {
        System.out.println("parent.static");//1
    }
    public void func(){
        System.out.println("parent.func()");//8
    }
}
public class Son extends Parent{
    private String school;
    public Son(){
        System.out.println("Son.init()");//6
    }
    {
        System.out.println("Son.instance()");//5
    }
    static{
        System.out.println("Son.static");//2
    }
    public void func(){
        System.out.println("Son.func()");//7
    }
}
public class TestDemo {
    public static void main(String[] args) {
        Son son=new Son();
        Parent parent=new Son();
        parent.func();
        son.func();
    }
}

```



## 数据库

### 1.mysql的联合索引

- **最左匹配**

所谓最左原则指的就是如果你的 SQL 语句中用到了联合索引中的最左边的索引，那么这条 SQL 语句就可以利用这个联合索引去进行匹配，值得注意的是，当遇到范围查询(>、<、between、like)就会停止匹配。
假设，我们对(a,b)字段建立一个索引，也就是说，你where后条件为

```sql
a = 1
a = 1 and b = 2
```

是可以匹配索引的。但是要注意的是~你执行

```sql
b= 2 and a =1
```

也是能匹配到索引的，因为Mysql有优化器会自动调整a,b的顺序与索引顺序一致。
相反的，你执行

```sql
b = 2
```

就匹配不到索引了。

而你对(a,b,c,d)建立索引,where后条件为

```
a = 1 and b = 2 and c > 3 and d = 4 
```

那么，a,b,c三个字段能用到索引，而d就匹配不到。因为遇到了范围查询！

- **最左匹配的原理**

假设，我们对(a,b)字段建立索引，那么入下图所示

![725429-20200324102532494-195018706](images\725429-20200324102532494-195018706.png)

如图所示他们是按照a来进行排序，在a相等的情况下，才按b来排序。

因此，我们可以看到a是有序的1，1，2，2，3，3。而b是一种全局无序，局部相对有序状态!

**什么意思呢？**
从全局来看，b的值为1，2，1，4，1，2，是无序的，因此直接执行`b = 2`这种查询条件没有办法利用索引。

从局部来看，当a的值确定的时候，b是有序的。例如a = 1时，b值为1，2是有序的状态。当a=2时候，b的值为1,4也是有序状态。
因此，你执行`a = 1 and b = 2`是a,b字段能用到索引的。而你执行`a > 1 and b = 2`时，a字段能用到索引，b字段用不到索引。因为a的值此时是一个范围，不是固定的，在这个范围内b值不是有序的，因此b字段用不上索引。

综上所示，最左匹配原则，在遇到范围查询的时候，就会停止匹配。

- **例子**

题型一：

如下的查询怎么建立索引？

```sql
SELECT * FROM table WHERE a = 1 and b = 2 and c = 3; 
```

如果此题回答为对(a,b,c)建立索引，那都可以回去等通知了。
此题正确答法是，(a,b,c)或者(c,b,a)或者(b,a,c)都可以，重点要的是将区分度高的字段放在前面，区分度低的字段放后面。像性别、状态这种字段区分度就很低，我们一般放后面。

例如假设区分度由大到小为b,a,c。那么我们就对(b,a,c)建立索引。在执行sql的时候，优化器会 帮我们调整where后a,b,c的顺序，让我们用上索引。

题型二：

如下的查询怎么建立索引？

```sql
SELECT * FROM table WHERE a > 1 and b = 2; 
```

如果此题回答为对(a,b)建立索引，那都可以回去等通知了。
此题正确答法是，对(b,a)建立索引。如果你建立的是(a,b)索引，那么只有a字段能用得上索引，毕竟最左匹配原则遇到范围查询就停止匹配。

题型三：

如下的查询怎么建立索引？

```sql
SELECT * FROM `table` WHERE a > 1 and b = 2 and c > 3; 
```

此题回答也是不一定，(b,a)或者(b,c)都可以，要结合具体情况具体分析。

题型四：

如下的查询怎么建立索引？

```sql
SELECT * FROM `table` WHERE a = 1 ORDER BY b;
```

这还需要想？一看就是对(a,b)建索引，当a = 1的时候，b相对有序，可以避免再次排序！

```sql
SELECT * FROM `table` WHERE a > 1 ORDER BY b; 
```

对(a)建立索引，因为a的值是一个范围，这个范围内b值是无序的，没有必要对(a,b)建立索引。

题型五：

如下的查询怎么建立索引？

```sql
SELECT * FROM `table` WHERE a IN (1,2,3) and b > 1; 
```

还是对(a，b)建立索引，因为IN在这里可以视为等值引用，不会中止索引匹配，所以还是(a,b)!

2.SQL语句



## Spring SpringBoot

### 1.注解

> @SpringBootApplication

其标志的类是springboot的住配置类，springboot会运行该类的main方法启动spring boot应用。

@SpringBootApplication同时等价于好多注解的功能，主要是@SpringBootConfiguration,@EnableAutoConfiguration 和@ComponentScan这三个功能：

@SpringBootConfiguration其实就是spring中的@Configure，表示当前的类是IOC容器的配置类。

@EnableAutoConfiguration的意思是打开springboot 的自动配置功能

@ComponentScan允许包的自动扫描，扫描当前包及其子包下标注了@Component，@Controller，@Service，@Repository 类并纳入到 spring 容器中进行管理。

>@Transactional

实现原理：



### 2.Spring aop

#### 基础：

> aop定义与运用:

面向切面，解决一些横切行的问题。比如日志记录，权限验证，事务管理(Spring的是事务就是用aop实现的)

> spring aop的底层实现

1.jdk动态代理

2.cglib代理

> 是在编译时织入还是运行时织入？

运行时，生成字节码，加载到虚拟机，jdk利用反射原理，cglib运用ASM原理

> 初始化时织入还是获取对象时织入？

初始化的时候，已经将目标对象进行代理，放入到spring 容器中

> spring AOP 默认使用jdk动态代理还是cglib？

要看条件，如果实现了接口的类，是使用jdk。如果没实现接口，就使用cglib。

> 说一下aop中切面、切点、连接点和通知的关系？

```java
/**
 *
 * 切面
 * 一定要给spring 管理
 */
@Component
@Aspect
public class VingAspectJ {

    /**
     * 切点
     * 为什么切点要声明在一个方法上?目的是为了将注解写在上面而已
     * pointcut是连接点的集合（就是方法的集合）
     */
    @Pointcut("execution(* com.ving.dao.*.*(..))")
    public void pointCut(){

    }

    /**
     * 通知---》配置切点
     */
    @After("com.ving.config.VingAspectJ.pointCut()")
    public void after(){
        System.out.println("after");
    }

    @Before("com.ving.config.VingAspectJ.pointCut()")
    public void before(){
        System.out.println("before");
    }
}
```

#### 手动实现：

> spring内部创建代理对象的过程--手动实现一遍

大概：

在***\*Spring\****的底层，如果我们配置了代理模式，***\*Spring\****会为每一个***\*Bean\****创建一个对应的***\*ProxyFactoryBean\****的***\*FactoryBean\****来创建某个对象的代理对象。

例子：假定我们现在有一个接口***\*TicketService\****及其实现类***\*RailwayStation\****，我们打算创建一个代理类，在执行***\*TicketService\****的方法时的各个阶段，插入对应的业务代码。

```java
/**
 * 售票服务
 * Created by louis on 2016/4/14.
 */
public interface TicketService {
 
    //售票
    public void sellTicket();
 
    //问询
    public void inquire();
 
    //退票
    public void withdraw();
}

```

```java
/**
 * RailwayStation 实现 TicketService
 * Created by louis on 2016/4/14.
 */
public class RailwayStation implements TicketService {
 
    public void sellTicket(){
        System.out.println("售票............");
    }
 
    public void inquire() {
        System.out.println("问询.............");
    }
 
    public void withdraw() {
        System.out.println("退票.............");
    }
}
```

```java

/**
 * 执行RealSubject对象的方法之前的处理意见
 * Created by louis on 2016/4/14.
 */
public class TicketServiceBeforeAdvice implements MethodBeforeAdvice {
 
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("BEFORE_ADVICE: 欢迎光临代售点....");
    }
}


/**
 * 返回结果时后的处理意见
 * Created by louis on 2016/4/14.
 */
public class TicketServiceAfterReturningAdvice implements AfterReturningAdvice {
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("AFTER_RETURNING：本次服务已结束....");
    }
}

/**
 * 抛出异常时的处理意见
 * Created by louis on 2016/4/14.
 */
public class TicketServiceThrowsAdvice implements ThrowsAdvice {
 
    public void afterThrowing(Exception ex){
        System.out.println("AFTER_THROWING....");
    }
    public void afterThrowing(Method method, Object[] args, Object target, Exception ex){
        System.out.println("调用过程出错啦！！！！！");
    }
 
}


/**
 *
 * AroundAdvice
 * Created by louis on 2016/4/15.
 */
public class TicketServiceAroundAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("AROUND_ADVICE:BEGIN....");
        Object returnValue = invocation.proceed();
        System.out.println("AROUND_ADVICE:END.....");
        return returnValue;
    }
}


```

ProxyFactoryBean手动创建代理对象

```java
public class App {
 
    public static void main(String[] args) throws Exception {
 
        //1.针对不同的时期类型，提供不同的Advice
        Advice beforeAdvice = new TicketServiceBeforeAdvice();
        Advice afterReturningAdvice = new TicketServiceAfterReturningAdvice();
        Advice aroundAdvice = new TicketServiceAroundAdvice();
        Advice throwsAdvice = new TicketServiceThrowsAdvice();
 
        RailwayStation railwayStation = new RailwayStation();
 
        //2.创建ProxyFactoryBean,用以创建指定对象的Proxy对象
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
       //3.设置Proxy的接口
        proxyFactoryBean.setInterfaces(TicketService.class);
        //4. 设置RealSubject
        proxyFactoryBean.setTarget(railwayStation);
        //5.使用JDK基于接口实现机制的动态代理生成Proxy代理对象，如果想使用CGLIB，需要将这个flag设置成true
        proxyFactoryBean.setProxyTargetClass(true);
 
        //6. 添加不同的Advice
 
        proxyFactoryBean.addAdvice(afterReturningAdvice);
        proxyFactoryBean.addAdvice(aroundAdvice);
        proxyFactoryBean.addAdvice(throwsAdvice);
        proxyFactoryBean.addAdvice(beforeAdvice);
        proxyFactoryBean.setProxyTargetClass(false);
        //7通过ProxyFactoryBean生成Proxy对象
        TicketService ticketService = (TicketService) proxyFactoryBean.getObject();
        ticketService.sellTicket();
 
    }
 
}
结果：
    AROUND_ADVICE:BEGIN
    BEFOR_ADVICE
    售票
    AROUND_ADVICE:END
    AFTER_RETURING
```

#### ProxyFactoryBean

通过上述分析，ProxyFactoryBean应该有如下功能，

1). Proxy应该感兴趣的Adivce列表；

2). 真正的实例对象引用ticketService;

3).告诉ProxyFactoryBean使用基于接口实现的JDK动态代理机制实现proxy: 

4). Proxy应该具备的Interface接口：TicketService;

![image-20210601113030675](images\image-20210601113030675.png)

![1622518418(1)](images\1622518418(1).jpg)

#### 基于JDK面向接口的动态代理JdkDynamicAopProxy生成代理对象

```java

final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {
        //省略若干...
	/** Proxy的配置信息，这里主要提供Advisor列表，并用于返回AdviceChain */
	private final AdvisedSupport advised;
 
	/**
	 * Construct a new JdkDynamicAopProxy for the given AOP configuration.
	 * @param config the AOP configuration as AdvisedSupport object
	 * @throws AopConfigException if the config is invalid. We try to throw an informative
	 * exception in this case, rather than let a mysterious failure happen later.
	 */
	public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("No advisors and no TargetSource specified");
		}
		this.advised = config;
	}
 
 
	@Override
	public Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}
        //返回代理实例对象
	@Override
	public Object getProxy(ClassLoader classLoader) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating JDK dynamic proxy: target source is " + this.advised.getTargetSource());
		}
		Class<?>[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised);
		findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
                //这里的InvocationHandler设置成了当前实例对象，即对这个proxy调用的任何方法，都会调用这个类的invoke()方法
                //这里的invoke方法被调用，动态查找Advice列表，组成ReflectMethodInvocation
		return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
	}
	/**
	 * 对当前proxy调用其上的任何方法，都将转到这个方法上
         * Implementation of {@code InvocationHandler.invoke}.
	 * <p>Callers will see exactly the exception thrown by the target,
	 * unless a hook method throws an exception.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocation invocation;
		Object oldProxy = null;
		boolean setProxyContext = false;
 
		TargetSource targetSource = this.advised.targetSource;
		Class<?> targetClass = null;
		Object target = null;
 
		try {
			if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
				// The target does not implement the equals(Object) method itself.
				return equals(args[0]);
			}
			if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				// The target does not implement the hashCode() method itself.
				return hashCode();
			}
			if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// Service invocations on ProxyConfig with the proxy config...
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}
 
			Object retVal;
 
			if (this.advised.exposeProxy) {
				// Make invocation available if necessary.
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}
 
			// May be null. Get as late as possible to minimize the time we "own" the target,
			// in case it comes from a pool.
			target = targetSource.getTarget();
			if (target != null) {
				targetClass = target.getClass();
			}
 
			// Get the interception chain for this method.获取当前调用方法的拦截链
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
 
			// Check whether we have any advice. If we don't, we can fallback on direct
			// reflective invocation of the target, and avoid creating a MethodInvocation.
                        //如果没有拦截链，则直接调用Joinpoint连接点的方法。
			if (chain.isEmpty()) {
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying.
				Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
			}
			else {
				// We need to create a method invocation...
                                //根据给定的拦截链和方法调用信息，创建新的MethodInvocation对象，整个拦截链的工作逻辑都在这个ReflectiveMethodInvocation里 
				invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
				// Proceed to the joinpoint through the interceptor chain.
				retVal = invocation.proceed();
			}
 
			// Massage return value if necessary.
			Class<?> returnType = method.getReturnType();
			if (retVal != null && retVal == target && returnType.isInstance(proxy) &&
					!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
				// Special case: it returned "this" and the return type of the method
				// is type-compatible. Note that we can't help if the target sets
				// a reference to itself in another returned object.
				retVal = proxy;
			}
			else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
				throw new AopInvocationException(
						"Null return value from advice does not match primitive return type for: " + method);
			}
			return retVal;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				// Must have come from TargetSource.
				targetSource.releaseTarget(target);
			}
			if (setProxyContext) {
				// Restore old proxy.
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}

```

#### 基于Cglib子类继承方式的动态代理CglibAopProxy生成代理对象

```java
@SuppressWarnings("serial")
class CglibAopProxy implements AopProxy, Serializable {
 
	// Constants for CGLIB callback array indices
	private static final int AOP_PROXY = 0;
	private static final int INVOKE_TARGET = 1;
	private static final int NO_OVERRIDE = 2;
	private static final int DISPATCH_TARGET = 3;
	private static final int DISPATCH_ADVISED = 4;
	private static final int INVOKE_EQUALS = 5;
	private static final int INVOKE_HASHCODE = 6;
 
 
	/** Logger available to subclasses; static to optimize serialization */
	protected static final Log logger = LogFactory.getLog(CglibAopProxy.class);
 
	/** Keeps track of the Classes that we have validated for final methods */
	private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<Class<?>, Boolean>();
 
 
	/** The configuration used to configure this proxy */
	protected final AdvisedSupport advised;
 
	protected Object[] constructorArgs;
 
	protected Class<?>[] constructorArgTypes;
 
	/** Dispatcher used for methods on Advised */
	private final transient AdvisedDispatcher advisedDispatcher;
 
	private transient Map<String, Integer> fixedInterceptorMap;
 
	private transient int fixedInterceptorOffset;
 
 
	/**
	 * Create a new CglibAopProxy for the given AOP configuration.
	 * @param config the AOP configuration as AdvisedSupport object
	 * @throws AopConfigException if the config is invalid. We try to throw an informative
	 * exception in this case, rather than let a mysterious failure happen later.
	 */
	public CglibAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("No advisors and no TargetSource specified");
		}
		this.advised = config;
		this.advisedDispatcher = new AdvisedDispatcher(this.advised);
	}
 
	/**
	 * Set constructor arguments to use for creating the proxy.
	 * @param constructorArgs the constructor argument values
	 * @param constructorArgTypes the constructor argument types
	 */
	public void setConstructorArguments(Object[] constructorArgs, Class<?>[] constructorArgTypes) {
		if (constructorArgs == null || constructorArgTypes == null) {
			throw new IllegalArgumentException("Both 'constructorArgs' and 'constructorArgTypes' need to be specified");
		}
		if (constructorArgs.length != constructorArgTypes.length) {
			throw new IllegalArgumentException("Number of 'constructorArgs' (" + constructorArgs.length +
					") must match number of 'constructorArgTypes' (" + constructorArgTypes.length + ")");
		}
		this.constructorArgs = constructorArgs;
		this.constructorArgTypes = constructorArgTypes;
	}
 
 
	@Override
	public Object getProxy() {
		return getProxy(null);
	}
 
	@Override
	public Object getProxy(ClassLoader classLoader) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating CGLIB proxy: target source is " + this.advised.getTargetSource());
		}
 
		try {
			Class<?> rootClass = this.advised.getTargetClass();
			Assert.state(rootClass != null, "Target class must be available for creating a CGLIB proxy");
 
			Class<?> proxySuperClass = rootClass;
			if (ClassUtils.isCglibProxyClass(rootClass)) {
				proxySuperClass = rootClass.getSuperclass();
				Class<?>[] additionalInterfaces = rootClass.getInterfaces();
				for (Class<?> additionalInterface : additionalInterfaces) {
					this.advised.addInterface(additionalInterface);
				}
			}
 
			// Validate the class, writing log messages as necessary.
			validateClassIfNecessary(proxySuperClass, classLoader);
 
			// Configure CGLIB Enhancer...
			Enhancer enhancer = createEnhancer();
			if (classLoader != null) {
				enhancer.setClassLoader(classLoader);
				if (classLoader instanceof SmartClassLoader &&
						((SmartClassLoader) classLoader).isClassReloadable(proxySuperClass)) {
					enhancer.setUseCache(false);
				}
			}
			enhancer.setSuperclass(proxySuperClass);
			enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setStrategy(new ClassLoaderAwareUndeclaredThrowableStrategy(classLoader));
 
			Callback[] callbacks = getCallbacks(rootClass);
			Class<?>[] types = new Class<?>[callbacks.length];
			for (int x = 0; x < types.length; x++) {
				types[x] = callbacks[x].getClass();
			}
			// fixedInterceptorMap only populated at this point, after getCallbacks call above
			enhancer.setCallbackFilter(new ProxyCallbackFilter(
					this.advised.getConfigurationOnlyCopy(), this.fixedInterceptorMap, this.fixedInterceptorOffset));
			enhancer.setCallbackTypes(types);
 
			// Generate the proxy class and create a proxy instance.
			return createProxyClassAndInstance(enhancer, callbacks);
		}
		catch (CodeGenerationException ex) {
			throw new AopConfigException("Could not generate CGLIB subclass of class [" +
					this.advised.getTargetClass() + "]: " +
					"Common causes of this problem include using a final class or a non-visible class",
					ex);
		}
		catch (IllegalArgumentException ex) {
			throw new AopConfigException("Could not generate CGLIB subclass of class [" +
					this.advised.getTargetClass() + "]: " +
					"Common causes of this problem include using a final class or a non-visible class",
					ex);
		}
		catch (Exception ex) {
			// TargetSource.getTarget() failed
			throw new AopConfigException("Unexpected AOP exception", ex);
		}
	}
 
	protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
		enhancer.setInterceptDuringConstruction(false);
		enhancer.setCallbacks(callbacks);
		return (this.constructorArgs != null ?
				enhancer.create(this.constructorArgTypes, this.constructorArgs) :
				enhancer.create());
	}
 
	/**
	 * Creates the CGLIB {@link Enhancer}. Subclasses may wish to override this to return a custom
	 * {@link Enhancer} implementation.
	 */
	protected Enhancer createEnhancer() {
		return new Enhancer();
	}
 
 
 
	private Callback[] getCallbacks(Class<?> rootClass) throws Exception {
		// Parameters used for optimisation choices...
		boolean exposeProxy = this.advised.isExposeProxy();
		boolean isFrozen = this.advised.isFrozen();
		boolean isStatic = this.advised.getTargetSource().isStatic();
 
		// Choose an "aop" interceptor (used for AOP calls).
		Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);
 
		// Choose a "straight to target" interceptor. (used for calls that are
		// unadvised but can return this). May be required to expose the proxy.
		Callback targetInterceptor;
		if (exposeProxy) {
			targetInterceptor = isStatic ?
					new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedExposedInterceptor(this.advised.getTargetSource());
		}
		else {
			targetInterceptor = isStatic ?
					new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedInterceptor(this.advised.getTargetSource());
		}
 
		// Choose a "direct to target" dispatcher (used for
		// unadvised calls to static targets that cannot return this).
		Callback targetDispatcher = isStatic ?
				new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp();
 
		Callback[] mainCallbacks = new Callback[] {
				aopInterceptor,  // for normal advice
				targetInterceptor,  // invoke target without considering advice, if optimized
				new SerializableNoOp(),  // no override for methods mapped to this
				targetDispatcher, this.advisedDispatcher,
				new EqualsInterceptor(this.advised),
				new HashCodeInterceptor(this.advised)
		};
 
		Callback[] callbacks;
 
		// If the target is a static one and the advice chain is frozen,
		// then we can make some optimisations by sending the AOP calls
		// direct to the target using the fixed chain for that method.
		if (isStatic && isFrozen) {
			Method[] methods = rootClass.getMethods();
			Callback[] fixedCallbacks = new Callback[methods.length];
			this.fixedInterceptorMap = new HashMap<String, Integer>(methods.length);
 
			// TODO: small memory optimisation here (can skip creation for methods with no advice)
			for (int x = 0; x < methods.length; x++) {
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(methods[x], rootClass);
				fixedCallbacks[x] = new FixedChainStaticTargetInterceptor(
						chain, this.advised.getTargetSource().getTarget(), this.advised.getTargetClass());
				this.fixedInterceptorMap.put(methods[x].toString(), x);
			}
 
			// Now copy both the callbacks from mainCallbacks
			// and fixedCallbacks into the callbacks array.
			callbacks = new Callback[mainCallbacks.length + fixedCallbacks.length];
			System.arraycopy(mainCallbacks, 0, callbacks, 0, mainCallbacks.length);
			System.arraycopy(fixedCallbacks, 0, callbacks, mainCallbacks.length, fixedCallbacks.length);
			this.fixedInterceptorOffset = mainCallbacks.length;
		}
		else {
			callbacks = mainCallbacks;
		}
		return callbacks;
	}
 
 
	/**
	 * General purpose AOP callback. Used when the target is dynamic or when the
	 * proxy is not frozen.
	 */
	private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {
 
		private final AdvisedSupport advised;
 
		public DynamicAdvisedInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}
 
		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			boolean setProxyContext = false;
			Class<?> targetClass = null;
			Object target = null;
			try {
				if (this.advised.exposeProxy) {
					// Make invocation available if necessary.
					oldProxy = AopContext.setCurrentProxy(proxy);
					setProxyContext = true;
				}
				// May be null. Get as late as possible to minimize the time we
				// "own" the target, in case it comes from a pool...
				target = getTarget();
				if (target != null) {
					targetClass = target.getClass();
				}
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
				Object retVal;
				// Check whether we only have one InvokerInterceptor: that is,
				// no real advice, but just reflective invocation of the target.
				if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
					// We can skip creating a MethodInvocation: just invoke the target directly.
					// Note that the final invoker must be an InvokerInterceptor, so we know
					// it does nothing but a reflective operation on the target, and no hot
					// swapping or fancy proxying.
					Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
					retVal = methodProxy.invoke(target, argsToUse);
				}
				else {
					// We need to create a method invocation...
					retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed();
				}
				retVal = processReturnType(proxy, target, method, retVal);
				return retVal;
			}
			finally {
				if (target != null) {
					releaseTarget(target);
				}
				if (setProxyContext) {
					// Restore old proxy.
					AopContext.setCurrentProxy(oldProxy);
				}
			}
		}
		//省略...
	}
 
 
	/**
	 * Implementation of AOP Alliance MethodInvocation used by this AOP proxy.
	 */
	private static class CglibMethodInvocation extends ReflectiveMethodInvocation {
 
		private final MethodProxy methodProxy;
 
		private final boolean publicMethod;
 
		public CglibMethodInvocation(Object proxy, Object target, Method method, Object[] arguments,
				Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers, MethodProxy methodProxy) {
 
			super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
			this.methodProxy = methodProxy;
			this.publicMethod = Modifier.isPublic(method.getModifiers());
		}
 
		/**
		 * Gives a marginal performance improvement versus using reflection to
		 * invoke the target when invoking public methods.
		 */
		@Override
		protected Object invokeJoinpoint() throws Throwable {
			if (this.publicMethod) {
				return this.methodProxy.invoke(this.target, this.arguments);
			}
			else {
				return super.invokeJoinpoint();
			}
		}
	}
 
}

```

#### Advice的执行顺序和方法调用

JdkDynamicAopProxy 和CglibAopProxy只是创建代理方式的两种方式而已，实际上我们为方法调用添加的各种Advice的执行逻辑都是统一的。在Spring的底层，会把我们定义的各个Adivce分别 包裹成一个 MethodInterceptor,这些Advice按照加入Advised顺序，构成一个AdivseChain.

```java
  proxyFactoryBean.addAdvice(afterReturningAdvice);
        proxyFactoryBean.addAdvice(aroundAdvice);
        proxyFactoryBean.addAdvice(throwsAdvice);
        proxyFactoryBean.addAdvice(beforeAdvice);
        proxyFactoryBean.setProxyTargetClass(false);
        //通过ProxyFactoryBean生成
        TicketService ticketService = (TicketService) proxyFactoryBean.getObject();
        ticketService.sellTicket();

```

当我们调用 ticketService.sellTicket()时，Spring会把这个方法调用转换成一个MethodInvocation对象，然后结合上述的我们添加的各种Advice,组成一个ReflectiveMethodInvocation。

 各种Advice本质而言是一个方法调用拦截器。

```java

@SuppressWarnings("serial")
public class MethodBeforeAdviceInterceptor implements MethodInterceptor, Serializable {
 
	private MethodBeforeAdvice advice;
 
 
	/**
	 * Create a new MethodBeforeAdviceInterceptor for the given advice.
	 * @param advice the MethodBeforeAdvice to wrap
	 */
	public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
		Assert.notNull(advice, "Advice must not be null");
		this.advice = advice;
	}
 
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		//在调用方法之前，先执行BeforeAdvice
		this.advice.before(mi.getMethod(), mi.getArguments(), mi.getThis() );
		return mi.proceed();
	}
 

```

```java
@SuppressWarnings("serial")
public class AfterReturningAdviceInterceptor implements MethodInterceptor, AfterAdvice, Serializable {
 
	private final AfterReturningAdvice advice;
 
 
	/**
	 * Create a new AfterReturningAdviceInterceptor for the given advice.
	 * @param advice the AfterReturningAdvice to wrap
	 */
	public AfterReturningAdviceInterceptor(AfterReturningAdvice advice) {
		Assert.notNull(advice, "Advice must not be null");
		this.advice = advice;
	}
 
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		//先调用invocation
		Object retVal = mi.proceed();
		//调用成功后，调用AfterReturningAdvice
		this.advice.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
		return retVal;
	}
}
```

```java
public class ThrowsAdviceInterceptor implements MethodInterceptor, AfterAdvice {
 
	private static final String AFTER_THROWING = "afterThrowing";
 
	private static final Log logger = LogFactory.getLog(ThrowsAdviceInterceptor.class);
 
 
	private final Object throwsAdvice;
 
	/** Methods on throws advice, keyed by exception class */
	private final Map<Class<?>, Method> exceptionHandlerMap = new HashMap<Class<?>, Method>();
 
 
	/**
	 * Create a new ThrowsAdviceInterceptor for the given ThrowsAdvice.
	 * @param throwsAdvice the advice object that defines the exception
	 * handler methods (usually a {@link org.springframework.aop.ThrowsAdvice}
	 * implementation)
	 */
	public ThrowsAdviceInterceptor(Object throwsAdvice) {
		Assert.notNull(throwsAdvice, "Advice must not be null");
		this.throwsAdvice = throwsAdvice;
 
		Method[] methods = throwsAdvice.getClass().getMethods();
		for (Method method : methods) {
			//ThrowsAdvice定义的afterThrowing方法是Handler方法
			if (method.getName().equals(AFTER_THROWING) &&
					(method.getParameterTypes().length == 1 || method.getParameterTypes().length == 4) &&
					Throwable.class.isAssignableFrom(method.getParameterTypes()[method.getParameterTypes().length - 1])
				) {
				// Have an exception handler
				this.exceptionHandlerMap.put(method.getParameterTypes()[method.getParameterTypes().length - 1], method);
				if (logger.isDebugEnabled()) {
					logger.debug("Found exception handler method: " + method);
				}
			}
		}
 
		if (this.exceptionHandlerMap.isEmpty()) {
			throw new IllegalArgumentException(
					"At least one handler method must be found in class [" + throwsAdvice.getClass() + "]");
		}
	}
 
	public int getHandlerMethodCount() {
		return this.exceptionHandlerMap.size();
	}
 
	/**
	 * Determine the exception handle method. Can return null if not found.
	 * @param exception the exception thrown
	 * @return a handler for the given exception type
	 */
	private Method getExceptionHandler(Throwable exception) {
		Class<?> exceptionClass = exception.getClass();
		if (logger.isTraceEnabled()) {
			logger.trace("Trying to find handler for exception of type [" + exceptionClass.getName() + "]");
		}
		Method handler = this.exceptionHandlerMap.get(exceptionClass);
		while (handler == null && exceptionClass != Throwable.class) {
			exceptionClass = exceptionClass.getSuperclass();
			handler = this.exceptionHandlerMap.get(exceptionClass);
		}
		if (handler != null && logger.isDebugEnabled()) {
			logger.debug("Found handler for exception of type [" + exceptionClass.getName() + "]: " + handler);
		}
		return handler;
	}
 
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		//使用大的try，先执行代码，捕获异常
		try {
			return mi.proceed();
		}
		catch (Throwable ex) {
			//获取异常处理方法
			Method handlerMethod = getExceptionHandler(ex);
			//调用异常处理方法
			if (handlerMethod != null) {
				invokeHandlerMethod(mi, ex, handlerMethod);
			}
			throw ex;
		}
	}
 
	private void invokeHandlerMethod(MethodInvocation mi, Throwable ex, Method method) throws Throwable {
		Object[] handlerArgs;
		if (method.getParameterTypes().length == 1) {
			handlerArgs = new Object[] { ex };
		}
		else {
			handlerArgs = new Object[] {mi.getMethod(), mi.getArguments(), mi.getThis(), ex};
		}
		try {
			method.invoke(this.throwsAdvice, handlerArgs);
		}
		catch (InvocationTargetException targetEx) {
			throw targetEx.getTargetException();
		}
	}
 
}

```

关于AroundAdivce,其本身就是一个MethodInterceptor，所以不需要额外做转换了。

在拦截器串中，每个拦截器最后都会调用MethodInvocation的proceed()方法。如果按照简单的拦截器的执行串来执行的话，MethodInvocation的proceed()方法至少要执行N次(N表示拦截器Interceptor的个数)，因为每个拦截器都会调用一次proceed()方法。更直观地讲，比如我们调用了ticketService.sellTicket()方法，那么，按照这个逻辑，我们会打印出四条记录：
售票。。。。

售票。。。。

售票。。。。

售票。。。。

真实的Spring的方法调用过程能够控制这个逻辑按照我们的思路执行，Spring将这个整个方法调用过程连同若干个Advice组成的拦截器链组合成ReflectiveMethodInvocation对象，让我们来看看这一执行逻辑是怎么控制的：

```java
public class ReflectiveMethodInvocation implements ProxyMethodInvocation, Cloneable {
 
	protected final Object proxy;
 
	protected final Object target;
 
	protected final Method method;
 
	protected Object[] arguments;
 
	private final Class<?> targetClass;
 
	/**
	 * Lazily initialized map of user-specific attributes for this invocation.
	 */
	private Map<String, Object> userAttributes;
 
	/**
	 * List of MethodInterceptor and InterceptorAndDynamicMethodMatcher
	 * that need dynamic checks.
	 */
	protected final List<?> interceptorsAndDynamicMethodMatchers;
 
	/**
	 * Index from 0 of the current interceptor we're invoking.
	 * -1 until we invoke: then the current interceptor.
	 */
	private int currentInterceptorIndex = -1;
 
 
	/**
	 * Construct a new ReflectiveMethodInvocation with the given arguments.
	 * @param proxy the proxy object that the invocation was made on
	 * @param target the target object to invoke
	 * @param method the method to invoke
	 * @param arguments the arguments to invoke the method with
	 * @param targetClass the target class, for MethodMatcher invocations
	 * @param interceptorsAndDynamicMethodMatchers interceptors that should be applied,
	 * along with any InterceptorAndDynamicMethodMatchers that need evaluation at runtime.
	 * MethodMatchers included in this struct must already have been found to have matched
	 * as far as was possibly statically. Passing an array might be about 10% faster,
	 * but would complicate the code. And it would work only for static pointcuts.
	 */
	protected ReflectiveMethodInvocation(
			Object proxy, Object target, Method method, Object[] arguments,
			Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {
 
		this.proxy = proxy;//proxy对象
		this.target = target;//真实的realSubject对象
		this.targetClass = targetClass;//被代理的类类型
		this.method = BridgeMethodResolver.findBridgedMethod(method);//方法引用
		this.arguments = AopProxyUtils.adaptArgumentsIfNecessary(method, arguments);//调用参数
		this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;//Advice拦截器链
	}
 
 
	@Override
	public final Object getProxy() {
		return this.proxy;
	}
 
	@Override
	public final Object getThis() {
		return this.target;
	}
 
	@Override
	public final AccessibleObject getStaticPart() {
		return this.method;
	}
 
	/**
	 * Return the method invoked on the proxied interface.
	 * May or may not correspond with a method invoked on an underlying
	 * implementation of that interface.
	 */
	@Override
	public final Method getMethod() {
		return this.method;
	}
 
	@Override
	public final Object[] getArguments() {
		return (this.arguments != null ? this.arguments : new Object[0]);
	}
 
	@Override
	public void setArguments(Object... arguments) {
		this.arguments = arguments;
	}
 
 
	@Override
	public Object proceed() throws Throwable {
		//	没有拦截器，则直接调用Joinpoint上的method，即直接调用MethodInvocation We start with an index of -1 and increment early.
		if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
			return invokeJoinpoint();
		}
                // 取得第拦截器链上第N个拦截器 
		Object interceptorOrInterceptionAdvice =
				this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
		//PointcutInterceptor会走这个逻辑
                if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
			// Evaluate dynamic method matcher here: static part will already have
			// been evaluated and found to match.
			InterceptorAndDynamicMethodMatcher dm =
					(InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
			//当前拦截器是符合拦截规则，每个拦截器可以定义是否特定的类和方法名是否符合拦截规则
                        //实际上PointCut定义的方法签名最后会转换成这个MethodMatcher，并置于拦截器中
                        if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
			     //符合拦截规则，调用拦截器invoke()	
                             return dm.interceptor.invoke(this);
			}
			else {
				// Dynamic matching failed.
				// Skip this interceptor and invoke the next in the chain.
                                // 当前方法不需要拦截器操作，则直接往前推进
                                return proceed();
			}
		}
		else {
			// It's an interceptor, so we just invoke it: The pointcut will have
			// been evaluated statically before this object was constructed.
                        //直接调用拦截器，
                        return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
		}
	}
 
	/**
	 * Invoke the joinpoint using reflection.
	 * Subclasses can override this to use custom invocation.
	 * @return the return value of the joinpoint
	 * @throws Throwable if invoking the joinpoint resulted in an exception
	 */
	protected Object invokeJoinpoint() throws Throwable {
		return AopUtils.invokeJoinpointUsingReflection(this.target, this.method, this.arguments);
   }
```

![1622526012(1)](images\1622526012(1).jpg)

#### PointCut与Advice的结合---Advice的条件执行

上面我们提供了几个Adivce，你会发现，这些Advice是无条件地加入了我们创建的对象中。无论调用Target的任何方法，这些Advice都会被触发到。

那么，我们可否告诉Advice，只让它对特定的方法或特定类起作用呢？ 这个实际上是要求我们添加一个过滤器，如果满足条件，则Advice生效，否则无效。

spring将过滤器抽象成如下接口：

```java
public interface MethodMatcher {
 
	boolean matches(Method method, Class<?> targetClass);
 

	boolean isRuntime();
 

	boolean matches(Method method, Class<?> targetClass, Object... args);
 
 
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;
 
}

```

将这个匹配器MethodMatcher和拦截器Interceptor 结合到一起，就构成了一个新的类InterceptorAndDynamicMethodMatcher ：

```java
class InterceptorAndDynamicMethodMatcher {
 
	final MethodInterceptor interceptor;
 
	final MethodMatcher methodMatcher;
 
	public InterceptorAndDynamicMethodMatcher(MethodInterceptor interceptor, MethodMatcher methodMatcher) {
		this.interceptor = interceptor;
		this.methodMatcher = methodMatcher;
	}
}
```

我们再将上述的包含整个拦截器执行链逻辑的ReflectiveMethodInvocation实现的核心代码在过一遍：

```java
@Override
	public Object proceed() throws Throwable {
		//	We start with an index of -1 and increment early.
		if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
			return invokeJoinpoint();
		}
 
		Object interceptorOrInterceptionAdvice =
				this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
		//起到一定的过滤作用，如果不匹配，则直接skip
                if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
			// Evaluate dynamic method matcher here: static part will already have
			// been evaluated and found to match.
			InterceptorAndDynamicMethodMatcher dm =
					(InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
			//满足匹配规则，则拦截器Advice生效
                        if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
				return dm.interceptor.invoke(this);
			}
			else {
				// Dynamic matching failed.
				// Skip this interceptor and invoke the next in the chain.
                                //拦截器尚未生效，直接skip
                                return proceed();
			}
		}
		else {
			// It's an interceptor, so we just invoke it: The pointcut will have
			// been evaluated statically before this object was constructed.
			return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
		}
	}

```

##### 实战：

```java
/**
 * 实现一个PointcutAdvisor，通过提供的Pointcut,对Advice的执行进行过滤
 * Created by louis on 2016/4/16.
 */
public class FilteredAdvisor implements PointcutAdvisor {
 
    private Pointcut pointcut;
    private Advice advice;
 
    public FilteredAdvisor(Pointcut pointcut, Advice advice) {
        this.pointcut = pointcut;
        this.advice = advice;
    }
 
    /**
     * Get the Pointcut that drives this advisor.
     */
    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }
 
    @Override
    public Advice getAdvice() {
        return advice;
    }
 
    @Override
    public boolean isPerInstance() {
        return false;
    }
}

```



```java
/**
 * 通过ProxyFactoryBean 手动创建 代理对象
 * Created by louis on 2016/4/14.
 */
public class App {
 
    public static void main(String[] args) throws Exception {
 
        //1.针对不同的时期类型，提供不同的Advice
        Advice beforeAdvice = new TicketServiceBeforeAdvice();
        Advice afterReturningAdvice = new TicketServiceAfterReturningAdvice();
        Advice aroundAdvice = new TicketServiceAroundAdvice();
        Advice throwsAdvice = new TicketServiceThrowsAdvice();
 
        RailwayStation railwayStation = new RailwayStation();
 
        //2.创建ProxyFactoryBean,用以创建指定对象的Proxy对象
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
       //3.设置Proxy的接口
        proxyFactoryBean.setInterfaces(TicketService.class);
        //4. 设置RealSubject
        proxyFactoryBean.setTarget(railwayStation);
        //5.使用JDK基于接口实现机制的动态代理生成Proxy代理对象，如果想使用CGLIB，需要将这个flag设置成true
        proxyFactoryBean.setProxyTargetClass(true);
 
        //5. 添加不同的Advice
 
        proxyFactoryBean.addAdvice(afterReturningAdvice);
        proxyFactoryBean.addAdvice(aroundAdvice);
        proxyFactoryBean.addAdvice(throwsAdvice);
        //proxyFactoryBean.addAdvice(beforeAdvice);
        proxyFactoryBean.setProxyTargetClass(false);
 
        //手动创建一个pointcut,专门拦截sellTicket方法
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution( * sellTicket(..))");
        //传入创建的beforeAdvice和pointcut
        FilteredAdvisor sellBeforeAdvior = new FilteredAdvisor(pointcut,beforeAdvice);
        //添加到FactoryBean中
        proxyFactoryBean.addAdvisor(sellBeforeAdvior);
        
        //通过ProxyFactoryBean生成
        TicketService ticketService = (TicketService) proxyFactoryBean.getObject();
        ticketService.sellTicket();
        System.out.println("---------------------------");
        ticketService.inquire();
 
    }
}


```

结果：

![1622527244(1)](images\1622527244(1).jpg)

从结果中你可以清晰地看到，我们可以对某一个Advisor(即Advice)添加一个pointcut限制，这样就可以针对指定的方法执行Advice了！本例中使用了PointcutAdvisor,实际上，带底层代码中，Spring会将PointcutAdvisor转换成InterceptorAndDynamicMethodMatcher 参与ReflectiveMethodInvocation关于拦截器链的执行逻辑

#### 总结：

springaop的三个核心点：

1. 代理对象是怎么生成的(JDK or Cglib)

2. Advice链(即拦截器链)的构造过程以及执行机制

3. 如何在Advice上添加pointcut,并且这个pointcut是如何工作的(实际上起到的过滤作用)

## git

![1624005104(1)](images\1624005104(1).jpg)

```
git  clone 远程地址//拉取到本地
git add --all//将修改放到暂存区
git commit -m“修改了啥啥功能”//将修改放到仓库区
git push origin 分支名//将代码推到某个分支

git fetch origin 分支名//获取远程某个分支，在本地创建
git checkout 分支名//切换到远程拉取的分支名
git pull origin 分支名//将该分支代码拉取到本地


git stash//存储在某个分支的修改，以便不想commit的情况下，切换到另外的分支
git stash pop//切换到某个分支，对之前的修改的内容进行恢复

git reset --hard HEAD//回退到上一个版本

git checkout -b 分支名//切换分支，不存在则创建
git branch//查看分支
gitbranch -d 分支名//删除分支

git merge --no-ff origin/master //拉取master到本地分支

```

### 工作开发流程

#### 1.初始时

初始时位于master分支，这时需要拉一下master最新代码：

```bash
git pull origin master
```



#### 2.开发新功能时

然后开发新功能时，就可以切换出一个新分支：

```bash
git checkout - feat/功能描述名称（比如my_feature）
```



附录：

> 分支命名规范：[基于 Git 的分支策略，你值得一看！](https://zhuanlan.zhihu.com/p/50063660)
>
> - master： 被保护的分支
> - feat/feature_name：新功能分支
> - hotfix：线上bug修复分支



#### 3.代码提交

（1）提交代码到本地仓库

然后写代码，写完代码后，提交自己更新到本地仓库

```bash
## 查看工作区情况
git status 

## 提交代码到暂存区
git add --all

## 提交代码到本地仓库
git commit -m"描述本次提交修改了啥"
```

这样代码就提交到了本地仓库



附录：

> commit信息格式：[angular_contributing](https://github.com/angular/angular/blob/master/CONTRIBUTING.md)
>
> #### Commit Message Header
>
> ```
> <type>(<scope>): <short summary>
> │       │             │
> │       │             └─⫸ Summary in present tense. Not capitalized. No period at the end.
> │       │
> │       └─⫸ Commit Scope: animations|bazel|benchpress|common|compiler|compiler-cli|core|
> │                          elements|forms|http|language-service|localize|platform-browser|
> │                          platform-browser-dynamic|platform-server|router|service-worker|
> │                          upgrade|zone.js|packaging|changelog|dev-infra|docs-infra|migrations|
> │                          ngcc|ve
> │
> └─⫸ Commit Type: build|ci|docs|feat|fix|perf|refactor|test
> ```
>
> The `<type>` and `<summary>` fields are mandatory, the `(<scope>)` field is optional.
>
> ##### Type
>
> Must be one of the following:
>
> - **build**: Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)
> - **ci**: Changes to our CI configuration files and scripts (example scopes: Circle, BrowserStack, SauceLabs)
> - **docs**: Documentation only changes
> - **feat**: A new feature
> - **fix**: A bug fix
> - **perf**: A code change that improves performance
> - **refactor**: A code change that neither fixes a bug nor adds a feature
> - **test**: Adding missing tests or correcting existing tests



（2）提交代码到远程仓库

```bash
## 1.提交代码到远程仓库的当前分支
git push origin feat/my_feature

## 2.先拉取master最新代码，因为在开发期间，可能master上有了别人的更新
git pull origin master

## 2.1 此时若有冲突，则需要解决冲突，解决冲突之后，再次提交解决冲突的更新
git status 
git add --all
git commit -m"merge: resolve conflict of master"

## 3. 再此提交代码到远程仓库的当前分支
git push origin feat/my_feature
```



附录：

> 冲突解决：[解决代码提交的冲突](https://support.huaweicloud.com/usermanual-codehub/devcloud_hlp_0934.html)



#### 4.提交合并请求

在 gitlab 提交一个merge request

将代码合并到master之后，就正式结束了功能的开发了



#### 5.版本回退

有时可能需要进行版本回退

```bash
## 回退到上一次提交
git reset --hard HEAD^

## 回退到指定的那次提交
git reset --hard commitId

```

### stash

使用场景：某个分支开发一半，功能没开发完，忽然想切换到别的分支，做一些事情。因为功能没开发完，所以不想push到git上。但是工作区有内容是不可以切换分支的。所以可以git stash，先将内容放到暂存区。此时的工作区就干净了。再切换就可以了。

实际遇到情况：在一个分支上开发功能，开发完了，发现开发错分支了。复杂的方法：将改的内容复制出来，将这个分支回退，重新新建个分支，将改的代码复制进去。

简洁处理：git stash-->新建分支--->git stash pop

## maven：

##### 解决问题：

1.jar包依赖2.jar依赖传递3.jar版本冲突4.项目生命周期

##### 文件解析：

![1625198265(1)](images\1625198265(1).jpg)

##### 运行过程：

1.找mvn可执行文件---bin下面的运行脚本

2.找配置文件 conf/setting.xml ,优先找目录优先级：~/.m2 > M2_HOME/conf

3.本地仓库寻找插件clean，没有则去远程仓库下载

4.运行插件

##### 标签详解：

```xml
<groupId>项目包名</groupId>
<artifactId>项目唯一标识</artifactId>
<version>版本号</version>
<!--打包类型：jar/war/pom-->
<!--父级文件用pm：install时不会生成jar或者war包
	主要作用：1.可以通过<modules>标签来整合子模块的编译顺序（Maven引入依赖使用最短路径原则，例如a<–b<–c1.0 ，d<–e<–f<–c1.1，由于路径最短，最终引入的为c1.0；但路径长度相同时，则会引入先申明的依赖）。因此尽量将更加底层的service放在更先的位置优先加载依赖较为合适。
2.可以将一些子项目中共用的依赖或将其版本统一写到父级配置中，以便统一管理。
3.groupId, artifactId, version能直接从父级继承，减少子项目的pom配置。
-->
<!--需要部署用war：编译后的.class文件按层级结构形成文件树后打包形成的压缩包。它会将项目中依赖的所有jar包都放在WEB-INF/lib这个文件夹下-->
<!--内部调用用jar：当我们使用mvn install命令的时候，能够发现在项目中与src文件夹同级新生成了一个target文件夹，这个文件夹内的classes文件夹即为刚才提到的编译后形成的文件夹-->
 <packaging>打包类型</packaging>
<!--声明项目描述符遵循哪一个POM模型版本。模型本身的版本很少改变，虽然如此，但它仍然是必不可少的，这是为了当Maven引入了新的特性或者其他模型变更的时候，确保稳定性。 -->
 <modelVersion>4.0.0</modelVersion>
 <parent>
        <artifactId />
        <groupId />
        <version />
        <!-- 父项目的pom.xml文件的相对路径。相对路径允许你选择一个不同的路径。默认值是../pom.xml。Maven首先在构建当前项目的地方寻找父项 
            目的pom，其次在文件系统的这个位置（relativePath位置），然后在本地仓库，最后在远程仓库寻找父项目的pom。 -->
        <relativePath />
 </parent>
<!-- 继承自该项目的所有子项目的默认依赖信息。这部分的依赖信息不会被立即解析,而是当子项目声明一个依赖（必须描述group ID和 artifact 
        ID信息），如果group ID和artifact ID以外的一些信息没有描述，则通过group ID和artifact ID 匹配到这里的依赖，并使用这里的依赖信息。 -->
 <dependencyManagement>
        <dependencies>
            <dependency>
                ......
            </dependency>
        </dependencies>
  </dependencyManagement>
 <!--该元素描述了项目相关的所有依赖。 这些依赖组成了项目构建过程中的一个个环节。它们自动从项目定义的仓库中下载。要获取更多信息，请看项目依赖机制。 -->
  <dependencies>
       <!--参见dependencies/dependency元素 -->
       <dependency>
         	<!--依赖的group ID -->
            <groupId>org.apache.maven</groupId>
            <!--依赖的artifact ID -->
            <artifactId>maven-artifact</artifactId>
            <!--依赖的版本号。 在Maven 2里, 也可以配置成版本号的范围。 -->
            <version>3.8.1</version>
            <!-- 依赖类型，默认类型是jar。它通常表示依赖的文件的扩展名，但也有例外。一个类型可以被映射成另外一个扩展名或分类器。类型经常和使用的打包方式对应， 
                尽管这也有例外。一些类型的例子：jar，war，ejb-client和test-jar。如果设置extensions为 true，就可以在 plugin里定义新的类型。所以前面的类型的例子不完整。 -->
            <type>jar</type>
            <!--依赖范围。在项目发布过程中，帮助决定哪些构件被包括进来。欲知详情请参考依赖机制。 - compile ：默认范围，用于编译 - provided：类似于编译，但支持你期待jdk或者容器提供，类似于classpath 
                - runtime: 在执行时需要使用 - test: 用于test任务时使用 - system: 需要外在提供相应的元素。通过systemPath来取得 
                - systemPath: 仅用于范围为system。提供相应的路径 - optional: 当项目自身被依赖时，标注依赖是否传递。用于连续依赖时使用 -->
            <scope>test</scope>
             <!--当计算传递依赖时， 从依赖构件列表里，列出被排除的依赖构件集。即告诉maven你只依赖指定的项目，不依赖项目的依赖。此元素主要用于解决版本冲突问题 -->
            <exclusions>
                <exclusion>
                    <artifactId>spring-core</artifactId>
                    <groupId>org.springframework</groupId>
                </exclusion>
            </exclusions>
           <!--可选依赖，如果你在项目B中把C依赖声明为可选，你就需要在依赖于B的项目（例如项目A）中显式的引用对C的依赖。可选依赖阻断依赖的传递性。 -->
            <optional>true</optional>
       </dependency>
  </dependencies>
 <!--构建项目所需要的信息-->
 <build>
     <!--子项目可以引用的默认插件信息。该插件配置项直到被引用时才会被解析或绑定到生命周期。给定插件的任何本地配置都会覆盖这里的配置 -->
        <pluginManagement>
            <!--使用的插件列表 。 -->
            <plugins>
                <!--plugin元素包含描述插件所需要的信息。 -->
                <plugin>
                    <!--插件在仓库里的group ID -->
                    <groupId />
                    <!--插件在仓库里的artifact ID -->
                    <artifactId />
                    <!--被使用的插件的版本（或版本范围） -->
                    <version />
                </plugin>
                 <!--在构建生命周期中执行一组目标的配置。每个目标可能有不同的配置。 -->
                    <executions>
                        <!--execution元素包含了插件执行需要的信息 -->
                        <execution>
                            <!--执行目标的标识符，用于标识构建过程中的目标，或者匹配继承过程中需要合并的执行目标 -->
                            <id />
                            <!--绑定了目标的构建生命周期阶段，如果省略，目标会被绑定到源数据里配置的默认阶段 -->
                            <phase />
                            <!--配置的执行目标 -->
                            <goals />
                            <!--配置是否被传播到子POM -->
                            <inherited />
                            <!--作为DOM对象的配置 -->
                            <configuration />
                        </execution>
                    </executions>
                    <!--项目引入插件所需要的额外依赖 -->
                    <dependencies>
                        <!--参见dependencies/dependency元素 -->
                        <dependency>
                            ......
                        </dependency>
                    </dependencies>
                    <!--任何配置是否被传播到子项目 -->
                    <inherited />
                    <!--作为DOM对象的配置 -->
                    <configuration />
            </plugins>
        </pluginManagement>
 </build>
```

##### maven仓库

分为本地和远程两个仓库

本地：~/.m2/settings.xml  

```xml
<localRepository>本地仓库地址</localRepository>
```

远程：在配置文件中找到repositories和mirror，设置远程仓库和镜像。

mirror相当于一个拦截器，它会拦截maven对remote repository的相关请求，把请求里的remote repository地址，重定向到mirror里配置的地址。

##### mvn构建生命周期：
```plantuml
@startuml
:valid：验证项目;
:compile：执行编译;
:test:使用合适的框架进行测试;
:package：创建jar/war包;
:verify:对继承测试的结果进行检查;
:install：打包到本地仓库，供其他项目使用;
:depoly：拷贝到远程仓库;
@enduml
```

为了完成 default 生命周期，这些阶段（包括其他未在上面罗列的生命周期阶段）将被按顺序地执行。

Maven 有以下三个标准的生命周期：

- **clean**：项目清理的处理
- **default(或 build)**：项目部署的处理
- **site**：项目站点文档创建的处理

常用命令和生命周期的关系：

*注：当一个阶段通过 Maven 命令调用时，例如 mvn compile，只有该阶段之前以及包括该阶段在内的所有阶段会被执行。*

 mvn clean: 移除上一下次构建生成的文件（具体涉及clean生命周期，有兴趣可以自己查查）

mvn clean install :执行clean，然后再从头valid一直到install

clean install -DskipTests：跳过测试执行clean install

## MQ

### 1.为什么使用mq？优缺点？

出处:微信公众号(java面试题精选)

主要说以前的项目里哪里用到了mq，为什么用。

首先说一下mq的作用也就是优点：

> 1.系统解耦，A调用D系统的接口，直接将D系统的接口调用写在代码里，显然两个系统有严重的耦合。D系统的任何变动，A都得修改。消息队列就是向一个主题发送消息之后，D系统自己消费去，与A系统完全解耦开。

![1626089830(1)](images\1626089830(1).png)

> 2.异步调用：

> 场景二，还是ABCD四个系统，A系统收到一个请求，需要在自己本地写库，还需要往BCD三个系统写库，A系统自己写本地库需要3ms，往其他系统写库相对较慢，B系统200ms ，C系统350ms，D系统400ms，这样算起来，整个功能从请求到响应的时间为3ms+200ms+350ms+400ms=953ms，接近一秒，对于用户来说，点个按钮要等这么长时间，基本是无法接受的

![1626090143(1)](images\1626090143(1).png)

> 如果用了MQ，用户发送请求到A系统耗时3ms，A系统发送三条消息到MQ，假如耗时5ms，用户从发送请求到相应3ms+5ms=8ms，仅用了8ms，用户的体验非常好。

![1626090227(1)](images\1626090227(1).png)

> 3.流量削峰

> 场景三，这次举个实例吧，也是近期发生的，我们都知道 ，2020年爆发的这场新冠病毒，导致各大线上商城APP里面的口罩被抢购一空，在这种情况下，JD商城开启了一场每晚八点的抢购3Q口罩的活动，每天下午三点进行预约，晚上八点抢购，从JD商城刚上线这个活动，我连续抢了近一个周，也算是见证了一个百万并发量系统从出现问题到完善的一个过程，最初第一天，我抢购的时候，一百多万预约，到八点抢购估计也能有百万的并发量，可是第一天，到八点我抢的时候，由于并发量太高，直接把JD服务器弄崩了，直接报了异常，可能JD在上线这个活动的时候也没能够想到会有那么高的并发，打了一个猝不及防，但是这只是在前一两天出现报异常的情况，后面却没有再出现异常信息，到后来再抢购只是响应的时间变得很慢，但是JD系统并没有崩溃，这种情况下一般就是用了MQ（或者之前用了MQ，这次换了个吞吐量级别更高的MQ），也正是利用了MQ的三大好处之一——削峰。

> 如果使用了MQ，每秒百万个请求写入MQ，因为JD系统每秒能处理1W+的请求，JD系统处理完然后再去MQ里面，再拉取1W+的请求处理，每次不要超过自己能处理的最大请求量就ok，这样下来，等到八点高峰期的时候，系统也不会挂掉，但是近一个小时内，系统处理请求的速度是肯定赶不上用户的并发请求的，所以都会积压在MQ中，甚至可能积压千万条，但是高峰期过后，每秒只会有一千多的并发请求进入MQ，但是JD系统还是会以每秒1W+的速度处理请求，所以高峰期一过，JD系统会很快消化掉积压在MQ的请求，在用户那边可能也就是等的时间长一点，但是绝对不会让系统挂掉。

接下来说说mq的缺点：

> 1.系统可用性降低：即面临的风险变高，本来是直接的系统间调用，引入了mq，万一mq挂掉了，那么系统也就挂掉了。
>
> 2.系统的复杂度提高：需要考虑消息的重复消费，消息丢失，保证消息传递的顺序等问题
>
> 3.数据不一致的问题：A系统处理完再传递给MQ就直接返回成功了，用户以为你这个请求成功了，但是，如果在BCD的系统里，BC两个系统写库成功，D系统写库失败了怎么办，这样就导致数据不一致了。

最后说说项目中用到的场景：

之前实习做得项目是一个多语言翻译的中台，当时我负责开发了机器翻译的功能，里面有一个需要机器翻译完的生成报表问题，用户可以查询日期范围内的报表，并且也可以下载报表。下载功能用到了mq，主要是因为想让用户体验更加的好，点击下载报表之后，前端页面就之后有响应，页面上会有一个

![1626091009(1)](images\1626091009(1).png)

存储好了变成这样：

![1626091090(1)](images\1626091090(1).jpg)

点击蓝色字体会进行下载，这样的设计用户体验更加的好。(逻辑：有个record表记录操作记录和状态，filestorge记录存储在s3的位置(即服务器上的位置)，一开始用户点击下载，回先生成record记录其状态是正在处理，然后发消息给上传文件的job，上传完成之后更改reord状态为上传完成，并且在filestorge中记录在服务器的位置。)

### 2.kafuka，ActiveMQ，RabbitMQ，RocketMQ等主流MQ的区别？

> 吞吐量：*吞吐量*是指对网络、设备、端口、虚电路或其他设施，单位时间内成功地传送数据的数量（以比特、字节、分组等测量）。 这里指的是每秒能处理的消息数量。

> ActiveMQ，没经过大规模吞吐量场景的验证(单机能达到万级)，社区也不是很活跃。用的比较少。
>
> RabbitMQ是一个由 Erlang 语言开发的 AMQP 的开源实现。，高吞吐(单机达到万级，通过集群方式拓展可以达到10W/s的吞吐速率),高堆积(支持topic下消费者较长时间离线，消息堆积量大),能够快速持久化。主从架构实现高可用性。
>
> RocketMQ，阿里开发，topic可以达到几百，几千个的级别，吞吐量会有较小幅度的下降(单机10W/s)。源码是java，可以自己阅读源码，定制自己公司的MQ。主从架构实现高可用性。
>
> kafka,单机吞吐量：十万级。分布式架构实现高可用性，一个数据多个副本，少数机器宕机，不会丢失数据，不会导致不可用。
>
> 在大数据领域的实时计算以及日志采集被大规模使用。



### 3.RabbitMQ:[出处](https://www.jianshu.com/p/79ca08116d57)

#### 3.1基本概念：

RabbitMQ 是 AMQP 协议的一个开源实现，所以其内部实际上也是 AMQP 中的基本概念：

![1626094128(1)](images\1626094128(1).jpg)

> Message:
>  消息，消息是不具名的，它由消息头和消息体组成。消息体是不透明的，而消息头则由一系列的可选属性组成，这些属性包括routing-key（路由键）、priority（相对于其他消息的优先权）、delivery-mode（指出该消息可能需要持久性存储）等。

> Publisher:
>
> 消息的生产者，也是一个向交换器发布消息的客户端应用程序。

> Exchange:
>
> 交换器，用来接收生产者发送的消息并将这些消息路由给服务器中的队列。

>Binding:
>
>绑定，用于消息队列和交换器之间的关联。一个绑定就是基于路由键将交换器和消息队列连接起来的路由规则，所以可以将交换器理解成一个由绑定构成的路由表。

> Queue:
>
> 消息队列，用来保存消息直到发送给消费者。它是消息的容器，也是消息的终点。一个消息可投入一个或多个队列。消息一直在队列里面，等待消费者连接到这个队列将其取走。

> Connection:
>
> 网络连接，比如一个TCP连接。

> Channel:
>
> 信道，多路复用连接中的一条独立的双向数据流通道。信道是建立在真实的TCP连接内地虚拟连接，AMQP 命令都是通过信道发出去的，不管是发布消息、订阅队列还是接收消息，这些动作都是通过信道完成。因为对于操作系统来说建立和销毁 TCP 都是非常昂贵的开销，所以引入了信道的概念，以复用一条 TCP 连接。

> Consumer:
>
> 消息的消费者，表示一个从消息队列中取得消息的客户端应用程序。

> Broker:
>
> 表示消息队列服务器实体

#### 3.2 AMQP中的消息路由

AMQP 中增加了 Exchange 和 Binding 的角色。生产者把消息发布到 Exchange 上，消息最终到达队列并被消费者接收，而 Binding 决定交换器的消息应该发送到那个队列。

![1626094888(1)](images\1626094888(1).jpg)

##### Exchange 类型

Exchange分发消息时根据类型的不同分发策略有区别，目前共四种类型：direct、fanout、topic、headers

> 1.direct:完全匹配
>
> 消息中的路由键（routing key）如果和 Binding 中的 binding key 一致， 交换器就将消息发到对应的队列中。路由键与队列名完全匹配！

​	![1626143690(1)](images\1626143690(1).jpg)

> 2.fanout：
>
> 每个发到 fanout 类型交换器的消息都会分到所有绑定的队列上去。fanout 交换器不处理路由键，只是简单的将队列绑定到交换器上，每个发送到交换器的消息都会被转发到与该交换器绑定的所有队列上。

![1626145397(1)](images\1626145397(1).jpg)

> 3.topic
>
> topic 交换器通过模式匹配分配消息的路由键属性，将路由键和某个模式进行匹配，此时队列需要绑定到一个模式上。它将路由键和绑定键的字符串切分成单词，这些单词之间用点隔开。它同样也会识别两个通配符：符号“#”和符号“*”。#匹配0个或多个单词，*匹配不多不少一个单词。

![1626145845(1)](images\1626145845(1).jpg)

#### 3.3 java客户端访问实例

```xml
<dependency>
    <groupId>com.rabbitmq</groupId>
    <artifactId>amqp-client</artifactId>
    <version>4.1.0</version>
</dependency>
```

> 消息生产者

```java
public class Producer {

    public static void main(String[] args) throws IOException, TimeoutException {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        //设置 RabbitMQ 地址
        factory.setHost("localhost");
        //建立到代理服务器到连接
        Connection conn = factory.newConnection();
        //获得信道
        Channel channel = conn.createChannel();
        //声明交换器
        String exchangeName = "hello-exchange";
        channel.exchangeDeclare(exchangeName, "direct", true);

        String routingKey = "hola";
        //发布消息
        byte[] messageBodyBytes = "quit".getBytes();
        channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);

        channel.close();
        conn.close();
    }
}
```

> 消息消费者

```java
public class Consumer {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setHost("localhost");
        //建立到代理服务器到连接
        Connection conn = factory.newConnection();
        //获得信道
        final Channel channel = conn.createChannel();
        //声明交换器
        String exchangeName = "hello-exchange";
        channel.exchangeDeclare(exchangeName, "direct", true);
        //声明队列
        String queueName = channel.queueDeclare().getQueue();
        String routingKey = "hola";
        //绑定队列，通过键 hola 将队列和交换器绑定起来
        channel.queueBind(queueName, exchangeName, routingKey);
        while(true) {
            //消费消息
            boolean autoAck = false;
            String consumerTag = "";
            channel.basicConsume(queueName, autoAck, consumerTag, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    String routingKey = envelope.getRoutingKey();
                    String contentType = properties.getContentType();
                    System.out.println("消费的路由键：" + routingKey);
                    System.out.println("消费的内容类型：" + contentType);
                    long deliveryTag = envelope.getDeliveryTag();
                    //确认消息
                    channel.basicAck(deliveryTag, false);
                    System.out.println("消费的消息体内容：");
                    String bodyStr = new String(body, "UTF-8");
                    System.out.println(bodyStr);

                }
            });
        }
    }
}
```

#### 3.4 RabbitMQ集群

RabbitMQ 内部利用 Erlang 提供的分布式通信框架 OTP 来满足上述需求，使客户端在失去一个 RabbitMQ 节点连接的情况下，还是能够重新连接到集群中的任何其他节点继续生产、消费消息。

RabbitMQ 会始终记录以下四种类型的内部元数据：

1. 队列元数据
    包括队列名称和它们的属性，比如是否可持久化，是否自动删除
2. 交换器元数据
    交换器名称、类型、属性
3. 绑定元数据
    内部是一张表格记录如何将消息路由到队列
4. vhost 元数据
    为 vhost 内部的队列、交换器、绑定提供命名空间和安全属性

在单一节点中，RabbitMQ 会将所有这些信息存储在内存中，同时将标记为可持久化的队列、交换器、绑定存储到硬盘上。存到硬盘上可以确保队列和交换器在节点重启后能够重建。而在集群模式下同样也提供两种选择：存到硬盘上（独立节点的默认设置），存在内存中。

> 镜像队列：如果在集群中创建队列，集群只会在单个节点而不是所有节点上创建完整的队列信息（元数据、状态、内容）。结果是只有队列的所有者节点知道有关队列的所有信息，因此当集群节点崩溃时，该节点的队列和绑定就消失了，并且任何匹配该队列的绑定的新消息也丢失了。还好RabbitMQ 2.6.0之后提供了镜像队列以避免集群节点故障导致的队列内容不可用

RabbitMQ 集群中可以共享 user、vhost、exchange等，所有的数据和状态都是必须在所有节点上复制的。当在集群中声明队列、交换器、绑定的时候，这些操作会直到所有集群节点都成功提交元数据变更后才返回。

> 磁盘节点和内存节点：集群中有内存节点和磁盘节点两种类型，内存节点虽然不写入磁盘，但是它的执行比磁盘节点要好。内存节点可以提供出色的性能，磁盘节点能保障配置信息在节点重启后仍然可用，那集群中如何平衡这两者呢？
>
> RabbitMQ 只要求集群中至少有一个磁盘节点，所有其他节点可以是内存节点，当节点加入火离开集群时，它们必须要将该变更通知到至少一个磁盘节点。如果只有一个磁盘节点，刚好又是该节点崩溃了，那么集群可以继续路由消息，但不能创建队列、创建交换器、创建绑定、添加用户、更改权限、添加或删除集群节点。换句话说集群中的唯一磁盘节点崩溃的话，集群仍然可以运行，但知道该节点恢复，否则无法更改任何东西。

#### 3.5 常见问题

- **如何保证消息尽量发送成功？**

> 1.生产者确认：

​		首先，我们要确保生产者能成功地将消息发送到RabbitMQ服务器。
​		默认情况下生产者发送消息并不会返回任何状态信息，也就是它并不知道消息有没有正确地到达服务器。

​			针对这个问题，RabbitMQ提供了两种解决方案：

​			1.事务机制：事务机制是阻塞形式的，一条消息发送之后会使消息端阻塞，以等待RabbitMQ的回应，才能发送下一个消息。使用事务机制会影响RabbitMQ的性能，因此还是推荐使用发送方确认机制。

​	   			*事务机制*相关的方法主要有三个：

​						1.channel.txSelect：用于将当前的channel设置成事务模式;

​						2.channel.txCommit：用于提交事务;

​						3.channel.txRollback：用于回滚事务.

​			2.通过发送方确认机制（publisher confirm）：*发送方确认*机制是指生产者将channel设置成confirm模式，所有在该信道上发布的消息都会被指派一个唯一ID(从1开始)，一旦消息被投递到RabbitMQ服务器之后，RabbitMQ就会发送一个包含了消息唯一ID的确认（Basic.Ack）给生产者，使生产者知道消息已经正确到达了目的地。如果RabbitMQ因为内部错误导致消息丢失，就会发送一条nack(Basic.Nack)命令，生产者可以在回调方法中处理该nack命令。这个过程是异步的，不需要等待确认信息回来，因此效率更高。
​					相关的方法：

​						1.channel.confirmSelect();

​						2.channel.waitForConfirms；

​						3.channel.addConfirmListener；

注意：事务机制和生产者确认机制是互斥的，不能共存！

> 2.持久化

​				RabbitMQ持久化分为：交换机持久化；队列持久化；消息持久化；

- ​	**如何保证消息被正确消费**？

> 这部分要处理的场景是： 当消费者接收到消息后，还没处理完业务逻辑，消费者挂掉了，此时消息等同于丢失了。
>
> 为了确保消息被消费者成功消费，RabbitMQ提供了消息确认机制，主要通过显示Ack模式来实现。
>
> 默认情况下，RabbitMQ会自动把发送出去的消息置为确认，然后从内存(或磁盘)删除，但是我们在使用时可以手动设置autoAck为False的，当然具体做法各个语言都不一样。
>
> 需要注意的时，如果设置autoAck为false，也就意味者每条消息需要我们自己发送ack确认，RabbitMQ才能正确标识消息的状态

## Spock测试框架：

[原文](https://juejin.cn/post/6844903957475622926#heading-21)

### 背景：

Spock是Java和Groovy应用程序的测试和规范框架

测试代码使用基于groovy语言扩展而成的规范说明语言（specification language）

通过junit runner调用测试，兼容绝大部分junit的运行场景（ide，构建工具，持续集成等）

#### 扩展：

groovy：是以扩展java为目的为设计的JVM语言，可以使用java语法和api，广泛应用于：jenkins，elasticsearch，gradle

#### BDD:Behavior-driven development行为驱动测试

通过某种规范说明语言去描述程序“应该”做什么，再通过一个测试框架读取这些描述、并验证应用程序是否符合预期。把需求转化成Given/When/Then的三段式

### 依赖

```xml
<dependency>
	<groupId>org.codehaus.groovy</groupId>
	<artifactId>groovy-all</artifactId>
	<version>2.4.12</version>
</dependency>

<dependency>
	<groupId>junit</groupId>
	<artifactId>junit</artifactId>
	<version>4.12</version>
	<scope>test</scope>
</dependency>

<dependency>
	<groupId>org.spockframework</groupId>
	<artifactId>spock-core</artifactId>
	<version>1.1-groovy-2.4</version>
	<scope>test</scope>
</dependency>	
```

### Demo

```java
public interface CacheService {

    String getUserName();
}
public class Calculator {

    private CacheService cacheService;

    public Calculator(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public boolean isLoggedInUser(String userName) {
        return Objects.equals(userName, cacheService.getUserName());
    }
    ...
}
```

```groovy
class CalculatorSpec extends Specification {
    
    // mock对象
//    CacheService cacheService = Mock()
    def cacheService = Mock(CacheService)
    def calculator
    void setup() {
       calculator = new Calculator(cacheService)
    }
    def  "is username equal to logged in username"() {
        // stub 打桩
        cacheService.getUserName(*_) >> "Richard"
        when:
        def result = calculator.isLoggedInUser("Richard")
        then:
        result
    }
    ...
}

```

### Spock深入

在Spock中，待测系统(system under test; SUT) 的行为是由规格(specification) 所定义的。在使用Spock框架编写测试时，测试类需要继承自Specification类。命名遵循Java规范。

#### 结构：

每个测试方法可以直接用文本作为方法名，方法内部由`given-when-then`的三段式块（block）组成。除此以外，还有`and`、`where`、`expect`等几种不同的块。

```groovy

@Title("测试的标题")
@Narrative("""关于测试的大段文本描述""")
@Subject(Adder)  //标明被测试的类是Adder
@Stepwise  //当测试方法间存在依赖关系时，标明测试方法将严格按照其在源代码中声明的顺序执行
class TestCaseClass extends Specification {  
  @Shared //在测试方法之间共享的数据
  SomeClass sharedObj
  def setupSpec() {
    //TODO: 设置每个测试类的环境
  }
  def setup() {
    //TODO: 设置每个测试方法的环境，每个测试方法执行一次
  }
  @Ignore("忽略这个测试方法")
  @Issue(["问题#23","问题#34"])
  def "测试方法1" () {
    given: "给定一个前置条件"
    //TODO: code here
    and: "其他前置条件"
    expect: "随处可用的断言"
    //TODO: code here
    when: "当发生一个特定的事件"
    //TODO: code here
    and: "其他的触发条件"
    then: "产生的后置结果"
    //TODO: code here
    and: "同时产生的其他结果"
    where: "不是必需的测试数据"
    input1 | input2 || output
     ...   |   ...  ||   ...   
  }
 
  @IgnoreRest //只测试这个方法，而忽略所有其他方法
  @Timeout(value = 50, unit = TimeUnit.MILLISECONDS)  // 设置测试方法的超时时间，默认单位为秒
  def "测试方法2"() {
    //TODO: code here
  }
 
  def cleanup() {
    //TODO: 清理每个测试方法的环境，每个测试方法执行一次
  }
 
  def cleanupSepc() {
    //TODO: 清理每个测试类的环境
  }


```

#### setup与given

```groovy
 def  "is username equal to logged in username"() {
        setup://习惯于携程given:
        def str = "Richard"
        // stub 打桩
        cacheService.getUserName(*_) >> str
        when:
        def result = calculator.isLoggedInUser("Richard")
        then:
        result
    }

```

when_then与wxpect

```groovy
when:
def x = Math.max(1, 2)  
then:
x == 2
//等价于
expect:
Math.max(1, 2) == 2  
```

#### assert

条件类似junit中的assert，就像上面的例子，在then或expect中会默认assert所有返回值是boolean型的顶级语句**。**如果要在其它地方增加断言，需要显式增加assert关键字

#### 异常断言：

```groovy
  def "peek"() {
    when: stack.peek()
    then: thrown(EmptyStackException)
  }
//如果要验证没有抛出某种异常，可以用notThrown()
```

#### Mock

##### 创建对象

```groovy

def subscriber = Mock(Subscriber)
def subscriber2 = Mock(Subscriber)
    
Subscriber subscriber = Mock()
Subscriber subscriber2 = Mock()    

```

##### 注入对象

```groovy
class PublisherSpec extends Specification {
  Publisher publisher = new Publisher()
  Subscriber subscriber = Mock()
  Subscriber subscriber2 = Mock()

  def setup() {
    publisher.subscribers << subscriber // << is a Groovy shorthand for List.add()
    publisher.subscribers << subscriber2
  }

```

##### 调用频率约束

```groovy
1 * subscriber.receive("hello")      // exactly one call
0 * subscriber.receive("hello")      // zero calls
(1..3) * subscriber.receive("hello") // between one and three calls (inclusive)
(1.._) * subscriber.receive("hello") // at least one call
(_..3) * subscriber.receive("hello") // at most three calls
_ * subscriber.receive("hello")      // any number of calls, including zero
                                     // (rarely needed; see 'Strict Mocking')

```

##### 目标约束

```groovy
1 * subscriber.receive("hello") // a call to 'subscriber'
1 * _.receive("hello")          // a call to any mock object
```

##### 方法约束

```groovy
1 * subscriber.receive("hello") // a method named 'receive'
1 * subscriber./r.*e/("hello")  // a method whose name matches the given regular expression (here: method name starts with 'r' and ends in 'e')

```

##### 参数约束

```groovy
1 * subscriber.receive("hello")        // an argument that is equal to the String "hello"
1 * subscriber.receive(!"hello")       // an argument that is unequal to the String "hello"
1 * subscriber.receive()               // the empty argument list (would never match in our example)
1 * subscriber.receive(_)              // any single argument (including null)
1 * subscriber.receive(*_)             // any argument list (including the empty argument list)
1 * subscriber.receive(!null)          // any non-null argument
1 * subscriber.receive(_ as String)    // any non-null argument that is-a String
1 * subscriber.receive(endsWith("lo")) // any non-null argument that is-a String
1 * subscriber.receive({ it.size() > 3 && it.contains('a') })
// an argument that satisfies the given predicate, meaning that
// code argument constraints need to return true of false
// depending on whether they match or not
// (here: message length is greater than 3 and contains the character a)

```

#### Spy

和Mock的区别是，Spy使用类的原有方法，有打桩则使用打桩方法；Mock使用类的打桩方法，如果没有打桩，则类的所有方法默认都返回null。

```groovy
//spy的标准是：如果不打桩，默认执行真实的方法，如果打桩则返回桩实现。
List<String> list = new LinkedList<String>();  
List<String> spy = spy(list);  
when(spy.size()).thenReturn(100);  
  
spy.add("one");  
spy.add("two");  
  
assertEquals(spy.get(0), "one");  
assertEquals(100, spy.size());
```

#### Stub打桩

```groovy
subscriber.receive(_) >> "ok"
|          |       |     |
|          |       |     response generator
|          |       argument constraint
|          method constraint
target constraint

```

如：`subscriber.receive(_) >> "ok"` 意味，不管什么实例，什么参数，调用 receive 方法皆返回字符串 ok

##### 返回固定值

```groovy
subscriber.receive(_) >> "ok"
//subscriber.receive(_) >> mockOk()
//private String  mockOk(){
//	return "ok"
//}

```

##### 返回值序列

```groovy
subscriber.receive(_) >>> ["ok", "error", "error", "ok"]
```

##### 动态计算返回值

```groovy
subscriber.receive(_) >> { args -> args[0].size() > 3 ? "ok" : "fail" }
subscriber.receive(_) >> { String message -> message.size() > 3 ? "ok" : "fail" }

```

##### 产生副作用

```groovy
subscriber.receive(_) >> { throw new InternalError("ouch") }

```

##### 链式响应

```groovy
subscriber.receive(_) >>> ["ok", "fail", "ok"] >> { throw new InternalError() } >> "ok"
```

## 前端相关：

#### Content-type：

HTTP 协议是以 ASCII 码传输，建立在 TCP/IP 协议之上的应用层规范。规范把 HTTP 请求分为三个部分：**状态行、请求头、消息主体**。但是协议没规定消息格式。下面是ajax中POST请求中的Content-Type。

##### （1）application/x-www-form-urlencoded

这应该是最常见的 POST 提交数据的方式了。浏览器的原生 form 表单，如果不设置 enctype 属性，那么最终就会以 application/x-www-form-urlencoded 方式提交数据。

![1625195750(1)](images\1625195750(1).jpg)

##### （2）multipart/form-data

这也是一个常见的 POST 数据提交的方式。我们使用表单**上传文件**时，就要让 form 的 enctype 等于这个值。

![1625195716(1)](images\1625195716(1).jpg)

##### （3）application/json

JSON 格式支持比键值对复杂得多的结构化数据，这一点也很有用。

![1625195827(1)](images\1625195827(1).jpg)

##### （4）text/xml

相比于JSON，[XML](https://link.jianshu.com?t=http://www.w3school.com.cn/x.asp)不能更好的适用于数据交换，它包含了太多的包装, 而且它跟大多数编程语言的数据模型不匹配，让大多数程序员感到诧异，XML是面向数据的，JSON是面向对象和结构的，后者会给程序员一种更加亲切的感觉。

XML 存储数据，存储配置文件等需要结构化存储的地方使用；

![1625195926(1)](images\1625195926(1).jpg)

## 设计模式

原文：微信公众号：知识追寻者。

设计模式是对大家实际工作中写的各种代码进行高层次抽象的总结，其中最出名的当属 *Gang of Four* (*GoF*) 的分类了，他们将设计模式分类为 23 种经典的模式，根据用途我们又可以分为三大类，分别为创建型模式、结构型模式和行为型模式。

### 设计原则：

1. 依赖倒置原则(DIP),面向接口编程，而不是面向实现。依赖抽象不依赖于具体。这个很重要，也是优雅的、可扩展的代码的第一步。

2. 职责单一原则(SRP)。每个类都应该只有一个单一的功能，并且该功能应该由这个类完全封装起来。

3. 开闭原则(OCP),对修改关闭，对扩展开放。对修改关闭是说，我们辛辛苦苦加班写出来的代码，该实现的功能和该修复的 bug 都完成了，别人可不能说改就改；对扩展开放就比较好理解了，也就是说在我们写好的代码基础上，很容易实现扩展。

4. 里式替换原则(LSP)：任何基类可能出现的地方，子类一定可以出现

5. 迪米特法则(Low of Dmeter)：也叫作知道最少原则，一个实体应当尽量少的和其他实体之间发生相互作用，使得系统功能模块相对独立。

6. 接口分离原则(ISP)：使用多个隔离的接口，比使用单个接口更好。

   

以上贯穿设计模式的全文。

### 创建型模式

#### 简单工厂

```java
public class FoodFactory {
    public static Food makeFood(String name) {
        if (name.equals("noodle")) {
            Food noodle = new LanZhouNoodle();
            noodle.addSpicy("more");
            return noodle;
        } else if (name.equals("chicken")) {
            Food chicken = new HuangMenChicken();
            chicken.addCondiment("potato");
            return chicken;
        } else {
            return null;
        }
    }
}
```

生产同一父类或者同一接口的实例对象。

强调**职责单一**原则，一个类只提供一种功能，FoodFactory 的功能就是只要负责生产各种 Food。

#### 工厂模式

可以相当于多个简单工厂。

```java
public interface FoodFactory {
    Food makeFood(String name);
}
public class ChineseFoodFactory implements FoodFactory {

    @Override
    public Food makeFood(String name) {
        if (name.equals("A")) {
            return new ChineseFoodA();
        } else if (name.equals("B")) {
            return new ChineseFoodB();
        } else {
            return null;
        }
    }
}
public class AmericanFoodFactory implements FoodFactory {

    @Override
    public Food makeFood(String name) {
        if (name.equals("A")) {
            return new AmericanFoodA();
        } else if (name.equals("B")) {
            return new AmericanFoodB();
        } else {
            return null;
        }
    }
}
```

客户端调用：

```java
public class APP {
    public static void main(String[] args) {
        // 先选择一个具体的工厂
        FoodFactory factory = new ChineseFoodFactory();
        // 由第一步的工厂产生具体的对象，不同的工厂造出不一样的对象
        Food food = factory.makeFood("A");
    }
}
```

这完全符合设计原则的面向接口编程。

#### 抽象工厂模式：

涉及到产品族的时候使用抽象工厂。

一个经典的例子是造一台电脑。我们先不引入抽象工厂模式，看看怎么实现。

因为电脑是由许多的构件组成的，我们将 CPU 和主板进行抽象，然后 CPU 由 CPUFactory 生产，主板由 MainBoardFactory 生产，然后，我们再将 CPU 和主板搭配起来组合在一起。

- 传统的工厂模式：

```java
// 得到 Intel 的 CPU
CPUFactory cpuFactory = new IntelCPUFactory();
CPU cpu = cpuFactory.makeCPU();

// 得到 AMD 的主板
MainBoardFactory mainBoardFactory = new AmdMainBoardFactory();
MainBoard mainBoard = mainBoardFactory.make();

// 组装 CPU 和主板
Computer computer = new Computer(cpu, mainBoard);
```

但是，这种方式有一个问题，那就是如果 **Intel 家产的 CPU 和 AMD 产的主板不能兼容使用**，那么这代码就容易出错，因为客户端并不知道它们不兼容，也就会错误地出现随意组合。

> 当涉及到这种产品族的问题的时候，就需要抽象工厂模式来支持了。我们不再定义 CPU 工厂、主板工厂、硬盘工厂、显示屏工厂等等，我们直接定义电脑工厂，每个电脑工厂负责生产所有的设备，这样能保证肯定不存在兼容问题。

![1627472509(1)](images\1627472509(1).png)

- 抽象工厂模式：

```java
public static void main(String[] args) {
    // 第一步就要选定一个“大厂”
    ComputerFactory cf = new AmdFactory();
    // 从这个大厂造 CPU
    CPU cpu = cf.makeCPU();
    // 从这个大厂造主板
    MainBoard board = cf.makeMainBoard();
      // 从这个大厂造硬盘
      HardDisk hardDisk = cf.makeHardDisk();

    // 将同一个厂子出来的 CPU、主板、硬盘组装在一起
    Computer result = new Computer(cpu, board, hardDisk);
}
```

#### 单例模式：

- 恶汉模式：

```java
public class Singleton {
    // 首先，将 new Singleton() 堵死
    private Singleton() {};
    // 创建私有静态实例，意味着这个类第一次使用的时候就会进行创建
    private static Singleton instance = new Singleton();

    public static Singleton getInstance() {
        return instance;
    }
    // 瞎写一个静态方法。这里想说的是，如果我们只是要调用 Singleton.getDate(...)，
    // 本来是不想要生成 Singleton 实例的，不过没办法，已经生成了
    public static Date getDate(String mode) {return new Date();}
}
```

- 饱汉模式

>  双重检查，指的是两次检查 instance 是否为 null。
>
> volatile 在这里是需要的，希望能引起读者的关注。
>
> 很多人不知道怎么写，直接就在 getInstance() 方法签名上加上 synchronized，这就不多说了，性能太差。

```java
public class Singleton {
    // 首先，也是先堵死 new Singleton() 这条路
    private Singleton() {}
    // 和饿汉模式相比，这边不需要先实例化出来，注意这里的 volatile，它是必须的
    private static volatile Singleton instance = null;

    public static Singleton getInstance() {
        if (instance == null) {
            // 加锁
            synchronized (Singleton.class) {
                // 这一次判断也是必须的，不然会有并发问题
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

#### 建造者模式（用的不多）

```java
class User {
    // 下面是“一堆”的属性
    private String name;
    private String password;
    private String nickName;
    private int age;

    // 构造方法私有化，不然客户端就会直接调用构造方法了
    private User(String name, String password, String nickName, int age) {
        this.name = name;
        this.password = password;
        this.nickName = nickName;
        this.age = age;
    }
    // 静态方法，用于生成一个 Builder，这个不一定要有，不过写这个方法是一个很好的习惯，
    // 有些代码要求别人写 new User.UserBuilder().a()...build() 看上去就没那么好
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        // 下面是和 User 一模一样的一堆属性
        private String  name;
        private String password;
        private String nickName;
        private int age;

        private UserBuilder() {
        }

        // 链式调用设置各个属性值，返回 this，即 UserBuilder
        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder nickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public UserBuilder age(int age) {
            this.age = age;
            return this;
        }

        // build() 方法负责将 UserBuilder 中设置好的属性“复制”到 User 中。
        // 当然，可以在 “复制” 之前做点检验
        public User build() {
            if (name == null || password == null) {
                throw new RuntimeException("用户名和密码必填");
            }
            if (age <= 0 || age >= 150) {
                throw new RuntimeException("年龄不合法");
            }
            // 还可以做赋予”默认值“的功能
              if (nickName == null) {
                nickName = name;
            }
            return new User(name, password, nickName, age);
        }
    }
}
```

客户端调用：

```java
public class APP {
    public static void main(String[] args) {
        User d = User.builder()
                .name("foo")
                .password("pAss12345")
                .age(25)
                .build();
    }
}
```

提倡了一种链式编程，但是多写了很多代码。过，当属性很多，而且有些必填，有些选填的时候，这个模式会使代码清晰很多。我们可以在 **Builder 的构造方法**中强制让调用者提供必填字段，还有，在 build() 方法中校验各个参数比在 User 的构造方法中校验，代码要优雅一些。

#### 原型模式：

原型模式很简单：有一个原型**实例**，基于这个原型实例产生新的实例，也就是“克隆”了。

### 结构性模式：

结构型模式旨在通过改变代码结构来达到解耦的目的，使得我们的代码容易维护和扩展。

#### 代理模式

用一个代理来隐藏具体实现类的实现细节，通常还用于在真实的实现的前后添加一部分逻辑。

既然说是**代理**，那就要对客户端隐藏真实实现，由代理来负责客户端的所有请求。当然，代理只是个代理，它不会完成实际的业务逻辑，而是一层皮而已，但是对于客户端来说，它必须表现得就是客户端需要的真实实现。

![1627482528(1)](images\1627482528(1).jpg)

#### 适配器模式：

##### 对象适配器

```java

public interface Duck {
    public void quack(); // 鸭的呱呱叫
    public void fly(); // 飞
}

public interface Cock {
    public void gobble(); // 鸡的咕咕叫
    public void fly(); // 飞
}

public class WildCock implements Cock {
    public void gobble() {
        System.out.println("咕咕叫");
    }
    public void fly() {
        System.out.println("鸡也会飞哦");
    }
}
```

鸭接口有 fly() 和 quare() 两个方法，鸡 Cock 如果要冒充鸭，fly() 方法是现成的，但是鸡不会鸭的呱呱叫，没有 quack() 方法。这个时候就需要适配了。

```java
// 毫无疑问，首先，这个适配器肯定需要 implements Duck，这样才能当做鸭来用
public class CockAdapter implements Duck {

    Cock cock;
    // 构造方法中需要一个鸡的实例，此类就是将这只鸡适配成鸭来用
      public CockAdapter(Cock cock) {
        this.cock = cock;
    }

    // 实现鸭的呱呱叫方法
    @Override
      public void quack() {
        // 内部其实是一只鸡的咕咕叫
        cock.gobble();
    }

      @Override
      public void fly() {
        cock.fly();
    }
}
```

![1627484588(1)](images\1627484588(1).png)

##### 类适配器：

![1627484684(1)](images\1627484684(1).jpg)

看到这个图，大家应该很容易理解的吧，通过继承的方法，适配器自动获得了所需要的大部分方法。这个时候，客户端使用更加简单，直接 `Target t = new SomeAdapter();` 就可以了。

> 类适配器和对象适配器的异同：
>
> 一个采用继承，一个采用组合；
>
> 类适配属于静态实现，对象适配属于组合的动态实现，对象适配需要多实例化一个对象。
>
> 总体来说，对象适配用得比较多。

#### 适配器和代理模式异同：

对象适配器和代理模式代码结构基本相似，都是通过组合的方式，在我们的目标类中通过组合注入一个具体实现类的实例。但是两者的目标不一样，代理更倾向于增强原有的方法功能；适配器倾向于转化的功能，注入的实例和我们这个目标类可能没有任何关系(代理模式：注入的实例和目标类实现了同一个接口)

#### 装饰模式:

从名字来简单解释下装饰器。既然说是装饰，那么往往就是**添加小功能**这种，而且，我们要满足可以添加多个小功能。最简单的，代理模式就可以实现功能的增强，但是代理不容易实现多个功能的增强，当然你可以说用代理包装代理的多层包装方式，但是那样的话代码就复杂了。

一个例子，先把装饰模式弄清楚，然后再介绍下 java io 中的装饰模式的应用。

最近大街上流行起来了“快乐柠檬”，我们把快乐柠檬的饮料分为三类：红茶、绿茶、咖啡，在这三大类的基础上，又增加了许多的口味，什么金桔柠檬红茶、金桔柠檬珍珠绿茶、芒果红茶、芒果绿茶、芒果珍珠红茶、烤珍珠红茶、烤珍珠芒果绿茶、椰香胚芽咖啡、焦糖可可咖啡等等，每家店都有很长的菜单，但是仔细看下，其实原料也没几样，但是可以搭配出很多组合，如果顾客需要，很多没出现在菜单中的饮料他们也是可以做的。

在这个例子中，红茶、绿茶、咖啡是最基础的饮料，其他的像金桔柠檬、芒果、珍珠、椰果、焦糖等都属于装饰用的。当然，在开发中，我们确实可以像门店一样，开发这些类：LemonBlackTea、LemonGreenTea、MangoBlackTea、MangoLemonGreenTea......但是，很快我们就发现，这样子干肯定是不行的，这会导致我们需要组合出所有的可能，而且如果客人需要在红茶中加双份柠檬怎么办？三份柠檬怎么办？

![微信图片_20210728232446](images\微信图片_20210728232446.png)

客户端调用：

```java
public static void main(String[] args) {
    // 首先，我们需要一个基础饮料，红茶、绿茶或咖啡
    Beverage beverage = new GreenTea();
    // 开始装饰
    beverage = new Lemon(beverage); // 先加一份柠檬
    beverage = new Mongo(beverage); // 再加一份芒果

    System.out.println(beverage.getDescription() + " 价格：￥" + beverage.cost());
    //"绿茶, 加柠檬, 加芒果 价格：￥16"
}
```

典型的案例：

![微信图片_20210728233329](images\微信图片_20210728233329.png)

我们知道 InputStream 代表了输入流，具体的输入来源可以是文件（FileInputStream）、管道（PipedInputStream）、数组（ByteArrayInputStream）等，这些就像前面奶茶的例子中的红茶、绿茶，属于基础输入流。

FilterInputStream 承接了装饰模式的关键节点，它的实现类是一系列装饰器，比如 BufferedInputStream 代表用缓冲来装饰，也就使得输入流具有了缓冲的功能，LineNumberInputStream 代表用行号来装饰，在操作的时候就可以取得行号了，DataInputStream 的装饰，使得我们可以从输入流转换为 java 中的基本类型值。

### 行为型模式

行为型模式关注的是各个类之间的相互作用，将职责划分清楚，使得我们的代码更加地清晰。

#### 策略模式：

![策略模式](images\策略模式.png)

#### 观察者模式：

观察者模式对于我们来说，真是再简单不过了。无外乎两个操作，观察者订阅自己关心的主题和主题有数据变化后通知观察者们。

```java
//定义主题
public class Subject {    
    private List<Observer> observers = new ArrayList<Observer>();    
    private int state;    
    public int getState() {        
        return state;    
    }    
    public void setState(int state) {        
        this.state = state;        
        // 数据已变更，通知观察者们        
        notifyAllObservers();    
    }    
    // 注册观察者    
    public void attach(Observer observer) {        
        observers.add(observer);    
    }   
    // 通知观察者们    
    public void notifyAllObservers() {        
        for (Observer observer : observers) {            
            observer.update();        
        }    
    }}
```

```java
//定义观察者
public abstract class Observer {    
    protected Subject subject;   
    public abstract void update();
}
```

```java
//具体观察者
public class BinaryObserver extends Observer {    
    // 在构造方法中进行订阅主题    
    public BinaryObserver(Subject subject) {        
        this.subject = subject;        
        // 通常在构造方法中将 this 发布出去的操作一定要小心        
        this.subject.attach(this);    
    }    
    // 该方法由主题类在数据变更的时候进行调用    
    @Override    
    public void update() {        
        String result = Integer.toBinaryString(subject.getState());        
        System.out.println("订阅的数据发生变化，新的数据处理为二进制值为：" + result);    
    }
}
public class HexaObserver extends Observer {    
    public HexaObserver(Subject subject) {        
        this.subject = subject;        
        this.subject.attach(this);    
    }    
    @Override    
    public void update() {        
        String result = Integer.toHexString(subject.getState()).toUpperCase();        
        System.out.println("订阅的数据发生变化，新的数据处理为十六进制值为：" + result);    }
}
```

客户端：

```java
public static void main(String[] args) {    
    // 先定义一个主题    
    Subject subject1 = new Subject();    
    // 定义观察者    
    new BinaryObserver(subject1);    
    new HexaObserver(subject1);    
    // 模拟数据变更，这个时候，观察者们的 update 方法将会被调用    
    subject.setState(11);
}
```

实际开发中，观察者模式往往用消息中间件来实现.

#### 模板方法模式：

在含有继承结构的代码中，模板方法模式是非常常用的。

```java
public abstract class AbstractTemplate {
    // 这就是模板方法
    public void templateMethod() {
        init();
        apply(); // 这个是重点
        end(); // 可以作为钩子方法
    }

    protected void init() {
        System.out.println("init 抽象层已经实现，子类也可以选择覆写");
    }

    // 留给子类实现
    protected abstract void apply();

    protected void end() {
    }
}
```

模板方法中调用了 3 个方法，其中 apply() 是抽象方法，子类必须实现它，其实模板方法中有几个抽象方法完全是自由的，我们也可以将三个方法都设置为抽象方法，让子类来实现。也就是说，模板方法只负责定义第一步应该要做什么，第二步应该做什么，第三步应该做什么，至于怎么做，由子类来实现。

```java
public class ConcreteTemplate extends AbstractTemplate {
    public void apply() {
        System.out.println("子类实现抽象方法 apply");
    }

    public void end() {
        System.out.println("我们可以把 method3 当做钩子方法来使用，需要的时候覆写就可以了");
    }
}
```

客户端：

```java
public static void main(String[] args) {
    AbstractTemplate t = new ConcreteTemplate();
    // 调用模板方法
    t.templateMethod();
}
```

