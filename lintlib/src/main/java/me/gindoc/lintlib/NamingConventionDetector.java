package me.gindoc.lintlib;

import com.android.annotations.Nullable;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.visitor.AbstractUastVisitor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Author: GIndoc on 2019/8/19.
 * FOR   : 参考 https://mp.weixin.qq.com/s?__biz=MzIwMTAzMTMxMg==&mid=2649494528&idx=1&sn=e7146723dd9d1a07de7b5050c3d7457e&chksm=8eec9fffb99b16e98bc5152d5afa9957aea7ce91b8e117ff45072324d833f72849cf7fa3abcb&mpshare=1&scene=1&srcid=&sharer_sharetime=1566222823122&sharer_shareid=ed468296591247c46d788196550bd35b&key=8da435df4d73ffb2ce393afddb387bccce79738486a073e139bf0266d4847721c475609b2c1a1a3325ff2035b69ea734e3d179b302d8a74d4cc556f7ad40ab573e8b4fe4bce178e0f4f50c70b84c12c3&ascene=0&uin=NTMyODczNzM1&devicetype=iMac+MacBookPro14%2C1+OSX+OSX+10.14.4+build(18E226)&version=12020810&nettype=WIFI&lang=zh_CN&fontScale=100&pass_ticket=BB8uJscGCVp1B1GXmPVKia8UYOwXSLtIMQxlBI1Io1KRPHxefLMsVjdFGRPGym%2Bn
 * Detector 查找指定的Issue，一个Issue对应一个Detector。自定义Lint 规则的过程也就是重写Detector类相关方法的过程
 * Scanner  扫描并发现代码中的Issue,Detector需要实现Scaner,可以继承一个到多个
 */
public class NamingConventionDetector extends Detector implements Detector.UastScanner {

    /**
     * 定义命名规范规则
     * 第一个参数id 唯一的id,简要表面当前提示的问题。
     * 第二个参数briefDescription 简单描述当前问题
     * 第三个参数explanation 详细解释当前问题和修复建议
     * 第四个参数category 问题类别，例如上文讲到的Security、Usability等等。
     * 第五个参数priority 优先级，从1到10，10最重要
     * 第六个参数Severity 严重程度：FATAL（奔溃）, ERROR（错误）, WARNING（警告）,INFORMATIONAL（信息性）,IGNORE（可忽略）
     * 第七个参数Implementation Issue和哪个Detector绑定，以及声明检查的范围。
     */
    public static final Issue ISSUE = Issue.create("NamingConventionWarning",
            "命名规范错误",
            "使用驼峰命名法，方法命名开头小写",
            Category.USABILITY,
            5,
            Severity.WARNING,
            new Implementation(NamingConventionDetector.class,
                    EnumSet.of(Scope.JAVA_FILE)));


    //返回我们所有感兴趣的类，即返回的类都被会检查
    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.<Class<? extends UElement>>singletonList(UClass.class);
    }

    //重写该方法，创建自己的处理器
    @Nullable
    @Override
    public UElementHandler createUastHandler(@NotNull final JavaContext context) {
        return new UElementHandler() {
            @Override
            public void visitClass(@NotNull UClass node) {
                node.accept(new NamingConventionVisitor(context, node));
            }
        };
    }

    //定义一个继承自AbstractUastVisitor的访问器，用来处理感兴趣的问题
    public static class NamingConventionVisitor extends AbstractUastVisitor {

        JavaContext context;

        UClass uClass;

        NamingConventionVisitor(JavaContext context, UClass uClass) {
            this.context = context;
            this.uClass = uClass;
        }

        @Override
        public boolean visitClass(@NotNull UClass node) {
            if (node.getName() == null) return super.visitClass(node);
            //获取当前类名
            int code = node.getName().charAt(0);
            //如果类名不是大写字母，则触碰Issue，lint工具提示问题
            if (97 < code && code < 122) {
                context.report(ISSUE, context.getNameLocation(node),
                        "the  name of class must start with uppercase:" + node.getName());
                //返回true表示触碰规则，lint提示该问题；false则不触碰
                return true;
            }

            return super.visitClass(node);
        }

        @Override
        public boolean visitMethod(@NotNull UMethod node) {
            //当前方法不是构造方法
            if (!node.isConstructor()) {
                int code = node.getName().charAt(0);
                //当前方法首字母是大写字母，则报Issue
                if (65 < code && code < 90) {
                    context.report(ISSUE, context.getLocation(node),
                            "the method must start with lowercase:" + node.getName());
                    //返回true表示触碰规则，lint提示该问题；false则不触碰
                    return true;
                }
            }
            return super.visitMethod(node);

        }

    }


}
