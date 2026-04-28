//package cn.ayeez.blogserver;
//
//public class testLevel2 {
//}
//
//interface RulePlugin<I, O> {
//    void init(Ctx c);
//
//    O execute(I in);
//
//    void dispose();
//}   // 接口+生命周期
//
//class A implements RulePlugin<In, Out> { ...
//}
//
//class B implements RulePlugin<In, Out> { ...
//} // 策略A/B
//
//class Engine {
//    RulePlugin<In, Out> p;
//
//    void switchTo(RulePlugin<In, Out> n, Ctx c) {
//        if (p != null) p.dispose();
//        n.init(c);
//        p = n;
//    }
//
//    Out run(In in) {
//        return p.execute(in);
//    }
//} // 切换与委托
//
//class Service {
//    Engine e;
//
//    Out handle(In in) {
//        return e.run(in);
//    }
//}  // 业务只依赖接口，不依赖实现
//// Controller 完全不改；只换插件实现(A->B)就能改变业务结果；Ctx=主系统注入给插件的受控依赖