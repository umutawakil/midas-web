package com.midas.helmet.configuration

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.view.ThymeleafViewResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

@Configuration
class ApplicationConfiguration {
    @Bean
    fun templateResolver() : ClassLoaderTemplateResolver {
        val secondaryTemplateResolver               =  ClassLoaderTemplateResolver()
        secondaryTemplateResolver.prefix            = "templates/"
        secondaryTemplateResolver.suffix            = ".html"
        secondaryTemplateResolver.templateMode      = TemplateMode.HTML
        secondaryTemplateResolver.characterEncoding = "UTF-8"

        return secondaryTemplateResolver
    }

    @Bean
    fun templateEngine() : SpringTemplateEngine {
        val templateEngine = SpringTemplateEngine()
        templateEngine.setTemplateResolver(templateResolver())
        templateEngine.enableSpringELCompiler = true
        templateEngine.addDialect(LayoutDialect())
        return templateEngine
    }

    @Bean
    fun viewResolver() : ThymeleafViewResolver {
        val viewResolver = ThymeleafViewResolver()
        viewResolver.templateEngine = templateEngine()
        return viewResolver
    }

}