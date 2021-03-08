#

获取方法名：

```
//不能封装，否则只能得到封装的方法名了。
Thread.currentThread().getStackTrace()[1].getMethodName()
```
