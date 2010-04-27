For the upgrade to Spring 3.0, antlr-runtime-3.2.jar is required. However, Hibernate 3.25 needs antlr_2.7.6.jar and they can
both exist because their packaging is different. The earlier version, 2.7.6, is antlr.* whereas 3.2 is org.antlr.*, so 
they can co-habitate here just fine.

For more information, see this article: http://blog.newsplore.com/2009/03/07/upgrading-to-spring-30